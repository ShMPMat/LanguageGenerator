package io.tashtabash.lang.language.util

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.NumeralSystemBase
import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.Number
import io.tashtabash.lang.language.category.NumberValue
import io.tashtabash.lang.language.category.PSpeechPart
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.category.realization.CategoryApplicator
import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.derivation.Derivation
import io.tashtabash.lang.language.derivation.DerivationParadigm
import io.tashtabash.lang.language.derivation.PassingCategoryChanger
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.phonology.RestrictionsParadigm
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.features.*
import io.tashtabash.lang.language.syntax.features.PredicatePossessionType.HaveVerb
import io.tashtabash.lang.language.syntax.numeral.NumeralParadigm
import io.tashtabash.lang.utils.MapWithDefault
import io.tashtabash.random.toSampleSpaceObject


val defSpeechPart = TypedSpeechPart(SpeechPart.Noun)
val defCategoryChanger = PassingCategoryChanger(0)
val defNumberCategory = Number(
    listOf(NumberValue.Singular, NumberValue.Dual, NumberValue.Paucal, NumberValue.Plural),
    setOf(PSpeechPart(SpeechPart.Noun, CategorySource.Self))
)
val defOrder = SovOrder(
    // This word order is kinda weird to be applicable to transitive and intransitive clauses
    listOf(
        listOf(
            SyntaxRelation.Topic,
            SyntaxRelation.Addressee,
            SyntaxRelation.Agent,
            SyntaxRelation.Argument,
            SyntaxRelation.Patient,
            SyntaxRelation.Verb,
            SyntaxRelation.Benefactor,
            SyntaxRelation.Location
        ).toSampleSpaceObject(1.0)
    ),
    "Name"
)

fun getDefNumberSourcedCategory(isCompulsory: Boolean) = SourcedCategory(
    defNumberCategory,
    CategorySource.Self,
    CompulsoryData(isCompulsory)
)

fun makeDefExponenceCluster(isCompulsory: Boolean) = ExponenceCluster(
    getDefNumberSourcedCategory(isCompulsory)
)

fun makeDefNounChangeParadigm(vararg applicators: CategoryApplicator, isCompulsory: Boolean = false) =
    SpeechPartChangeParadigm(
        defSpeechPart,
        listOf(makeDefExponenceCluster(isCompulsory) to MapApplicatorSource(
            makeDefExponenceCluster(isCompulsory).possibleValues,
            applicators.toList()
        )
        ),
        ProsodyChangeParadigm(StressType.Initial)
    )

fun makeDefLang(
    words: List<Word>,
    derivations: List<Derivation> = listOf(),
    nounChangeParadigm: SpeechPartChangeParadigm = makeDefNounChangeParadigm(
        PassingCategoryApplicator,
        PassingCategoryApplicator,
        PassingCategoryApplicator,
        PassingCategoryApplicator
    )
): Language = makeDefLang(
    words,
    WordChangeParadigm(
        listOf(defNumberCategory),
        mapOf(
            defSpeechPart to nounChangeParadigm,
            // Set a paradigm for particles in case the lang has them
            SpeechPart.Particle.toDefault() to SpeechPartChangeParadigm(SpeechPart.Particle.toDefault())
        )
    ),
    derivations
)

fun makeDefLang(
    words: List<Word>,
    wordChangeParadigm: WordChangeParadigm,
    derivations: List<Derivation> = listOf(),
    syntaxLogic: SyntaxLogic = SyntaxLogic(),
    predicatePossessionType: PredicatePossessionType = HaveVerb
) = Language(
    Lexis(words, mapOf(), mapOf()).reifyPointers(),
    testPhonemeContainer,
    StressType.NotFixed,
    RestrictionsParadigm(mutableMapOf()),
    DerivationParadigm(derivations, listOf()),
    ChangeParadigm(
        WordOrder(
            MapWithDefault(defOrder),
            mapOf(),
            NominalGroupOrder.DNP
        ),
        wordChangeParadigm,
        SyntaxParadigm(
            CopulaPresence(listOf(CopulaType.None.toSso(1.0))),
            QuestionMarkerPresence(null),
            PredicatePossessionPresence(listOf(predicatePossessionType.toSso(1.0)))
        ),
        NumeralParadigm(NumeralSystemBase.Restricted3, listOf()),
        syntaxLogic
    )
)
