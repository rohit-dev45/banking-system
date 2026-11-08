import { createServerFn } from "@tanstack/react-start";
import { z } from "zod";
import { requireUser } from "@/lib/auth-middleware";

const fail = (message, status = 400) => Object.assign(new Error(message), { status });

const LOAN_DAYS = 14;

export const borrowBook = createServerFn({ method: "POST" })
  .middleware([requireUser])
  .validator(z.object({ book_id: z.string().min(1) }))
  .handler(async ({ data, context }) => {
    const db = context.getBase44();
    const admin = db.asServiceRole;

    const book = await db.entities.LibraryBook.get(data.book_id).catch(() => null);
    if (!book) throw fail("That title is no longer in the catalogue", 404);
    if ((book.available_copies ?? 0) <= 0) throw fail("All copies of this title are currently on loan");

    const existing = await db.entities.LibraryLoan.filter({ book_id: book.id, status: "borrowed" });
    if (existing.length > 0) throw fail("You already have this title on loan");

    const now = new Date();
    const due = new Date(now.getTime() + LOAN_DAYS * 24 * 60 * 60 * 1000);

    await admin.entities.LibraryBook.update(book.id, {
      available_copies: Math.max(0, (book.available_copies ?? 1) - 1),
    });

    try {
      return await admin.entities.LibraryLoan.create({
        book_id: book.id,
        book_title: book.title,
        book_author: book.author,
        member_email: context.user.email,
        member_name: context.user.full_name || context.user.email,
        borrowed_date: now.toISOString(),
        due_date: due.toISOString(),
        status: "borrowed",
        owner_id: context.user.id,
      });
    } catch (error) {
      // Put the copy back rather than leave the catalogue short of stock.
      await admin.entities.LibraryBook.update(book.id, {
        available_copies: Math.min(book.total_copies ?? 1, (book.available_copies ?? 0) + 1),
      });
      throw error;
    }
  });

export const returnLoan = createServerFn({ method: "POST" })
  .middleware([requireUser])
  .validator(z.object({ loan_id: z.string().min(1) }))
  .handler(async ({ data, context }) => {
    const db = context.getBase44();
    const admin = db.asServiceRole;

    const loan = await db.entities.LibraryLoan.get(data.loan_id).catch(() => null);
    if (!loan) throw fail("Loan not found", 404);
    // Checked explicitly too: returns are the borrower's action, never an admin's on their behalf.
    if (loan.owner_id !== context.user.id) throw fail("You can only return your own loans", 403);
    if (loan.status === "returned") return loan;

    await admin.entities.LibraryLoan.update(loan.id, {
      status: "returned",
      returned_date: new Date().toISOString(),
    });

    const book = await admin.entities.LibraryBook.get(loan.book_id).catch(() => null);
    if (book) {
      await admin.entities.LibraryBook.update(book.id, {
        available_copies: Math.min(book.total_copies ?? 1, (book.available_copies ?? 0) + 1),
      });
    }

    return { ...loan, status: "returned" };
  });