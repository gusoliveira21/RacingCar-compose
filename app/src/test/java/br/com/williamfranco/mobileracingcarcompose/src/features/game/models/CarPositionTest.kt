package br.com.williamfranco.mobileracingcarcompose.src.features.game.models

import org.junit.Assert.assertEquals
import org.junit.Test

class CarPositionTest {

    @Test
    fun fromLeftOffsetIndex_returnsExpectedValues() {
        assertEquals(0f, CarPosition.Left.fromLeftOffsetIndex())
        assertEquals(1f, CarPosition.Middle.fromLeftOffsetIndex())
        assertEquals(2f, CarPosition.Right.fromLeftOffsetIndex())
    }
}
