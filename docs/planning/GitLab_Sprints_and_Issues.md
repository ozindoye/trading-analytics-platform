# GitLab sprints and issues: Trading Analytics Platform (CO3204)

How to use this file:
- **Milestones:** each `## Milestone` heading is one GitLab milestone. Copy its title, dates and description into Plan → Milestones → New milestone.
- **Issues:** each `### Issue` is one GitLab issue. Copy the title, set the labels and milestone shown, and paste the **Goal** and **Acceptance criteria** into the issue description. The `- [ ]` lines become tick-boxes in GitLab.
- **Labels to create first** (Manage → Labels):
  - Priority: `Must`, `Should`, `Could`
  - Type: `feature`, `bug`, `test`, `infra`, `data`, `docs`
  - Board status: `Doing`, `Review`
- **Requirement IDs:** IDs in brackets (FR6, NFR5, E2…) refer to the tables in the interim report, so markers can trace issue → requirement → test.
- **Detail level:** only S0–S2 need to be complete now. Create later sprints' issues now (they show the plan), and refine them when the sprint starts.

**Definition of done** (paste into every milestone description, or once in the project README):

> An issue is done when the work is merged to `main` with a passing CI pipeline, has tests if it contains logic, is deployed, and has a `Decisions.md` entry if it involved choosing between alternatives.

**Retrospective** (add to the milestone description when the sprint ends):

> **Went well:** … **Didn't go well:** … **Change for next sprint:** …

---

## Milestone S0: Interim report and quick wins (9–19 Nov 2026)

**Description:** Finish the code-quality fixes A3 to A6 and the GitLab migration, and submit the interim report. Milestone M1. Close this milestone once the report is submitted.

### Issue: A3 Validate ticker path variables and normalise case
Labels: `Must` `bug` · Milestone: S0
**Goal:** Reject malformed tickers with a clear 400, and make `/metrics` accept lower-case tickers like `/prices` does.
**Acceptance criteria:**
- [ ] `@Pattern(regexp = "[A-Za-z]{1,10}")` on every `{ticker}` path variable, with `@Validated` on the controllers
- [ ] Method-validation exception mapped to 400 with the standard `ErrorResponse` shape
- [ ] `MetricsService` upper-cases the ticker; `/api/funds/spy/metrics` returns 200
- [ ] Tests cover a valid ticker, an invalid ticker (`SP-Y!`) and a lower-case ticker

### Issue: A4 Skip partial-day bars and run ingestion after the US close
Labels: `Must` `bug` · Milestone: S0
**Goal:** Never store an incomplete bar for the current day.
**Acceptance criteria:**
- [ ] Ingestion skips any bar dated today (UTC)
- [ ] Production scheduler uses `@Scheduled(cron = "0 0 22 * * *", zone = "UTC")`
- [ ] Test proves today's bar is skipped and yesterday's is saved
- [ ] `Decisions.md` entry superseding the delay-based schedule

### Issue: A5 Remove the N+1 query in ingestion
Labels: `Must` `infra` · Milestone: S0
**Goal:** Load existing dates once instead of one query per bar.
**Acceptance criteria:**
- [ ] `findDatesByFund(Fund)` returns `Set<LocalDate>` (JPQL projection)
- [ ] Ingestion filters new bars in memory using that set
- [ ] Re-running ingestion on the same data saves 0 new rows (idempotency test)

### Issue: A6 Typed DTO for the compare endpoint
Labels: `Must` `feature` · Milestone: S0
**Goal:** Replace `List<Map<String, Object>>` with a proper response record.
**Acceptance criteria:**
- [ ] `CompareRowResponse` record used by `PriceQueryService` and `PriceController`
- [ ] JSON shape unchanged for the frontend (or frontend updated in the same merge)
- [ ] Controller test checks the response shape

### Issue: Migrate repository to university GitLab with GitHub mirror
Labels: `Must` `infra` · Milestone: S0
**Goal:** GitLab is the primary repo, holding the full history; one push updates both remotes.
**Acceptance criteria:**
- [x] Full history merged into the lecturer-created project (README and FAQ kept)
- [x] `origin` fetches from GitLab and pushes to GitLab and GitHub
- [ ] Labels and milestones S0 to S9 created

