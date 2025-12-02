package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.category.CaseValue
import io.tashtabash.lang.language.syntax.SyntaxRelation


interface ObjectType {
    val relation: SyntaxRelation
}

// Relation represents the default option (a bit eurocentric), which can be remapped by verb governance
enum class MainObjectType(override val relation: SyntaxRelation): ObjectType {
    Argument(SyntaxRelation.Argument),
    Agent(SyntaxRelation.Agent),
    Patient(SyntaxRelation.Patient),
    Experiencer(SyntaxRelation.Agent),
    Stimulus(SyntaxRelation.Patient);

    companion object {
        val syntaxRelations = entries.map { it.relation }
    }
}

enum class AdjunctType(val caseValue: CaseValue, override val relation: SyntaxRelation): ObjectType {
    Instrument(CaseValue.Instrumental, SyntaxRelation.Instrument),
    Addressee(CaseValue.Dative, SyntaxRelation.Addressee),
    Location(CaseValue.Locative, SyntaxRelation.Location),
    Benefactor(CaseValue.Benefactive, SyntaxRelation.Benefactor);

    override fun toString() = name
}
