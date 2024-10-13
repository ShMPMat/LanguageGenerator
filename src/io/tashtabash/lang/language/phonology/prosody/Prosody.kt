package io.tashtabash.lang.language.phonology.prosody


interface Prosody{
    val mark: String
}

object Stress : Prosody {
    override val mark = "\u0301"
}
