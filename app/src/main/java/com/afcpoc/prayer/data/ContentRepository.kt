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
 *
 * Readiness is split:
 * - [areAssetsReady] — JSON decoded into caches
 * - [areStepsWarmed] — default guided step lists built
 * - [isPreloaded] — both true (never claims loaded while only assets are ready)
 */
class ContentRepository private constructor(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val assetsReady = AtomicBoolean(false)
    private val stepsWarmed = AtomicBoolean(false)
    private val warmLock = Any()

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
                maybeMarkAssetsReadyLocked()
            }
        }

    val rosary: RosaryContent
        get() = rosaryCache ?: synchronized(this) {
            rosaryCache ?: loadAsset<RosaryContent>("rosary.json").also {
                rosaryCache = it
                maybeMarkAssetsReadyLocked()
            }
        }

    val divineMercy: DivineMercyContent
        get() = divineMercyCache ?: synchronized(this) {
            divineMercyCache ?: loadAsset<DivineMercyContent>("divine_mercy.json").also {
                divineMercyCache = it
                maybeMarkAssetsReadyLocked()
            }
        }

    /**
     * Decode all JSON assets and warm default guided step lists.
     * Call from [Dispatchers.IO] / Default — never the main thread.
     *
     * Does not set [isPreloaded] until step warm finishes. Cache misses that
     * still build on Default are fine; the flag must not claim full load early.
     */
    fun preload() {
        ensureAssetsLoaded()
        warmDefaultSteps()
    }

    /** JSON assets decoded (step lists may still be cold). */
    fun areAssetsReady(): Boolean = assetsReady.get()

    /** Default guided step lists have been warmed into cache. */
    fun areStepsWarmed(): Boolean = stepsWarmed.get()

    /** True only when assets are ready *and* default steps have been warmed. */
    fun isPreloaded(): Boolean = assetsReady.get() && stepsWarmed.get()

    private fun ensureAssetsLoaded() {
        if (assetsReady.get() &&
            afcPrayersCache != null &&
            rosaryCache != null &&
            divineMercyCache != null
        ) {
            return
        }
        synchronized(this) {
            if (afcPrayersCache == null) {
                afcPrayersCache = loadAsset("afc_prayers.json")
            }
            if (rosaryCache == null) {
                rosaryCache = loadAsset("rosary.json")
            }
            if (divineMercyCache == null) {
                divineMercyCache = loadAsset("divine_mercy.json")
            }
            assetsReady.set(true)
        }
    }

    private fun maybeMarkAssetsReadyLocked() {
        if (afcPrayersCache != null && rosaryCache != null && divineMercyCache != null) {
            assetsReady.set(true)
        }
    }

    private fun warmDefaultSteps() {
        if (stepsWarmed.get()) return
        synchronized(warmLock) {
            if (stepsWarmed.get()) return
            ensureAssetsLoaded()
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
            stepsWarmed.set(true)
        }
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
        val mapped = rosary.mysteriesByDay[dayName]
        return if (mapped.isNullOrBlank()) {
            "Glorious"
        } else {
            mapped
        }
    }

    /** USCCB Advent/Lent Sunday note; dedicated field, not a mystery-set key. */
    fun mysteryCalendarNote(): String? =
        rosary.mysteryCalendarNotes?.takeIf { it.isNotBlank() }

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