### Issue: Submit interim report
Labels: `Must` `docs` · Milestone: S0
**Goal:** Submit the final interim report on Blackboard by 12:00 on 19 Nov.
**Acceptance criteria:**
- [ ] Tracked changes reviewed; Figure 10 replaced; cover page completed; contents table updated
- [ ] Claims about A3 to A6 match what is actually merged
- [ ] PDF checked page by page and submitted

---

## Milestone S1: Engineering baseline (16–29 Nov 2026)

**Description:** Automated testing on every push, database migrations under version control, a health check, and two known metric defects fixed. Choose the data provider before S2 starts. Milestone M2.

### Issue: B1 CI pipeline (backend tests and frontend lint/build)
Labels: `Must` `infra` · Milestone: S1
**Goal:** Every push is built and tested automatically (NFR7).
**Acceptance criteria:**
- [ ] `.gitlab-ci.yml`, or GitHub Actions on the mirror if university runners aren't available
- [ ] Backend job: Java 17, `mvn --batch-mode test`
- [ ] Frontend job: `npm ci && npm run lint && npm run build` in `frontend/`
- [ ] A failing test turns the pipeline red; badge or link in the README

### Issue: B3 Actuator health endpoint
Labels: `Should` `infra` · Milestone: S1
**Goal:** A health URL for deployment checks.
**Acceptance criteria:**
- [ ] `/actuator/health` returns UP in production
- [ ] No other actuator endpoints are exposed in production

### Issue: B2 Flyway migrations and `ddl-auto=validate` in production
Labels: `Must` `infra` · Milestone: S1
**Goal:** Schema changes are versioned scripts, not Hibernate guesses (needed before D3 adds columns).
**Acceptance criteria:**
- [ ] `flyway-core` (plus the MySQL module) added; `V1__initial_schema.sql` matches the current schema
- [ ] Existing production database baselined without data loss
- [ ] Production uses `ddl-auto=validate`; the app starts cleanly on H2 and MySQL

### Issue: Fix compare endpoint to use a common start date (FR5)
Labels: `Must` `bug` · Milestone: S1
**Goal:** All funds in a comparison are rebased on the same day.
**Acceptance criteria:**
- [ ] Rebasing starts at the first date on which every selected fund has a bar
- [ ] Test with two funds whose histories start on different dates

### Issue: Report Sharpe as undefined when volatility is zero
Labels: `Should` `bug` · Milestone: S1
**Goal:** A flat price series shows "not defined", not a misleading 0.
**Acceptance criteria:**
- [ ] `MetricsCalculator` returns `null` Sharpe when the standard deviation is 0
- [ ] Stat card shows "not defined"
- [ ] Existing test updated

### Issue: Choose data provider (Tiingo vs EODHD) and confirm licence
Labels: `Must` `docs` · Milestone: S1
**Goal:** A written-permission data source chosen before S2 (Decision 18 revisited).
**Acceptance criteria:**
- [ ] Replies received from Tiingo (storage, post-cancellation, recorded demo) and EODHD (closed-group display)
- [ ] Provider chosen; cost and conditions recorded in `Decisions.md`, Notion and CLAUDE.md
- [ ] Subscription started only when D1 begins

### Issue: Update README roadmap
Labels: `Could` `docs` · Milestone: S1
**Goal:** The README reflects the current state and the backtesting direction.
**Acceptance criteria:**
- [ ] v2 and v3 marked complete; backtesting engine and robustness listed as the main roadmap

---

## Milestone S2: Adjusted data for 28 funds (30 Nov–13 Dec 2026)

**Description:** Switch to dividend- and split-adjusted data, backfill full history safely, and expand to 28 searchable ETFs with descriptions and first available dates. Milestone M3.

