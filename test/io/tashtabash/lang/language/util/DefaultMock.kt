package io.tashtabash.lang.language.util

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.NumeralSystemBase
import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.Number
import io.tashtabash.lang.language.category.NumberValue
import io.tashtabash.lang.language.category.PSpeechPart
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.category.realization.CategoryApplicator
import io.tashtabash.lang.language.derivation.Derivation
import io.tashtabash.lang.language.derivation.DerivationParadigm
import io.tashtabash.lang.language.derivation.PassingCategoryChanger
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.phonology.RestrictionsParadigm
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.clause.translation.VerbSentenceType
import io.tashtabash.lang.language.syntax.features.*
import io.tashtabash.lang.language.syntax.numeral.NumeralParadigm
import io.tashtabash.random.toSampleSpaceObject


val defSpeechPart = TypedSpeechPart(SpeechPart.Noun)
val defCategoryChanger = PassingCategoryChanger(0)
val defNumberCategory = Number(
    listOf(NumberValue.Singular, NumberValue.Dual, NumberValue.Paucal, NumberValue.Plural),
    setOf(PSpeechPart(SpeechPart.Noun, CategorySource.Self)),
    setOf()
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
        listOf(makeDefExponenceCluster(isCompulsory)),
        mapOf(makeDefExponenceCluster(isCompulsory) to makeDefExponenceCluster(isCompulsory)
            .possibleValues
            .zip(applicators)
            .toMap()),
        ProsodyChangeParadigm(StressType.Initial)
    )

fun makeDefLang(
    words: List<Word>,
    derivations: List<Derivation>,
    nounChangeParadigm: SpeechPartChangeParadigm
): Language = makeDefLang(
    words,
    WordChangeParadigm(listOf(defNumberCategory), mapOf(defSpeechPart to nounChangeParadigm)),
    derivations
)

fun makeDefLang(
    words: List<Word>,
    wordChangeParadigm: WordChangeParadigm,
    derivations: List<Derivation> = listOf(),
    syntaxLogic: SyntaxLogic = SyntaxLogic()
) = Language(
    Lexis(words, mapOf(), mapOf()).reifyPointers(),
    testPhonemeContainer,
    StressType.NotFixed,
    RestrictionsParadigm(mutableMapOf()),
    DerivationParadigm(derivations, listOf()),
    ChangeParadigm(
        WordOrder(
            mapOf(
                VerbSentenceType.MainVerbClause to
                        SovOrder(listOf(listOf(SyntaxRelation.Agent, SyntaxRelation.Verb).toSampleSpaceObject(1.0)), "Name")),
            mapOf(),
            NominalGroupOrder.DNP
        ),
        wordChangeParadigm,
        SyntaxParadigm(
            CopulaPresence(listOf(CopulaType.None.toSso(1.0))),
            QuestionMarkerPresence(null),
            PredicatePossessionPresence(listOf(PredicatePossessionType.HaveVerb.toSso(1.0)))
        ),
        NumeralParadigm(NumeralSystemBase.Restricted3, listOf()),
        syntaxLogic
    )
)
