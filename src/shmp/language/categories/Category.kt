package shmp.language.categories

import shmp.language.CategoryValue
import shmp.language.SpeechPart

interface Category {
    val values: List<CategoryValue>
    val possibleValues: Set<CategoryValue>
    val affectedSpeechParts: Set<SpeechPart>
    val outType: String
}