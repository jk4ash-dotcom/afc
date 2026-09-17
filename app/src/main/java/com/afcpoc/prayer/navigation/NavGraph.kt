package com.afcpoc.prayer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.afcpoc.prayer.AfcApp
import com.afcpoc.prayer.data.ContentRepository
import com.afcpoc.prayer.ui.screens.AboutScreen
import com.afcpoc.prayer.ui.screens.AfcDetailScreen
import com.afcpoc.prayer.ui.screens.AfcListScreen
import com.afcpoc.prayer.ui.screens.HomeScreen
import com.afcpoc.prayer.ui.screens.RosaryFlowScreen
import com.afcpoc.prayer.ui.screens.RosaryHubScreen

@Composable
fun AfcNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember {
        runCatching { AfcApp.from(context).repository }
            .getOrElse { ContentRepository.getInstance(context) }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onAfc = { navController.navigate(Routes.AFC_LIST) },
                onRosary = { navController.navigate(Routes.ROSARY_HUB) },
                onAbout = { navController.navigate(Routes.ABOUT) }
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.AFC_LIST) {
            AfcListScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpen = { id -> navController.navigate(Routes.afcDetail(id)) }
            )
        }
        composable(
            route = Routes.AFC_DETAIL,
            arguments = listOf(navArgument("prayerId") { type = NavType.StringType })
        ) { entry ->
            AfcDetailScreen(
                prayerId = entry.arguments?.getString("prayerId").orEmpty(),
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ROSARY_HUB) {
            RosaryHubScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onStart = { setName, includeAfter ->
                    navController.navigate(Routes.rosaryFlow(setName, includeAfter))
                }
            )
        }
        composable(
            route = Routes.ROSARY_FLOW,
            arguments = listOf(
                navArgument("setName") { type = NavType.StringType },
                navArgument("includeAfter") { type = NavType.BoolType }
            )
        ) { entry ->
            RosaryFlowScreen(
                setName = entry.arguments?.getString("setName") ?: "Joyful",
                includeAfter = entry.arguments?.getBoolean("includeAfter") ?: true,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
