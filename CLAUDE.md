# CLAUDE.md: Build Rulebook for "Courtside AI"

Native Android app (Kotlin + Jetpack Compose, Material 3) for one user first: Nunna (non-coder, PM background, ADHD, highly visual; doesn't follow basketball). Public repo; may be promoted as a "2026-27 NBA Tracker". Sister project/pattern source: `godsmustb/GuitarNoodle` (Obtainium + GitHub Releases pipeline).

## Non-negotiable rules
1. **Release pipeline:** every push to `main` → `.github/workflows/release.yml` runs `testDebugUnitTest assembleRelease`, signs with secrets `CS_KEYSTORE_B64` / `CS_KEYSTORE_PASSWORD` (alias `courtside`), publishes Release `v0.1.<run_number>`. versionCode = 100 + run_number. Never change applicationId `com.nunna.courtside` or the signing key.
2. **No private data in the repo.** It's public: no coworker names, league IDs, ESPN cookies (`espn_s2`, `SWID`), API keys or keystores. Personal data lives only on the phone (SharedPreferences via `Store`).
3. **Data, not code:** player values/tiers/max bids = `app/src/main/assets/players.json`; strategy text = `playbook.json`. Both are validated by `AssetsTest`.
4. **Pure engines** (`engine/`: AuctionMath, CapPlanner, Names, Dates, Roster) have no Android imports and have unit tests. Bugs start with a failing test.
5. **Live data:** ESPN public site API (`site.api.espn.com/.../nba/scoreboard`, `/injuries`), parsed defensively in `net/Espn.kt`. Every failure shows a plain-English message with an error code; never crash.
6. **No betting features, no gambling links, no NBA/ESPN logos.** Keep the "not affiliated" disclaimer.
7. **Plain English** in UI copy; explain basketball terms.
8. Keep `BUILD_LOG.md` updated (date, what changed, test results, known limits).

## League context (FIQ, 2026-27)
10 teams, ESPN H2H Most Categories 9-cat, $200 auction (Oct 11 2026 8 PM EDT), 12-man roster (PG,SG,SF,PF,C,G,F,UTIL×2 +3 BE +2 IR), **25 games-played cap per weekly matchup** (checked at start of each day), 7 adds/week, trade deadline Feb 19 2027. Strategy: punt-FT% "bigs" build, stars & scrubs, cap-overflow trick.

## Backlog ideas
- ESPN league sync (read-only) via league ID + cookies stored on-device only
- Opponent weekly category projection
- Home-screen widget: "start N today"
- Waiver suggestions from ESPN free-agent feed
