package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.Language
import shmp.lang.language.syntax.SubstitutingOrder
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.SyntaxRelation.*
import shmp.lang.language.syntax.arranger.PassingArranger
import shmp.lang.language.syntax.arranger.RelationArranger
import shmp.lang.language.syntax.clause.translation.VerbSentenceType
import shmp.lang.language.syntax.features.QuestionMarker
import shmp.lang.language.syntax.features.WordSyntaxRole
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
                            .wordToNode(PassingArranger, SyntaxRelation.QuestionMarker)
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
