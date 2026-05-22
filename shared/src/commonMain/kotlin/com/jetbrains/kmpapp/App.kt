package com.jetbrains.kmpapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import com.jetbrains.kmpapp.screens.detail.DetailScreen
import com.jetbrains.kmpapp.screens.list.ListScreen
import com.jetbrains.kmpapp.screens.EmptyScreenContent
import kotlinx.serialization.Serializable

@Serializable
data object ListDestination : NavKey

@Serializable
data class DetailDestination(val objectId: Int) : NavKey

private val savedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ListDestination::class, ListDestination.serializer())
            subclass(DetailDestination::class, DetailDestination.serializer())
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun App() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface {
            val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
            val backStack = rememberNavBackStack(savedStateConfig, ListDestination)

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategies = listOf(listDetailStrategy),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<ListDestination>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = {
                                EmptyScreenContent(Modifier.fillMaxSize())
                            }
                        )
                    ) { _ ->
                        ListScreen(navigateToDetails = { objectId ->
                            backStack.add(DetailDestination(objectId))
                        })
                    }
                    entry<DetailDestination>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) { key ->
                        DetailScreen(
                            objectId = key.objectId,
                            navigateBack = {
                                backStack.removeLastOrNull()
                            }
                        )
                    }
                }
            )
        }
    }
}
