# Prompts to paste into Claude Code

## Prompt 1 — Kick-off (paste this first)

Use the java-interview-study-material skill.

Create a complete Java interview study repository in this folder for me.
About me: Java backend developer, 3.5+ years (banking domain), preparing for
interviews. I know Java basics — go deep on internals, complexity, trade-offs
and interview-level detail.

Scope, in this exact order:
1. DSA with Java Collections: foundations, collections overview, List,
   Queue/Deque, Set, Map, mixed DSA problem sets
2. Streams API
3. Multithreading & concurrency
4. Java interview questions (structured, with model answers)
5. Spring Boot interview questions (structured, with model answers)
6. Revision cheat sheets + mock interviews

Requirements:
- Numbered folders so the learning order is clear
- Concept notes for every topic, with diagrams and Big-O tables
- Runnable example programs for every topic
- Exercises for ME to solve (TODO stubs + JUnit tests), Easy → Hard,
  solutions kept in a separate package
- An 8-week study plan and a PROGRESS.md checklist

Start with project setup + 00-roadmap + 01-java-foundations-for-dsa.
Build and test, then stop and show me a summary. Wait for me to say
"next" before the next module.

## Prompt 2 — Continue
next — generate the next module from PROGRESS.md. Same quality bar. Build, test, summarize, stop.

## Prompt 3 — Go deeper on a topic
Go deeper on <topic, e.g. HashMap internals>: add more internals detail,
3 more Hard exercises with tests + solutions, and 10 more interview questions.

## Prompt 4 — Check my solution
I solved the exercises in <module>. Run the exercise tests, review my code
like an interviewer (correctness, complexity, edge cases, clean code),
and tell me what to improve. Don't rewrite it for me unless I ask.

## Prompt 5 — Mock interview
Take a mock interview from me on <module>. Ask one question at a time,
wait for my answer, grade it (1–10), give the ideal answer, then next question.
10 questions total, mix of theory + one coding problem.
