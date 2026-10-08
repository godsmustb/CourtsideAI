# Build Log

## 2026-10-08 · v0.1 (first build)
**What's in it**
- Draft tab: 131 players (top-130 9-cat consensus + IR stash), tiers, max bids, nominate-to-drain list, budget/max-bid/open-spots/room-heat tracker, guard counter, sale entry + undo + reset.
- Today tab: ESPN live scoreboard, week strip with games per day, prev/next day, "your players" per game.
- Injuries tab: ESPN injury report with My players / Draft targets / All filters and search.
- My Team tab: roster from draft + manual adds/drops, games left this week, injury badges, 25-game cap planner (hold at 24 → all-in night).
- Playbook tab: 9 plain-English sections + About.
- Background injury alerts (WorkManager, hourly) for rostered players; first run sets a baseline (no spam).

**Tests (JVM):** AuctionMathTest, CapPlannerTest, NamesTest, EspnParseTest, AssetsTest. Run in CI on every push.

**Known limits**
- Games-used count is entered by hand (copied from ESPN matchup page). ESPN league sync is backlog.
- ESPN's public feed is unofficial and may change.
- Player values are pre-season estimates (Oct 8, 2026).
