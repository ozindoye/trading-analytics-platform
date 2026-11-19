# Decisions

Architectural and tooling decisions for the Trading Analytics Platform, with the reasoning behind each. I only record a decision here when I genuinely chose between real alternatives, not for routine implementation steps.

## 1. Market data: Twelve Data over Alpha Vantage
Both APIs cover the ETFs I need, so coverage wasn't the deciding factor — the daily request ceiling was. Alpha Vantage's free tier caps at 25 calls/day, which is unworkable during active development; Twelve Data gives roughly 800/day. I went with Twelve Data for the headroom.

## 2. Track ETFs, not raw indices
I'm tracking index-tracking ETFs (SPY, QQQ, VTI) instead of raw index values. Raw index data carries stricter licensing on free tiers, and ETFs are what retail investors actually buy — so it's both more realistic and cleaner to fetch.

## 3. Scheduled ingestion into my own database, not live per-request
A scheduled job pulls fund data into my own database on a timer, and every user request is served from that database — the app never calls Twelve Data live. This turns a tight external rate limit into a non-issue by design rather than something the app can be blocked by, and it keeps response times fast and predictable.

## 4. AI feature: grounded and educational only
The AI explanation is built on the app's own computed metrics — I pass real numbers to the model and have it explain those, rather than letting it free-associate. That prevents hallucinated figures. I've also scoped it strictly to education, never personalized advice, to stay clearly on the right side of the line around financial advice.

## 5. Domain model: two entities, not one flat table
I modelled a Fund as its own entity rather than repeating the ticker as a string on every price row; each price bar holds a foreign key back to its fund. It's one extra class up front, but it makes funds first-class — which is exactly what side-by-side comparison and the saved watchlist both rely on — and it avoids a messy denormalization later. I kept the relationship one-directional (the foreign key lives only on the price bar) to avoid bidirectional serialization loops and lazy-loading pitfalls.

## 6. Store the full price bar, not just the closing price
Each daily row stores the full open/high/low/close/volume bar, even though the first chart only plots the close. The later metrics work — high/low, volatility, moving average — needs the rest, so storing it now means that work becomes pure computation over data I already hold, with no schema change and no re-fetching of history later.

## 7. Authentication as the final version, and non-gating
I'm adding login (Spring Security + JWT) as its own final version, paired with a personal watchlist, rather than bolting it on early. A login only earns its place when it protects real per-user state, so auth and the watchlist ship together. I deliberately kept it non-gating: the core charts stay publicly viewable, and logging in only unlocks saving a watchlist — so the deployed app shows something immediately instead of a signup wall. It's last because it's the largest piece of new technology in the project, and placing it last keeps every earlier version independently deployable.

## 8. Manual ingestion trigger during development
Rather than wiring the scheduled job straight away, I trigger ingestion during development
from a temporary endpoint I hit myself (POST /admin/ingest/{ticker}). This lets me control
exactly when I spend real API credits instead of firing a call on every restart. The manual
endpoint and the eventual scheduled job both call the same ingestion service, so the core
logic is proven first and adding the scheduler later is a one-line change.

## 9. Seeding funds with a CommandLineRunner
I seed the three funds (SPY, QQQ, VTI) with a small CommandLineRunner that runs on startup,
rather than a data.sql script. Doing it in Java means it uses my own entities and I can
explain every line, and checking whether each ticker already exists before inserting makes
it safe to run on every boot, which matters because my in-memory dev database resets each time.

## 10. v1 price endpoint: minimal shape, full history
My price endpoint returns just the date and close for each bar, not the full OHLCV, because
that is exactly what the v1 line chart needs and it keeps the API contract and payload
minimal. I also return a fund's whole history and let the frontend's time buttons window it
client-side, rather than filtering by date on the server. That is the simplest path to a
working chart, and I have left a server-side date range as an easy enhancement to add before
the payload size actually matters.

## 11. Ingestion trigger: manual in dev, scheduled in prod
I trigger data ingestion differently per environment, using Spring profiles so the two never
overlap. In development I keep the manual endpoint I built (scoped with @Profile("!prod")),
and in production a @Scheduled job (scoped with @Profile("prod")) refreshes the funds
automatically. A useful side effect is that the manual endpoint does not exist in production
at all, which removes an unsecured endpoint that could otherwise be hit to burn through my
API credits. I used a delay-based schedule (run about 30 seconds after startup, then every
24 hours) rather than a cron expression, so a fresh deploy fills its database almost
immediately instead of waiting for a set time. Each fund is ingested in its own try/catch so
one failure does not stop the others, and because ingestion is idempotent the daily refresh
only ever saves genuinely new bars.

