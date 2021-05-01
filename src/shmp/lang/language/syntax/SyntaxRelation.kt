package shmp.lang.language.syntax

enum class SyntaxRelation {
    Agent,
    Patient,
    Argument,
    SubjectCompliment,
    Verb,

    PossessorAdjunct,

    CopulaParticle,
    QuestionMarker,

    Nominal,
    Definition,

    Possessed,
    Possessor
}

typealias SyntaxRelations = List<SyntaxRelation>