package shmp.language.categories.change

import shmp.language.CategoryRealization
import shmp.language.Word

abstract class WordCategoryApplicator(
    val applicatorWord: Word,
    type: CategoryRealization
) : AbstractCategoryApplicator(type)