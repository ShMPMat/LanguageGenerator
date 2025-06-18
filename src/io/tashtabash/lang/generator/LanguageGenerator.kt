package io.tashtabash.lang.generator

import io.tashtabash.lang.containers.PhonemePool
import io.tashtabash.lang.generator.phoneme.PhonemeGenerator
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.lang.language.category.CategoryPool
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.lexis.toAdnominal
import io.tashtabash.lang.language.lexis.toDefault
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.randomElement
import java.io.File
import java.text.ParseException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class LanguageGenerator(private val supplementPath: String) {
    val phonemePool = PhonemePool(supplementPath)
    val phonemeGenerator = PhonemeGenerator(phonemePool)
    private val phonemeContainer = phonemeGenerator.generate()

    private val syllableGenerator = randomSyllableGenerator()
    private val restrictionsParadigm = generateRestrictionParadigm(
        entries.map { it.toDefault() } + DeixisPronoun.toAdnominal()
    )
    private val stressPattern = StressType.entries.randomElement()
    private val lexisGenerator = LexisGenerator(
        supplementPath,
        syllableGenerator,
        restrictionsParadigm,
        phonemeContainer,
        stressPattern,
        RandomSingleton.random
    )
    private val changeGenerator = ChangeGenerator(lexisGenerator)
    private val categoryGenerator = CategoryGenerator()
    private val changeParadigmGenerator = ChangeParadigmGenerator(
        stressPattern,
        lexisGenerator,
        changeGenerator,
        restrictionsParadigm
    )

    fun generateLanguage(wordAmount: Int): Language {
        val categoriesWithMappers = categoryGenerator.randomCategories()
        val categories = CategoryPool(categoriesWithMappers.map { it.first })
        var changeParadigm = changeParadigmGenerator.generateChangeParadigm(categoriesWithMappers)
        val derivationParadigm = lexisGenerator.derivationGenerator.generateDerivationParadigm(
            changeGenerator,
            categories
        )
        val lexis = lexisGenerator.generateLexis(
            wordAmount + changeParadigmGenerator.numeralParadigmGenerator.numeralTemplates.size,
            categories,
            changeParadigm.syntaxParadigm,
            changeParadigmGenerator.numeralParadigmGenerator.numeralTemplates
        )

        changeParadigm = changeGenerator.injectIrregularity(changeParadigm, lexis)

        return Language(
            lexis,
            phonemeContainer,
            stressPattern,
            restrictionsParadigm,
            derivationParadigm,
            changeParadigm
        )
    }

    private fun generateRestrictionParadigm(speechParts: List<TypedSpeechPart>): RestrictionsParadigm {//TODO make smth meaningful
        val generalAvgWordLength = 5
        val syllable = syllableGenerator.template

        val map = mutableMapOf<TypedSpeechPart, PhoneticRestrictions>()
        val allInitial = syllableGenerator.template.initialPhonemeTypes
            .flatMap { phonemeContainer.getPhonemes(it) }
            .toSet()
        val allNucleus = syllableGenerator.template.nucleusPhonemeTypes
            .flatMap { phonemeContainer.getPhonemes(it) }
            .toSet()
        val allFinals = syllableGenerator.template.finalPhonemeTypes
            .flatMap { phonemeContainer.getPhonemes(it) }
            .toSet()

        for (speechPart in speechParts) {
            val actualAvgWordLength =
                if (speechPart.type !in listOf(Article, Particle, Adposition, PersonalPronoun, DeixisPronoun))
                    generalAvgWordLength
                else 2

            map[speechPart] = PhoneticRestrictions(syllable, actualAvgWordLength, allInitial, allNucleus, allFinals)
        }

        return RestrictionsParadigm(map)
    }

    private fun randomSyllableGenerator(): SyllableValenceGenerator {
        val syllableTemplates = HashMap<SyllableValenceTemplate, Double>()
        File("$supplementPath/SyllableTypes").forEachLine {
            if (it.isNotBlank()) {
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

                    valencies += ValencyPlace(char.toPhonemeType(), probability)
                }
                syllableTemplates[SyllableValenceTemplate(valencies)] = syllableProbability.toDouble()
            }
        }

        val finalTemplate = syllableTemplates.keys.sortedBy { it.toString() }
            .randomElement { syllableTemplates[it] ?: 0.0 }

        return SyllableValenceGenerator(finalTemplate)
    }
}
