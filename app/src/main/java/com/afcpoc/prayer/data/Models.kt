package com.afcpoc.prayer.data

import kotlinx.serialization.Serializable

@Serializable
data class AfcPrayer(
    val id: String,
    val title: String,
    val category: String,
    val sourceUrl: String? = null,
    val body: String
)

@Serializable
data class RosaryContent(
    val title: String,
    val sources: List<String> = emptyList(),
    /** Day-of-week display name → mystery set name (Joyful/Sorrowful/…). */
    val mysteriesByDay: Map<String, String> = emptyMap(),
    /** USCCB Advent/Lent Sunday calendar commentary — not a day→set entry. */
    val mysteryCalendarNotes: String? = null,
    val mysterySets: Map<String, MysterySet> = emptyMap(),
    val beadSequenceTemplate: List<BeadTemplateStep> = emptyList(),
    val prayers: Map<String, RosaryPrayer> = emptyMap(),
    val afterRosarySet: AfterRosarySet? = null
)

@Serializable
data class MysterySet(
    val days: List<String> = emptyList(),
    val mysteries: List<Mystery> = emptyList()
)

@Serializable
data class Mystery(
    val name: String,
    val fruit: String? = null
)

@Serializable
data class BeadTemplateStep(
    val order: Int,
    val bead: String,
    val action: String,
    val prayerId: String? = null,
    val prayerIds: List<String>? = null,
    val repeat: Int? = null,
    val parts: List<String>? = null
)

@Serializable
data class RosaryPrayer(
    val title: String,
    val sourceUrl: String? = null,
    val note: String? = null,
    val body: String? = null
)

@Serializable
data class AfterRosarySet(
    val description: String? = null,
    val sourceUrl: String? = null,
    val prayers: List<AfterRosaryPrayer> = emptyList()
)

@Serializable
data class AfterRosaryPrayer(
    val id: String,
    val title: String,
    val `when`: String? = null,
    val body: String? = null,
    val ref: String? = null
)

@Serializable
data class DivineMercyContent(
    val title: String,
    val sources: List<String> = emptyList(),
    val chaplet: Chaplet,
    val hourOfGreatMercy: HourOfGreatMercy
)

@Serializable
data class Chaplet(
    val description: String? = null,
    val beadSteps: List<ChapletStep> = emptyList(),
    val optionalLongerOpening: OptionalPrayer? = null,
    val optionalLongerClosing: OptionalPrayer? = null
)

@Serializable
data class ChapletStep(
    val step: Int,
    val bead: String,
    val label: String,
    val repeat: Int = 1,
    val perDecade: Boolean = false,
    val text: String
)

@Serializable
data class OptionalPrayer(
    val title: String,
    val sourceUrl: String? = null,
    val text: String
)

@Serializable
data class HourOfGreatMercy(
    val title: String,
    val sourceUrl: String? = null,
    val diaryQuote: String? = null,
    val prayers: List<HourPrayer> = emptyList(),
    val notes: String? = null
)

@Serializable
data class HourPrayer(
    val id: String,
    val label: String,
    val text: String? = null,
    val repeat: Int? = null,
    val sourceUrl: String? = null,
    val texts: List<String>? = null
)

/** One navigable bead/step in a guided flow. */
data class GuidedStep(
    val title: String,
    val subtitle: String? = null,
    val body: String,
    val progressLabel: String? = null
)
