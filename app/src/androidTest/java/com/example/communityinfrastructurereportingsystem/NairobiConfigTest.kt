package com.example.communityinfrastructurereportingsystem

import com.example.communityinfrastructurereportingsystem.data.model.NairobiConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NairobiConfigTest {

    @Test
    fun nairobiCbd_isInsideNairobi() {
        assertTrue(NairobiConfig.isInsideNairobi(-1.2864, 36.8172))
    }

    @Test
    fun mombasa_isOutsideNairobi() {
        assertFalse(NairobiConfig.isInsideNairobi(-4.0435, 39.6682))
    }

    @Test
    fun kisumu_isOutsideNairobi() {
        assertFalse(NairobiConfig.isInsideNairobi(-0.0917, 34.7680))
    }

    @Test
    fun nairobiHasSeventeenSubCounties() {
        assertEquals(17, NairobiConfig.subCounties.size)
    }
}