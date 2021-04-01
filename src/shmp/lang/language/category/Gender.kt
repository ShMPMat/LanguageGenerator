package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValue
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.GenderValue.*
import shmp.lang.language.lexis.MeaningCluster
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.lexis.toUnspecified
import shmp.lang.language.syntax.SyntaxRelation
import shmp.random.SampleSpaceObject
import shmp.random.randomSublist
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElement


const val genderName = "Gender"

class Gender(
    categories: List<GenderValue>,
    affected: Set<PSpeechPart>,
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

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        SpeechPart.Noun -> listOf(SourceTemplate(SelfStated, 100.0))
        SpeechPart.Verb -> listOf(
            SourceTemplate(RelationGranted(SyntaxRelation.Subject), 95.0),
            SourceTemplate(RelationGranted(SyntaxRelation.Object), 5.0)
        )
        SpeechPart.Adjective -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Subject), 95.0))
        SpeechPart.Adverb -> listOf()
        SpeechPart.Numeral -> listOf()
        SpeechPart.Article -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Subject), 90.0))
        SpeechPart.PersonalPronoun -> listOf(SourceTemplate(SelfStated, 99.0))
        SpeechPart.DeixisPronoun -> listOf(SourceTemplate(SelfStated, 90.0))
        SpeechPart.Particle -> listOf()
    }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == genderName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when(speechPart) {
            SpeechPart.PersonalPronoun -> setOf(//TODO no actual data
                noValue(1.0),
                RealizationBox(CategoryRealization.NewWord, 2.0)
            )
            SpeechPart.DeixisPronoun -> setOf(//TODO no actual data
                RealizationBox(CategoryRealization.Suffix, 1.5),
                RealizationBox(CategoryRealization.Prefix, 1.5)
            )
            else -> emptyRealization
        }
    }

    override fun randomRealization(): List<GenderValue> {
        val type = GenderPresence.values().randomElement()

        return if (type == GenderPresence.NonGendered)
            randomSublist(type.possibilities, RandomSingleton.random, min = 2)
        else
            type.possibilities
    }

    override fun randomStaticSpeechParts() = setOf(SpeechPart.Noun)
}

enum class GenderPresence(override val probability: Double, val possibilities: List<GenderValue>): SampleSpaceObject {
    None(145.0, listOf()),
    FmcnGendered(6.0, listOf(Common, Female, Male, Neutral)),
    FmnGendered(26.0, listOf(Female, Male, Neutral)),
    CnGendered(26.0, listOf(Common, Neutral)),
    FmGendered(26.0, listOf(Female, Male)),
    NonGendered(28.0, GenderValue.values().toList())
}

enum class GenderValue(override val semanticsCore: SemanticsCore, override val shortName: String) : CategoryValue {
    //TODO more classes (don't forget to add tags for words after it!)
    Female(
        SemanticsCore(
            MeaningCluster("(female gender indicator)"),
            SpeechPart.Particle.toUnspecified(),
            setOf()
        ),
        "FEM"
    ),
    Male(SemanticsCore(MeaningCluster("(male gender indicator)"), SpeechPart.Particle.toUnspecified(), setOf()), "MALE"),
    Neutral(
        SemanticsCore(
            MeaningCluster("(neutral gender indicator)"),
            SpeechPart.Particle.toUnspecified(),
            setOf()
        ),
        "NEUT"
    ),
    Common(
        SemanticsCore(
            MeaningCluster("(common gender indicator)"),
            SpeechPart.Particle.toUnspecified(),
            setOf()
        ),
        "COMM"
    ),

    Person(
        SemanticsCore(
            MeaningCluster("(person class indicator)"),
            SpeechPart.Particle.toUnspecified(),
            setOf()
        ),
        "PERS"
    ),
    Plant(SemanticsCore(MeaningCluster("(plant class indicator)"), SpeechPart.Particle.toUnspecified(), setOf()), "PLANT"),
    Fruit(SemanticsCore(MeaningCluster("(fruit class indicator)"), SpeechPart.Particle.toUnspecified(), setOf()), "FRUIT"),
    LongObject(
        SemanticsCore(
            MeaningCluster("(long object class indicator)"),
            SpeechPart.Particle.toUnspecified(),
            setOf()
        ),
        "LONG.OBJ"
    );

    override val parentClassName = genderName
}