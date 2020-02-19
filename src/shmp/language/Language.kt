package shmp.language

import shmp.containers.PhonemeContainer
import shmp.language.nominal_categories.NominalCategory

class Language(
    val words: List<Word>,
    val phonemeContainer: PhonemeContainer,
    val stress: Stress,
    val SovOrder: SovOrder,
    val changeParadigm: ChangeParadigm
) {
    override fun toString(): String {
        return """phonemes:
                 |${phonemeContainer}
                 |Stress patern: $stress
                 |words:
                 |$words
                 |SOV order: $SovOrder
                 |${changeParadigm.nominalCategories.joinToString("\n")}
                 |""".trimMargin()
    }
}