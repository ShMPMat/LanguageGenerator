package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.clause.translation.SentenceNode
import io.tashtabash.lang.language.syntax.context.ActorType
import io.tashtabash.lang.language.syntax.features.WordSyntaxRole
import kotlin.random.Random


data class NominalClause(
    val nominal: Word,
    val definitions: List<NounDefinerClause>,
    val additionalCategories: CategoryValues = listOf(),
    private val actorType: ActorType? = null
) : SyntaxClause {
    init {
        val validNominals = listOf(SpeechPart.Noun, SpeechPart.PersonalPronoun, SpeechPart.DeixisPronoun)
        if (nominal.semanticsCore.speechPart.type !in validNominals)
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
            .toNode(
                SyntaxRelation.Nominal,
                additionalCategories,
                RelationArranger(changeParadigm.wordOrder.nominalGroupOrder)
            )

        definitions
            .map { it.relationFromNoun to it.toNode(language, random) }
            .forEach {
                node.addStrayChild(it.first, it.second)
            }

        if (nominal.semanticsCore.hasMeaning("_personal_pronoun"))
            node.isDropped =
                language.changeParadigm.syntaxLogic.resolvePersonalPronounDrop(additionalCategories, actorType)

        return node
    }
}
