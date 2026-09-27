# Craftorio Testing Checklist

> **Test most, if not all, of these in multiplayer with 2 clients** (a dedicated server plus `Client` and `Client2`, e.g. a **Fresh Launch (…)** run configuration). Many features behave differently per player, are shared in universal worlds, or only break when a second player is online.

Tick a box by changing `- [ ]` to `- [x]`.

## Config and world

- [ ] Craftorio config applies
- [ ] Craftorio world generation works

## Shop

- [ ] Shop modes work
- [ ] Shop purchasing works

## Points

- [ ] Advancement point gain works

## Land (border based)

- [ ] Expand border and expand extra
- [ ] Border vision on XaeroMap

## Land (chunk based)

- [ ] Chunk claim item purchase works
- [ ] Chunk claim item works
- [ ] Chunk vision on XaeroMap
- [ ] Opposing colors in chunk border colors and Xaero map colors
- [ ] Player cannot own another player's chunk when No Borders is false

## Values and search

- [ ] Value item checker works
- [ ] Search tip works

## Contracts

- [ ] Contract stuff works
- [ ] Item contract fulfilling works
- [ ] Build contract fulfilling and buttons work

## Effects and runes

- [ ] Chronosphere works and chronokilling works
- [ ] Rune crafting works
- [ ] Rune works
- [ ] Active effects work

## Hub

- [ ] Statistics work
- [ ] Loan shark works
- [ ] Skill tree works

## Machines

- [ ] Machine blocks work
- [ ] Double or nothing works

## Rebirth

- [ ] Rebirthing with consent and without it works, and skipping

## Sacrifice

- [ ] Threshold to activate animation with sacrifice shard appearance
- [ ] Sacrifice works
- [ ] Sacrifice animation related stuff works
- [ ] Player sacrifice only affects the player's area and not other areas for No Borders and chunk based
- [ ] Player sacrifice only affects the player's area and not other areas for No Borders and non chunk based

## Non-universal separation

- [ ] Advancement is separated in non universal
- [ ] Players are split off in non universal in terms of where they are positioned, ownership like contracts, effects, points etc

## Dev tools

- [ ] Dev tools work
