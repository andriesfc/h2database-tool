package h2databasetool.commons.security

enum class CipherMode(private val cipherCode: String, val description: String) {

    AES("AES", "AES-128"),
    XTEA("XTEA", "Extended Tiny Encryption Algorithm"),
    FOG("FOG", "Pseudo encryption. Only good for hiding data in text editor");

    fun cipherCode(): String = cipherCode

    companion object {
        fun of(cipherCode: String): CipherMode? =
            entries.find { cipherMode -> cipherMode.cipherCode.equals(cipherCode, ignoreCase = true) }
    }
}

