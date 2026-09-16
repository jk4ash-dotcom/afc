package com.afcpoc.prayer.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Loads prayer JSON from assets. Prefer [preload] / [getInstance] so decode
 * and guided-step list construction happen off the main thread (IO/Default).
 */
class ContentRepository private constructor(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val loaded = AtomicBoolean(false)

    @Volatile
    private var afcPrayersCache: List<AfcPrayer>? = null

    @Volatile
    private var rosaryCache: RosaryContent? = null

    @Volatile
    private var divineMercyCache: DivineMercyContent? = null

    private val rosaryStepsCache = ConcurrentHashMap<String, List<GuidedStep>>()
    private val chapletStepsCache = ConcurrentHashMap<String, List<GuidedStep>>()

    val afcPrayers: List<AfcPrayer>
        get() = afcPrayersCache ?: synchronized(this) {
            afcPrayersCache ?: loadAsset<List<AfcPrayer>>("afc_prayers.json").also {
                afcPrayersCache = it
            }
        }

    val rosary: RosaryContent
        get() = rosaryCache ?: synchronized(this) {
            rosaryCache ?: loadAsset<RosaryContent>("rosary.json").also {
                rosaryCache = it
            }
        }

    val divineMercy: DivineMercyContent
        get() = divineMercyCache ?: synchronized(this) {
            divineMercyCache ?: loadAsset<DivineMercyContent>("divine_mercy.json").also {
                divineMercyCache = it
            }
        }

    /**
     * Decode all JSON assets and warm default guided step lists.
     * Call from [Dispatchers.IO] / Default — never the main thread.
     */
    fun preload() {
        if (loaded.get()) return
        synchronized(this) {
            if (loaded.get()) return
            afcPrayersCache = loadAsset("afc_prayers.json")
            rosaryCache = loadAsset("rosary.json")
            divineMercyCache = loadAsset("divine_mercy.json")
            loaded.set(true)
        }
        val today = todayMysterySetName()
        cachedRosarySteps(today, includeAfterRosary = true)
        mysterySetNames().forEach { name ->
            cachedRosarySteps(name, includeAfterRosary = true)
            cachedRosarySteps(name, includeAfterRosary = false)
        }
        cachedChapletSteps(includeOptionalOpenings = true, includeOptionalClosings = true)
        cachedChapletSteps(includeOptionalOpenings = false, includeOptionalClosings = false)
        cachedChapletSteps(includeOptionalOpenings = true, includeOptionalClosings = false)
        cachedChapletSteps(includeOptionalOpenings = false, includeOptionalClosings = true)
    }

    fun isPreloaded(): Boolean = loaded.get()

    private inline fun <reified T> loadAsset(name: String): T {
        val text = context.assets.open(name).bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }

    fun afcPrayerById(id: String): AfcPrayer? = afcPrayers.find { it.id == id }

    fun afcGrouped(): Map<String, List<AfcPrayer>> =
        afcPrayers.groupBy { it.category }

    fun todayMysterySetName(): String {
        val dayName = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val mapped = rosary.mysteriesByDay[dayName]
        // "notes" is calendar commentary stuffed into mysteriesByDay — never a set name
        return if (mapped.isNullOrBlank() || mapped.equals("notes", ignoreCase = true)) {
            "Glorious"
        } else {
            mapped
        }
    }

    /** USCCB Advent/Lent Sunday note; not a mystery-set key. */
    fun mysteryCalendarNote(): String? =
        rosary.mysteriesByDay["notes"]?.takeIf { it.isNotBlank() }

    fun mysterySetNames(): List<String> = listOf("Joyful", "Sorrowful", "Glorious", "Luminous")

    fun cachedRosarySteps(setName: String, includeAfterRosary: Boolean): List<GuidedStep> {
        val key = "$setName|$includeAfterRosary"
        return rosaryStepsCache.getOrPut(key) { buildRosarySteps(setName, includeAfterRosary) }
    }

    fun cachedChapletSteps(
        includeOptionalOpenings: Boolean,
        includeOptionalClosings: Boolean
    ): List<GuidedStep> {
        val key = "$includeOptionalOpenings|$includeOptionalClosings"
        return chapletStepsCache.getOrPut(key) {
            buildChapletSteps(includeOptionalOpenings, includeOptionalClosings)
        }
    }

    fun buildRosarySteps(setName: String, includeAfterRosary: Boolean): List<GuidedStep> =
        GuidedStepBuilder.buildRosarySteps(rosary, setName, includeAfterRosary)

    fun buildChapletSteps(
        includeOptionalOpenings: Boolean,
        includeOptionalClosings: Boolean
    ): List<GuidedStep> =
        GuidedStepBuilder.buildChapletSteps(
            divineMercy,
            includeOptionalOpenings,
            includeOptionalClosings
        )

    companion object {
        @Volatile
        private var instance: ContentRepository? = null

        fun getInstance(context: Context): ContentRepository {
            return instance ?: synchronized(this) {
                instance ?: ContentRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
