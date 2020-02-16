package shmp.language.nominal_categories.change

import shmp.language.NominalCategoryRealization
import shmp.language.Word

abstract class WordCategoryApplicator(
    val applicatorWord: Word,
    type: NominalCategoryRealization
) : AbstractCategoryApplicator(type)