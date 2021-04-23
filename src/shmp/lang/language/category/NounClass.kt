package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValue
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.NounClassValue.*
import shmp.lang.language.lexis.*
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.syntax.SyntaxRelation
import shmp.random.SampleSpaceObject
import shmp.random.randomSublist
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability


const val nounClassName = "NounClass"

class NounClass(
    categories: List<NounClassValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    NounClassValue.values().toSet(),
    affected,
    staticSpeechParts,
    nounClassName
)

object NounClassRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not actual data
            CategoryRealization.PrefixSeparateWord -> 10.0
            CategoryRealization.SuffixSeparateWord -> 10.0
            CategoryRealization.Prefix -> 100.0
            CategoryRealization.Suffix -> 100.0
            CategoryRealization.Reduplication -> 0.0
            CategoryRealization.Passing -> 0.0
            CategoryRealization.NewWord -> 0.0
        }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf(SourceTemplate(SelfStated, 100.0))
        Verb -> listOf(
            SourceTemplate(RelationGranted(SyntaxRelation.Agent), 95.0),
            SourceTemplate(RelationGranted(SyntaxRelation.Patient), 5.0)
        )
        Adjective -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Agent), 95.0))
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Agent), 90.0))
        PersonalPronoun -> listOf(SourceTemplate(SelfStated, 99.0))
        DeixisPronoun -> listOf(SourceTemplate(SelfStated, 90.0))
        Particle -> listOf()
        Adposition -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Agent), 2.0))
    }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == nounClassName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when(speechPart) {
            PersonalPronoun -> setOf(//TODO no actual data
                noValue(1.0),
                RealizationBox(CategoryRealization.NewWord, 2.0)
            )
            DeixisPronoun -> setOf(//TODO no actual data
                RealizationBox(CategoryRealization.Suffix, 1.5),
                RealizationBox(CategoryRealization.Prefix, 1.5)
            )
            else -> emptyRealization
        }
    }

    override fun randomRealization(): List<NounClassValue> {
        val type = NounClassPresence.values().randomElement()

        return if (type == NounClassPresence.NonGendered)
            randomSublist(type.possibilities, RandomSingleton.random, min = 4)
        else
            type.possibilities
    }

    override fun randomStaticSpeechParts() = setOf(Noun)

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Noun -> true
        Verb -> 0.95.testProbability()
        Adjective -> 0.9.testProbability()
        Article -> 0.9.testProbability()
        PersonalPronoun -> 0.8.testProbability()
        DeixisPronoun -> 0.8.testProbability()
        Adposition -> 0.6.testProbability()
        else -> true
    }
}

enum class NounClassPresence(override val probability: Double, val possibilities: List<NounClassValue>): SampleSpaceObject {
    None(145.0, listOf()),
    FmcnGendered(6.0, listOf(Common, Female, Male, Neutral)),
    FmnGendered(26.0, listOf(Female, Male, Neutral)),
    CnGendered(26.0, listOf(Common, Neutral)),
    FmGendered(26.0, listOf(Female, Male)),
    NonGendered(28.0, NounClassValue.values().toList())
}

enum class NounClassValue(override val semanticsCore: SemanticsCore, override val shortName: String) : CategoryValue {
    //TODO more classes (don't forget to add tags for words after it!)
    Female(SemanticsCore("(female class ind)".toCluster(), Particle.toUnspecified()), "FEM"),
    Male(SemanticsCore("(male class ind)".toCluster(), Particle.toUnspecified()), "MALE"),
    Neutral(SemanticsCore("(neutral class ind)".toCluster(), Particle.toUnspecified()), "NEUT"),
    Common(SemanticsCore("(common class ind)".toCluster(), Particle.toUnspecified()), "COMM"),

    Person(SemanticsCore("(person class ind)".toCluster(), Particle.toUnspecified()), "PERS"),
    Plant(SemanticsCore("(plant class ind)".toCluster(), Particle.toUnspecified()), "PLANT"),
    Fruit(SemanticsCore("(fruit class ind)".toCluster(), Particle.toUnspecified()), "FRUIT"),
    LongObject(SemanticsCore("(long object class ind)".toCluster(), Particle.toUnspecified()), "LONG.OBJ");

    override val parentClassName = nounClassName
}