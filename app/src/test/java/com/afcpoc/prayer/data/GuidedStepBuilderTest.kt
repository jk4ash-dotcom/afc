package com.afcpoc.prayer.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test

class GuidedStepBuilderTest {

    companion object {
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }
        lateinit var rosary: RosaryContent
        lateinit var divineMercy: DivineMercyContent

        @JvmStatic
        @BeforeClass
        fun load() {
            rosary = json.decodeFromString(readResource("rosary.json"))
            divineMercy = json.decodeFromString(readResource("divine_mercy.json"))
        }

        private fun readResource(name: String): String {
            val stream = GuidedStepBuilderTest::class.java.classLoader!!
                .getResourceAsStream(name)
                ?: error("Missing test resource $name (expected via sourceSets assets)")
            return stream.bufferedReader().use { it.readText() }
        }
    }

    @Test
    fun rosary_joyful_hasOpeningThenFiveDecadesThenClosing() {
        val steps = GuidedStepBuilder.buildRosarySteps(rosary, "Joyful", includeAfterRosary = false)
        assertTrue("expected substantial step list, got ${steps.size}", steps.size >= 70)
        assertTrue(steps.first().title.contains("Sign", ignoreCase = true) ||
            steps[0].subtitle == "Opening")

        // Opening block before first decade announce
        val firstAnnounce = steps.indexOfFirst { it.title.startsWith("Announce:") }
        assertTrue(firstAnnounce > 0)
        assertTrue(steps.take(firstAnnounce).all { it.subtitle == "Opening" || it.subtitle?.startsWith("For ") == true })

        // Five decades in order
        for (d in 1..5) {
            assertTrue(
                "missing decade $d",
                steps.any { it.subtitle == "Decade $d of 5 — Joyful" }
            )
        }

        // First decade has Hail Mary 1..10 in order
        val d1 = steps.filter { it.subtitle == "Decade 1 of 5 — Joyful" }
        val hail = d1.filter { it.title.startsWith("Hail Mary") }
        assertEquals(10, hail.size)
        hail.forEachIndexed { i, step ->
            assertEquals("Hail Mary (${i + 1} of 10)", step.title)
            assertEquals("Bead ${i + 1}/10", step.progressLabel)
        }

        // Closing after decades
        assertTrue(steps.any { it.subtitle == "Closing" })
        assertTrue(steps.indexOfLast { it.subtitle?.startsWith("Decade") == true } <
            steps.indexOfFirst { it.subtitle == "Closing" })
    }

    @Test
    fun rosary_afterRosary_increasesCount_whenEnabled() {
        val without = GuidedStepBuilder.buildRosarySteps(rosary, "Glorious", false)
        val with = GuidedStepBuilder.buildRosarySteps(rosary, "Glorious", true)
        assertTrue(with.size > without.size)
        assertTrue(with.any { it.subtitle == "AFC after-Rosary" })
        assertFalse(without.any { it.subtitle == "AFC after-Rosary" })
    }

    @Test
    fun rosary_allMysterySets_nonEmpty() {
        listOf("Joyful", "Sorrowful", "Glorious", "Luminous").forEach { name ->
            val steps = GuidedStepBuilder.buildRosarySteps(rosary, name, false)
            assertTrue("$name empty", steps.isNotEmpty())
            assertEquals(5, steps.mapNotNull { it.subtitle }.filter { it.startsWith("Decade") }.map {
                it.substringAfter("Decade ").substringBefore(" of")
            }.toSet().size)
        }
    }

    @Test
    fun chaplet_core_hasFiveDecades_ofTenBeads() {
        val steps = GuidedStepBuilder.buildChapletSteps(
            divineMercy,
            includeOptionalOpenings = false,
            includeOptionalClosings = false
        )
        assertTrue("expected ~60+ steps, got ${steps.size}", steps.size >= 55)
        for (d in 1..5) {
            val decade = steps.filter { it.subtitle == "Decade $d of 5" }
            val beads = decade.filter { it.progressLabel != null }
            assertEquals("decade $d bead count", 10, beads.size)
            beads.forEachIndexed { i, step ->
                assertEquals("Bead ${i + 1}/10", step.progressLabel)
                assertTrue(step.title.contains("sorrowful Passion", ignoreCase = true))
            }
        }
        assertFalse(steps.any { it.title.contains("Optional Opening", ignoreCase = true) })
        assertFalse(steps.any { it.subtitle == "Optional opening" })
        assertFalse(steps.any { it.subtitle == "Optional closing" })
    }

    @Test
    fun chaplet_optionalOpenClose_addDistinctSteps_withoutDuplicateTitles() {
        val core = GuidedStepBuilder.buildChapletSteps(divineMercy, false, false)
        val full = GuidedStepBuilder.buildChapletSteps(divineMercy, true, true)
        assertTrue(full.size > core.size)

        // Short bead openings + longer Faustina opening when enabled
        val optionalTitles = full.filter {
            it.title.contains("Optional Opening", ignoreCase = true) ||
                it.subtitle == "Optional opening"
        }.map { it.title }
        assertTrue("expected optional openings, got $optionalTitles", optionalTitles.isNotEmpty())
        assertEquals(
            "duplicate optional titles: $optionalTitles",
            optionalTitles.size,
            optionalTitles.distinctBy { it.lowercase() }.size
        )

        // Allow intentional repeats of decade prayers; forbid duplicate optional longer titles
        val longerOpen = divineMercy.chaplet.optionalLongerOpening?.title
        if (longerOpen != null) {
            assertEquals(1, full.count { it.title == longerOpen })
        }
        val longerClose = divineMercy.chaplet.optionalLongerClosing?.title
        if (longerClose != null) {
            assertEquals(1, full.count { it.title == longerClose })
        }
    }

    @Test
    fun chaplet_decades_come_before_holyGod_conclude() {
        val steps = GuidedStepBuilder.buildChapletSteps(divineMercy, true, true)
        val firstDecade = steps.indexOfFirst { it.subtitle == "Decade 1 of 5" }
        val conclude = steps.indexOfFirst {
            it.title.contains("Holy God", ignoreCase = true) ||
                it.title.contains("Conclude", ignoreCase = true)
        }
        assertTrue(firstDecade >= 0)
        assertTrue(conclude > firstDecade)
    }

    @Test
    fun mysteryCalendarNotes_is_dedicated_not_day_map_key() {
        assertFalse(
            "notes must not live in mysteriesByDay",
            rosary.mysteriesByDay.containsKey("notes")
        )
        assertFalse(rosary.mysterySets.containsKey("notes"))
        assertTrue(
            !rosary.mysteryCalendarNotes.isNullOrBlank()
        )
        assertTrue(
            rosary.mysteryCalendarNotes!!.contains("Advent", ignoreCase = true) ||
                rosary.mysteryCalendarNotes!!.contains("Lent", ignoreCase = true)
        )
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday").forEach { day ->
            val set = rosary.mysteriesByDay[day]
            assertTrue("$day -> $set", set in setOf("Joyful", "Sorrowful", "Glorious", "Luminous"))
        }
    }
}
