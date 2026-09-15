package com.afcpoc.prayer.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class ContentRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val afcPrayers: List<AfcPrayer> by lazy {
        loadAsset("afc_prayers.json")
    }

    val rosary: RosaryContent by lazy {
        loadAsset("rosary.json")
    }

    val divineMercy: DivineMercyContent by lazy {
        loadAsset("divine_mercy.json")
    }

    private inline fun <reified T> loadAsset(name: String): T {
        val text = context.assets.open(name).bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }

    fun afcPrayerById(id: String): AfcPrayer? = afcPrayers.find { it.id == id }

    fun afcGrouped(): Map<String, List<AfcPrayer>> =
        afcPrayers.groupBy { it.category }

    fun todayMysterySetName(): String {
        val dayName = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        return rosary.mysteriesByDay[dayName] ?: "Glorious"
    }

    fun mysterySetNames(): List<String> = listOf("Joyful", "Sorrowful", "Glorious", "Luminous")

    fun buildRosarySteps(setName: String, includeAfterRosary: Boolean): List<GuidedStep> {
        val set = rosary.mysterySets[setName] ?: return emptyList()
        val prayers = rosary.prayers
        val steps = mutableListOf<GuidedStep>()

        fun prayerBody(id: String): String =
            prayers[id]?.body
                ?: rosary.afterRosarySet?.prayers?.find { it.id == id }?.body
                ?: ""

        fun prayerTitle(id: String): String =
            prayers[id]?.title
                ?: rosary.afterRosarySet?.prayers?.find { it.id == id }?.title
                ?: id

        steps += GuidedStep(prayerTitle("sign-of-cross"), "Opening", prayerBody("sign-of-cross"))
        steps += GuidedStep(prayerTitle("apostles-creed"), "Opening", prayerBody("apostles-creed"))
        steps += GuidedStep(prayerTitle("our-father"), "Opening", prayerBody("our-father"))
        listOf("Faith", "Hope", "Charity").forEachIndexed { i, intention ->
            steps += GuidedStep(
                title = "Hail Mary (${i + 1} of 3)",
                subtitle = "For $intention",
                body = prayerBody("hail-mary")
            )
        }
        steps += GuidedStep(prayerTitle("glory-be"), "Opening", prayerBody("glory-be"))

        set.mysteries.forEachIndexed { decadeIndex, mystery ->
            val decadeLabel = "Decade ${decadeIndex + 1} of 5 — $setName"
            steps += GuidedStep(
                title = "Announce: ${mystery.name}",
                subtitle = decadeLabel,
                body = buildString {
                    append(mystery.name)
                    mystery.fruit?.let { append("\n\nFruit of the Mystery: $it") }
                }
            )
            steps += GuidedStep(prayerTitle("our-father"), decadeLabel, prayerBody("our-father"))
            repeat(10) { bead ->
                steps += GuidedStep(
                    title = "Hail Mary (${bead + 1} of 10)",
                    subtitle = decadeLabel,
                    body = prayerBody("hail-mary"),
                    progressLabel = "Bead ${bead + 1}/10"
                )
            }
            steps += GuidedStep(prayerTitle("glory-be"), decadeLabel, prayerBody("glory-be"))
            steps += GuidedStep(prayerTitle("fatima-prayer"), decadeLabel, prayerBody("fatima-prayer"))
        }

        steps += GuidedStep(prayerTitle("hail-holy-queen"), "Closing", prayerBody("hail-holy-queen"))
        steps += GuidedStep(
            prayerTitle("rosary-concluding-prayer"),
            "Closing",
            prayerBody("rosary-concluding-prayer")
        )

        if (includeAfterRosary) {
            rosary.afterRosarySet?.prayers?.forEach { p ->
                val body = p.body
                    ?: p.ref?.removePrefix("prayers.")?.let { prayers[it]?.body }
                    ?: ""
                if (body.isNotBlank()) {
                    steps += GuidedStep(p.title, "AFC after-Rosary", body)
                }
            }
        }
        return steps
    }

    fun buildChapletSteps(
        includeOptionalOpenings: Boolean,
        includeOptionalClosings: Boolean
    ): List<GuidedStep> {
        val steps = mutableListOf<GuidedStep>()
        val chaplet = divineMercy.chaplet
        val decadeEternal = chaplet.beadSteps.find { it.bead == "our-father-bead" }
        val decadeSmall = chaplet.beadSteps.find { it.perDecade || it.bead == "hail-mary-beads" }

        fun addRepeated(step: ChapletStep, subtitle: String = "Chaplet") {
            val times = step.repeat.coerceAtLeast(1)
            if (times == 1) {
                steps += GuidedStep(step.label, subtitle, step.text)
            } else {
                repeat(times) { i ->
                    steps += GuidedStep(
                        title = "${step.label} (${i + 1} of $times)",
                        subtitle = subtitle,
                        body = step.text
                    )
                }
            }
        }

        chaplet.beadSteps.forEach { step ->
            when {
                step.bead.startsWith("optional-opening") && !includeOptionalOpenings -> Unit
                step.bead.startsWith("optional-closing") && !includeOptionalClosings -> Unit
                step.bead == "our-father-bead" || step.perDecade || step.bead == "hail-mary-beads" -> {
                    // Expanded as five decades once, below, after opening block
                }
                else -> addRepeated(step)
            }
        }

        if (includeOptionalOpenings) {
            chaplet.optionalLongerOpening?.let { opt ->
                val insertAt = steps.indexOfFirst {
                    it.title.contains("Our Father", ignoreCase = true)
                }.let { if (it >= 0) it else steps.size }
                steps.add(insertAt, GuidedStep(opt.title, "Optional opening", opt.text))
            }
        }

        // Build decade block and splice before conclude / optional closing
        val decadeBlock = mutableListOf<GuidedStep>()
        if (decadeEternal != null && decadeSmall != null) {
            repeat(5) { decade ->
                val label = "Decade ${decade + 1} of 5"
                decadeBlock += GuidedStep(decadeEternal.label, label, decadeEternal.text)
                repeat(10) { bead ->
                    decadeBlock += GuidedStep(
                        title = "For the sake of His sorrowful Passion (${bead + 1} of 10)",
                        subtitle = label,
                        body = decadeSmall.text,
                        progressLabel = "Bead ${bead + 1}/10"
                    )
                }
            }
        }

        val concludeIndex = steps.indexOfFirst {
            it.title.contains("Holy God", ignoreCase = true) ||
                it.title.contains("Conclude", ignoreCase = true)
        }
        return if (concludeIndex >= 0) {
            val result = steps.take(concludeIndex) + decadeBlock + steps.drop(concludeIndex)
            if (includeOptionalClosings) {
                chaplet.optionalLongerClosing?.let { opt ->
                    return result + GuidedStep(opt.title, "Optional closing", opt.text)
                }
            }
            result
        } else {
            val result = steps + decadeBlock
            if (includeOptionalClosings) {
                chaplet.optionalLongerClosing?.let { opt ->
                    return result + GuidedStep(opt.title, "Optional closing", opt.text)
                }
            }
            result
        }
    }
}
