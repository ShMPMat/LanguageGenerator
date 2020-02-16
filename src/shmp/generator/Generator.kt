package shmp.generator

import shmp.containers.PhonemeBase
import shmp.containers.PhonemeImmutableContainer
import shmp.containers.WordBase
import shmp.language.*
import shmp.language.morphem.*
import shmp.language.nominal_categories.*
import shmp.language.nominal_categories.change.AffixCategoryApplicator
import shmp.language.nominal_categories.change.CategoryApplicator
import shmp.language.nominal_categories.change.PrefixWordCategoryApplicator
import shmp.language.nominal_categories.change.SuffixWordCategoryApplicator
import java.io.File
import java.text.ParseException
import kotlin.random.Random

class Generator(seed: Long) {
    private val random = Random(seed)
    private val phonemeBase = PhonemeBase()
    private val wordBase = WordBase()
    private val vowelAmount =
        randomElementWithProbability(VowelQualityAmount.values(), { it.probability }, random).amount
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
    private val syllableTemplate = randomSyllableTemplate()

    fun generateLanguage(wordAmount: Int): Language {
        val words = ArrayList<Word>()
        val cores = randomSublist(wordBase.words, random, 10, 11)
        for (i in 0 until wordAmount) {
            words.add(randomWord(cores[i]))
        }

        return Language(
            words,
            phonemeContainer,
            randomElementWithProbability(Stress.values(), { it.probability }, random),
            randomElementWithProbability(SovOrder.values(), { it.probability }, random),
            listOf(randomArticles(), randomGender())
        )
    }

    private fun randomArticles() = Articles(
        randomCategoryApplicatorsForNominalCategory(
            randomElementWithProbability(ArticlePresence.values(), { it.probability }, random).presentArticles,
            NominalCategoryRealization::probabilityForArticle
        )
    )

    private fun randomGender() = Gender(
        randomCategoryApplicatorsForNominalCategory(
            randomElementWithProbability(GenderPresence.values(), { it.probability }, random).possibilities,
            NominalCategoryRealization::probabilityForGender
        )
    )

    private fun randomCategoryApplicatorsForNominalCategory(
        presentElements: Set<NominalCategoryEnum>,
        mapper: (NominalCategoryRealization) -> Double
    ): Map<NominalCategoryEnum, CategoryApplicator> {

        val map = HashMap<NominalCategoryEnum, CategoryApplicator>()
        val realizationType = randomElementWithProbability(
            NominalCategoryRealization.values(),
            mapper,
            random
        )
        presentElements.forEach {
            map[it] =
                randomCategoryApplicator(realizationType, it.syntaxCore)
        }
        return map
    }

    private fun randomCategoryApplicator(
        realizationType: NominalCategoryRealization,
        syntaxCore: SyntaxCore
    ): CategoryApplicator = when (realizationType) {
        NominalCategoryRealization.PrefixSeparateWord -> PrefixWordCategoryApplicator(randomWord(
            syntaxCore,
            maxSyllableLength = 3,
            lengthWeight = { ((3 * 3 + 1 - it * it) * (3 * 3 + 1 - it * it)).toDouble() }
        ))
        NominalCategoryRealization.SuffixSeparateWord -> SuffixWordCategoryApplicator(randomWord(
            syntaxCore,
            maxSyllableLength = 3,
            lengthWeight = { ((3 * 3 + 1 - it * it) * (3 * 3 + 1 - it * it)).toDouble() }
        ))
        NominalCategoryRealization.Prefix -> {
            val changes = generateChanges(Position.Beginning, false)
            AffixCategoryApplicator(
                Prefix(TemplateWordChange(changes.map { TemplateChange(Position.Beginning, it.first, it.second) })),
                NominalCategoryRealization.Prefix
            )
        }
        NominalCategoryRealization.Suffix -> {
            val changes = generateChanges(Position.End, true)
            AffixCategoryApplicator(
                Suffix(TemplateWordChange(changes.map { TemplateChange(Position.End, it.first, it.second) })),
                NominalCategoryRealization.Suffix
            )
        }
    }

    private fun generateChanges(
        position: Position,
        isClosed: Boolean
    ): List<Pair<List<PositionTemplate>, List<PositionSubstitution>>> {
        val getSyllableSubstitution = { c: Boolean, i: Boolean ->
            syllableTemplate.generateSyllable(phonemeContainer, random, isClosed = c, shouldHaveInitial = i).phonemes
                .map { PhonemePositionSubstitution(it) }
        }
        val result = when (randomElementWithProbability(AffixTypes.values(), { it.probability }, random)) {
            AffixTypes.UniversalAffix -> {
                listOf(
                    listOf<PositionTemplate>() to getSyllableSubstitution(isClosed, false)
                )
            }
            AffixTypes.PhonemeTypeAffix -> {
                val addPasser = { list: List<PositionSubstitution>, sub: PositionSubstitution ->
                    when (position) {
                        Position.Beginning -> list + listOf(sub)
                        Position.End -> listOf(sub) + list
                    }
                }
                PhonemeType.values().map {
                    listOf(TypePositionTemplate(it)) to addPasser(
                        getSyllableSubstitution(
                            isClosed || it == PhonemeType.Vowel,
                            it == PhonemeType.Vowel && position == Position.End
                        ),
                        PassingPositionSubstitution()
                    )
                }
            }
        }
        return result
    }

    private fun randomWord(
        core: SyntaxCore,
        maxSyllableLength: Int = 4,
        lengthWeight: (Int) -> Double = { (maxSyllableLength * maxSyllableLength + 1 - it * it).toDouble() }
    ): Word {
        val syllables = ArrayList<Syllable>()
        val length = getRandomWordLength(maxSyllableLength, lengthWeight)
        for (j in 0..length)
            syllables.add(
                syllableTemplate.generateSyllable(
                    phonemeContainer,
                    random,
                    isClosed = j == length,
                    prefix = syllables
                )
            )
        return Word(syllables, syllableTemplate, core)
    }

    private fun randomSyllableTemplate(): SyllableTemplate {
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
                    valencies.add(ValencyPlace(char.toPhonemeType(), probability))
                }
                syllableTemplates[SyllableValenceTemplate(valencies)] = syllableProbability.toDouble()
            }
        }
        return randomElementWithProbability(syllableTemplates.keys, { syllableTemplates[it] ?: 0.0 }, random)
    }

    private fun getRandomWordLength(max: Int, lengthWeight: (Int) -> Double) =
        randomElementWithProbability((1..max), lengthWeight, random)
}

enum class AffixTypes(val probability: Double) {
    UniversalAffix(100.0),
    PhonemeTypeAffix(50.0)
}


