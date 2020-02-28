package shmp.generator

import shmp.containers.PhonemeBase
import shmp.containers.PhonemeImmutableContainer
import shmp.language.*
import shmp.language.categories.Category
import shmp.language.categories.ChangeParadigm
import shmp.language.categories.SpeechPartChangeParadigm
import shmp.language.phonology.*
import shmp.random.randomElementWithProbability
import shmp.random.randomSublist
import java.io.File
import java.text.ParseException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random

class LanguageGenerator(seed: Long) {
    private val random = Random(seed)
    private val phonemeBase = PhonemeBase()
    private val vowelAmount = randomElementWithProbability(VowelQualityAmount.values(), random).amount
    private val consonantAmount = random.nextInt(6, 16)
    private val phonemeContainer = PhonemeImmutableContainer(
        randomSublist(
            phonemeBase.getPhonemesByType(PhonemeType.Vowel),
            random,
            vowelAmount,
            vowelAmount + 1
        ).union(
            randomSublist(
                phonemeBase.getPhonemesByType(PhonemeType.Consonant),
                random,
                consonantAmount,
                consonantAmount + 1
            )
        ).toList()
    )

    private val syllableGenerator = randomSyllableTemplate()
    private val restrictionsParadigm = generateRestrictionParadigm()
    private val lexisGenerator = LexisGenerator(syllableGenerator, restrictionsParadigm, phonemeContainer, random)
    private val changeGenerator = ChangeGenerator(lexisGenerator, random)
    private val categoryGenerator = CategoryGenerator(lexisGenerator, changeGenerator, random)

    fun generateLanguage(wordAmount: Int): Language {
        val stressPattern = randomElementWithProbability(Stress.values(), random)
        val wordOrder = randomElementWithProbability(SovOrder.values(), random)
        val categoriesWithMappers = categoryGenerator.randomCategories()
        val categories = categoriesWithMappers.map { it.first }
        val words = lexisGenerator.generateWords(wordAmount, categories)
        return Language(
            words,
            phonemeContainer,
            stressPattern,
            wordOrder,
            restrictionsParadigm,
            generateChangeParadigm(restrictionsParadigm, categoriesWithMappers)
        )
    }

    private fun generateRestrictionParadigm(): RestrictionsParadigm {//TODO make smth meaningful
        val map = EnumMap<SpeechPart, PhoneticRestrictions>(SpeechPart::class.java)
        val allInitial = syllableGenerator.template.initialPhonemeTypes
            .flatMap { phonemeContainer.getPhonemesByType(it) }
            .toSet()
        val allNucleus = syllableGenerator.template.nucleusPhonemeTypes
            .flatMap { phonemeContainer.getPhonemesByType(it) }
            .toSet()
        val allFinals = syllableGenerator.template.finalPhonemeTypes
            .flatMap { phonemeContainer.getPhonemesByType(it) }
            .toSet()
        for (speechPart in SpeechPart.values()) {
            map[speechPart] = PhoneticRestrictions(allInitial, allNucleus, allFinals)
        }
        return RestrictionsParadigm(map)
    }

    private fun generateChangeParadigm(
        restrictionsParadigm: RestrictionsParadigm,
        categoriesWithMappers: List<Pair<Category, (CategoryRealization) -> Double>>
    ): ChangeParadigm {
        val categories = categoriesWithMappers.map { it.first }
        return ChangeParadigm(
            categories,
            SpeechPart.values().map { speechPart ->
                val speechPartCategoriesWithMappers = categoriesWithMappers
                    .filter { it.first.affectedSpeechParts.contains(speechPart) }
                    .filter { it.first.values.isNotEmpty() }
                val applicators = categoryGenerator.randomApplicatorsForSpeechPart(
                    speechPart,
                    restrictionsParadigm.restrictionsMapper.getValue(speechPart),
                    speechPartCategoriesWithMappers
                )
                val orderedApplicators = categoryGenerator.randomApplicatorsOrder(applicators)
                speechPart to SpeechPartChangeParadigm(
                    speechPart,
                    orderedApplicators,
                    applicators
                )
            }.toMap()
        )
    }

    private fun randomSyllableTemplate(): SyllableValenceGenerator {
        val syllableTemplates = HashMap<SyllableValenceTemplate, Double>()
        File("SupplementFiles/SyllableTypes").forEachLine {
            if (!it.isBlank()) {
                val (template, syllableProbability) = it.split(" +".toRegex())
                val valencies = ArrayList<ValencyPlace>()
                var i = 0
                while (i < template.length) {
                    var char = template[i]
                    var probability = 1.0
                    if (char == '(') {
                        if (template[i + 2] != ')')
                            throw ParseException("Wrong syntax for syllable templates: no ) found", i + 2)
                        char = template[i + 1]
                        probability = 0.5
                        i += 3
                    } else
                        i++
                    valencies.add(
                        ValencyPlace(
                            char.toPhonemeType(),
                            probability
                        )
                    )
                }
                syllableTemplates[SyllableValenceTemplate(valencies)] = syllableProbability.toDouble()
            }
        }
        return SyllableValenceGenerator(
            randomElementWithProbability(
                syllableTemplates.keys,
                { syllableTemplates[it] ?: 0.0 },
                random
            )
        )
    }
}


