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
        @JvmStatic
        @BeforeClass
        fun load() {
            rosary = json.decodeFromString(readResource("rosary.json"))
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

        // Opening block before first decade mystery announce
        val firstAnnounce = steps.indexOfFirst { it.title.startsWith("The first mystery:") }
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
    fun rosary_mysteryAnnounceTitles_useNaturalOrdinals() {
        val steps = GuidedStepBuilder.buildRosarySteps(rosary, "Joyful", includeAfterRosary = false)
        val names = rosary.mysterySets["Joyful"]!!.mysteries.map { it.name }
        val expected = listOf(
            "The first mystery: ${names[0]}",
            "The second mystery: ${names[1]}",
            "The third mystery: ${names[2]}",
            "The fourth mystery: ${names[3]}",
            "The fifth mystery: ${names[4]}"
        )
        val announces = steps.filter { it.title.contains(" mystery: ") }
        assertEquals(5, announces.size)
        assertEquals(expected, announces.map { it.title })
        // First decade announce dump for smoke REPORT
        assertEquals("The first mystery: The Annunciation", announces.first().title)
        assertEquals("Decade 1 of 5 — Joyful", announces.first().subtitle)
        // No legacy Announce: prefix
        assertFalse(steps.any { it.title.startsWith("Announce:") })
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
    fun rosary_allFor_appearsExactlyFiveTimes_immediatelyAfterEachFatima() {
        val allForBody = rosary.afterRosarySet!!.prayers.find { it.id == "all-for" }!!.body!!
        // Always inserted for AFC guided Rosary (even without after-Rosary overlay)
        val steps = GuidedStepBuilder.buildRosarySteps(rosary, "Joyful", includeAfterRosary = false)
        val allForSteps = steps.filter {
            it.title.contains("All For", ignoreCase = true) || it.body.trim() == allForBody.trim()
        }
        assertEquals("All For must appear exactly 5 times (once per decade)", 5, allForSteps.size)

        // Each All For immediately follows a Fatima step within that decade
        for (d in 1..5) {
            val decade = steps.filter { it.subtitle == "Decade $d of 5 — Joyful" }
            val fatimaIdx = decade.indexOfFirst {
                it.title.contains("Fatima", ignoreCase = true) ||
                    it.body.contains("O my Jesus", ignoreCase = true)
            }
            assertTrue("decade $d missing Fatima", fatimaIdx >= 0)
            assertTrue(
                "decade $d: All For must immediately follow Fatima",
                fatimaIdx + 1 < decade.size
            )
            val after = decade[fatimaIdx + 1]
            assertTrue(
                "decade $d expected All For after Fatima, got '${after.title}'",
                after.title.contains("All For", ignoreCase = true) ||
                    after.body.trim() == allForBody.trim()
            )
        }

        // Not only-at-end: last All For must still be inside a decade subtitle, before Closing
        val lastAllFor = steps.indexOfLast {
            it.title.contains("All For", ignoreCase = true) || it.body.trim() == allForBody.trim()
        }
        val firstClosing = steps.indexOfFirst { it.subtitle == "Closing" }
        assertTrue(lastAllFor >= 0 && firstClosing > lastAllFor)
        assertTrue(steps[lastAllFor].subtitle!!.startsWith("Decade"))
    }

    @Test
    fun rosary_afterRosary_stJosephContrition_withoutTrailingHhqOrAllFor() {
        val allForBody = rosary.afterRosarySet!!.prayers.find { it.id == "all-for" }!!.body!!
        val with = GuidedStepBuilder.buildRosarySteps(rosary, "Sorrowful", includeAfterRosary = true)
        val trailing = with.filter { it.subtitle == "AFC after-Rosary" }
        assertTrue(trailing.isNotEmpty())
        assertFalse(
            "All For must not appear in trailing after-Rosary set",
            trailing.any {
                it.title.contains("All For", ignoreCase = true) ||
                    it.body.trim() == allForBody.trim()
            }
        )
        assertFalse(
            "Hail Holy Queen must not appear again in trailing after-Rosary (Closing only)",
            trailing.any { it.title.contains("Hail Holy Queen", ignoreCase = true) }
        )
        assertTrue(trailing.any { it.title.contains("St. Joseph", ignoreCase = true) })
        assertTrue(trailing.any { it.title.contains("Act of Contrition", ignoreCase = true) })

        // Closing still has exactly one HHQ, before the overlay
        val hhqIndices = with.withIndex().filter { (_, s) ->
            s.title.contains("Hail Holy Queen", ignoreCase = true)
        }.map { it.index }
        assertEquals("HHQ must appear exactly once (Closing)", 1, hhqIndices.size)
        val hhqIdx = hhqIndices.single()
        assertEquals("Closing", with[hhqIdx].subtitle)
        val firstOverlay = with.indexOfFirst { it.subtitle == "AFC after-Rosary" }
        assertTrue("Closing HHQ must precede after-Rosary overlay", hhqIdx < firstOverlay)

        // Still exactly 5 All For copies total (per-decade only)
        val allForCount = with.count {
            it.title.contains("All For", ignoreCase = true) || it.body.trim() == allForBody.trim()
        }
        assertEquals(5, allForCount)
    }

    @Test
    fun rosary_endOrder_closingHhqThenOverlayStJosephContrition() {
        val steps = GuidedStepBuilder.buildRosarySteps(rosary, "Glorious", includeAfterRosary = true)
        val last = steps.takeLast(8)
        val titles = last.map { "${it.subtitle}: ${it.title}" }
        // Expect: ... Closing HHQ, Closing concluding, then overlay St Joseph + Contrition
        val closing = steps.filter { it.subtitle == "Closing" }
        assertTrue(closing.any { it.title.contains("Hail Holy Queen", ignoreCase = true) })
        assertTrue(closing.any { it.title.contains("concluding", ignoreCase = true) ||
            it.title.contains("O God", ignoreCase = true) ||
            it.body.contains("O God", ignoreCase = true) })

        val trailing = steps.filter { it.subtitle == "AFC after-Rosary" }
        assertEquals(
            listOf("St. Joseph Prayer after the Rosary", "Act of Contrition"),
            trailing.map { it.title }
        )
        assertEquals(
            "AFC after-Rosary",
            steps.last().subtitle
        )
        // Dump helper for smoke REPORT (titles only)
        assertTrue("end dump size", last.size == 8)
        assertFalse(titles.any { it.contains("All For", ignoreCase = true) && it.startsWith("AFC") })
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