### Issue: D1 Adjusted-price ingestion client
Labels: `Must` `data` · Milestone: S2
**Goal:** Ingest adjusted OHLC plus dividend and split fields from the chosen provider (FR1).
**Acceptance criteria:**
- [ ] New client and `@ConfigurationProperties`; API key only in git-ignored config or environment variables
- [ ] Flyway migration adds `adj_open/high/low/close`, `div_cash`, `split_factor`
- [ ] Ingestion is idempotent (re-run saves 0 rows) and respects the provider's rate limit
- [ ] Client tested against a saved sample response (no live API in tests)

### Issue: D1b Re-ingest a fund's history after a dividend or split
Labels: `Must` `data` · Milestone: S2
**Goal:** Adjusted history stays correct when old prices are re-adjusted.
**Acceptance criteria:**
- [ ] Daily refresh detects `div_cash > 0` or `split_factor ≠ 1` and re-downloads that fund in one transaction
- [ ] Raw columns never change; adjusted columns are updated
- [ ] Fund's last-adjustment date (`data_version`) recorded
- [ ] Integration test with a simulated dividend

### Issue: D2 Memory-bounded full-history backfill in production
Labels: `Must` `infra` · Milestone: S2
**Goal:** Load about 20 years for all funds without repeating the earlier out-of-memory crash.
**Acceptance criteria:**
- [ ] Secured trigger (admin key from an environment variable), or an equivalent one-off mechanism
- [ ] Processes one fund at a time; logs progress
- [ ] All 28 funds backfilled in production

### Issue: D3 Expand to 28 ETFs with metadata (FR2)
Labels: `Must` `data` · Milestone: S2
**Goal:** Funds carry a description, asset class and first available date.
**Acceptance criteria:**
- [ ] Flyway migration adds `description`, `asset_class`, `first_date`
- [ ] Seeder holds the 28 funds with plain-English "tracks…" descriptions
- [ ] `first_date` set from the provider's metadata or the earliest stored bar

### Issue: D3b Fund list and search API (FR3)
Labels: `Should` `feature` · Milestone: S2
**Goal:** `GET /api/funds?q=` lists and searches funds.
**Acceptance criteria:**
- [ ] `FundResponse` DTO (ticker, name, description, asset class, first date)
- [ ] Case-insensitive search on ticker and description ("japan" finds EWJ)
- [ ] Controller tests

### Issue: D3c Frontend fund selector with search
Labels: `Should` `feature` · Milestone: S2
**Goal:** Replace the hard-coded `FUNDS` array.
**Acceptance criteria:**
- [ ] `FundSelector` component loads from `/api/funds`, with a search box
- [ ] Shows what each fund tracks and its start date
- [ ] Charts and compare work with any of the 28 funds

### Issue: Data-quality checks at ingestion
Labels: `Should` `data` · Milestone: S2
**Goal:** Flag suspicious bars rather than silently trusting them.
**Acceptance criteria:**
- [ ] Logs day-to-day adjusted moves over 25%, missing trading days and zero-volume bars
- [ ] Unit tests for each check

---

## Milestone Winter: Correctness and reproducibility (14 Dec 2026–10 Jan 2027)

**Description:** Reduced capacity; no new features. Prove the metrics are correct, add an integration test and Docker, and write up the literature review for the dissertation. Milestone M4 (11 Jan).

### Issue: C1 Decide double vs BigDecimal for metrics and engine
Labels: `Must` `docs` · Milestone: Winter
**Goal:** An explicit, logged precision decision that also applies to the engine.
**Acceptance criteria:**
- [ ] Options and trade-offs written in `Decisions.md` (exact decimals for cash, doubles for statistics, or another split)
- [ ] Test tolerances documented

### Issue: C2 Reference-verified metric tests (E4)
Labels: `Must` `test` · Milestone: Winter
**Goal:** Metric tests compare against an independent calculation, not my own numbers.
**Acceptance criteria:**
- [ ] Python/pandas script computes return, volatility, drawdown and Sharpe on exported bars
- [ ] Java tests assert agreement within ±0.01 percentage points for 3 funds × 5 windows
- [ ] Script committed under `evaluation/`

### Issue: C3 Integration test for metrics
Labels: `Must` `test` · Milestone: Winter
**Goal:** Replace `contextLoads()` with a real end-to-end test.
**Acceptance criteria:**
- [ ] `@SpringBootTest` seeds a fund and known bars in H2, calls the metrics endpoint, and checks results
- [ ] Runs in CI

