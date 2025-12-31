package io.tashtabash.lang.language.syntax.features


data class QuestionMarkerPresence(val questionMarker: QuestionMarker?) {
    override fun toString() = questionMarker?.toString() ?: "none"
}


data object QuestionMarker: SyntaxFeature {
    override val probability = 1.0
}
