package shmp.language.nominal_categories

import shmp.language.*
import shmp.language.nominal_categories.change.CategoryApplicator

abstract class AbstractChangeNominalCategory(
    private val speechPart: SpeechPart,
    val categoryApplicators: Map<NominalCategoryEnum, CategoryApplicator>
) :
    NominalCategory {
    override fun apply(word: Word, nominalCategoryEnum: NominalCategoryEnum) =
        if (word.syntaxCore.speechPart == speechPart) categoryApplicators[nominalCategoryEnum]?.apply(word)
            ?: throw LanguageException(
                "Trying to change word $word with category $nominalCategoryEnum, " +
                        "but it is not defined in Language"
            )
        else throw LanguageException(
            "Tried to apply Nominal Category for $speechPart to the word $word which is ${word.syntaxCore.speechPart}."
        )
}