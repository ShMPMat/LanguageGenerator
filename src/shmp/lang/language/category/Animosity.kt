package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValue
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
import shmp.lang.language.lexis.*
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.lexis.SpeechPart.Verb
import shmp.lang.language.syntax.SyntaxRelation.*
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability


const val animosityName = "Animosity"

class Animosity(
    categories: List<AnimosityValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    AnimosityValue.values().toSet(),
    affected,
    staticSpeechParts,
    animosityName
)

object AnimosityRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not an actual data
            CategoryRealization.PrefixSeparateWord -> 20.0
            CategoryRealization.SuffixSeparateWord -> 20.0
            CategoryRealization.Prefix -> 100.0
            CategoryRealization.Suffix -> 100.0
            CategoryRealization.Reduplication -> 0.0
            CategoryRealization.Passing -> 0.0
            CategoryRealization.NewWord -> 0.0
        }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf(SourceTemplate(SelfStated, 200.0))//TODO no data at all
        Verb -> listOf(
            SourceTemplate(RelationGranted(Agent, nominals), 20.0),
            SourceTemplate(RelationGranted(Patient, nominals), 1.0)
        )
        Adjective -> listOf(SourceTemplate(RelationGranted(Nominal, nominals), 20.0))
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf(SourceTemplate(RelationGranted(Agent, nominals), 10.0))
        PersonalPronoun -> listOf(SourceTemplate(SelfStated, 10.0))
        DeixisPronoun -> listOf(SourceTemplate(SelfStated, 10.0))
        Particle -> listOf()
        Adposition -> listOf()
    }

    override fun specialRealization(
        values: List<CategoryValue>,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == animosityName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when (value) {
            AnimosityValue.Inanimate -> setOf(
                RealizationBox(CategoryRealization.Passing, 1.0),
                noValue(1.0)
            )
            else -> return when(speechPart) {
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
    }

    override fun randomRealization() = AnimosityPresence.values().randomElement().possibilities

    override fun randomStaticSpeechParts() = setOf(Noun)

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Noun -> true
        Verb -> 0.8.testProbability()
        Adjective -> 0.8.testProbability()
        Article -> 0.7.testProbability()
        PersonalPronoun -> 0.4.testProbability()
        DeixisPronoun -> 0.4.testProbability()
        else -> true
    } withCoCategories listOf()
}

enum class AnimosityPresence(
    override val probability: Double,
    val possibilities: List<AnimosityValue>
) : SampleSpaceObject {
    NoAnimosity(100.0, listOf()),
    SimpleAnimosity(10.0, listOf(AnimosityValue.Animate, AnimosityValue.Inanimate))
}

enum class AnimosityValue(override val semanticsCore: SemanticsCore, override val alias: String) : CategoryValue {
    Animate(SemanticsCore("animate indicator", Particle.toUnspecified()), "ANIM"),
    Inanimate(SemanticsCore("inanimate indicator", Particle.toUnspecified()), "INANIM");

    override val parentClassName = animosityName
}