### Issue: B4 Dockerfile and docker compose
Labels: `Should` `infra` · Milestone: Winter
**Goal:** The whole system starts with one command (NFR10).
**Acceptance criteria:**
- [ ] Multi-stage backend Dockerfile; compose file with MySQL and the frontend
- [ ] Data key supplied via environment variables; README instructions tested on a clean machine

### Issue: Literature review write-up for the dissertation
Labels: `Should` `docs` · Milestone: Winter
**Goal:** Turn interim report Chapter 2 into a dissertation-ready draft.
**Acceptance criteria:**
- [ ] Draft chapter saved in the repo (`docs/`) or a shared document
- [ ] New sources from the interim feedback added

---

## Milestone S3: Engine core (11–24 Jan 2027)

**Description:** Exam period, reduced capacity. Build the event-driven engine and the first strategy, with lookahead made structurally impossible and tested.

### Issue: E1 Backtesting engine core
Labels: `Must` `feature` · Milestone: S3
**Goal:** `BacktestEngine.run(bars, strategy, params, config)` as a pure function (FR7, FR8).
**Acceptance criteria:**
- [ ] `Strategy` interface, read-only `BarView` (bars 0..t), `Portfolio`, `ExecutionModel` (fill at next open), `CostModel` (non-zero default bps), `BacktestResult`
- [ ] Long/flat only; whole shares; cash earns 0%
- [ ] No database or clock access inside the engine
- [ ] Hand-computed 10-bar unit tests: no trades, one round trip, costs applied, warm-up only

### Issue: E2 SMA crossover strategy (FR10)
Labels: `Must` `feature` · Milestone: S3
**Goal:** First preset, with `SmaCalculator` as a pure static class.
**Acceptance criteria:**
- [ ] Fast/slow parameters with defaults 50/200; fast = 1 gives the trend filter
- [ ] Declares its warm-up (slow window)
- [ ] Hand-verified signals on a small fixed series

