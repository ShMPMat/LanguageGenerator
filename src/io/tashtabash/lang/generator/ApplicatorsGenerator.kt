package io.tashtabash.lang.generator

import io.tashtabash.lang.language.category.paradigm.ExponenceCluster
import io.tashtabash.lang.language.category.paradigm.ExponenceValue
import io.tashtabash.lang.language.category.paradigm.ApplicatorMap
import io.tashtabash.lang.language.category.realization.*
import io.tashtabash.lang.language.category.realization.CategoryRealization.*
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.morphem.Prefix
import io.tashtabash.lang.language.morphem.Suffix
import io.tashtabash.lang.language.morphem.change.Position
import io.tashtabash.lang.language.phonology.PhoneticRestrictions
import io.tashtabash.lang.language.syntax.sequence.LatchType
import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.testProbability
import io.tashtabash.random.toSampleSpaceObject


class ApplicatorsGenerator(private val lexisGenerator: LexisGenerator, private val changeGenerator: ChangeGenerator) {
    private val exponenceGenerator = ExponenceGenerator()
    private val words = mutableListOf<Word>()
    private val derivativeStemProb = 0.1
    private val clauseLatchProb = 0.9

    internal fun randomApplicatorsForSpeechPart(
        speechPart: TypedSpeechPart,
        phoneticRestrictions: PhoneticRestrictions,
        categoriesAndSupply: List<SupplementedSourcedCategory>
    ): Result {
        words.clear()

        val orderedTemplates = exponenceGenerator.splitCategoriesOnClusters(categoriesAndSupply, speechPart)
        val resultMap = orderedTemplates.map { it.cluster to ApplicatorMap() }

        val realizations = mutableListOf<RealizationTemplate>()

        for (i in orderedTemplates.indices) {
            val clusterMap = resultMap[i].second
            val allRealizations = orderedTemplates[i].realizations

            for ((exponenceValue, realization) in allRealizations) {
                val semanticsCore = adjustSemanticsCore(exponenceValue.core, realization.chosen, speechPart)
                clusterMap[exponenceValue] = getApplicator(realization.chosen, phoneticRestrictions, semanticsCore)
            }
            realizations += allRealizations
        }

        val i = findFirstMorphemeCluster(realizations)
        if (i != null) {
            val (exponenceCluster, clusterMap) = resultMap[i]
            val currentRealizations = realizations[i]

            injectDerivationMorpheme(exponenceCluster, clusterMap, phoneticRestrictions, currentRealizations)
        }
        return Result(words, resultMap)
    }

    private fun adjustSemanticsCore(
        core: SemanticsCore,
        realization: CategoryRealization,
        speechPart: TypedSpeechPart
    ): SemanticsCore {
        // Suppleted word should have the same speech part
        if (realization == Suppletion)
            return core.copy(speechPart = speechPart)
        // Catch function words entering endless loop of modifying themselves
        if (core.speechPart.type == speechPart.type)
            return core.copy(speechPart = SpeechPart.Particle.toDefault())

        return core
    }

    private fun findFirstMorphemeCluster(realizations: MutableList<RealizationTemplate>): Int? {
        for ((i, map) in realizations.withIndex()) {
            val isFullyOuter = map.all { it.value.chosen in listOf(SuffixWord, PrefixWord) }
            if (isFullyOuter)
                continue

            return i
        }

        return null
    }

    private fun getApplicator(
        type: CategoryRealization,
        phoneticRestrictions: PhoneticRestrictions,
        core: SemanticsCore
    ): CategoryApplicator = randomCategoryApplicator(type, phoneticRestrictions, core)
        .also {
            if (it is WordCategoryApplicator)
                words += it.word
        }

