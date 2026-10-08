@file:OptIn(ExperimentalMaterial3Api::class)

package com.nunna.courtside.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nunna.courtside.data.Pick
import com.nunna.courtside.data.Player
import com.nunna.courtside.engine.AuctionMath
import com.nunna.courtside.engine.Names

private enum class DraftFilter(val label: String) {
    TARGETS("🎯 Targets"), DRAIN("🗑 Nominate"), ALL("All"), MINE("✅ Mine"), SOLD("Sold")
}

private val tierOrder = listOf("ANCHOR", "STAR2", "CORE", "VALUE", "ENDGAME")

@Composable
fun DraftScreen(state: AppState) {
    val data = state.data
    var filter by remember { mutableStateOf(DraftFilter.TARGETS) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Player?>(null) }
    var showPlan by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }

    val sold = data.picks.associateBy { Names.key(it.player) }
    val remaining = AuctionMath.remaining(data.budget, data.picks)
    val maxBid = AuctionMath.maxBid(data.budget, data.rosterSize, data.picks)
    val open = AuctionMath.openSlots(data.rosterSize, data.picks)
    val heat = AuctionMath.inflation(state.players, data.picks, data.leagueTeams, data.budget, data.rosterSize)
    val mine = data.picks.filter { it.mine }
    val guards = mine.count { p -> state.player(p.player)?.pos?.let { "PG" in it || "SG" in it } == true }

    val list = state.players.filter { p ->
        val k = Names.key(p.name)
        val matches = query.isBlank() || p.name.contains(query, ignoreCase = true) || p.team.contains(query, ignoreCase = true)
        matches && when (filter) {
            DraftFilter.TARGETS -> p.tier in tierOrder && k !in sold
            DraftFilter.DRAIN -> p.tier == "DRAIN" && k !in sold
            DraftFilter.ALL -> true
            DraftFilter.MINE -> sold[k]?.mine == true
            DraftFilter.SOLD -> k in sold
        }
    }.let { l ->
        when (filter) {
            DraftFilter.TARGETS -> l.sortedWith(compareBy<Player> { tierOrder.indexOf(it.tier) }.thenByDescending { it.maxBid ?: 0 })
            DraftFilter.DRAIN -> l.sortedByDescending { it.value }
            DraftFilter.SOLD -> {
                val order = data.picks.map { Names.key(it.player) }
                l.sortedByDescending { order.indexOf(Names.key(it.name)) }
            }
            else -> l.sortedBy { it.rank }
        }
    }

    LazyColumn(Modifier.fillMaxWidth()) {
        item {
            SectionCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Stat("Budget left", "$$remaining", Modifier.weight(1f), Orange)
                    Stat("Max bid", "$$maxBid", Modifier.weight(1f))
                    Stat("Open spots", "$open", Modifier.weight(1f))
                    Stat(
                        "Room heat", "×" + String.format("%.2f", heat), Modifier.weight(1f),
                        if (heat > 1.08) Bad else if (heat < 0.95) Good else Muted,
                    )
                }
                Text(
                    "Guards owned: $guards/3 · " + when {
                        heat > 1.08 -> "Room is HOT: prices will run high. Be patient; bargains come later."
                        heat < 0.95 -> "Room is COLD: money is piling up. Buy good players now."
                        else -> "Prices are normal."
                    },
                    fontSize = 12.sp, color = Muted,
                )
            }
        }
        item {
            SectionCard {
                Row(Modifier.fillMaxWidth().clickable { showPlan = !showPlan }, verticalAlignment = Alignment.CenterVertically) {
                    Text("📋 Tonight's plan", fontWeight = FontWeight.Bold, color = Orange, modifier = Modifier.weight(1f))
                    Text(if (showPlan) "Hide" else "Show", color = Muted)
                }
                if (showPlan) {
                    Bullet("Build: big men. Win FG%, REB, BLK, TO + PTS or STL. Ignore FT%.")
                    Bullet("Plan A: Wembanyama ~$78 + Giannis ~$52 + one core big ~$30, then values and $1 guards.")
                    Bullet("Plan B (Wemby > $80): Jokic ~$80 + Jalen Johnson + Amen Thompson + Duren + Clingan.")
                    Bullet("Early on, nominate players from the 🗑 Nominate list so rivals spend.")
                    Bullet("Never go past MAX. Lost a bidding war? Move that money to the next player in the same tier.")
                    Bullet("Get 3+ guard-eligible players. Finish with $0-3 left; leftover money is worthless.")
                }
            }
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
                DraftFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.label) },
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search player or team") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        if (list.isEmpty()) {
            item {
                Text(
                    if (filter == DraftFilter.MINE) "No players yet. Tap a player and choose \"I bought\"." else "Nothing here.",
                    color = Muted, modifier = Modifier.padding(16.dp),
                )
            }
        }
        items(list, key = { it.name }) { p ->
            PlayerRow(p, sold[Names.key(p.name)], heat) { selected = p }
        }
        item {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { confirmReset = true }, modifier = Modifier.padding(horizontal = 12.dp)) {
                Text("Reset draft (start over)", color = Bad)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    selected?.let { p ->
        PlayerDialog(
            player = p,
            pick = sold[Names.key(p.name)],
            maxBid = maxBid,
            onDismiss = { selected = null },
            onSold = { price, isMine ->
                state.update { d -> d.copy(picks = d.picks.filterNot { Names.key(it.player) == Names.key(p.name) } + Pick(p.name, price, isMine)) }
                selected = null
            },
            onUndo = {
                state.update { d -> d.copy(picks = d.picks.filterNot { Names.key(it.player) == Names.key(p.name) }) }
                selected = null
            },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Start the draft over?") },
            text = { Text("This clears every sale you've entered. Players added later on My Team stay.") },
            confirmButton = {
                TextButton(onClick = { state.update { it.copy(picks = emptyList()) }; confirmReset = false }) {
                    Text("Clear draft", color = Bad)
                }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun PlayerRow(p: Player, pick: Pick?, heat: Double, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        p.name + if (p.rookie) " (R)" else "",
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (pick != null && !pick.mine) TextDecoration.LineThrough else null,
                    )
                    if (p.tier != null) {
                        Spacer(Modifier.width(6.dp))
                        Tag(tierLabel(p.tier), tierColor(p.tier))
                    }
                }
                Text(
                    "${p.team} · ${p.pos}" + (p.espnAdp?.let { " · ESPN ADP $it" } ?: "") + (if (p.rank < 999) " · #${p.rank}" else ""),
                    fontSize = 12.sp, color = Muted,
                )
                p.role?.let { Text(it, fontSize = 12.sp, color = Muted, maxLines = 1) }
            }
            Column(horizontalAlignment = Alignment.End) {
                when {
                    pick != null -> Text(
                        (if (pick.mine) "MINE $" else "SOLD $") + pick.price,
                        color = if (pick.mine) Good else Muted, fontWeight = FontWeight.Bold,
                    )
                    p.tier == "DRAIN" -> Text("Don't buy", color = Bad, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    p.maxBid != null -> Text("MAX $${p.maxBid}", color = Orange, fontWeight = FontWeight.Bold)
                    else -> {}
                }
                Text("value $${p.value} · room ~$${AuctionMath.roomPrice(p.value, heat)}", fontSize = 11.sp, color = Muted)
            }
        }
    }
    HorizontalDivider(Modifier.padding(horizontal = 12.dp), color = androidx.compose.ui.graphics.Color(0xFF262A33))
}

