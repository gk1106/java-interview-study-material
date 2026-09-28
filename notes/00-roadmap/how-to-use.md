# How to use this repo

## Prerequisites

- Java 21 JDK on PATH (`java -version`).
- Maven: this machine doesn't have `mvn` on the global PATH, so use the
  wrapper scripts committed at the repo root instead:
  - bash / Git Bash: `./mvnw <args>`
  - PowerShell / cmd: `mvnw.cmd <args>`

  They point at a cached Maven distribution under
  `~/.m2/wrapper/dists/apache-maven-3.9.11/...`. If you're on a machine with
  Maven already on PATH, plain `mvn <args>` works identically.

## Running an example

Every topic's notes link to a runnable class under
`src/main/java/com/gk/study/<module>/examples/`. Each has a `main()` method.
Run it directly:

```
./mvnw -q compile exec:java -Dexec.mainClass="com.gk.study.list.examples.ArrayListInternalsDemo"
```

(or just run the class's `main()` from your IDE — that's usually faster).

## Solving an exercise

1. Open the stub in `src/main/java/com/gk/study/<module>/exercises/`. It has
   a Javadoc with the problem statement, an example input/output, and a
   `// TODO` where the method throws `UnsupportedOperationException`.
2. Implement it in place.
3. Run its test:
   ```
   ./mvnw -q test -Dtest=com.gk.study.<module>.exercises.<ExerciseName>Test
   ```
4. It should go from failing (or erroring on the `TODO`) to green.
5. Only after solving it yourself, compare with
   `src/main/java/com/gk/study/<module>/solutions/` — the equivalent solved
   version with its own passing test in
   `src/test/java/com/gk/study/<module>/solutions/`.

## Useful commands

```
./mvnw -q test-compile                    # confirm everything compiles (main + test)
./mvnw -q test -Dtest='*SolutionTest'     # run only reference-solution tests (should always be green)
./mvnw -q test -Dtest='com.gk.study.list.*'   # run every test in one module
./mvnw -q test                            # run everything (many exercise tests fail until solved — expected)
```

## Checking progress

`PROGRESS.md` at the repo root tracks which modules are generated and which
build/test batches are green. It's updated as modules are completed.