    private fun randomCategoryApplicator(
        realizationType: CategoryRealization,
        phoneticRestrictions: PhoneticRestrictions,
        semanticsCore: SemanticsCore
    ) = when (realizationType) {
        PrefixWord -> PrefixWordCategoryApplicator(
            lexisGenerator.generateWord(semanticsCore),
            clauseLatchProb.chanceOf<LatchType> { LatchType.ClauseLatch } ?: LatchType.InPlace
        )
        SuffixWord -> SuffixWordCategoryApplicator(
            lexisGenerator.generateWord(semanticsCore),
            clauseLatchProb.chanceOf<LatchType> { LatchType.ClauseLatch } ?: LatchType.InPlace
        )
        Prefix ->
        AffixCategoryApplicator(
            Prefix(changeGenerator.generateChanges(Position.Beginning, phoneticRestrictions)),
            Prefix
        )
        Suffix -> AffixCategoryApplicator(
            Suffix(changeGenerator.generateChanges(Position.End, phoneticRestrictions)),
            Suffix
        )
        Reduplication -> WordReduplicationCategoryApplicator()
        Passing -> PassingCategoryApplicator
        Suppletion -> SuppletionCategoryApplicator(
            lexisGenerator.generateWord(semanticsCore)
        )
    }

    private fun injectDerivationMorpheme(
        exponenceCluster: ExponenceCluster,
        clusterMap: MutableMap<ExponenceValue, CategoryApplicator>,
        phoneticRestrictions: PhoneticRestrictions,
        realizations: RealizationTemplate
    ) {
        if (exponenceCluster.isCompulsory && exponenceCluster.possibleValues.size > 2) {
            val distinctRealizations = realizations.map { it.value.chosen }
                .distinct()
                .subtract(listOf(Passing))

            if (distinctRealizations.size == 1 && derivativeStemProb.testProbability()) {
                val realizationType = distinctRealizations.first()
                val defaultValues = constructUnderivedValues(realizations)

                if (defaultValues.isEmpty())
                    return

                val possibleDerivations = listOf(Prefix, Suffix)
                val isDefaultGood = realizationType in possibleDerivations
                val derivationRealization = if (!isDefaultGood || !0.95.testProbability())
                    possibleDerivations.randomElement()
                else realizationType

                val derivationApplicator = getApplicator(
                    derivationRealization,
                    phoneticRestrictions,
                    SemanticsCore("(OBL root)", SpeechPart.Particle.toDefault())
                )

                for (value in exponenceCluster.possibleValues subtract defaultValues) {
                    val existingApplicator = clusterMap.getValue(value)

                    clusterMap[value] = ConsecutiveApplicator(derivationApplicator, existingApplicator)
                }
            }
        }
    }

    private fun constructUnderivedValues(realizations: RealizationTemplate): List<ExponenceValue> {
        val passingValues = realizations.filter { it.value.chosen == Passing }
            .map { it.key }
        val possiblePassingValues = realizations.mapNotNull { (e, p) ->
            p.possible.firstOrNull { it.realization == Passing }
                ?.let { e.toSampleSpaceObject(it.probability) }
        }

        val result = passingValues.toMutableList()
        val maxProb = possiblePassingValues.maxByOrNull { it.probability }
            ?.probability

        if (maxProb != null)
            result += if (result.isEmpty())
                possiblePassingValues.mapNotNull { v ->
                    v.value.takeIf { (v.probability / maxProb).testProbability() }
                }
            else
                possiblePassingValues.mapNotNull { v ->
                    v.value.takeIf { (v.probability / (maxProb * 2)).testProbability() }
                }

        //TODO more variants

        return result
    }
}


data class Result(val words: List<Word>, val solver: List<Pair<ExponenceCluster, ApplicatorMap>>)

fun <E : SampleSpaceObject> uniteMutualProbabilities(
    objectLists: List<Collection<E>>,
    copy: E.(Double) -> E
): List<E> {
    var previousVariants = objectLists.first().toMutableSet()
    var newVariants = mutableSetOf<E>()

    for (variantList in objectLists.drop(1)) {
        for (variant in variantList) {
            val same = previousVariants.firstOrNull { it == variant }
            if (same == null)
                newVariants.add(variant)
            else
                newVariants.add(same.copy(same.probability * variant.probability))
        }
        previousVariants = newVariants
        newVariants = mutableSetOf()
    }

    return previousVariants.toList()
}
