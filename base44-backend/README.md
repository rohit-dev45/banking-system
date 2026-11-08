# Base44 backend implementation

Live app: https://rohit-full-stack-projects-e2cbb008.base44.app

This directory contains the exported backend logic, authentication middleware, entity schemas and dependency/configuration references from the managed Base44 app. It is a source excerpt, not a complete standalone frontend checkout. The generated frontend, auth components and managed platform headers are supplied by Base44; do not run this directory as a standalone Vercel app. Base44 automatic GitHub sync is not enabled on the current plan. These files were copied manually through the permitted code-reading API.

## Architecture
- Original Java/Spring Boot implementation: repository root; MySQL and Hibernate, with Java tests.
- Live showcase implementation: TanStack Start server functions + Base44 auth and entity storage. It does not run the Java backend.
- Amazon remains UI-only; no real checkout or payments.

## Managed server operations
Banking: openAccount, depositMoney, withdrawMoney, transferMoney in src/lib/banking-fns.js. All require an authenticated user. Transfers are limited to the caller's own accounts. Deposits create simulated funds.
Library: borrowBook and returnLoan in src/lib/library-fns.js, with ownership checks.
Entity RLS permits owner reads and denies direct client create/update/delete for protected balances, transactions and loans; server writes use the managed service role after validation. Never expose service-role credentials to a browser.

## Integrity limitations
This is an educational persistence demo. Entity operations are sequential, not database transactions. Concurrency and partial-failure consistency are not guaranteed. Do not use real money or rely on stock counts under concurrent requests. The original Java implementation demonstrates transactional operations separately.

## Verification
The Base44 builder reported a successful build. Server code and RLS were reviewed, and the public sign-in page was opened successfully. Signed-in persistence and multi-user behavior have not been independently verified in this session.

For a Vercel API migration, preserve server authentication and provision supported server-side Base44 access first. The managed Base44 server receives platform headers that are not automatically available on Vercel. GitHub stores source, not application records.