## 12. Deployment: Railway plus MySQL, Vercel for the frontend
I deployed the backend and a managed MySQL database to Railway, and the React frontend to
Vercel. I chose Railway (about £5/month) over a free option like Render because a free
backend sleeps after inactivity and its free database expires after 30 days, both of which
would make the demo look broken when someone clicks it. Since this is the project I show
recruiters, an always-on, instant-loading demo was worth the small cost, and Railway also
offers MySQL so production matches the stack I designed for. In production the app runs under
a Spring "prod" profile pointing at MySQL, and every secret and connection detail (API key,
database URL, username, password) is injected as an environment variable on the host rather
than committed, so one codebase runs on H2 locally and MySQL in production with no secrets in
the repo.

## 13. Production ingestion is incremental, to bound memory
My scheduled ingestion originally pulled the full 20-year history for all three funds on
every run, which crashed the production container by running it out of memory. I changed the
scheduled job to fetch only recent bars (about 30 days), because a daily refresh only needs
to catch what is new, not re-download two decades each time. Full-history loading stays a
deliberate on-demand action in development, where memory is not constrained. This fixed the
crash and is the correct design: the production refresh is incremental and memory-light.

## 14. Fund comparison is computed server-side
For comparing funds I built a dedicated endpoint that returns each fund's series already
rebased to percentage change from the start of the window and aligned by date, rather than
having the frontend fetch each fund separately and do the maths itself. Comparing funds
fairly means comparing performance, not price level, so each fund is normalised to
(close / firstClose − 1) × 100 with its own first close in the window as the 0% baseline.
Keeping this on the server means the transform lives in one place I can unit-test, the
frontend just draws the result, it is one request instead of several, and it sets up the
metrics work in v3 to live in the same computation layer.

## 15. Sharpe ratio: assume a 0% risk-free rate
The Sharpe ratio is return per unit of risk — (return − risk-free rate) ÷ volatility. Doing it precisely would mean sourcing and date-aligning a daily risk-free series (like T-bills), which is a whole second data feed for very little payoff in a tool where every fund is judged on the same basis. So I assumed a 0% risk-free rate and documented it as a deliberate simplification rather than burying it. The ranking between funds barely shifts, the metric stays honest and comparable, and if I ever want the exact figure it's a single constant to change.

## 16. Track raw OHLC, not dividend/split-adjusted close
The technically correct input for long-run return and volatility is the adjusted close, which folds dividends and splits back into the price. Twelve Data's free tier doesn't expose it, and I didn't want to pay for a higher tier or hand-roll an adjustment I couldn't fully stand behind. So I store the raw OHLC the free tier gives me and call out the limitation openly. Over the recent windows this app compares, the difference is small.

## 17. Metrics are windowed server-side via optional from/to dates
The stat cards sit above the chart, so if the chart shows one month and the cards report all-time figures, the whole thing looks broken. Metrics have to track the visible window. My metrics endpoint takes optional from and to ISO dates and recomputes over that window server-side, and no params still means full history. I chose explicit dates over a ?range=1M token so the backend stays ignorant of the UI's presets, the contract stays RESTful, and it's exactly the shape my later custom date-picker will need, so the presets wire up now and the picker slots into the same endpoint with no backend change. Crucially my MetricsCalculator doesn't change at all, it still just takes a list of price bars, so only the query that selects the bars changed and my unit tests still hold.

## 18. Tiingo for adjusted price data, on a licensed plan
A backtest on raw prices is wrong in two ways: a share split looks like a crash, and buy-and-hold is understated because dividends are missing. Twelve Data's free tier has no adjusted prices, so I moved to Tiingo, which returns adjusted open, high, low and close alongside the raw values, plus the dividend paid and the split factor for each day. One request returns a fund's whole history. I compared Alpha Vantage (adjusted daily data is premium-only), EODHD (20 requests a day) and Financial Modeling Prep (limited symbols, and display needs a paid plan). Every free tier I looked at is for personal use only, so I asked Tiingo directly. They confirmed in writing that the Individual Power plan allows me to store the data while I am subscribed, as long as every copy is deleted when the subscription ends, and to show results to a closed, unpaid group of fewer than 10 people behind a private login, with no public pages and no way to download the raw data.

Adjusted history changes every time a fund pays a dividend, because adjustment rescales all earlier prices. I keep raw columns as immutable facts and treat adjusted columns as derived values: when the daily refresh sees a dividend or split, it re-downloads that fund's full history in one transaction.

## 19. Fund universe: 28 US-listed ETFs that track markets worldwide
Three US equity funds move almost together, so they say little about whether a strategy works in general. I chose 28 ETFs covering US, developed and emerging markets, single countries, bonds, T-bills, gold, real estate and commodities, each with a plain-English description so they can be searched by what they track as well as by ticker. All of them are US-listed, which gives one currency and one trading calendar, so the days line up across funds while the holdings still give global coverage. Non-US listings would have brought currency conversion and different holiday calendars for very little gain.

