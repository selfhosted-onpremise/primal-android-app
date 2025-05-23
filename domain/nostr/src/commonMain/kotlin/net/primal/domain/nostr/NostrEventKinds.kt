package net.primal.domain.nostr

fun NostrEventKind.isPrimalEventKind() = value in NostrEventKindRange.PrimalEvents

fun NostrEventKind.isNotPrimalEventKind() = !isPrimalEventKind()

fun NostrEventKind.isUnknown() = this == NostrEventKind.Unknown

fun NostrEventKind.isNotUnknown() = !isUnknown()
