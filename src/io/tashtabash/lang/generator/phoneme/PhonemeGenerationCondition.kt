package io.tashtabash.lang.generator.phoneme

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.random.singleton.testProbability


interface PhonemeGenerationCondition {
    fun run(immutablePhonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer?
}


open class SimplePhonemeGenerationCondition(
    private val applicator: GenerationApplicator,
    private val condition: (ImmutablePhonemeContainer) -> Boolean
): PhonemeGenerationCondition {
    override fun run(immutablePhonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer? {
        if (condition(immutablePhonemeContainer))
            return applicator.apply(immutablePhonemeContainer)
        return null
    }
}

class LoopPhonemeGenerationCondition(
    private val phonemeGenerationCondition: PhonemeGenerationCondition,
    private val stopCondition: ((ImmutablePhonemeContainer) -> Boolean)?
): PhonemeGenerationCondition {
    override fun run(immutablePhonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer {
        var resultPhonemeContainer = immutablePhonemeContainer

        while (stopCondition?.invoke(resultPhonemeContainer) != false) {
            resultPhonemeContainer = phonemeGenerationCondition.run(resultPhonemeContainer)
                ?: break
        }

        return resultPhonemeContainer
    }
}


fun GenerationApplicator.withCondition(condition: (ImmutablePhonemeContainer) -> Boolean) =
    SimplePhonemeGenerationCondition(this, condition)

fun GenerationApplicator.withProbability(probabilityFun: (ImmutablePhonemeContainer) -> Double) =
    withCondition { probabilityFun(it).testProbability() }

fun PhonemeGenerationCondition.repeat(stopCondition: ((ImmutablePhonemeContainer) -> Boolean)?) =
    LoopPhonemeGenerationCondition(this, stopCondition)


