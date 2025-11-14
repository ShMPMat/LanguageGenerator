package io.tashtabash.lang.language.syntax


enum class SyntaxRelation(val shortName: String) {
    Agent("AG"), // Of trans verb
    Patient("PAT"),
    Argument("ARG"), // Of intrans verb
    SubjectCompliment("SC"),
    Verb("VRB"),

    PossessorAdjunct("POS_ADJ"),
    Instrument("INSTR"),
    Addressee("ADRSEE"),
    Location("LOCATION"),
    Benefactor("BENEFACTOR"),

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
