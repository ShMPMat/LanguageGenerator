package shmp.generator

import shmp.language.derivation.Derivation
import shmp.language.derivation.DerivationClass
import shmp.language.lexis.Word
import shmp.language.morphem.Prefix
import shmp.language.morphem.Suffix
import shmp.language.morphem.change.Position
import shmp.language.phonology.RestrictionsParadigm
import shmp.random.randomSublist
import kotlin.random.Random

class DerivationGenerator(val restrictionsParadigm: RestrictionsParadigm, val random: Random) {
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