package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.category.CaseValue
import io.tashtabash.lang.language.syntax.SyntaxRelation


interface ObjectType {
    val relation: SyntaxRelation
}

enum class MainObjectType(override val relation: SyntaxRelation): ObjectType {
    Argument(SyntaxRelation.Argument),
    Agent(SyntaxRelation.Agent),
    Patient(SyntaxRelation.Patient);
}

enum class AdjunctType(val caseValue: CaseValue, override val relation: SyntaxRelation): ObjectType {
    Instrument(CaseValue.Instrumental, SyntaxRelation.Instrument),
    Addressee(CaseValue.Dative, SyntaxRelation.Addressee),
    Location(CaseValue.Locative, SyntaxRelation.Location),
    Benefactor(CaseValue.Benefactive, SyntaxRelation.Benefactor);

    override fun toString() = name
}
