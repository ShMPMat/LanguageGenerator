package shmp.language.categories.realization_type

import shmp.language.CategoryRealization
import shmp.language.Word

abstract class WordCategoryApplicator(
    val applicatorWord: Word,
    type: CategoryRealization
) : AbstractCategoryApplicator(type)