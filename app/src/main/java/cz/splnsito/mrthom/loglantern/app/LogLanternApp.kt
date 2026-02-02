package cz.splnsito.mrthom.loglantern.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import cz.splnsito.mrthom.loglantern.core.ui.theme.LogLanternTheme
import cz.splnsito.mrthom.loglantern.navigation.LogLanternNavGraph
import cz.splnsito.mrthom.loglantern.navigation.MainScaffold

@Composable
fun LogLanternApp() {
    LogLanternTheme {
        val navController = rememberNavController()
        MainScaffold(navController = navController) { nav ->
            LogLanternNavGraph(navController = nav)
        }
    }
}
