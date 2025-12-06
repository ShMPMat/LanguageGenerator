package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.SubstitutingOrder
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.clause.syntax.VerbSentenceType
import kotlin.random.Random


class ObliquePredicatePossessionClause(
    val verbClause: IntransitiveVerbClause,
    val type: VerbSentenceType
) : SentenceClause() {
    override fun toNode(language: Language, random: Random) =
        verbClause.toNode(language, random).apply {
            if (type == VerbSentenceType.QuestionVerbClause)
                addQuestionMarker(language)
            arranger = RelationArranger(
                SubstitutingOrder(
                    language.changeParadigm.wordOrder.sovOrder.getValue(type),
                    mapOf(Patient to Argument)
                )
            )
        }
}
