package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.CategoryValue
import shmp.lang.language.Language
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.clause.translation.SentenceNode
import shmp.lang.language.syntax.arranger.RelationArranger
import shmp.lang.language.syntax.context.ActorType
import shmp.lang.language.syntax.features.WordSyntaxRole
import kotlin.random.Random


class NominalClause(
    val nominal: Word,
    val definitions: List<NounDefinerClause>,
    val additionalCategories: List<CategoryValue> = listOf(),
    private val actorType: ActorType?
) : SyntaxClause {
    init {
        val validNominals = listOf(SpeechPart.Noun, SpeechPart.PersonalPronoun, SpeechPart.DeixisPronoun)
        if (nominal.semanticsCore.speechPart !in validNominals)
            throw SyntaxException("$nominal is not a noun or pronoun")
    }

    override fun toNode(language: Language, random: Random): SentenceNode {
        val changeParadigm = language.changeParadigm

        val node = nominal
            .let {
                when {
                    nominal.semanticsCore.hasMeaning("_personal_pronoun") ->
                        it.copy(syntaxRole = WordSyntaxRole.PersonalPronoun)
                    nominal.semanticsCore.hasMeaning("_deixis_pronoun") ->
                        it.copy(syntaxRole = WordSyntaxRole.Demonstrative)
                    else -> it
                }
            }
            .wordToNode(
                changeParadigm,
                RelationArranger(changeParadigm.wordOrder.nominalGroupOrder),
                SyntaxRelation.Subject,
                additionalCategories
            )

        definitions
            .map { it.toNode(language, random) }
            .forEach {
                node.addStrayChild(SyntaxRelation.Definition, it)
            }

        if (nominal.semanticsCore.hasMeaning("_personal_pronoun")) {
            node.isDropped =
                language.changeParadigm.syntaxLogic.resolvePersonalPronounDrop(additionalCategories, actorType!!)
        }

        return node
    }
}
