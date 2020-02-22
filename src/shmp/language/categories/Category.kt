package shmp.language.categories

import shmp.language.CategoryEnum
import shmp.language.SpeechPart

interface Category {
    val categories: List<CategoryEnum>
    val possibleCategories: Set<CategoryEnum>
    val affectedSpeechParts: Set<SpeechPart>
}