package io.tashtabash.lang.language.syntax

enum class SyntaxRelation(val shortName: String) {
    Agent("AG"),
    Patient("PAT"),
    Argument("ARG"),
    SubjectCompliment("SC"),
    Verb("VRB"),

    PossessorAdjunct("POS_ADJ"),
    Instrument("INSTR"),
    Addressee("ADRSEE"),
    Location("LOCATION"),

    CopulaParticle("COP"),
    QuestionMarker("QM"),

    Nominal("NOMINAL"),
    Definition("DEFINITION"),
    AdNumeral("NUM"),
    SumNumeral("SUM.NUM"),
    MulNumeral("MUL.NUM"),

    Possessed("POSSESED"),
    Possessor("POSSESOR");
}

typealias SyntaxRelations = List<SyntaxRelation>