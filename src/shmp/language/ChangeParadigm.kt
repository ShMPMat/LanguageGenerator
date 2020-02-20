package shmp.language

import shmp.language.categories.Category
import shmp.language.categories.change.CategoryApplicator

class ChangeParadigm(
    val categories: List<Category>,
    val speechPartChangeParadigms: Map<SpeechPart, SpeechPartChangeParadigm>
) {
    fun apply(word: Word, nominalCategoryEnums: Set<NominalCategoryEnum>): Clause {
        return speechPartChangeParadigms[word.syntaxCore.speechPart]?.apply(word, nominalCategoryEnums)
            ?: throw LanguageException("No SpeechPartChangeParadigm for ${word.syntaxCore.speechPart}")
    }

    fun getDefaultState(speechPart: SpeechPart): List<NominalCategoryEnum> {
        return speechPartChangeParadigms[speechPart]?.categories
            ?.filter { it.categories.isNotEmpty() }
            ?.map { it.categories[0] }
            ?: throw LanguageException("No SpeechPartChangeParadigm for $speechPart")
    }

    override fun toString(): String {
        return categories.joinToString("\n") + "\n\n" +
                speechPartChangeParadigms
                    .map { it.value }
                    .filter { it.hasChanges() }
                    .joinToString("\n")
    }
}

class SpeechPartChangeParadigm(
    val speechPart: SpeechPart,
    val categories: List<Category>,
    val applicators: Map<Category, Map<NominalCategoryEnum, CategoryApplicator>>
) {
    fun apply(word: Word, nominalCategoryEnums: Set<NominalCategoryEnum>): Clause {
        if (word.syntaxCore.speechPart != speechPart)
            throw LanguageException(
                "SpeechPartChangeParadigm for $speechPart has been given ${word.syntaxCore.speechPart}"
            )
        var currentClause = Clause(listOf(word))
        var currentWord = word
        var wordPosition = 0
        for (nominalCategory in categories) {
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
        category: Category,
        nominalCategoryEnum: NominalCategoryEnum
    ): Clause {
        val word = clause[wordPosition]
        return if (applicators[category]?.containsKey(nominalCategoryEnum) == true)
            applicators[category]?.get(nominalCategoryEnum)?.apply(clause, wordPosition)
                ?: throw LanguageException(
                    "Tried to change word \"$word\" for category $nominalCategoryEnum but it isn't defined in Language"
                )
        else Clause(listOf(word.copy()))
    }

    private fun getCategory(nominalCategoryEnums: Set<NominalCategoryEnum>, category: Category): NominalCategoryEnum? {
        val categories = nominalCategoryEnums.filter { category.categories.contains(it) }
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