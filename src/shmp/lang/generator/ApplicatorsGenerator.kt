package shmp.lang.generator

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryRealization.*
import shmp.lang.language.category.CategoryRandomSupplements
import shmp.lang.language.category.RealizationBox
import shmp.lang.language.category.paradigm.ExponenceCluster
import shmp.lang.language.category.paradigm.ExponenceValue
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.realization.*
import shmp.lang.language.lexis.*
import shmp.lang.language.morphem.Prefix
import shmp.lang.language.morphem.Suffix
import shmp.lang.language.morphem.change.Position
import shmp.lang.language.phonology.PhoneticRestrictions
import shmp.random.SampleSpaceObject
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElement
import shmp.random.singleton.randomUnwrappedElementOrNull
import shmp.random.singleton.testProbability
import shmp.random.toSampleSpaceObject


class ApplicatorsGenerator(private val lexisGenerator: LexisGenerator, private val changeGenerator: ChangeGenerator) {
    private val exponenceGenerator = ExponenceGenerator()
    private val words = mutableListOf<Word>()
    private val derivativeStemProb = 0.1

    internal fun randomApplicatorsForSpeechPart(
        speechPart: TypedSpeechPart,
        phoneticRestrictions: PhoneticRestrictions,
        categoriesAndSupply: List<SupplementedSourcedCategory>
    ): Result {
        words.clear()

        val categories = categoriesAndSupply.map { it.first }
        val map = HashMap<ExponenceCluster, MutableMap<ExponenceValue, CategoryApplicator>>()

        val exponenceTemplates = exponenceGenerator.splitCategoriesOnClusters(categoriesAndSupply)
        exponenceTemplates.forEach { map[it.exponenceCluster] = mutableMapOf() }

        val realizationTypes = exponenceTemplates
            .mapIndexed { i, t -> t to values().randomElement { c -> t.mapper(i, c) } }
        val orderedRealizationTypes = randomApplicatorsOrder(realizationTypes)
        val realizations = mutableListOf<Map<ExponenceValue, Pair<CategoryRealization, List<RealizationBox>>>>()

        for (i in orderedRealizationTypes.indices) {
            val (cluster, startType) = orderedRealizationTypes[i]
            val clusterMap = map.getValue(cluster.exponenceCluster)
            val currentRealizations = mutableMapOf<ExponenceValue, Pair<CategoryRealization, List<RealizationBox>>>()

            for (value in cluster.exponenceCluster.possibleValues) {
                val types = getRealizationTypes(value, cluster.supplements, speechPart, categories, i)
                val type = types.randomUnwrappedElementOrNull()
                    ?: startType

                clusterMap[value] = getApplicator(type, phoneticRestrictions, value.core)

                currentRealizations[value] = type to types
            }
            realizations += currentRealizations
        }

        val i = findFirstMorphemeCluster(realizations)
        if (i != null) {
            val exponenceCluster = orderedRealizationTypes[i].first.exponenceCluster
            val clusterMap = map.getValue(exponenceCluster)
            val currentRealizations = realizations[i]

            injectDerivationMorpheme(exponenceCluster, clusterMap, phoneticRestrictions, currentRealizations)
        }
        return Result(words, map, orderedRealizationTypes.map { it.first.exponenceCluster })
    }

    private fun findFirstMorphemeCluster(realizations: MutableList<Map<ExponenceValue, Pair<CategoryRealization, List<RealizationBox>>>>): Int? {
        for ((i, map) in realizations.withIndex()) {
            val isFullyOuter = map.all { it.value.first in listOf(SuffixWord, PrefixWord) }
            if (isFullyOuter)
                continue

            return i
        }

        return null
    }

    private fun getRealizationTypes(
        value: ExponenceValue,
        supplements: List<CategoryRandomSupplements>,
        speechPart: TypedSpeechPart,
        categories: List<SourcedCategory>,
        position: Int
    ): List<RealizationBox> {
        val categoryValues = value.categoryValues.map { it.categoryValue }
        val variants = supplements.map {
            it.specialRealization(categoryValues, speechPart.type, categories)
        }

        return uniteMutualProbabilities(variants) { copy(probability = it) }
            .filter { position == 0 || it.realization != Suppletion }
    }

    private fun randomCategoryApplicator(
        realizationType: CategoryRealization,
        phoneticRestrictions: PhoneticRestrictions,
        semanticsCore: SemanticsCore
    ) = when (realizationType) {
        PrefixWord -> {
            val word = lexisGenerator.generateWord(semanticsCore)
            PrefixWordCategoryApplicator(word) to word
        }
        SuffixWord -> {
            val word = lexisGenerator.generateWord(semanticsCore)
            SuffixWordCategoryApplicator(word) to word
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

    private fun getApplicator(
        type: CategoryRealization,
        phoneticRestrictions: PhoneticRestrictions,
        core: SemanticsCore
    ): CategoryApplicator {
        val (applicator, word) = randomCategoryApplicator(type, phoneticRestrictions, core)

        word?.let { words += it }

        return applicator
    }

    private fun randomApplicatorsOrder(realizations: List<Pair<ExponenceTemplate, CategoryRealization>>) =
        realizations.sortedBy { r -> r.first.exponenceCluster.categories.joinToString { it.category.outType } }
            .shuffled(RandomSingleton.random)

    private fun injectDerivationMorpheme(
        exponenceCluster: ExponenceCluster,
        clusterMap: MutableMap<ExponenceValue, CategoryApplicator>,
        phoneticRestrictions: PhoneticRestrictions,
        realizations: Map<ExponenceValue, Pair<CategoryRealization, List<RealizationBox>>>
    ) {
        if (exponenceCluster.isCompulsory && exponenceCluster.possibleValues.size > 2) {
            val distinctRealizations = realizations.map { it.value.first }
                .distinct()
                .subtract(listOf(Passing))

            if (distinctRealizations.size == 1 && derivativeStemProb.testProbability()) {
                val realizationType = distinctRealizations.first()
                val defaultValues = constructDefaultValues(realizations)

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
                    SemanticsCore("(OBL root)", SpeechPart.Particle.toUnspecified())
                )

                for (value in exponenceCluster.possibleValues subtract defaultValues) {
                    val existingApplicator = clusterMap.getValue(value)

                    clusterMap[value] = ConsecutiveApplicator(derivationApplicator, existingApplicator)
                }
            }
        }
    }

    private fun constructDefaultValues(realizations: Map<ExponenceValue, Pair<CategoryRealization, List<RealizationBox>>>): List<ExponenceValue> {
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


typealias RealizationMapper = (CategoryRealization) -> Double
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
