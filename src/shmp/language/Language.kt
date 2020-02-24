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
                 |Stress patern: $stress
                 |words:
                 |${words.map {it.toString() + " - " + it.syntaxCore.word } .joinToString()}
                 |${words.map { changeParadigm.apply(it).toString() + " - " + it.syntaxCore.word }.joinToString()}
                 |SOV order: $sovOrder
                 |${changeParadigm}
                 |""".trimMargin()
    }
}