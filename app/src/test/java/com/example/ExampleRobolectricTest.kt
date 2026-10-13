package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BranchSettingsEntity
import com.example.data.model.TreatmentEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches SPA Session Manager`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SPA Session Manager", appName)
    }

    @Test
    fun `treatment multi bundle adds total duration plus steam interval`() {
        val treatments = listOf(
            TreatmentEntity("tr_1", "branch_1", "Deep Tissue", durationMinutes = 60),
            TreatmentEntity("tr_2", "branch_1", "Botanical Facial", durationMinutes = 45)
        )
        val hasSteamBetween = true
        val settings = BranchSettingsEntity(branchId = "branch_1", steamBetweenExtraMinutes = 15)

        val baseMinutes = treatments.sumOf { it.durationMinutes }
        val extraSteam = if (hasSteamBetween) settings.steamBetweenExtraMinutes else 0
        val totalMinutes = baseMinutes + extraSteam

        assertEquals(105, baseMinutes)
        assertEquals(120, totalMinutes)
    }

    @Test
    fun `card prefix and numbering range formatting`() {
        val settings = BranchSettingsEntity(
            branchId = "branch_1",
            cardPrefix = "SP-",
            cardStartNumber = 101,
            cardEndNumber = 200
        )
        val firstCard = "${settings.cardPrefix}${settings.cardStartNumber}"
        assertEquals("SP-101", firstCard)
        assertTrue(settings.cardEndNumber > settings.cardStartNumber)
    }
}
