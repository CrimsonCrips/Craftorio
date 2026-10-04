# Craftorio Testing Checklist

> **Test most, if not all, of these in multiplayer with 2 clients** (a dedicated server plus `Client` and `Client2`, e.g. a **Fresh Launch (…)** run configuration). Many features behave differently per player, are shared in universal worlds, or only break when a second player is online.

Tick a box by changing `- [ ]` to `- [x]`.

## Config and world

- [x] Craftorio config applies
- [x] Craftorio world generation works

## Shop

- [x] Shop modes work
- [x] Shop purchasing works

## Points

- [x] Advancement point gain works

## Land (border based)

- [x] Expand border and expand extra
- [x] Border vision on XaeroMap

## Land (chunk based)

- [x] Chunk claim item purchase works
- [x] Chunk claim item works
- [x] Chunk vision on XaeroMap
- [x] Opposing colors in chunk border colors and Xaero map colors
- [x] Player cannot own another player's chunk when No Borders is false

## Values and search

- [x] Value item checker works
- [x] Search tip works

## Contracts

- [x] Contract stuff works
- [x] Item contract fulfilling works
- [x] Build contract fulfilling and buttons work

## Effects and runes

- [x] Chronosphere works and chronokilling works
- [x] Rune crafting works
- [x] Rune works
- [x] Active effects work

## Hub

- [x] Statistics work
- [x] Loan shark works
- [x] Skill tree works

## Machines

- [x] Machine blocks work
- [x] Double or nothing works

## Rebirth

- [x] Rebirthing with consent and without it works, and skipping

## Sacrifice

- [x] Threshold to activate animation with sacrifice shard appearance
- [x] Sacrifice works
- [x] Sacrifice animation related stuff works
- [x] Player sacrifice only affects the player's area and not other areas for No Borders and chunk based
- [x] Player sacrifice only affects the player's area and not other areas for No Borders and non chunk based
- [x] Joining while a reset is happening sends the player to the Haven, and they are handled correctly once it completes

## Non-universal separation

- [x] Advancement is separated in non universal
- [x] Players are split off in non universal in terms of where they are positioned, ownership like contracts, effects, points etc

## Dev tools

- [x] Dev tools work
