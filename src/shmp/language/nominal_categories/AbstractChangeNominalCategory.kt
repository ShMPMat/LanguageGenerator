package shmp.language.nominal_categories

import shmp.language.*
import shmp.language.nominal_categories.change.CategoryApplicator

abstract class AbstractChangeNominalCategory(
    final override val categories: Set<NominalCategoryEnum>,
    final override val possibleCategories: Set<NominalCategoryEnum>,
    val categoryApplicators: Map<SpeechPart, Map<NominalCategoryEnum, CategoryApplicator>>,
    private val outType: String,
    private val noCategoriesOut: String
) : NominalCategory {
    init {
        if (!possibleCategories.containsAll(categories))
            throw LanguageException(
                "Nominal Category $outType was initialized with values which do not represent this category: "
                    + categories.filter { !possibleCategories.contains(it) } .joinToString()
            )
    }

    override fun apply(clause: Clause, wordPosition: Int, nominalCategoryEnum: NominalCategoryEnum): Clause {
        val word = clause[wordPosition]
        return if (categoryApplicators.containsKey(word.syntaxCore.speechPart))
            categoryApplicators[word.syntaxCore.speechPart]?.get(nominalCategoryEnum)?.apply(clause, wordPosition)
                ?: throw LanguageException(
                    "Tried to change word \"$word\" for category $nominalCategoryEnum but it isn't defined in Language"
                )
        else Clause(listOf(word.copy()))
    }

    override fun toString(): String {
        return outType + ":\n" + if (categories.isEmpty()) noCategoriesOut
        else categoryApplicators.map { entry ->
            entry.key.toString() + ":\n" + entry.value.map {
                it.key.toString() + ": " + it.value
            }.joinToString("\n")
        }.joinToString("\n")
    }


}