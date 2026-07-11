package io.tashtabash.lang.generator

import io.tashtabash.lang.language.category.paradigm.WordChangeParadigm
import io.tashtabash.lang.language.derivation.DerivationType
import io.tashtabash.lang.language.lexis.SpeechPart.Verb
import io.tashtabash.lang.language.lexis.toInf
import io.tashtabash.lang.language.syntax.StaticOrder
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomUnwrappedElement
import io.tashtabash.random.withProb


class AuxGenerator(private val changeParadigm: WordChangeParadigm) {
    val order: RelationArranger = listOf(
        RelationArranger(StaticOrder(SyntaxRelation.Auxiliary, SyntaxRelation.Predicate)).withProb(1.0),
        RelationArranger(StaticOrder(SyntaxRelation.Predicate, SyntaxRelation.Auxiliary)).withProb(1.0)
    ).randomUnwrappedElement()

    fun chooseDerivationType(): DerivationType? =
        listOf(DerivationType.Inf.takeIf { Verb.toInf() in changeParadigm.speechParts }, null)
            .randomElement()
}
