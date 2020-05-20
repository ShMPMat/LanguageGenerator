package shmp.language

import shmp.containers.PhonemeContainer
import shmp.language.categories.ChangeParadigm
import shmp.language.phonology.RestrictionsParadigm
import shmp.language.phonology.prosody.StressType

class Language(
    private val words: List<Word>,
    private val phonemeContainer: PhonemeContainer,
    private val stressType: StressType,
    private val sovOrder: SovOrder,
    private val numeralSystemBase: NumeralSystemBase,
    private val restrictionsParadigm: RestrictionsParadigm,
    private val changeParadigm: ChangeParadigm
) {
    override fun toString(): String {
        return """phonemes:
                 |${phonemeContainer}
                 |Syllable structure: ${words[0].syllableTemplate}
                 |Stress patern: $stressType
                 |Numeral system base: $numeralSystemBase
                 |words:
                 |${words.joinToString { it.toString() + " - " + it.syntaxCore.word }}
                 |${words.joinToString { "${changeParadigm.apply(it)} - ${it.syntaxCore.word}" }}
                 |SOV order: $sovOrder
                 |${changeParadigm}
                 |""".trimMargin()
    }
}