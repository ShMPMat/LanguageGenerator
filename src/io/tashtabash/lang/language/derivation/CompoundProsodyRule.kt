package io.tashtabash.lang.language.derivation

import io.tashtabash.lang.language.phonology.prosody.Prosody
import io.tashtabash.lang.language.phonology.prosody.Stress


interface CompoundProsodyRule {
    fun changeProsody(wordInd: Int, prosodies: List<Prosody>): List<Prosody>

    val defaultToString: String
}

abstract class AbstractCompoundProsodyRule : CompoundProsodyRule {
    override fun toString() = defaultToString
}


object PassingProsodyRule : AbstractCompoundProsodyRule() {
    override fun changeProsody(wordInd: Int, prosodies: List<Prosody>) = prosodies

    override val defaultToString = "Words are concatenated without change"
}


class StressOnWordRule(private val stressedWordIndex: Int) : AbstractCompoundProsodyRule() {
    override fun changeProsody(wordInd: Int, prosodies: List<Prosody>) =
        if (wordInd != stressedWordIndex)
            prosodies.filter { it !is Stress }
        else prosodies

    override val defaultToString = "Stress is put on ${stressedWordIndex + 1}th word"
}
