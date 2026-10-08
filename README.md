# 🏀 Courtside AI: 2026-27 NBA Tracker & Fantasy Assistant

An Android app for fantasy basketball managers who **don't follow the NBA closely** and still want to win.

| Tab | What it does |
|---|---|
| 💰 **Draft** | Auction (salary-cap) draft assistant: target list with max bids, live budget / max-bid / open-spot tracker, "room heat" (are prices running hot or cold?), nominate-to-drain list |
| 📅 **Today** | Live NBA scores, tip-off times, games per day this week (🔥 = busiest night), which of your players are playing |
| 🚑 **Injuries** | Live NBA injury report, filtered to your players or your draft targets |
| 👥 **My Team** | Your roster, games left this week, injury badges, and a **weekly games-limit planner** that picks your all-in night for ESPN's "games played" cap |
| 📘 **Playbook** | Plain-English strategy for 9-category head-to-head leagues |
| 🔔 **Alerts** | Background check about once an hour; your phone pings when one of *your* players' injury status changes |

## Install & auto-update (Obtainium)
1. Install [Obtainium](https://github.com/ImranR98/Obtainium/releases) on your Android phone.
2. In Obtainium: **Add App** → paste `https://github.com/godsmustb/CourtsideAI` → **Add** → **Install**.
3. Every new version shows up in Obtainium automatically.

Or download the latest `.apk` from [Releases](https://github.com/godsmustb/CourtsideAI/releases).

## How updates are made
Every push to `main` runs the unit tests, builds a signed APK in GitHub Actions and publishes it as a Release (`v0.1.<build>`).

## Data & disclaimer
Live scores and injuries come from ESPN's public site feed (unofficial; may change). Player values and strategy are research estimates for 2026-27 (10-team, $200, 9-cat H2H most-categories leagues). Not affiliated with, endorsed by or sponsored by the NBA, ESPN or any team. For entertainment and information only. No betting features.

Built with Kotlin + Jetpack Compose. Made by Nunna AI Consulting.
