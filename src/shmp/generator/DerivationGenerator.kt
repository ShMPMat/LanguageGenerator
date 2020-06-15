package shmp.generator

import shmp.containers.DerivationClusterTemplate
import shmp.containers.SemanticsCoreTemplate
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

class DerivationGenerator(
    private val restrictionsParadigm: RestrictionsParadigm,
    private val random: Random,
    private val injectors: List<DerivationInjector> = defaultInjectors
) {
    internal fun injectDerivationOptions(words: List<SemanticsCoreTemplate>): List<SemanticsCoreTemplate> {
        if (words.isEmpty())
            return emptyList()

        val newWords = injectors.flatMap { inj ->
            words.mapNotNull { inj.injector(it) }
        }

        return newWords + injectDerivationOptions(newWords)
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

data class DerivationInjector(val injector: (SemanticsCoreTemplate) -> SemanticsCoreTemplate?)

val defaultInjectors = listOf(
    DerivationInjector {
        if (it.speechPart != SpeechPart.Noun || it.derivationClusterTemplate.internalTypes.contains(DerivationType.Smallness))
            return@DerivationInjector null
        val link = DerivationLink(
            SemanticsCoreTemplate(
                "little_" + it.word,
                SpeechPart.Noun,
                it.tagClusters,
                DerivationClusterTemplate(
                    internalTypes = it.derivationClusterTemplate.internalTypes + setOf(DerivationType.Smallness)
                )
            ),
            1.0
        )
        it.derivationClusterTemplate.typeToCore[DerivationType.Smallness] = link
        return@DerivationInjector link.template
    }
)