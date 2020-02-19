package shmp.language

import shmp.language.nominal_categories.NominalCategory
import shmp.language.nominal_categories.change.CategoryApplicator

class ChangeParadigm(
    val nominalCategories: List<NominalCategory>,
    val speechPartChangeParadigms: Map<SpeechPart, SpeechPartChangeParadigm>
) {
    fun apply(word: Word, nominalCategoryEnums: Set<NominalCategoryEnum>): Clause {
        return speechPartChangeParadigms[word.syntaxCore.speechPart]?.apply(word, nominalCategoryEnums)
            ?: throw LanguageException("No SpeechPartChangeParadigm for ${word.syntaxCore.speechPart}")
    }

    override fun toString(): String {
        return nominalCategories.joinToString("\n") + "\n\n" +
                speechPartChangeParadigms
                    .map { it.value }
                    .filter { it.hasChanges() }
                    .joinToString("\n")
    }
}

class SpeechPartChangeParadigm(
    val speechPart: SpeechPart,
    val nominalCategories: List<NominalCategory>,
    val applicators: Map<NominalCategory, Map<NominalCategoryEnum, CategoryApplicator>>
) {
    fun apply(word: Word, nominalCategoryEnums: Set<NominalCategoryEnum>): Clause {
        if (word.syntaxCore.speechPart != speechPart)
            throw LanguageException(
                "SpeechPartChangeParadigm for $speechPart has been given ${word.syntaxCore.speechPart}"
            )
        var currentClause = Clause(listOf(word))
        var currentWord = word
        var wordPosition = 0
        for (nominalCategory in nominalCategories) {
            val category = getCategory(nominalCategoryEnums, nominalCategory) ?: continue
            val newClause = useCategoryApplicator(currentClause, wordPosition, nominalCategory, category)
            if (currentClause.size != newClause.size) {
                for (i in wordPosition until  newClause.size) {
                    if (currentWord == newClause[i]) {
                        wordPosition = i
                        break
                    }
                }
            } else
                currentWord = newClause[wordPosition]
            currentClause = newClause
        }
        return currentClause


    }

    private fun useCategoryApplicator(
        clause: Clause,
        wordPosition: Int,
        nominalCategory: NominalCategory,
        nominalCategoryEnum: NominalCategoryEnum
    ): Clause {
        val word = clause[wordPosition]
        return if (applicators[nominalCategory]?.containsKey(nominalCategoryEnum) == true)
            applicators[nominalCategory]?.get(nominalCategoryEnum)?.apply(clause, wordPosition)
                ?: throw LanguageException(
                    "Tried to change word \"$word\" for category $nominalCategoryEnum but it isn't defined in Language"
                )
        else Clause(listOf(word.copy()))
    }

    private fun getCategory(nominalCategoryEnums: Set<NominalCategoryEnum>, nominalCategory: NominalCategory): NominalCategoryEnum? {
        val categories = nominalCategoryEnums.filter { nominalCategory.categories.contains(it) }
        if (categories.size > 1) {
            throw LanguageException(
                "ChangeParadigm have been given more than one NominalCategory values: ${categories.joinToString()}"
            )
        }
        return if (categories.isEmpty()) null else categories[0]
    }

    override fun toString(): String {
        return "$speechPart changes on: \n${applicators.filter { it.key.categories.isNotEmpty() }.map { 
            it.key.toString() + ":\n" + it.value.map { it.key.toString() + ": " + it.value } .joinToString("\n")
        }.joinToString("\n")}"
    }

    fun hasChanges(): Boolean = applicators.isNotEmpty()
}