@Composable
private fun PlayerDialog(
    player: Player,
    pick: Pick?,
    maxBid: Int,
    onDismiss: () -> Unit,
    onSold: (price: Int, mine: Boolean) -> Unit,
    onUndo: () -> Unit,
) {
    var priceText by remember { mutableStateOf((pick?.price ?: player.maxBid?.takeIf { it > 0 } ?: player.value).toString()) }
    val price = priceText.toIntOrNull()
    val overMax = price != null && price > maxBid && pick?.mine != true
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(player.name) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("${player.team} · ${player.pos}" + (player.espnAdp?.let { " · ESPN ADP $it" } ?: ""), color = Muted)
                player.maxBid?.let {
                    Text(
                        if (it == 0) "On our DON'T-BUY list. Nominate him early so others spend money."
                        else "Our max: $$it · fair value $${player.value}",
                        color = Orange, fontWeight = FontWeight.Bold,
                    )
                }
                player.role?.let { Text("Role: $it", fontSize = 13.sp) }
                player.why?.let { Text("Why: $it", fontSize = 13.sp) }
                player.caution?.let { Text("Watch out: $it", fontSize = 13.sp, color = Warn) }
                if (player.strengths.isNotBlank()) Text("Good at: ${player.strengths}", fontSize = 13.sp)
                if (player.weaknesses.isNotBlank()) Text("Weak at: ${player.weaknesses}", fontSize = 13.sp)
                if (player.risk.isNotBlank()) Text("Risk: ${player.risk}", fontSize = 13.sp, color = Muted)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { v -> priceText = v.filter { it.isDigit() }.take(3) },
                    label = { Text("Sold for $") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                if (overMax) Text("That's above your max bid right now ($$maxBid). ESPN won't allow it.", color = Bad, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row {
                    Button(
                        onClick = { price?.let { onSold(it, true) } },
                        enabled = price != null && price >= 1 && !overMax,
                    ) { Text("I bought") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { price?.let { onSold(it, false) } },
                        enabled = price != null && price >= 1,
                    ) { Text("Other team") }
                }
                if (pick != null) {
                    TextButton(onClick = onUndo) { Text("Undo this sale", color = Bad) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
