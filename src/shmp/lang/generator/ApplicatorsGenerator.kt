package shmp.lang.generator

import shmp.lang.language.category.realization.CategoryRealization
import shmp.lang.language.category.realization.CategoryRealization.*
import shmp.lang.language.category.paradigm.ExponenceCluster
import shmp.lang.language.category.paradigm.ExponenceValue
import shmp.lang.language.category.realization.*
import shmp.lang.language.lexis.*
import shmp.lang.language.morphem.Prefix
import shmp.lang.language.morphem.Suffix
import shmp.lang.language.morphem.change.Position
import shmp.lang.language.phonology.PhoneticRestrictions
import shmp.lang.language.syntax.sequence.LatchType
import shmp.random.SampleSpaceObject
import shmp.random.singleton.*
import shmp.random.toSampleSpaceObject


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

        val map = HashMap<ExponenceCluster, MutableMap<ExponenceValue, CategoryApplicator>>()

        val orderedTemplates = exponenceGenerator.splitCategoriesOnClusters(categoriesAndSupply, speechPart)
        orderedTemplates.forEach { map[it.cluster] = mutableMapOf() }

        val realizations = mutableListOf<RealizationTemplate>()

        for (i in orderedTemplates.indices) {
            val (cluster, allRealizations) = orderedTemplates[i]
            val clusterMap = map.getValue(cluster)

            for ((value, pair) in allRealizations) {
                clusterMap[value] = getApplicator(pair.first, phoneticRestrictions, value.core)
            }
            realizations += allRealizations
        }

        val i = findFirstMorphemeCluster(realizations)
        if (i != null) {
            val exponenceCluster = orderedTemplates[i].cluster
            val clusterMap = map.getValue(exponenceCluster)
            val currentRealizations = realizations[i]

            injectDerivationMorpheme(exponenceCluster, clusterMap, phoneticRestrictions, currentRealizations)
        }
        return Result(words, map, orderedTemplates.map { it.cluster })
    }

    private fun findFirstMorphemeCluster(realizations: MutableList<RealizationTemplate>): Int? {
        for ((i, map) in realizations.withIndex()) {
            val isFullyOuter = map.all { it.value.first in listOf(SuffixWord, PrefixWord) }
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
    ): CategoryApplicator {
        val (applicator, word) = randomCategoryApplicator(type, phoneticRestrictions, core)

        word?.let { words += it }

        return applicator
    }

    private fun randomCategoryApplicator(
        realizationType: CategoryRealization,
        phoneticRestrictions: PhoneticRestrictions,
        semanticsCore: SemanticsCore
    ) = when (realizationType) {
        PrefixWord -> {
            val word = lexisGenerator.generateWord(semanticsCore)
            val latch = clauseLatchProb.chanceOf<LatchType> { LatchType.ClauseLatch } ?: LatchType.InPlace

            PrefixWordCategoryApplicator(word, latch) to word
        }
        SuffixWord -> {
            val word = lexisGenerator.generateWord(semanticsCore)
            val latch = clauseLatchProb.chanceOf<LatchType> { LatchType.ClauseLatch } ?: LatchType.InPlace

            SuffixWordCategoryApplicator(word, latch) to word
        }
        Prefix -> {
            val changes = changeGenerator.generateChanges(Position.Beginning, phoneticRestrictions)
            AffixCategoryApplicator(
                Prefix(changes),
                Prefix
            ) to null
        }
        Suffix -> {
            val change = changeGenerator.generateChanges(Position.End, phoneticRestrictions)
            AffixCategoryApplicator(
                Suffix(change),
                Suffix
            ) to null
        }
        Reduplication -> ReduplicationCategoryApplicator() to null
        Passing -> PassingCategoryApplicator to null
        Suppletion -> {
            val word = lexisGenerator.generateWord(semanticsCore)
            SuppletionCategoryApplicator(word) to word
        }
    }

    private fun injectDerivationMorpheme(
        exponenceCluster: ExponenceCluster,
        clusterMap: MutableMap<ExponenceValue, CategoryApplicator>,
        phoneticRestrictions: PhoneticRestrictions,
        realizations: RealizationTemplate
    ) {
        if (exponenceCluster.isCompulsory && exponenceCluster.possibleValues.size > 2) {
            val distinctRealizations = realizations.map { it.value.first }
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
        val passingValues = realizations.filter { it.value.first == Passing }
            .map { it.key }
        val probablePassingValues = realizations.mapNotNull { (e, p) ->
            p.second.firstOrNull { it.realization == Passing }
                ?.let { e.toSampleSpaceObject(it.probability) }
        }

        val result = passingValues.toMutableList()
        val maxProb = probablePassingValues.maxByOrNull { it.probability }
            ?.probability

        if (maxProb != null)
            result += if (result.isEmpty())
                probablePassingValues.mapNotNull { v ->
                    v.value.takeIf { (v.probability / maxProb).testProbability() }
                }
            else
                probablePassingValues.mapNotNull { v ->
                    v.value.takeIf { (v.probability / (maxProb * 2)).testProbability() }
                }

        //TODO more variants

        return result
    }
}


typealias ApplicatorMap = Map<ExponenceCluster, Map<ExponenceValue, CategoryApplicator>>

data class Result(val words: List<Word>, val solver: ApplicatorMap, val orderedClusters: List<ExponenceCluster>)

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
