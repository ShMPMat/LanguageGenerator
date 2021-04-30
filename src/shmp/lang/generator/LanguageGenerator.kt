package shmp.lang.generator

import shmp.lang.containers.PhonemeBase
import shmp.lang.language.Language
import shmp.lang.language.NumeralSystemBase
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.category.CategoryPool
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.lexis.toAdnominal
import shmp.lang.language.lexis.toUnspecified
import shmp.lang.language.phonology.*
import shmp.lang.language.phonology.prosody.StressType
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElement
import java.io.File
import java.text.ParseException
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
    private val restrictionsParadigm = generateRestrictionParadigm(
        SpeechPart.values().map { it.toUnspecified() } + listOf(DeixisPronoun.toAdnominal())
    )
    private val stressPattern = StressType.values().randomElement()
    private val lexisGenerator = LexisGenerator(
        supplementPath,
        syllableGenerator,
        restrictionsParadigm,
        phonemeContainer,
        stressPattern,
        random
    )
    private val changeGenerator = ChangeGenerator(lexisGenerator)
    private val categoryGenerator = CategoryGenerator()
    private val changeParadigmGenerator = ChangeParadigmGenerator(
        stressPattern,
        lexisGenerator,
        changeGenerator,
        restrictionsParadigm,
        random
    )

    fun generateLanguage(wordAmount: Int): Language {
        val numeralSystemBase = NumeralSystemBase.values().randomElement()
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

    private fun generateRestrictionParadigm(speechParts: List<TypedSpeechPart>): RestrictionsParadigm {//TODO make smth meaningful
        val generalAvgWordLength = 5

        val map = mutableMapOf<TypedSpeechPart, PhoneticRestrictions>()
        val allInitial = syllableGenerator.template.initialPhonemeTypes
            .flatMap { phonemeContainer.getPhonemesByType(it) }
            .toSet()
        val allNucleus = syllableGenerator.template.nucleusPhonemeTypes
            .flatMap { phonemeContainer.getPhonemesByType(it) }
            .toSet()
        val allFinals = syllableGenerator.template.finalPhonemeTypes
            .flatMap { phonemeContainer.getPhonemesByType(it) }
            .toSet()

        for (speechPart in speechParts) {
            val actualAvgWordLength =
                if (speechPart.type in listOf(Article, Particle, Adposition, PersonalPronoun, DeixisPronoun))
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
            syllableTemplates.keys
                .sortedBy { it.toString() }
                .randomElement { syllableTemplates[it] ?: 0.0 }
        )
    }
}