### Issue: Metamorphic lookahead test harness (E2, NFR2)
Labels: `Must` `test` · Milestone: S3
**Goal:** Prove that no strategy uses future data.
**Acceptance criteria:**
- [ ] Reusable test: for random t, perturb all bars after t; trades up to t must be identical
- [ ] Runs for every strategy automatically
- [ ] A deliberately leaky variant (fills at day t's close) fails the test

---

## Milestone S4: Engine MVP live (25 Jan–7 Feb 2027)

**Description:** Results, benchmark, API, a basic Backtest screen and the private login required by the data licence, all deployed in time to demonstrate at the interview (8–19 Feb). Milestone M5.

### Issue: E3 Results and buy-and-hold benchmark (FR9)
Labels: `Must` `feature` · Milestone: S4
**Goal:** Every run reports the equity curve, trades and metrics against buy-and-hold.
**Acceptance criteria:**
- [ ] Benchmark computed in the same run from the same bars, paying entry costs
- [ ] Return, volatility, drawdown, Sharpe, number of trades, time in market, total costs
- [ ] Assumptions and warm-up used included in the result

### Issue: E4 Backtest API with history-length rules (FR6)
Labels: `Must` `feature` · Milestone: S4
**Goal:** `GET /api/funds/{ticker}/backtest` with validated parameters.
**Acceptance criteria:**
- [ ] Bean Validation on all parameters; 400 for bad input
- [ ] `from` before first date + warm-up gives a 400 that states the earliest valid date
- [ ] Controller and integration tests

### Issue: E5 Backtest screen
Labels: `Must` `feature` · Milestone: S4
**Goal:** Run and inspect a backtest in the browser.
**Acceptance criteria:**
- [ ] Form: fund, dates (limited to the fund's history), strategy, parameters, costs
- [ ] Equity chart (strategy vs buy-and-hold), summary table, trade log with signal and fill dates
- [ ] Assumptions and the "education, not advice" notice visible

### Issue: FR17 Private login for invited viewers
Labels: `Must` `feature` · Milestone: S4
**Goal:** Meet the data licence's closed-group condition.
**Acceptance criteria:**
- [ ] Spring Security login; accounts created only by me (no sign-up page)
- [ ] BCrypt-hashed passwords; HTTPS only; failed-login rate limiting
- [ ] All pages and `/api/**` require login in production
- [ ] No data export or download features

### Issue: Interview demo preparation
Labels: `Must` `docs` · Milestone: S4
**Goal:** A reliable 5–10 minute demo and progress summary.
**Acceptance criteria:**
- [ ] Demo script; backup recording in case of network issues
- [ ] One-slide progress summary against the interim plan

---

## Milestone S5: Further strategies (8–21 Feb 2027)

**Description:** Interview period. Add the remaining three presets behind the same interface, each with its own lookahead test. Milestone M6.

### Issue: R1a Time-series momentum (FR10)
Labels: `Must` `feature` · Milestone: S5
**Goal:** Invested while the return over the look-back L (default 252) is positive.
**Acceptance criteria:**
- [ ] Declares a warm-up of L bars; hand-verified test
- [ ] Passes the metamorphic lookahead test

### Issue: R1b Z-score mean reversion (FR11)
Labels: `Should` `feature` · Milestone: S5
**Goal:** Buy when z < −c (defaults N = 20, c = 2); exit when z ≥ 0.
**Acceptance criteria:**
- [ ] Mean and standard deviation from the previous N closes only; hand-verified test
- [ ] Passes the metamorphic lookahead test

### Issue: R1c Donchian breakout (FR11)
Labels: `Should` `feature` · Milestone: S5
**Goal:** Enter when the close exceeds the highest high of the previous N days (55); exit below the lowest low of the previous M days (20).
**Acceptance criteria:**
- [ ] Channel excludes day t; uses adjusted highs and lows; hand-verified test
- [ ] Passes the metamorphic lookahead test

---

## Milestone S6: Robustness Musts (22 Feb–7 Mar 2027)

**Description:** The checks that measure overfitting: parameter sweep and in-sample/out-of-sample split, with their screen. Milestone M7.

### Issue: R2 Parameter sweep (FR12)
Labels: `Must` `feature` · Milestone: S6
**Goal:** Run a strategy over a capped grid and summarise the spread.
**Acceptance criteria:**
- [ ] Grid capped at 100 combinations; bars loaded once
- [ ] Returns worst/median/best Sharpe, % beating buy-and-hold, number of trials
- [ ] Verified on synthetic data with a known best setting

### Issue: R3a In-sample / out-of-sample split (FR13)
Labels: `Must` `feature` · Milestone: S6
**Goal:** Choose on the first 70%, test unchanged on the last 30%.
**Acceptance criteria:**
- [ ] Out-of-sample indicators warm up on earlier bars but never trade on them
- [ ] Reports in- and out-of-sample metrics and degradation
- [ ] Test confirms the out-of-sample period always follows the in-sample period

### Issue: Robustness screen (sweep and split)
Labels: `Must` `feature` · Milestone: S6
**Goal:** "Is this result real?" page opened from a backtest.
**Acceptance criteria:**
- [ ] Heatmap (two-parameter strategies) or line chart (one parameter)
- [ ] Split table with degradation; number of trials shown

---

## Milestone S7: Robustness Shoulds, saved runs and feature freeze (8–21 Mar 2027)

**Description:** Walk-forward, the bootstrap interval and saved runs. **Feature freeze at the end of this sprint (22 Mar):** only bug fixes afterwards. Milestone M8.

### Issue: R3b Walk-forward testing (FR14)
Labels: `Should` `feature` · Milestone: S7
**Goal:** Repeat the split over sliding windows and stitch the out-of-sample results.
**Acceptance criteria:**
- [ ] Rolling vs anchored windows decided and logged in `Decisions.md`
- [ ] Stitched out-of-sample Sharpe; count of windows where the chosen setting changed
- [ ] Capped at 10 windows × 30 combinations

### Issue: R4 Bootstrap confidence interval for Sharpe (FR15)
Labels: `Should` `feature` · Milestone: S7
**Goal:** A 95% interval from 1,000 resamples of the daily returns.
**Acceptance criteria:**
- [ ] Fixed random seed: same inputs give the same interval
- [ ] Flags when the interval includes 0; independence assumption shown
- [ ] Verified on synthetic returns with a known Sharpe

### Issue: R5 Save and compare runs (FR16)
Labels: `Should` `feature` · Milestone: S7
**Goal:** Store runs with every assumption and compare them fairly.
**Acceptance criteria:**
- [ ] `saved_runs` table via Flyway, owned by the logged-in user
- [ ] Comparison uses a common window and flags mismatched costs or dates
- [ ] Warns when a run's `data_version` is older than the fund's current one

---

## Milestone S8: Evaluation (22 Mar–4 Apr 2027)

**Description:** Validate against backtrader, measure performance and run the four experiments. No new features. Milestone M9.

### Issue: E3 Differential tests against backtrader
Labels: `Must` `test` · Milestone: S8
**Goal:** Independent validation on 40 cases (4 strategies × 5 funds × 2 periods).
**Acceptance criteria:**
- [ ] backtrader pinned to one version; fills, sizing, costs and returns configured to match
- [ ] Identical trade dates; final equity within 0.1%
- [ ] Every remaining difference explained in `evaluation/README.md`

### Issue: E6 Performance measurements (NFR5)
Labels: `Must` `test` · Milestone: S8
**Goal:** Time single backtests, sweeps and walk-forward on the production host.
**Acceptance criteria:**
- [ ] Timing script committed; results table
- [ ] 20-year backtest under 1 s; 100-combination sweep under 10 s

### Issue: E7 Four demonstration experiments
Labels: `Must` `test` · Milestone: S8
**Goal:** Quantify how much each error inflates results.
**Acceptance criteria:**
- [ ] Cost sensitivity (0, 5, 20 bps) for every strategy
- [ ] Adjusted vs raw prices
- [ ] Lookahead injection
- [ ] Overfitting across all 28 funds
- [ ] Results and charts saved for the dissertation

### Issue: E8 Usability review
Labels: `Should` `test` · Milestone: S8
**Goal:** Heuristic evaluation and supervisor walkthrough.
**Acceptance criteria:**
- [ ] Interface checked against Nielsen's 10 heuristics; issues logged as `bug` issues
- [ ] Walkthrough of UC1 to UC7 with supervisor; no critical issues left open

---

## Milestone S9: Dissertation and submission (5–18 Apr 2027, buffer to 25 Apr)

**Description:** Write the dissertation, fix bugs and finalise the repository. Use the contingency week 19–25 Apr. Submit on 26 Apr (Milestone M10); mini viva 27 Apr–14 May (M11).

### Issue: Dissertation draft
Labels: `Must` `docs` · Milestone: S9
**Goal:** Complete draft to supervisor by 12 Apr.
**Acceptance criteria:**
- [ ] Chapters: introduction, literature, design, implementation, evaluation (E1–E8), reflection, limitations
- [ ] Supervisor feedback addressed

### Issue: Final repository tidy-up
Labels: `Must` `docs` · Milestone: S9
**Goal:** The technical system is easy to assess on GitLab.
**Acceptance criteria:**
- [ ] README: setup, Docker, architecture, test commands, licence note
- [ ] CI green on `main`; no secrets; tag `v1.0-submission`

### Issue: Mini viva preparation
Labels: `Must` `docs` · Milestone: S9
**Goal:** A confident presentation and live demo.
**Acceptance criteria:**
- [ ] Slides; demo script; backup recording
- [ ] Practice answers to the primer's viva questions

---

## Backlog (no milestone): stretch goal

### Issue: AI plain-English explanation of results (FR18)
Labels: `Could` `feature` · Milestone: none (only after feature freeze if all Musts are done)
**Goal:** Explain a backtest result in plain English, grounded only in computed numbers.
**Acceptance criteria:**
- [ ] Server-side only; API key in environment variables
- [ ] Every number in the text matches the result; educational framing, no advice
