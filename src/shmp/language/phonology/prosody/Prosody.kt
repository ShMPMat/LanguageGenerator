package shmp.language.phonology.prosody

interface Prosody{
    val mark: String
}

class Stress : Prosody {
    override val mark = "'"
}
