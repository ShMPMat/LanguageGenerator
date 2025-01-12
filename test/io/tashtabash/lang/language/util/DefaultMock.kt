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
import io.tashtabash.lang.language.syntax.features.*
import io.tashtabash.lang.language.syntax.numeral.NumeralParadigm


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
    listOf(getDefNumberSourcedCategory(isCompulsory)),
    getDefNumberSourcedCategory(isCompulsory)
        .actualSourcedValues
        .map { listOf(it) }
        .toSet()
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
) = Language(
    Lexis(words, mapOf(), mapOf()).reifyPointers(),
    testPhonemeContainer,
    StressType.NotFixed,
    RestrictionsParadigm(mutableMapOf()),
    DerivationParadigm(derivations, listOf()),
    ChangeParadigm(
        WordOrder(mapOf(), mapOf(), NominalGroupOrder.DNP),
        WordChangeParadigm(listOf(defNumberCategory), mapOf(defSpeechPart to nounChangeParadigm)),
        SyntaxParadigm(
            CopulaPresence(listOf(CopulaType.None.toSso(1.0))),
            QuestionMarkerPresence(null),
            PredicatePossessionPresence(listOf(PredicatePossessionType.HaveVerb.toSso(1.0)))
        ),
        NumeralParadigm(NumeralSystemBase.Restricted3, listOf()),
        SyntaxLogic(mapOf(), mapOf(), mapOf(), mapOf(), null, mapOf(), mapOf(), listOf(), null)
    )
)
