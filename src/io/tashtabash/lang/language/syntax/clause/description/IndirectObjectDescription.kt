package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.CaseValue
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.realization.CaseAdjunctClause
import io.tashtabash.lang.language.syntax.context.Context
import kotlin.random.Random


class IndirectObjectDescription(val nominal: NominalDescription, val type: IndirectObjectType): ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random) = CaseAdjunctClause(
        nominal.toClause(language, context, random),
        type.caseValue,
        type.relation
    )
}


enum class IndirectObjectType(val caseValue: CaseValue, val relation: SyntaxRelation) {
    Instrument(CaseValue.Instrumental, SyntaxRelation.Instrument),
    Addressee(CaseValue.Dative, SyntaxRelation.Addressee),
    Location(CaseValue.Locative, SyntaxRelation.Location),
    Benefactor(CaseValue.Benefactive, SyntaxRelation.Benefactor);

    override fun toString() = name
}
