package io.tashtabash.lang.language.syntax.features


data class QuestionMarkerPresence(val questionMarker: QuestionMarker?) {
    override fun toString() = questionMarker?.toString() ?: "none"
}


object QuestionMarker: SyntaxFeature {
    override val probability = 1.0

    override fun toString() = "QuestionMarker"
}
