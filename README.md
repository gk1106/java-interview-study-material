# Java Interview Study Material

A single-Maven-project study repository for a Java backend developer (~3.5+ years,
banking domain) preparing for interviews. Goes deep on internals, time/space
complexity, trade-offs and interview-style Q&A — not beginner fluff.

## Scope

DSA with the Java Collections Framework, Streams, Multithreading &
Concurrency, then structured Java and Spring Boot interview question banks,
finishing with one-page cheat sheets and mock interviews.

See `notes/00-roadmap/study-plan.md` for the 8-week plan and
`references/folder-structure.md` (under `.claude/skills/...`) for the full
module list.

## Repository layout

```
notes/<NN-module>/<NN-topic>.md            concept notes (template: internals, Big-O, pitfalls, interview Qs)
src/main/java/com/gk/study/<module>/examples/    runnable demos (each has a main())
src/main/java/com/gk/study/<module>/exercises/   TODO stubs for YOU to solve
src/main/java/com/gk/study/<module>/solutions/   reference solutions
src/test/java/com/gk/study/<module>/exercises/   tests for your solutions (fail until solved)
src/test/java/com/gk/study/<module>/solutions/   tests proving the reference solutions work
```

Module package names drop the numeric prefix, e.g. `01-java-foundations-for-dsa`
→ package `com.gk.study.foundations`.

## How to use this repo

1. Read the notes for a topic (`notes/<module>/<topic>.md`).
2. Run the matching example class in `examples/` to see it in action.
3. Open the exercise stubs in `exercises/` and implement the `TODO`s.
4. Run the exercise tests — they fail until you solve the exercise:
   ```
   ./mvnw test -Dtest='com.gk.study.<module>.exercises.*Test'
   ```
5. Compare against `solutions/` only after attempting it yourself.
6. Revisit `notes/10-java-interview-questions` and
   `notes/11-spring-boot-interview-questions` for structured Q&A revision,
   and `notes/12-revision` for cheat sheets and mock interviews in the final week.

## Building

This repo uses a local Maven install cached under `~/.m2/wrapper/dists`.
Use the provided wrapper scripts so you don't need Maven on your global PATH:

```
./mvnw -q test-compile              # compile everything (main + test)
./mvnw -q test -Dtest='*SolutionTest'   # run only the reference-solution tests (should always pass)
./mvnw -q test                      # run everything, including exercise tests (many fail until you solve them)
```

On Windows PowerShell / cmd, use `mvnw.cmd` instead of `./mvnw`.

## Progress

See `PROGRESS.md` for the module-by-module checklist and current status.
