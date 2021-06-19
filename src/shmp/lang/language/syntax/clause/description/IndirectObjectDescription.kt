package shmp.lang.language.syntax.clause.description

import shmp.lang.language.Language
import shmp.lang.language.category.CaseValue
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.clause.realization.CaseAdjunctClause
import shmp.lang.language.syntax.context.Context
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
    Location(CaseValue.Locative, SyntaxRelation.Location);

    override fun toString() = name
}
