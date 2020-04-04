package shmp.language

import shmp.containers.PhonemeContainer
import shmp.language.categories.ChangeParadigm
import shmp.language.phonology.RestrictionsParadigm

class Language(
    private val words: List<Word>,
    private val phonemeContainer: PhonemeContainer,
    private val stress: Stress,
    private val sovOrder: SovOrder,
    private val restrictionsParadigm: RestrictionsParadigm,
    private val changeParadigm: ChangeParadigm
) {
    override fun toString(): String {//TODO first word output is a debug
        return """phonemes:
                 |${phonemeContainer}
                 |Syllable structure: ${words[0].syllableTemplate}
                 |Stress patern: $stress
                 |words:
                 |${words.joinToString { it.toString() + " - " + it.syntaxCore.word }}
                 |${words.joinToString { changeParadigm.apply(it).toString() + " - " + it.syntaxCore.word }}
                 |SOV order: $sovOrder
                 |${changeParadigm}
                 |""".trimMargin()
    }
}