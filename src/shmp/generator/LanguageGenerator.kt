package shmp.generator

import shmp.containers.PhonemeBase
import shmp.containers.PhonemeImmutableContainer
import shmp.language.*
import shmp.language.SpeechPart.*
import shmp.language.category.CategoryPool
import shmp.language.phonology.PhoneticRestrictions
import shmp.language.phonology.RestrictionsParadigm
import shmp.language.phonology.SyllableValenceTemplate
import shmp.language.phonology.ValencyPlace
import shmp.language.phonology.prosody.StressType
import shmp.random.randomElement
import shmp.random.randomSublist
import java.io.File
import java.text.ParseException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random


class LanguageGenerator(val supplementPath: String, seed: Long) {
    private val random = Random(seed)

    private val phonemeBase = PhonemeBase(supplementPath)
    private val vowelAmount = randomElement(VowelQualityAmount.values(), random).amount
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

    private val syllableGenerator = randomSyllableGenerator()
    private val restrictionsParadigm = generateRestrictionParadigm()
    private val stressPattern = randomElement(StressType.values(), random)
    private val lexisGenerator = LexisGenerator(
        supplementPath,
        syllableGenerator,
        restrictionsParadigm,
        phonemeContainer,
        stressPattern,
        random
    )
    private val changeGenerator = ChangeGenerator(lexisGenerator, random)
    private val categoryGenerator = CategoryGenerator(random)
    private val changeParadigmGenerator = ChangeParadigmGenerator(
        stressPattern,
        lexisGenerator,
        changeGenerator,
        restrictionsParadigm,
        random
    )

    fun generateLanguage(wordAmount: Int): Language {
        val numeralSystemBase = randomElement(NumeralSystemBase.values(), random)
        val categoriesWithMappers = categoryGenerator.randomCategories()
        val categories = CategoryPool(categoriesWithMappers.map { it.first })
        val changeParadigm = changeParadigmGenerator.generateChangeParadigm(categoriesWithMappers)
        val derivationParadigm = lexisGenerator.derivationGenerator.generateDerivationParadigm(
            changeGenerator,
            categories
        )
        val words = lexisGenerator.generateWords(wordAmount, categories)
        return Language(
            words,
            phonemeContainer,
            stressPattern,
            numeralSystemBase,
            restrictionsParadigm,
            derivationParadigm,
            changeParadigm
        )
    }

    private fun generateRestrictionParadigm(): RestrictionsParadigm {//TODO make smth meaningful
        val generalAvgWordLength = 5

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

        for (speechPart in values()) {
            val actualAvgWordLength =
                if (speechPart in listOf(Article, Particle, Pronoun))
                    2
                else generalAvgWordLength

            map[speechPart] = PhoneticRestrictions(actualAvgWordLength, allInitial, allNucleus, allFinals)
        }

        return RestrictionsParadigm(map)
    }

    private fun randomSyllableGenerator(): SyllableValenceGenerator {
        val syllableTemplates = HashMap<SyllableValenceTemplate, Double>()
        File("$supplementPath/SyllableTypes").forEachLine {
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
                        probability = 0.4
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
            randomElement(
                syllableTemplates.keys.sortedBy { it.toString() },
                { syllableTemplates[it] ?: 0.0 },
                random
            )
        )
    }
}
