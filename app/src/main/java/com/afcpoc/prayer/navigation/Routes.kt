package com.afcpoc.prayer.navigation

object Routes {
    const val HOME = "home"
    const val ABOUT = "about"
    const val AFC_LIST = "afc"
    const val AFC_DETAIL = "afc/{prayerId}"
    const val ROSARY_HUB = "rosary"
    const val ROSARY_FLOW = "rosary/flow/{setName}/{includeAfter}"

    fun afcDetail(id: String) = "afc/$id"
    fun rosaryFlow(setName: String, includeAfter: Boolean) =
        "rosary/flow/$setName/$includeAfter"
}
