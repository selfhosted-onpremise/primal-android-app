package net.primal.domain.nostr.cryptography.utils

import fr.acinq.secp256k1.Hex
import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.toByteArray

fun String.assureValidNsec() = if (startsWith("nsec")) this else this.hexToNsecHrp()

fun String.hexToNoteHrp() =
    Bech32.encodeBytes(
        hrp = "note",
        data = Hex.decode(this),
        encoding = Bech32.Encoding.Bech32,
    )

fun String.hexToNpubHrp() =
    Bech32.encodeBytes(
        hrp = "npub",
        data = Hex.decode(this),
        encoding = Bech32.Encoding.Bech32,
    )

fun String.hexToNsecHrp() =
    Bech32.encodeBytes(
        hrp = "nsec",
        data = Hex.decode(this),
        encoding = Bech32.Encoding.Bech32,
    )

fun String.urlToLnUrlHrp() =
    Bech32.encodeBytes(
        hrp = "lnurl",
        data = this.toByteArray(),
        encoding = Bech32.Encoding.Bech32,
    )

fun String.bech32ToHexOrThrow() = Bech32.decodeBytes(bech32 = this).second.toHex()

fun ByteArray.toNpub() = Bech32.encodeBytes(hrp = "npub", this, Bech32.Encoding.Bech32)

@OptIn(ExperimentalStdlibApi::class)
fun ByteArray.toHex() = Hex.encode(this)

@Throws(IllegalArgumentException::class)
fun String.bechToBytesOrThrow(hrp: String? = null): ByteArray {
    val decodedForm = Bech32.decodeBytes(this)
    hrp?.also { require(it == decodedForm.first) }
    return decodedForm.second
}

fun String.extractKeyPairFromPrivateKeyOrThrow(): Pair<String, String> {
    return try {
        val nsec = this.assureValidNsec()
        val decoded = Bech32.decodeBytes(nsec)
        val pubkey = CryptoUtils.publicKeyCreate(decoded.second)
        nsec to pubkey.toNpub()
    } catch (error: IllegalArgumentException) {
        Napier.w(error) { error.message ?: "" }
        throw InvalidNostrPrivateKeyException()
    }
}

class InvalidNostrPrivateKeyException : RuntimeException()
