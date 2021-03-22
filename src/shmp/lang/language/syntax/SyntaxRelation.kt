package shmp.lang.language.syntax

enum class SyntaxRelation {
    Subject,
    Object,
    SubjectCompliment,
    Verb,

    CopulaParticle,
    QuestionMarker,

    Nominal,
    Definition
}

typealias SyntaxRelations = List<SyntaxRelation>