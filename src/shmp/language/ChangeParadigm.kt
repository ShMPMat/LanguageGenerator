package shmp.language

import shmp.language.nominal_categories.NominalCategory

class ChangeParadigm(val nominalCategories: List<NominalCategory>) {
    fun apply(word: Word, nominalCategoryEnums: Set<NominalCategoryEnum>): Clause {
        var currentClause = Clause(listOf(word))
        var currentWord = word
        var wordPosition = 0
        for (nominalCategory in nominalCategories) {
            val category = getCategory(nominalCategoryEnums, nominalCategory) ?: continue
            val newClause = nominalCategory.apply(currentClause, wordPosition, category)
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

    private fun getCategory(nominalCategoryEnums: Set<NominalCategoryEnum>, nominalCategory: NominalCategory): NominalCategoryEnum? {
        val categories = nominalCategoryEnums.filter { nominalCategory.categories.contains(it) }
        if (categories.size > 1) {
            throw LanguageException(
                "ChangeParadigm have been given more than one NominalCategory values: ${categories.joinToString()}"
            )
        }
        return if (categories.isEmpty()) null else categories[0]
    }
}