## 20. Backtests and comparisons respect each fund's history
Funds started trading in different years (SPY in 1993, INDA around 2012). Each fund stores its first available date. A backtest whose start date is earlier than that is rejected with a 400 and the earliest valid date, never silently moved, and the warm-up an indicator needs (such as a 200-day average) is handled explicitly and shown in the result. When funds or runs are compared, the window starts at the latest first date among them, so every result covers exactly the same days. Quietly shortening a window, or comparing results over different periods, produces exactly the kind of misleading result this project exists to prevent.

## 21. Engine scope: one ETF per backtest, long or flat
Each backtest runs on a single fund and is either fully invested or in cash, and cash earns 0%, consistent with the 0% risk-free rate in my Sharpe ratio. Strategies that rotate between funds, such as 60/40 rebalancing or dual momentum, need multi-asset portfolio accounting, which adds a lot of complexity for the time I have. One asset done rigorously is worth more than several done loosely, so I have recorded multi-asset strategies as future work.

## 22. Four strategy presets, each chosen for a different way backtests mislead
Alongside the buy-and-hold benchmark, the engine offers an SMA crossover (which covers the price-versus-average trend filter when the fast window is 1), time-series momentum, z-score mean reversion and a Donchian channel breakout. Each one tests a different weakness: the crossover is the classic lookahead and whipsaw case, momentum is sensitive to its lookback length, mean reversion trades often so costs matter, and the breakout uses highs and lows and has its own lookahead trap (including today's high in the channel). I considered a volatility filter, a drawdown stop and RSI and left them out. Four strategies show the engine is not built around one idea; more would be breadth for its own sake.

## 23. backtrader as the independent reference for validation
Financial calculations rarely have a known right answer, so I check my engine against an established library on identical data and assumptions. I chose backtrader because it is event-driven like my engine, fills market orders at the next bar's open by default and has configurable commission, so any difference points to a real bug rather than a different design. vectorbt's vectorised style makes lookahead easy to introduce, which makes it a poor reference for correctness, and backtesting.py is simpler but less configurable. backtrader is slowly maintained, but that doesn't matter for a pinned tool I only run offline.

## 24. University GitLab is the primary repository, GitHub a mirror
The technical system is assessed on the university GitLab, and its issues and milestones are part of the evidence of how I worked, so GitLab is the primary repository. My deployments were already connected to GitHub, so rather than move them I push every commit to both remotes, which keeps the full history identical in both places.

## 25. Robustness checks are prioritised as Musts and Shoulds
The robustness layer is the only part of the project that deals with overfitting, so I prioritised it rather than risk losing all of it to a time slip. The parameter sweep and the in-sample/out-of-sample split are Musts: they are the simplest checks with the clearest meaning. Walk-forward testing and a plain bootstrap confidence interval on the Sharpe ratio are Shoulds. The block bootstrap, the Deflated Sharpe Ratio and the Probability of Backtest Overfitting are discussed as future work rather than half-built. All of these call the engine as a pure function, so I designed the engine with no database access inside it.

## 26. Ticker validation with Spring MVC's built-in method validation
I validate ticker path and query parameters with @Pattern directly on the controller method parameters, and I deliberately left @Validated off the controllers. Since Spring 6.1, MVC validates constrained parameters itself and raises HandlerMethodValidationException, which reports exactly which parameter failed. Adding @Validated would switch on the older AOP-proxy validation as well, which throws ConstraintViolationException instead, and having two mechanisms that can both fire on the same method is harder to reason about. I map HandlerMethodValidationException in my GlobalExceptionHandler, so a bad ticker returns a 400 with the same ErrorResponse JSON as every other error rather than Spring's default ProblemDetail format.

## 27. Only store finished daily bars, and run ingestion after the US close (supersedes the schedule in 11)
My provider's daily series includes today's bar while the market is still open, so a run during the session stored a half-finished bar. Because ingestion skips dates it already has, that bar would never have been corrected, which is silent data corruption for a backtesting engine. I considered simply skipping any bar dated today in UTC, but my nightly run happens after the US close, when today's bar is already final, so that rule would have left my data a day behind, and it judges "today" on the wrong calendar. Instead I check against the exchange's own clock: a bar dated today in New York is only accepted from 16:30 New York time, half an hour after the close. The check is a small pure function with unit tests at fixed times, including both sides of the cutoff in summer and winter and the hours when the UTC date has rolled over but New York's hasn't.

I also replaced the delay-based schedule (30 seconds after startup, then every 24 hours), which drifted with every deploy and could land mid-session, with a fixed run at 22:00 UTC. That is 17:00 or 18:00 in New York depending on daylight saving, so always after the cutoff. I kept one run on application startup so a fresh deploy still fills in promptly; it is safe now because the completeness check rejects a mid-session bar, which the 22:00 run then picks up.
