package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.SubstitutingOrder
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.clause.translation.VerbSentenceType
import io.tashtabash.lang.language.syntax.features.QuestionMarker
import io.tashtabash.lang.language.syntax.features.WordSyntaxRole
import kotlin.random.Random


class ObliquePredicatePossessionClause(
    val verbClause: IntransitiveVerbClause,
    val type: VerbSentenceType
) : SentenceClause() {
    override fun toNode(language: Language, random: Random) =
        verbClause.toNode(language, random).apply {
            if (type == VerbSentenceType.QuestionVerbClause)
                if (language.changeParadigm.syntaxParadigm.questionMarkerPresence.questionMarker != null)
                    setRelationChild(
                        SyntaxRelation.QuestionMarker,
                        language.lexis.getQuestionMarkerWord(QuestionMarker)
                            .copy(syntaxRole = WordSyntaxRole.QuestionMarker)
                            .toNode(SyntaxRelation.QuestionMarker)
                    )
            arranger = RelationArranger(
                SubstitutingOrder(
                    language.changeParadigm.wordOrder.sovOrder.getValue(type)
                ) { lst ->
                    lst.map { r ->
                        when (r) {
                            Patient -> Argument
                            Agent -> PossessorAdjunct
                            else -> r
                        }
                    }
                }
            )
        }
}
