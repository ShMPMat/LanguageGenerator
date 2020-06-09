package shmp.language.syntax

import shmp.random.SampleSpaceObject

data class WordOrder(val sovOrder: SovOrder) {
    override fun toString() = sovOrder.toString()
}

enum class SovOrder(val referenceOrder: List<SyntaxRelation>, override val probability: Double) : SampleSpaceObject {
    SOV(listOf(SyntaxRelation.Subject, SyntaxRelation.Object, SyntaxRelation.Verb), 565.0),
    SVO(listOf(SyntaxRelation.Subject, SyntaxRelation.Verb, SyntaxRelation.Object), 488.0),
    VSO(listOf(SyntaxRelation.Verb, SyntaxRelation.Subject, SyntaxRelation.Object), 95.0),
    VOS(listOf(SyntaxRelation.Verb, SyntaxRelation.Object, SyntaxRelation.Subject), 25.0),
    OVS(listOf(SyntaxRelation.Object, SyntaxRelation.Verb, SyntaxRelation.Subject), 11.0),
    OSV(listOf(SyntaxRelation.Object, SyntaxRelation.Subject, SyntaxRelation.Verb),4.0),
    None(listOf(SyntaxRelation.Object, SyntaxRelation.Verb, SyntaxRelation.Subject),189.0) //TODO none is none!
}