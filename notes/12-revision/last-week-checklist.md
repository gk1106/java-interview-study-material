# Last-Week Checklist

Day-by-day plan for the final 7 days before the interview. This is the concrete walk-through of
Week 8 in `notes/00-roadmap/study-plan.md` ("Final revision + mock interviews") — that plan says
*what* the week covers at a glance; this file is the checklist you actually tick off each day.
Adjust the calendar to your real interview date, but keep the same order and relative spacing.

---

## Day 7 — Cheat sheets, pass 1

- [ ] Read every file in `notes/12-revision/one-page-cheatsheets/` top to bottom, once, without
  stopping to look anything up — this is a calibration pass to find out what's actually rusty.
- [ ] For anything that felt shaky, note the module (e.g. "06-map: treeify threshold") in a scratch
  list — you'll target these specifically on Day 4.
- [ ] Re-skim `notes/03-list/`, `notes/04-queue-deque/` in full (not just the cheat sheet) — these
  two modules anchor the most DSA coding-round questions.

## Day 6 — Cheat sheets, pass 2 + Collections/Map deep re-skim

- [ ] Re-skim `notes/05-set/` and `notes/06-map/` in full — HashMap internals is the single most
  commonly asked "explain how it works" question across interviews.
- [ ] Redo the **build-it-yourself** exercises from memory, without looking at your old solution
  first: `MyArrayList`/`MySinglyLinkedList` (module 03), circular queue/min-heap (module 04),
  `MyHashMap`/LRU cache (module 06). Compare only after you've attempted each one.
- [ ] Re-read `notes/12-revision/one-page-cheatsheets/03-list-cheatsheet.md`,
  `04-queue-deque-cheatsheet.md`, `05-set-cheatsheet.md`, `06-map-cheatsheet.md`.

## Day 5 — Mock Interview Round 1 + Round 2

- [ ] Run **Round 1 — DSA & Collections (45 min)** from `mock-interviews.md` under a real timer,
  writing actual code, not pseudocode.
- [ ] Run **Round 2 — Streams & Java Language (30 min)**.
- [ ] Self-grade both against their rubrics immediately after finishing, while the attempt is fresh.
- [ ] Re-skim `notes/07-dsa-problem-sets/00-pattern-cheatsheet.md` — the pattern → signal-phrase
  table is the fastest ROI re-read in the whole repo the night before any DSA-heavy round.

## Day 4 — Weak-area drilling + Concurrency re-skim

- [ ] Go back to the scratch list from Day 7 and specifically re-read those notes files in full
  (not just the cheat sheet line) until each one no longer feels shaky.
- [ ] Re-skim `notes/09-multithreading-concurrency/` — at minimum topics 02 (synchronized/wait-notify),
  04 (locks), 08 (synchronizers), 10 (classic problems), 11 (virtual threads).
- [ ] Rebuild the classic concurrency problems from memory: deadlock (create + fix), producer-consumer
  (both the `wait`/`notify` version and the `BlockingQueue` version), odd/even alternation,
  double-checked-locking singleton.
- [ ] Re-read `notes/12-revision/one-page-cheatsheets/09-concurrency-cheatsheet.md`.

## Day 3 — Mock Interview Round 3 + Round 4

