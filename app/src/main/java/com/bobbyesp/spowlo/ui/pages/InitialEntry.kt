package com.bobbyesp.spowlo.ui.pages

import android.Manifest
import android.os.Build
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.bobbyesp.library.domain.UpdateStatus
import com.bobbyesp.spowlo.App
import com.bobbyesp.spowlo.ui.common.LocalWindowWidthState
import com.bobbyesp.spowlo.ui.common.Route
import com.bobbyesp.spowlo.ui.common.animatedComposable
import com.bobbyesp.spowlo.ui.common.arg
import com.bobbyesp.spowlo.ui.common.id
import com.bobbyesp.spowlo.ui.common.slideInVerticallyComposable
import com.bobbyesp.spowlo.ui.dialogs.bottomsheets.DownloaderBottomSheet
import com.bobbyesp.spowlo.ui.pages.download_tasks.DownloadTasksPage
import com.bobbyesp.spowlo.ui.pages.download_tasks.FullscreenConsoleOutput
import com.bobbyesp.spowlo.ui.pages.downloader.DownloaderPage
import com.bobbyesp.spowlo.ui.pages.downloader.DownloaderViewModel
import com.bobbyesp.spowlo.ui.pages.history.DownloadsHistoryPage
import com.bobbyesp.spowlo.ui.pages.metadata_viewer.playlists.PlaylistPageViewModel
import com.bobbyesp.spowlo.ui.pages.mod_downloader.ModsDownloaderPage
import com.bobbyesp.spowlo.ui.pages.mod_downloader.ModsDownloaderViewModel
import com.bobbyesp.spowlo.ui.pages.playlist.PlaylistMetadataPage
import com.bobbyesp.spowlo.ui.pages.settings.SettingsPage
import com.bobbyesp.spowlo.ui.pages.settings.about.AboutPage
import com.bobbyesp.spowlo.ui.pages.settings.appearance.AppThemePreferencesPage
import com.bobbyesp.spowlo.ui.pages.settings.appearance.AppearancePage
import com.bobbyesp.spowlo.ui.pages.settings.appearance.LanguagePage
import com.bobbyesp.spowlo.ui.pages.settings.cookies.CookieProfilePage
import com.bobbyesp.spowlo.ui.pages.settings.cookies.CookiesSettingsViewModel
import com.bobbyesp.spowlo.ui.pages.settings.cookies.WebViewPage
import com.bobbyesp.spowlo.ui.pages.settings.directories.DownloadsDirectoriesPage
import com.bobbyesp.spowlo.ui.pages.settings.documentation.DocumentationPage
import com.bobbyesp.spowlo.ui.pages.settings.downloader.AudioQualityDialog
import com.bobbyesp.spowlo.ui.pages.settings.downloader.DownloaderSettingsPage
import com.bobbyesp.spowlo.ui.pages.settings.general.GeneralSettingsPage
import com.bobbyesp.spowlo.ui.pages.settings.spotify.SpotifySettingsPage
import com.bobbyesp.spowlo.ui.pages.settings.updater.UpdaterPage
import com.bobbyesp.spowlo.utils.PreferencesUtil.getString
import com.bobbyesp.spowlo.utils.SPOTDL
import com.bobbyesp.spowlo.utils.UpdateUtil
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialNavigationApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun InitialEntry(
    downloaderViewModel: DownloaderViewModel,
    modsDownloaderViewModel: ModsDownloaderViewModel,
    playlistPageViewModel: PlaylistPageViewModel,
    isUrlShared: Boolean
) {
    //bottom sheet remember state
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)

    val isLandscape = remember { MutableTransitionState(false) }

    val windowWidthState = LocalWindowWidthState.current

    LaunchedEffect(windowWidthState) {
        isLandscape.targetState = windowWidthState == WindowWidthSizeClass.Expanded
    }

    var showDownloaderBottomSheet by remember { mutableStateOf(false) }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState()

    val cookiesViewModel: CookiesSettingsViewModel = viewModel()
    val onBackPressed: () -> Unit = { navController.popBackStack() }

    if (isUrlShared) {
        if (navController.currentDestination?.route != Route.DOWNLOADER) {
            navController.popBackStack(
                route = Route.DOWNLOADER, inclusive = false, saveState = true
            )
        }
    }
    NavHost(
        modifier = Modifier
            .fillMaxWidth(
                when (LocalWindowWidthState.current) {
                    WindowWidthSizeClass.Compact -> 1f
                    WindowWidthSizeClass.Expanded -> 1f
                    else -> 0.8f
                }
            ),
        navController = navController,
        startDestination = Route.DownloaderNavi,
        route = Route.NavGraph
    ) {
        navigation(startDestination = Route.DOWNLOADER, route = Route.DownloaderNavi) {
            animatedComposable(Route.DOWNLOADER) {
                DownloaderPage(
                    navigateToDownloads = {
                        navController.navigate(Route.DOWNLOADS_HISTORY) {
                            launchSingleTop = true
                        }
                    },
                    navigateToSettings = {
                        navController.navigate(Route.SETTINGS)
                    },
                    navigateToDownloaderSheet = {
                        showDownloaderBottomSheet = true
                    },
                    onSongCardClicked = {
                        navController.navigate(Route.PLAYLIST_METADATA_PAGE) {
                            launchSingleTop = true
                        }
                    },
                    navigateToTasks = {
                        navController.navigate(Route.DownloadTasksNavi)
                    },
                    downloaderViewModel = downloaderViewModel,
                    sheetState = sheetState
                )
            }
            animatedComposable(Route.SETTINGS) {
                SettingsPage(
                    navController = navController
                )
            }
            animatedComposable(Route.GENERAL_DOWNLOAD_PREFERENCES) {
                GeneralSettingsPage(
                    onBackPressed = onBackPressed
                )
            }
            animatedComposable(Route.DOWNLOADS_HISTORY) {
                DownloadsHistoryPage(
                    onBackPressed = onBackPressed,
                )
            }
            animatedComposable(Route.DOWNLOAD_DIRECTORY) {
                DownloadsDirectoriesPage {
                    onBackPressed()
                }
            }
            animatedComposable(Route.APPEARANCE) {
                AppearancePage(navController = navController)
            }
            animatedComposable(Route.APP_THEME) {
                AppThemePreferencesPage {
                    onBackPressed()
                }
            }
            animatedComposable(Route.SPOTIFY_PREFERENCES) {
                SpotifySettingsPage {
                    onBackPressed()
                }
            }
            animatedComposable(Route.DOWNLOADER_SETTINGS) {
                DownloaderSettingsPage {
                    onBackPressed()
                }
            }
            slideInVerticallyComposable(Route.PLAYLIST_METADATA_PAGE) {
                PlaylistMetadataPage(
                    onBackPressed,
                    //TODO: ADD THE ABILITY TO PASS JUST SONGS AND NOT GET THEM FROM THE MUTABLE STATE
                )
            }
            animatedComposable(Route.MODS_DOWNLOADER) {
                ModsDownloaderPage(
                    onBackPressed, modsDownloaderViewModel
                )
            }
            animatedComposable(Route.COOKIE_PROFILE) {
                CookieProfilePage(
                    cookiesViewModel = cookiesViewModel,
                    navigateToCookieGeneratorPage = { navController.navigate(Route.COOKIE_GENERATOR_WEBVIEW) },
                ) { onBackPressed() }
            }
            animatedComposable(
                Route.COOKIE_GENERATOR_WEBVIEW
            ) {
                WebViewPage(cookiesViewModel) { onBackPressed() }
            }
            animatedComposable(Route.UPDATER_PAGE) {
                UpdaterPage(
                    onBackPressed
                )
            }
            animatedComposable(Route.DOCUMENTATION) {
                DocumentationPage(
                    onBackPressed, navController
                )
            }

            animatedComposable(Route.ABOUT) {
                AboutPage {
                    onBackPressed()
                }
            }

            animatedComposable(Route.LANGUAGES) {
                LanguagePage {
                    onBackPressed()
                }
            }

            //DIALOGS -------------------------------
            dialog(Route.AUDIO_QUALITY_DIALOG) {
                AudioQualityDialog(
                    onBackPressed
                )
            }
        }

        navigation(
            startDestination = Route.DOWNLOAD_TASKS, route = Route.DownloadTasksNavi
        ) {

            animatedComposable(
                Route.FULLSCREEN_LOG arg "taskHashCode",
                arguments = listOf(navArgument("taskHashCode") {
                    type = NavType.IntType
                })
            ) {

                FullscreenConsoleOutput(
                    onBackPressed = onBackPressed,
                    taskHashCode = it.arguments?.getInt("taskHashCode") ?: -1
                )
            }

            animatedComposable(Route.DOWNLOAD_TASKS) {
                DownloadTasksPage(
                    onGoBack = onBackPressed,
                    onNavigateToDetail = { navController.navigate(Route.FULLSCREEN_LOG id it) }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        if (SPOTDL.getString().isNotEmpty()) return@LaunchedEffect
        kotlin.runCatching {
            withContext(Dispatchers.IO) {
                val result = UpdateUtil.updateSpotDL()
                if (result == UpdateStatus.DONE) {
                    ToastUtil.makeToastSuspend(
                        App.context.getString(R.string.spotdl_update_success)
                            .format(SPOTDL.getString())
                    )
                }
            }
        }
    }

    if (showDownloaderBottomSheet) {
        val storagePermission = rememberPermissionState(
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) { b: Boolean ->
            if (b) {
                downloaderViewModel.startDownloadSong()
            } else {
                ToastUtil.makeToast(R.string.permission_denied)
            }
        }

        val viewModelState = downloaderViewModel.viewStateFlow.collectAsStateWithLifecycle().value
        DownloaderBottomSheet(
            bottomSheetState = sheetState,
            onBackPressed = {
                showDownloaderBottomSheet = false
            },
            url = viewModelState.url,
            onDownloadPressed = {
                if (Build.VERSION.SDK_INT > 29 || storagePermission.status == PermissionStatus.Granted) downloaderViewModel.startDownloadSong()
                else {
                    storagePermission.launchPermissionRequest()
                }
            },
            onRequestMetadata = {
                downloaderViewModel.requestMetadata()
            },
            navigateToPlaylist = { id ->
                navController.navigate(
                    Route.PLAYLIST_PAGE + "/" + "playlist" + "/" + id,
                    navOptions = navOptions {
                        launchSingleTop = true
                        restoreState = true
                    })
            },
            navigateToAlbum = { id ->
                navController.navigate(
                    Route.PLAYLIST_PAGE + "/" + "album" + "/" + id,
                    navOptions = navOptions {
                        launchSingleTop = true
                        restoreState = true
                    })
            },
            navigateToArtist = { id ->
                navController.navigate(
                    Route.PLAYLIST_PAGE + "/" + "artist" + "/" + id,
                    navOptions = navOptions {
                        launchSingleTop = true
                        restoreState = true
                    })
            },
        )
    }

}
