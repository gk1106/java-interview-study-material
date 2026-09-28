---
name: java-interview-study-material
description: Generate a detailed, folder-ordered Java study repository covering DSA with the Java Collections Framework (List, Queue/Deque, Set, Map), Streams, Multithreading/Concurrency, Java interview questions and Spring Boot interview questions — with concept notes, runnable example programs, hands-on exercises (TODO stubs + JUnit tests) and separate solutions. Use this skill whenever the user asks for study material, notes, exercises, practice problems, revision plans or interview prep for Java, DSA, collections, streams, threads or Spring Boot, even if they don't name the skill.
---

# Java Interview Study Material Generator

You are building a study repository for a Java backend developer with ~3.5 years of
experience preparing for interviews. Assume they know basic Java syntax. Go DEEP:
internals, time complexity, trade-offs, pitfalls and "why" — not beginner fluff.

## Non-negotiable rules

1. **Follow the folder order** in `references/folder-structure.md` exactly. Numbered
   prefixes (`01-`, `02-` …) define the learning sequence. Never skip a module.
2. **Every topic uses the template** in `references/topic-template.md`
   (concept notes → internals → complexity table → example code → pitfalls →
   interview Qs → exercises).
3. **All code must compile and run.** Project is a single Maven project, Java 21,
   JUnit 5, AssertJ. After finishing each module run `mvn -q test-compile` and for
   solutions `mvn -q test -Dtest='*SolutionTest'` — fix anything that fails
   before moving on.
4. **Exercises are for the learner to solve.** Exercise classes contain method
   signatures + `// TODO` + `throw new UnsupportedOperationException("TODO")`.
   Tests for exercises live in `src/test/java/.../exercises/` and are EXPECTED to
   fail until the learner solves them. Reference solutions go ONLY in the
   `solutions` package with their own passing tests. Never put the answer inside
   the exercise file.
5. **Difficulty ladder per topic:** at least 3 Easy, 3 Medium, 2 Hard exercises,
   plus 1 "Build it yourself" (e.g. implement your own ArrayList / LRU cache /
   blocking queue).
6. **Work one module at a time.** After each module, stop, print a short summary
   (files created, exercise count, build status) and update `PROGRESS.md`.
   Continue to the next module only when the user says so (unless told
   "generate all").
7. Explanations in simple, clear English. Use ASCII diagrams for internals
   (array growth, node links, heap tree, hash buckets, thread states).
8. Tag every interview question with level: `[Basic]`, `[Intermediate]`,
   `[Advanced]` and give a model answer (short, spoken-interview style) plus a
   likely follow-up question.

## Package layout

```
src/main/java/com/gk/study/<module>/examples/     runnable demos (each has main())
src/main/java/com/gk/study/<module>/exercises/    TODO stubs
src/main/java/com/gk/study/<module>/solutions/    reference solutions
src/test/java/com/gk/study/<module>/exercises/    tests for learner (fail until solved)
src/test/java/com/gk/study/<module>/solutions/    tests proving solutions work
notes/<NN-module>/<NN-topic>.md                   concept notes
```

Module package names: drop the number prefix, use lowercase, e.g. `list`, `queue`,
`streams`, `concurrency`.

## Workflow

1. If `pom.xml` doesn't exist: create it (Java 21, JUnit 5, AssertJ, surefire),
   `README.md` (how to use the repo), `PROGRESS.md` (checklist of all modules),
   and `notes/00-roadmap/study-plan.md` (week-by-week plan — see folder structure).
2. Read `references/folder-structure.md` and pick the next unfinished module
   from `PROGRESS.md`.
3. For each topic in the module, read `references/topic-template.md` and produce
   the notes file + example code + exercises + solutions + tests.
4. Build and test (rule 3). Fix failures.
5. Update `PROGRESS.md`, print summary, stop.

## Quality bar (self-check before finishing a module)

- Did I explain *how it works inside* (e.g. ArrayList growth factor 1.5x,
  HashMap treeification at 8, PriorityQueue sift-up/down, ConcurrentHashMap
  CAS + synchronized bins)?
- Does every topic have a Big-O table for the main operations?
- Is there a "When to use / when NOT to use" section?
- Are pitfalls concrete (ConcurrentModificationException, `Arrays.asList`
  fixed size, `List.remove(int)` vs `remove(Object)`, mutable keys in HashSet,
  `parallelStream` misuse, deadlock, lost updates)?
- Would an interviewer's follow-up question be answered by these notes?
