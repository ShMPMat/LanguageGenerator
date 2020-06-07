package shmp.language.category

import shmp.language.*
import shmp.language.category.GenderValue.*
import shmp.language.syntax.SyntaxRelation
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import shmp.random.randomSublist
import kotlin.random.Random

const val genderName = "Gender"

class Gender(
    categories: List<GenderValue>,
    affected: Set<ParametrizedSpeechPart>,
    override val staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    values().toSet(),
    affected,
    genderName
)

object GenderRandomSupplements : CategoryRandomSupplements {
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

    override fun speechPartProbabilities(speechPart: SpeechPart): Double =
        when (speechPart) {
            SpeechPart.Noun -> 100.0
            SpeechPart.Verb -> 95.0
            SpeechPart.Adjective -> 95.0
            SpeechPart.Adverb -> 0.0
            SpeechPart.Numeral -> 0.0
            SpeechPart.Article -> 90.0
            SpeechPart.Pronoun -> 99.0
            SpeechPart.Particle -> 0.0
        }

    override fun speechPartCategorySource(speechPart: SpeechPart) =
        when (speechPart) {
            SpeechPart.Noun -> CategorySource.SelfStated
            SpeechPart.Verb -> CategorySource.RelationGranted(SyntaxRelation.Subject)
            SpeechPart.Adjective -> CategorySource.RelationGranted(SyntaxRelation.Subject)
            SpeechPart.Adverb -> null
            SpeechPart.Numeral -> null
            SpeechPart.Article -> CategorySource.RelationGranted(SyntaxRelation.Subject)
            SpeechPart.Pronoun -> CategorySource.RelationGranted(SyntaxRelation.Subject)
            SpeechPart.Particle -> null
        }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == genderName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when(speechPart) {
            SpeechPart.Pronoun -> setOf(//TODO no actual data
                noValue(1.0),
                RealizationBox(CategoryRealization.NewWord, 2.0)
            )
            else -> emptyRealization
        }
    }

    override fun randomRealization(random: Random): List<GenderValue> {
        val type = randomElement(
            GenderPresence.values(),
            random
        )
        return if (type == GenderPresence.NonGendered)
            randomSublist(type.possibilities, random, min = 2)
        else
            type.possibilities
    }

    override fun randomStaticSpeechParts(random: Random) = setOf(SpeechPart.Noun)
}

enum class GenderPresence(override val probability: Double, val possibilities: List<GenderValue>): SampleSpaceObject {
    None(145.0, listOf()),
    FmcnGendered(6.0, listOf(Common, Female, Male, Neutral)),
    FmnGendered(26.0, listOf(Female, Male, Neutral)),
    CnGendered(26.0, listOf(Common, Neutral)),
    FmGendered(26.0, listOf(Female, Male)),
    NonGendered(28.0, GenderValue.values().toList())
}

enum class GenderValue(override val semanticsCore: SemanticsCore) : CategoryValue {
    //TODO more classes (don't forget to add tags for words after it!)
    Female(SemanticsCore("(female gender indicator)", SpeechPart.Particle, setOf())),
    Male(SemanticsCore("(male gender indicator)", SpeechPart.Particle, setOf())),
    Neutral(SemanticsCore("(neutral gender indicator)", SpeechPart.Particle, setOf())),
    Common(SemanticsCore("(common gender indicator)", SpeechPart.Particle, setOf())),

    Person(SemanticsCore("(person class indicator)", SpeechPart.Particle, setOf())),
    Plant(SemanticsCore("(plant class indicator)", SpeechPart.Particle, setOf())),
    Fruit(SemanticsCore("(fruit class indicator)", SpeechPart.Particle, setOf())),
    LongObject(SemanticsCore("(long object class indicator)", SpeechPart.Particle, setOf()));

    override val parentClassName = genderName
}