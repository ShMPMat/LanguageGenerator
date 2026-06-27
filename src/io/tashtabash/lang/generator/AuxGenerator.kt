package io.tashtabash.lang.generator

import io.tashtabash.lang.language.syntax.StaticOrder
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.random.singleton.randomUnwrappedElement
import io.tashtabash.random.withProb


class AuxGenerator {
    val order: RelationArranger = listOf(
        RelationArranger(StaticOrder(SyntaxRelation.Auxiliary, SyntaxRelation.Predicate)).withProb(1.0),
        RelationArranger(StaticOrder(SyntaxRelation.Predicate, SyntaxRelation.Auxiliary)).withProb(1.0)
    ).randomUnwrappedElement()
}
