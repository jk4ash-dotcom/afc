package com.afcpoc.prayer.data

/**
 * Pure builders for guided Rosary / Chaplet step lists.
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
        divineMercy: DivineMercyContent,
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

        fun alreadyHas(title: String, body: String): Boolean {
            val t = title.trim()
            val b = body.trim()
            return steps.any {
                it.title.equals(t, ignoreCase = true) ||
                    (b.isNotEmpty() && it.body.trim() == b)
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

        // Longer Faustina opening is distinct from short bead optional openings;
        // only insert when not already present (title/body dedupe).
        if (includeOptionalOpenings) {
            chaplet.optionalLongerOpening?.let { opt ->
                if (!alreadyHas(opt.title, opt.text)) {
                    val insertAt = steps.indexOfFirst {
                        it.title.contains("Our Father", ignoreCase = true)
                    }.let { if (it >= 0) it else steps.size }
                    steps.add(insertAt, GuidedStep(opt.title, "Optional opening", opt.text))
                }
            }
        }

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
        val withDecades = if (concludeIndex >= 0) {
            steps.take(concludeIndex) + decadeBlock + steps.drop(concludeIndex)
        } else {
            steps + decadeBlock
        }

        if (includeOptionalClosings) {
            chaplet.optionalLongerClosing?.let { opt ->
                val has = withDecades.any {
                    it.title.equals(opt.title, ignoreCase = true) ||
                        it.body.trim() == opt.text.trim()
                }
                if (!has) {
                    return withDecades + GuidedStep(opt.title, "Optional closing", opt.text)
                }
            }
        }
        return withDecades
    }
}
