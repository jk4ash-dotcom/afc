package com.afcpoc.prayer.data

/**
 * Pure builders for guided Rosary step lists.
 * Keep heavy allocation off the UI thread; call from Dispatchers.Default/IO
 * or via [ContentRepository] caches after [ContentRepository.preload].
 */
object GuidedStepBuilder {

    fun buildRosarySteps(
        rosary: RosaryContent,
        setName: String,
        includeAfterRosary: Boolean
    ): List<GuidedStep> {
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

        // AFC practice: All For after each decade's Fatima (always for this app's
        // guided Rosary). Still strip All For from the trailing after-Rosary set
        // so it is not duplicated as an end-only 6th copy.
        val allFor = rosary.afterRosarySet?.prayers?.find { it.id == "all-for" }

        val ordinals = listOf("first", "second", "third", "fourth", "fifth")
        set.mysteries.forEachIndexed { decadeIndex, mystery ->
            val decadeLabel = "Decade ${decadeIndex + 1} of 5 — $setName"
            val ordinal = ordinals.getOrElse(decadeIndex) { "${decadeIndex + 1}th" }
            steps += GuidedStep(
                title = "The $ordinal mystery: ${mystery.name}",
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
            allFor?.let { p ->
                val body = p.body.orEmpty()
                if (body.isNotBlank()) {
                    steps += GuidedStep(p.title, decadeLabel, body)
                }
            }
        }

        steps += GuidedStep(prayerTitle("hail-holy-queen"), "Closing", prayerBody("hail-holy-queen"))
        steps += GuidedStep(
            prayerTitle("rosary-concluding-prayer"),
            "Closing",
            prayerBody("rosary-concluding-prayer")
        )

        if (includeAfterRosary) {
            rosary.afterRosarySet?.prayers?.forEach { p ->
                // All For already after each Fatima; HHQ already in Closing
                if (p.id == "all-for" || p.id == "hail-holy-queen") return@forEach
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
}