- [ ] Run **Round 3 — Concurrency (30 min)**.
- [ ] Run **Round 4 — Spring Boot & System Design Lite (45 min)**.
- [ ] Self-grade both immediately.
- [ ] Re-skim `notes/11-spring-boot-interview-questions/scenario-based.md` in full — these are the
  questions most likely to separate you from candidates who've only built features in a sandbox,
  and the *structure* of your answer (what you'd check first/second/third) is what's graded.
- [ ] Re-read `notes/12-revision/one-page-cheatsheets/10-java-questions-cheatsheet.md` and
  `11-spring-boot-cheatsheet.md`.

## Day 2 — Round 5 (optional) + full cheat-sheet sweep

- [ ] If you have energy for a third mock round, run **Round 5 — Full Mixed Rapid-Fire (60 min)** —
  otherwise this is optional; two rounds (Days 3 and 5) already satisfies the "at least 2 of 5"
  minimum.
- [ ] Read all 11 cheat sheets once more, back to back, in numeric order — by now this should take
  well under an hour and feel like confirmation, not new learning.
- [ ] Re-read the 13 output-prediction puzzles listed in `10-java-questions-cheatsheet.md` and make
  sure you can state the *mechanism* behind each one (not just the memorized printed value).
- [ ] Start the logistics checklist below — don't leave it all for tomorrow.

## Day 1 — Light review + logistics only

- [ ] Skim cheat sheets only — no new problems, no new deep-dives. The goal today is confidence,
  not cramming.
- [ ] Re-read your own notes/scratch list from Day 7/Day 4 one last time.
- [ ] Finish the full logistics checklist below.
- [ ] Stop studying at least 2-3 hours before bed. Get a full night's sleep — a tired brain loses
  more interview performance than one extra hour of review gains back. This is not optional advice;
  treat it as a checklist item.

## Interview Day

- [ ] Light skim of cheat sheets only, first thing, for 15-20 minutes max — a warm-up, not a review.
- [ ] Arrive/log in 10-15 minutes early; confirm audio/video/screen-share works *before* the
  interviewer joins, not after.
- [ ] Have water nearby. Eat something beforehand — don't interview hungry.
- [ ] Breathe. If you blank on a question, say so out loud and think through it verbally rather than
  going silent — interviewers grade process, not just the final answer (see the scenario-question
  rubric in `mock-interviews.md`).

---

## Final logistics checklist

- [ ] **Resume** — final version ready, matches what you'll actually say (don't list a technology
  you can't discuss for 5 minutes).
- [ ] **Questions to ask the interviewer** prepared in advance — have 3-5 ready, genuinely tailored
  (team structure, on-call/incident process, what a recent production issue looked like, how they
  do code review, what the next 6 months of the role looks like). Never end an interview with "no
  questions."
- [ ] **Laptop/environment for a live-coding round**:
  - IDE or editor you're comfortable in, with auto-formatting/autocomplete behavior you understand
    (don't discover unfamiliar autocomplete quirks live on camera).
  - Confirm whether the round uses a shared online editor (CoderPad, HackerRank, a Google Doc) vs
    your own machine — if shared, do a throwaway practice session in that exact tool beforehand so
    the environment itself isn't a surprise.
  - Charger plugged in, notifications/Slack/email silenced, camera framing checked.
  - A notepad or scratch file for working through complexity/edge cases before typing code.
- [ ] **Company/role research** — read the job description again, note 2-3 things from it to weave
  into answers (e.g. if it mentions high-throughput payments, lean on the banking-flavored examples
  from `notes/08-streams/07-banking-dataset-exercises.md` and the concurrency module).
- [ ] **Logistics** — confirm interview time zone, meeting link, and who you're meeting, the night
  before, not the morning of.
- [ ] **Sleep** — see Day 1 above. Repeated here because it's the single most underrated prep step.

---

## Red flags to avoid

Common mistakes that cost otherwise-strong candidates points in exactly these kinds of interviews:

- **Jumping straight to code without stating the approach/complexity first.** Interviewers can't
  follow your reasoning if the first thing they see is code — always say "I'll use a min-heap of
  size k, O(n log k)" before writing a line, exactly as drilled in every mock round above.
- **Silence when stuck.** Going quiet for 60+ seconds reads far worse than thinking out loud, even
  imperfectly. Narrate: "I think this needs a hash map because I need O(1) lookup, let me think
  about the key..."
- **Reciting a memorized definition instead of showing understanding.** "HashMap is O(1)" without
  being able to explain *why* (hashing, bucket index, treeification) doesn't survive a follow-up
  question — and there is always a follow-up question at 3.5+ YOE level.
- **Ignoring edge cases until the interviewer prompts for them.** Null input, empty collection,
  single-element input, duplicate keys — mention these proactively, don't wait to be asked.
- **Treating amortized complexity as worst-case, or vice versa**, when explicitly asked — this repo's
  own module 01 calls this out as the most common Big-O mix-up at this experience level.
- **For scenario/production questions: guessing a root cause immediately** instead of describing a
  triage process. "It's probably the database" with no investigation steps is a weaker answer than
  a structured "first I'd check blast radius, then recent changes, then resource saturation..." even
  if the guess happens to be right.
- **Not asking clarifying questions on an ambiguous problem statement** — assuming input constraints
  (sorted? duplicates allowed? size bounds?) instead of asking wastes time solving the wrong problem.
- **Overusing legacy/discouraged classes without knowing it** — reaching for `Vector`/`Stack`/
  `Hashtable`/`Collections.synchronizedList` as if they were the modern answer signals stale
  knowledge; know why the repo's own decision tables (module 02) steer away from them.
- **For concurrency code: forgetting `finally` for `unlock()`/`release()`**, or using `if` instead
  of `while` around a `wait()` condition — both are called out explicitly in
  `09-concurrency-cheatsheet.md` because they're the two most common live-coding slips under pressure.
- **Not having questions for the interviewer at the end.** It reads as low genuine interest even
  when that's not the intent.
