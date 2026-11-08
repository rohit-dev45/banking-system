import { createServerFn } from "@tanstack/react-start";
import { z } from "zod";
import { requireUser } from "@/lib/auth-middleware";

const MAX_AMOUNT = 1000000;

// Amounts are rejected, never silently rounded: finite, positive, at most two decimals.
const money = (label = "amount") =>
  z
    .number()
    .refine((n) => Number.isFinite(n), `Enter a valid ${label}`)
    .refine((n) => n > 0, `Enter an ${label} greater than zero`)
    .refine((n) => n <= MAX_AMOUNT, "Single movements are capped at 1,000,000 in this demo")
    .refine((n) => Math.abs(n * 100 - Math.round(n * 100)) < 1e-6, "Use at most two decimal places");

const cents = (n) => Math.round(n * 100) / 100;
const fail = (message, status = 400) => Object.assign(new Error(message), { status });
const toArray = (result) => (Array.isArray(result) ? result : result?.items ?? []);

async function ownedAccount(db, id, user) {
  const account = await db.entities.BankAccount.get(id).catch(() => null);
  if (!account) throw fail("Account not found", 404);
  if (account.owner_id !== user.id) throw fail("You do not own this account", 403);
  return account;
}

const generateNumber = () => {
  let digits = "";
  for (let i = 0; i < 8; i += 1) digits += Math.floor(Math.random() * 10);
  return `AC${digits}`;
};

// Ledger and balance writes are server-only: the client cannot write these records directly.
async function writeLedger(admin, userId, payload) {
  await admin.entities.BankTransaction.create({ ...payload, owner_id: userId });
}

export const openAccount = createServerFn({ method: "POST" })
  .middleware([requireUser])
  .validator(
    z.object({
      account_type: z.enum(["checking", "savings"]),
      nickname: z.string().max(40).optional(),
      initial_deposit: z
        .number()
        .refine((n) => Number.isFinite(n) && n >= 0, "Enter a valid opening deposit")
        .refine((n) => n <= MAX_AMOUNT, "Opening deposits are capped at 1,000,000 in this demo")
        .refine((n) => Math.abs(n * 100 - Math.round(n * 100)) < 1e-6, "Use at most two decimal places"),
    }),
  )
  .handler(async ({ data, context }) => {
    const admin = context.getBase44().asServiceRole;
    const opening = cents(data.initial_deposit);
    const account = await admin.entities.BankAccount.create({
      account_number: generateNumber(),
      account_type: data.account_type,
      nickname: data.nickname?.trim() || (data.account_type === "checking" ? "Everyday checking" : "High-yield savings"),
      holder_name: context.user.full_name || context.user.email,
      balance: opening,
      currency: "USD",
      owner_id: context.user.id,
    });

    await writeLedger(admin, context.user.id, {
      account_id: account.id,
      account_number: account.account_number,
      type: "opening",
      amount: opening,
      balance_after: opening,
      memo: "Account opened",
    });

    return account;
  });

export const depositMoney = createServerFn({ method: "POST" })
  .middleware([requireUser])
  .validator(z.object({ account_id: z.string().min(1), amount: money(), memo: z.string().max(120).optional() }))
  .handler(async ({ data, context }) => {
    const db = context.getBase44();
    const admin = db.asServiceRole;
    const account = await ownedAccount(db, data.account_id, context.user);
    const balance = cents(account.balance + data.amount);

    await admin.entities.BankAccount.update(account.id, { balance });
    await writeLedger(admin, context.user.id, {
      account_id: account.id,
      account_number: account.account_number,
      type: "deposit",
      amount: cents(data.amount),
      balance_after: balance,
      memo: data.memo || "Simulated deposit",
    });

    return { balance };
  });

export const withdrawMoney = createServerFn({ method: "POST" })
  .middleware([requireUser])
  .validator(z.object({ account_id: z.string().min(1), amount: money(), memo: z.string().max(120).optional() }))
  .handler(async ({ data, context }) => {
    const db = context.getBase44();
    const admin = db.asServiceRole;
    const account = await ownedAccount(db, data.account_id, context.user);
    if (account.balance < data.amount) throw fail("Insufficient funds for this withdrawal");

    const balance = cents(account.balance - data.amount);
    await admin.entities.BankAccount.update(account.id, { balance });
    await writeLedger(admin, context.user.id, {
      account_id: account.id,
      account_number: account.account_number,
      type: "withdrawal",
      amount: cents(data.amount),
      balance_after: balance,
      memo: data.memo || "Simulated withdrawal",
    });

    return { balance };
  });

export const transferMoney = createServerFn({ method: "POST" })
  .middleware([requireUser])
  .validator(
    z.object({
      from_account_id: z.string().min(1),
      to_account_number: z.string().min(4).max(20),
      amount: money(),
      memo: z.string().max(120).optional(),
    }),
  )
  .handler(async ({ data, context }) => {
    const db = context.getBase44();
    const admin = db.asServiceRole;
    const from = await ownedAccount(db, data.from_account_id, context.user);
    if (from.balance < data.amount) throw fail("Insufficient funds for this transfer");

    const target = String(data.to_account_number).trim().toUpperCase();
    // Documented signature: filter(query) returns the matching records.
    const matches = toArray(await admin.entities.BankAccount.filter({ account_number: target }));
    const to = matches[0];
    if (!to) throw fail("No account found with that number", 404);
    if (to.id === from.id) throw fail("Pick a different destination account");
    // Two accounts cannot be moved between without a transaction, so this demo stays inside one owner.
    if (to.owner_id !== context.user.id) throw fail("This demo moves money only between accounts you own", 403);

    const fromBalance = cents(from.balance - data.amount);
    const toBalance = cents(to.balance + data.amount);

    await admin.entities.BankAccount.update(from.id, { balance: fromBalance });
    await admin.entities.BankAccount.update(to.id, { balance: toBalance });

    await writeLedger(admin, context.user.id, {
      account_id: from.id,
      account_number: from.account_number,
      type: "transfer_out",
      amount: cents(data.amount),
      balance_after: fromBalance,
      memo: data.memo || "Outgoing transfer",
      counterparty: to.account_number,
    });

    await writeLedger(admin, context.user.id, {
      account_id: to.id,
      account_number: to.account_number,
      type: "transfer_in",
      amount: cents(data.amount),
      balance_after: toBalance,
      memo: data.memo || "Incoming transfer",
      counterparty: from.account_number,
    });

    return { balance: fromBalance };
  });