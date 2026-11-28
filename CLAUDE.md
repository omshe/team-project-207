# CLAUDE.md

All project rules live in AGENTS.md and apply to every agent and teammate working in this
repository (Claude Code and Codex included).
Read and follow it in full:

@AGENTS.md

These rules come from the CSC207 course notes, ch. 8–11 (program specification, SOLID, layered
architectures, Clean Architecture). They are requirements, and they apply to every change,
including small fixes and refactors.

**Before writing code** for a new feature or use case, check that README.md has a scoped user
story and a use case for it (AGENTS.md §5). If either is missing, write it first, or ask.
Decide which layer each new class belongs in (§1, §3) before creating it.

**Before saying a change is done**, follow AGENTS.md §9:

1. Run the Dependency Rule self-check greps in §1. Every command must print nothing.
2. Check every file you touched against the SOLID checklist (§4) and the red flags (§7).
3. Run `mvn -q test`.
4. Run `mvn checkstyle:check` (without `-q`) and fix every warning in the files you touched.
   Style and Javadoc follow the course notes on
   [code style and documentation](https://github.com/CSC207-UofT/207-course-notes/blob/main/code-style-and-documentation.md#code-style-and-checkstyle)
   (AGENTS.md §8).

In your reply, name any rule you had to bend and explain why. If a request can only be met by
breaking a rule, say so before writing the code and propose a design that keeps it.
