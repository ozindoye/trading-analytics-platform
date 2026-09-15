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