package shmp.lang.generator

import shmp.lang.containers.PhonemeBase
import shmp.lang.language.Language
import shmp.lang.language.NumeralSystemBase
import shmp.lang.language.SpeechPart
import shmp.lang.language.SpeechPart.*
import shmp.lang.language.category.CategoryPool
import shmp.lang.language.phonology.PhoneticRestrictions
import shmp.lang.language.phonology.RestrictionsParadigm
import shmp.lang.language.phonology.SyllableValenceTemplate
import shmp.lang.language.phonology.ValencyPlace
import shmp.lang.language.phonology.prosody.StressType
import shmp.lang.language.syntax.SyntaxLogic
import shmp.lang.language.toPhonemeType
import shmp.random.randomElement
import shmp.random.singleton.RandomSingleton
import java.io.File
import java.text.ParseException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random


class LanguageGenerator(val supplementPath: String, seed: Long) {
    private val random = Random(seed)

    init {
        RandomSingleton.safeRandom = random
    }

    private val phonemeBase = PhonemeBase(supplementPath)
    private val phonemeGenerator = PhonemeGenerator(phonemeBase)
    private val phonemeContainer = phonemeGenerator.generate()

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
        val lexis = lexisGenerator.generateLexis(wordAmount, categories, changeParadigm.syntaxParadigm)

        return Language(
            lexis,
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
