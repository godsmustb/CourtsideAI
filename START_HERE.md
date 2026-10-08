# START HERE: Courtside AI 🏀

## What this is
Your fantasy-basketball sidekick for the FIQ league (and a public "2026-27 NBA Tracker" app you can promote later).
Tabs: **Draft** (Sunday's auction), **Today** (live scores), **Injuries**, **My Team** (25-game planner), **Playbook**.

## Get it on your phone (one time, ~3 minutes)
1. Obtainium is already on your phone (from GuitarNoodle).
2. In Obtainium tap **Add App**. URL: `https://github.com/godsmustb/CourtsideAI`. Tap **Add**, then **Install**.
   - The repo is **public**, so no GitHub token is needed.
3. Open Courtside AI and allow notifications (for injury alerts).

## Updates
Every `git push` to `main` makes GitHub test, build and sign the app and publish a Release. Obtainium sees it and offers the update.

## Signing key (important)
All builds share one signing key. It lives in GitHub secrets (`CS_KEYSTORE_B64`, `CS_KEYSTORE_PASSWORD`) and a backup copy is on your laptop at
`C:\Users\scnun\Projects\ESPN Fantasy BasketBall 2026\_PRIVATE_signing_key\`. **Back that folder up and never share it.**
If it's lost, future updates can't install over the app (you'd uninstall and reinstall, losing draft data).

## Draft night (Sun Oct 11, 8 PM EDT)
1. Open **Draft** → 🎯 Targets. Each row shows **MAX $**: never bid past it.
2. Every time a player sells, tap him → enter the price → **I bought** or **Other team**.
3. The top card keeps Budget left, Max bid, Open spots and Room heat correct.
4. Your nomination turn early on: pick from 🗑 Nominate.

## Every day in season (5 minutes)
1. **Injuries → My players**: anyone Out? Move him to IR in ESPN.
2. **My Team**: enter ESPN's "Games Played" number; follow "start N" for today. ⭐ ALL-IN = start everyone who plays.

## Where things are
- `app/src/main/assets/players.json`: player values, tiers, max bids (edit data, not code)
- `app/src/main/assets/playbook.json`: Playbook text
- `BUILD_LOG.md`: what changed, test results, known limits
- Research and strategy docs: `C:\Users\scnun\Projects\ESPN Fantasy BasketBall 2026\`
