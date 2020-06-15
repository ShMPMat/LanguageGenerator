package shmp.generator

import shmp.containers.DerivationClusterTemplate
import shmp.containers.SemanticsCoreTemplate
import shmp.containers.WordBase
import shmp.language.SpeechPart
import shmp.language.derivation.Derivation
import shmp.language.derivation.DerivationClass
import shmp.language.derivation.DerivationType
import shmp.language.lexis.DerivationLink
import shmp.language.lexis.Word
import shmp.language.morphem.Prefix
import shmp.language.morphem.Suffix
import shmp.language.morphem.change.Position
import shmp.language.phonology.RestrictionsParadigm
import shmp.random.randomSublist
import kotlin.random.Random

class DerivationGenerator(val restrictionsParadigm: RestrictionsParadigm, val random: Random) {
    internal fun injectDerivationOptions(wordBase: WordBase): WordBase {//TODO special commands
        val goodWords = wordBase.baseWords
            .filter { it.speechPart == SpeechPart.Noun }

        goodWords.forEach {
            val link = DerivationLink(
                SemanticsCoreTemplate(
                    "little_" + it.word,
                    SpeechPart.Noun,
                    it.tagClusters,
                    DerivationClusterTemplate()
                ),
                1.0
            )
            it.derivationClusterTemplate.typeToCore[DerivationType.Smallness] = link
            wordBase.allWords.add(link.template)
        }

        return wordBase
    }

    internal fun makeDerivations(words: MutableList<Word>, changeGenerator: ChangeGenerator) {
        val derivations =
            randomSublist(DerivationClass.values().toList(), random, 0, DerivationClass.values().size + 1)
                .map {
                    val affix = if (random.nextBoolean()) {
                        Prefix(
                            changeGenerator.generateChanges(
                                Position.Beginning,
                                restrictionsParadigm.restrictionsMapper.getValue(it.speechPart)
                            )
                        )
                    } else {
                        Suffix(
                            changeGenerator.generateChanges(
                                Position.End,
                                restrictionsParadigm.restrictionsMapper.getValue(it.speechPart)
                            )
                        )
                    }
                    Derivation(affix, it)
                }

        var i = 0
        while (i < words.size) {
            val word = words[i]
            for (derivation in derivations) {
                val derivedWord = derivation.derive(word, random)
                    ?: continue
                words.add(derivedWord)
            }
            i++
        }
    }
}