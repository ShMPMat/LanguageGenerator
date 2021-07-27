package shmp.lang.generator

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryRealization.*
import shmp.lang.language.category.CategoryRandomSupplements
import shmp.lang.language.category.paradigm.ExponenceCluster
import shmp.lang.language.category.paradigm.ExponenceValue
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.realization.*
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.morphem.Prefix
import shmp.lang.language.morphem.Suffix
import shmp.lang.language.morphem.change.Position
import shmp.lang.language.phonology.PhoneticRestrictions
import shmp.random.SampleSpaceObject
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElement
import shmp.random.singleton.randomUnwrappedElement


class SpeechPartApplicatorsGenerator(
    private val lexisGenerator: LexisGenerator,
    private val changeGenerator: ChangeGenerator
) {
    private val exponenceGenerator = ExponenceGenerator()

    internal fun randomApplicatorsForSpeechPart(
        speechPart: TypedSpeechPart,
        phoneticRestrictions: PhoneticRestrictions,
        categoriesAndSupply: List<SupplementedSourcedCategory>
    ): Result {
        val categories = categoriesAndSupply.map { it.first }
        val map = HashMap<ExponenceCluster, MutableMap<ExponenceValue, CategoryApplicator>>()
        val words = mutableListOf<Word>()

        val exponenceTemplates = exponenceGenerator.splitCategoriesOnClusters(categoriesAndSupply)
        exponenceTemplates.forEach { map[it.exponenceCluster] = mutableMapOf() }

        val realizationTypes = exponenceTemplates
            .mapIndexed { i, t -> t to values().randomElement { c -> t.mapper(i, c) } }

        for (i in realizationTypes.indices) {
            val (cluster, realization) = realizationTypes[i]

            cluster.exponenceCluster.possibleValues.forEach { exponenceValue ->
                val cores = exponenceValue.categoryValues
                    .map { it.categoryValue.semanticsCore }
                val semanticsCore = combineSemanticCores(cores)
                val (applicator, word) = randomCategoryApplicator(
                    decideRealizationType(realization, exponenceValue, cluster.supplements, speechPart, categories, i),
                    phoneticRestrictions,
                    semanticsCore
                )
                map.getValue(cluster.exponenceCluster)[exponenceValue] = applicator
                word?.let {
                    words.add(it)
                }
            }
        }
        return Result(words, map)
    }

    private fun combineSemanticCores(semanticsCores: List<SemanticsCore>): SemanticsCore {
        var semanticsCore = semanticsCores[0]
        for (core in semanticsCores.drop(1))
            semanticsCore = SemanticsCore(
                semanticsCore.meaningCluster + core.meaningCluster,
                if (core.speechPart.type != SpeechPart.Particle) core.speechPart else semanticsCore.speechPart,
                semanticsCore.connotations + core.connotations,
                semanticsCore.tags + core.tags,
                semanticsCore.derivationCluster,
                semanticsCore.staticCategories + core.staticCategories
            )
        return semanticsCore
    }

    private fun decideRealizationType(
        categoryRealization: CategoryRealization,
        value: ExponenceValue,
        supplements: List<CategoryRandomSupplements>,
        speechPart: TypedSpeechPart,
        categories: List<SourcedCategory>,
        position: Int
    ): CategoryRealization {
        val categoryValues = value.categoryValues.map { it.categoryValue }
        val variants = supplements.map {
            it.specialRealization(categoryValues, speechPart.type, categories)
        }
        val finalVariants = uniteMutualProbabilities(variants) { copy(probability = it) }
            .filter { position == 0 || it.realization != NewWord }

        return finalVariants.randomUnwrappedElement()
            ?: categoryRealization
    }

    private fun randomCategoryApplicator(
        realizationType: CategoryRealization,
        phoneticRestrictions: PhoneticRestrictions,
        semanticsCore: SemanticsCore
    ): Pair<CategoryApplicator, Word?> = when (realizationType) {
        PrefixSeparateWord -> {
            val word = lexisGenerator.generateWord(semanticsCore)
            PrefixWordCategoryApplicator(word) to word
        }
        SuffixSeparateWord -> {
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
        NewWord -> {
            val word = lexisGenerator.generateWord(semanticsCore)
            NewWordCategoryApplicator(word) to word
        }
    }

    fun randomApplicatorsOrder(applicators: ApplicatorMap) = applicators.keys
        .sortedBy { k -> k.categories.joinToString { it.category.outType } }
        .shuffled(RandomSingleton.random)
}


typealias RealizationMapper = (CategoryRealization) -> Double
typealias ApplicatorMap = Map<ExponenceCluster, Map<ExponenceValue, CategoryApplicator>>

data class Result(val words: List<Word>, val solver: ApplicatorMap)

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
