package io.tashtabash.lang.language.lexis

import io.tashtabash.lang.utils.composeUniquePairs
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


internal class ConnotationsTest {
    @ParameterizedTest(name = "{0} distance {1} = {1} distance {0}")
    @MethodSource("connotationsProvider")
    fun `distance() is commutative`(first: Connotations, second: Connotations) {
        Assertions.assertEquals(
            first distance second,
            second distance first
        )
    }

    @ParameterizedTest(name = "{0} closeness {1} = {1} closeness {0}")
    @MethodSource("connotationsProvider")
    fun `closeness() is commutative`(first: Connotations, second: Connotations) {
        Assertions.assertEquals(
            first closeness second,
            second closeness first
        )
    }

    @Test
    fun `distance() returns 0 when the Connotations are equal`() {
        val connotations = Connotations(
            Connotation("c1", 1.0),
            Connotation("c2", 0.8),
            Connotation("c3", 0.1)
        )

        assertEquals(0.0, connotations distance connotations)
    }

    @Test
    fun `closeness() returns 1 when the Connotations are equal`() {
        val connotations = Connotations(
            Connotation("c2", 0.8),
            Connotation("c3", 0.1)
        )

        assertEquals(1.0, connotations closeness connotations)
    }

    @Test
    fun `distance() returns 1 when the Connotations are nothing alike`() {
        val leftConnotations = Connotations(
            Connotation("c1", 1.0),
            Connotation("c2", 0.8),
            Connotation("c3", 0.1)
        )
        val rightConnotations = Connotations(
            Connotation("c4", 1.0),
            Connotation("c5", 0.8)
        )

        assertEquals(1.0, leftConnotations distance rightConnotations)
    }

    @Test
    fun `closeness() returns 0 when the Connotations are nothing alike`() {
        val leftConnotations = Connotations(
            Connotation("c1", 1.0),
            Connotation("c3", 0.1)
        )
        val rightConnotations = Connotations(
            Connotation("c4", 1.0),
            Connotation("c5", 0.8),
            Connotation("c2", 0.8)
        )

        assertEquals(0.0, leftConnotations closeness rightConnotations)
    }

    @Test
    fun `distance() returns 1 when one of the Connotations is empty`() {
        val leftConnotations = Connotations(
            Connotation("c1", 1.0),
            Connotation("c2", 0.8),
            Connotation("c3", 0.1)
        )
        val rightConnotations = Connotations()

        assertEquals(1.0, leftConnotations distance rightConnotations)
    }

    @Test
    fun `closeness() returns 0 when one of the Connotations is empty`() {
        val leftConnotations = Connotations()
        val rightConnotations = Connotations(
            Connotation("c4", 1.0),
            Connotation("c5", 0.8),
            Connotation("c2", 0.8)
        )

        assertEquals(0.0, leftConnotations closeness rightConnotations)
    }

    @Test
    fun `distance() != 0 and != 1 when the Connotations have an overlap`() {
        val leftConnotations = Connotations(
            Connotation("c1", 1.0),
            Connotation("c3", 0.1)
        )
        val rightConnotations = Connotations(
            Connotation("c1", 0.1),
            Connotation("c5", 0.8),
            Connotation("c2", 0.8)
        )

        assertNotEquals(0.0, leftConnotations distance rightConnotations)
        assertNotEquals(1.0, leftConnotations distance rightConnotations)
    }

    @Test
    fun `closeness() != 0 and != 1 when the Connotations have an overlap`() {
        val leftConnotations = Connotations(
            Connotation("c1", 1.0),
            Connotation("c3", 0.1)
        )
        val rightConnotations = Connotations(
            Connotation("c1", 0.1),
            Connotation("c5", 0.8),
            Connotation("c2", 0.8)
        )

        assertNotEquals(0.0, leftConnotations closeness rightConnotations)
        assertNotEquals(1.0, leftConnotations closeness rightConnotations)
    }

    @Test
    fun `distance() is bigger when the strengths are farther apart`() {
        val mainConnotations = Connotations(
            Connotation("c1", 0.5),
        )
        val closerConnotations = Connotations(
            Connotation("c1", 0.4),
        )
        val fartherConnotations = Connotations(
            Connotation("c1", 0.9),
        )

        assert(mainConnotations distance closerConnotations < mainConnotations distance fartherConnotations)
    }

    @Test
    fun `closeness() is smaller when the strengths are farther apart`() {
        val mainConnotations = Connotations(
            Connotation("c1", 0.5),
        )
        val closerConnotations = Connotations(
            Connotation("c1", 0.4),
        )
        val fartherConnotations = Connotations(
            Connotation("c1", 0.9),
        )

        assert(mainConnotations closeness closerConnotations > mainConnotations closeness fartherConnotations)
    }

    companion object {
        @JvmStatic
        fun connotationsProvider(): Stream<Arguments> {
            val matchers = listOf(
                Connotations(Connotation("c1", 0.5)),
                Connotations(Connotation("c1", 0.4)),
                Connotations(Connotation("c1", 0.9)),
                Connotations(Connotation("c1", 1.0), Connotation("c2", 0.8)),
                Connotations(Connotation("c4", 1.0), Connotation("c5", 0.8)),
                Connotations(Connotation("c1", 1.0), Connotation("c5", 0.8)),
                Connotations(Connotation("c4", 0.3))
            )

            return composeUniquePairs(matchers, matchers)
                .map { (f, s) -> Arguments.of(f, s) }
                .stream()
        }
    }
}