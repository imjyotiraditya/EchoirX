package app.echoirx.presentation.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.echoirx.R
import app.echoirx.data.permission.PermissionsManager
import app.echoirx.data.utils.extensions.formatErrorMessage
import app.echoirx.data.utils.extensions.showSnackbar
import app.echoirx.domain.model.SearchResult
import app.echoirx.presentation.components.EmptyStateMessage
import app.echoirx.presentation.components.TrackBottomSheet
import app.echoirx.presentation.navigation.NavConstants
import app.echoirx.presentation.navigation.Route
import app.echoirx.presentation.screens.search.components.FilterBottomSheet
import app.echoirx.presentation.screens.search.components.SearchHistorySection
import app.echoirx.presentation.screens.search.components.SearchResultItem
import app.echoirx.presentation.screens.search.components.SearchSuggestionsSection
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    var selectedTrack by remember { mutableStateOf<SearchResult?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showFilterBottomSheet by remember { mutableStateOf(false) }
    val isPreviewPlaying by viewModel.isPreviewPlaying.collectAsState()
    val currentMediaInfo by viewModel.currentMediaInfo.collectAsState()
    val hasMediaPermission by viewModel.hasMediaPermission.collectAsState()

    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.let { savedState ->
            savedState.getStateFlow(NavConstants.KEY_FOCUS_SEARCH_BAR, false)
                .filter { it }
                .collect {
                    focusRequester.requestFocus()
                    savedState[NavConstants.KEY_FOCUS_SEARCH_BAR] = false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = state.query,
                    onValueChange = { viewModel.onQueryChange(it) },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    placeholder = {
                        Text(
                            text = stringResource(
                                R.string.hint_search,
                                stringResource(state.searchType.title)
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = stringResource(R.string.cd_search)
                        )
                    },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearSearch() }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_clear),
                                    contentDescription = stringResource(R.string.cd_clear_search)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (state.status == SearchStatus.Ready) {
                                viewModel.search()
                                focusManager.clearFocus()
                            }
                        }
                    )
                )

                IconButton(
                    onClick = {
                        when {
                            !hasMediaPermission -> {
                                val intent = PermissionsManager(context).getNotificationListenerSettingsIntent()
                                context.startActivity(intent)
                            }
                            currentMediaInfo?.isPlaying == true -> {
                                viewModel.searchCurrentMedia()
                                focusManager.clearFocus()
                                snackbarHostState.showSnackbar(
                                    scope = coroutineScope,
                                    message = context.getString(
                                        R.string.msg_searching_current_media,
                                        currentMediaInfo?.getDisplayText() ?: ""
                                    )
                                )
                            }
                            else -> {
                                snackbarHostState.showSnackbar(
                                    scope = coroutineScope,
                                    message = context.getString(R.string.msg_no_media_playing)
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = when {
                            !hasMediaPermission -> Icons.Outlined.NotificationsOff
                            currentMediaInfo?.isPlaying == true -> Icons.Outlined.MusicNote
                            else -> Icons.Outlined.MusicOff
                        },
                        contentDescription = null
                    )
                }

                IconButton(
                    onClick = { showFilterBottomSheet = true }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterAlt,
                        contentDescription = stringResource(R.string.cd_filter_button)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchType.entries.forEach { type ->
                    InputChip(
                        selected = state.searchType == type,
                        onClick = {
                            viewModel.onSearchTypeChange(type)
                            focusManager.clearFocus()
                        },
                        label = {
                            Text(
                                text = stringResource(type.title),
                            )
                        },
                        shape = MaterialTheme.shapes.extraLarge
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        if (state.query.isNotEmpty() && state.status == SearchStatus.Ready && state.suggestedQueries.isNotEmpty()) {
            SearchSuggestionsSection(
                suggestions = state.suggestedQueries,
                onSuggestionClick = { item ->
                    viewModel.useHistoryItem(item)
                    focusManager.clearFocus()
                }
            )
        } else {
            when (state.status) {
                SearchStatus.Empty -> {
                    SearchHistorySection(
                        searchHistory = state.searchHistory,
                        onHistoryItemClick = { item ->
                            viewModel.useHistoryItem(item)
                        },
                        onDeleteHistoryItem = { item ->
                            viewModel.deleteHistoryItem(item)
                        },
                        onClearHistory = {
                            viewModel.clearSearchHistory()
                        }
                    )
                }

                SearchStatus.Ready -> {
                    if (state.isShowingHistory) {
                        SearchHistorySection(
                            searchHistory = state.searchHistory,
                            onHistoryItemClick = { item ->
                                viewModel.useHistoryItem(item)
                            },
                            onDeleteHistoryItem = { item ->
                                viewModel.deleteHistoryItem(item)
                            },
                            onClearHistory = {
                                viewModel.clearSearchHistory()
                            }
                        )
                    } else {
                        EmptyStateMessage(
                            title = stringResource(R.string.msg_search_empty),
                            description = stringResource(R.string.msg_search_empty_desc),
                            painter = painterResource(R.drawable.ic_search)
                        )
                    }
                }

                SearchStatus.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ContainedLoadingIndicator()
                    }
                }

                SearchStatus.Success -> {
                    if (state.filteredResults.isNotEmpty()) {
                        LazyColumn(
                            state = lazyListState
                        ) {
                            itemsIndexed(
                                items = state.filteredResults,
                                key = { index, result -> "search_result_${result.id}_$index" }
                            ) { index, result ->
                                SearchResultItem(
                                    result = result,
                                    onClick = {
                                        if (state.searchType == SearchType.TRACKS) {
                                            selectedTrack = result
                                            showBottomSheet = true
                                        } else {
                                            navController.currentBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("result", result)
                                            navController.navigate(
                                                Route.Search.Details().createPath(
                                                    type = state.searchType.name,
                                                    id = result.id
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        EmptyStateMessage(
                            title = stringResource(R.string.msg_search_no_results_filters),
                            description = stringResource(R.string.msg_search_no_results_filters_desc),
                            painter = painterResource(R.drawable.ic_search)
                        )
                    }
                }

                SearchStatus.NoResults -> {
                    EmptyStateMessage(
                        title = stringResource(R.string.msg_search_no_results),
                        description = stringResource(R.string.msg_search_no_results_desc),
                        painter = painterResource(R.drawable.ic_search)
                    )
                }

                SearchStatus.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.showServerRecommendation) {
                            EmptyStateMessage(
                                title = stringResource(R.string.title_server_recommendation),
                                description = stringResource(R.string.msg_server_recommendation),
                                icon = Icons.Outlined.CloudOff
                            )
                        } else {
                            EmptyStateMessage(
                                title = stringResource(R.string.msg_unknown_error),
                                description = state.error.formatErrorMessage(
                                    defaultError = stringResource(R.string.msg_unknown_error)
                                ),
                                icon = Icons.Outlined.Error,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet && selectedTrack != null) {
        TrackBottomSheet(
            track = selectedTrack!!,
            onDownload = { config ->
                viewModel.downloadTrack(selectedTrack!!, config)
                snackbarHostState.showSnackbar(
                    scope = coroutineScope,
                    message = context.getString(
                        R.string.msg_download_started,
                        context.getString(config.label)
                    )
                )
            },
            onPreviewClick = {
                if (isPreviewPlaying) {
                    viewModel.stopTrackPreview()
                } else {
                    viewModel.playTrackPreview(selectedTrack!!.id)
                }
            },
            isPreviewPlaying = isPreviewPlaying,
            showPreviewButton = true,
            onDismiss = {
                viewModel.stopTrackPreview()
                showBottomSheet = false
            }
        )
    }

    if (showFilterBottomSheet) {
        FilterBottomSheet(
            currentFilter = state.searchFilter,
            onQualityFilterAdded = { quality ->
                viewModel.onSearchFilterQualityAdded(quality)
            },
            onQualityFilterRemoved = { quality ->
                viewModel.onSearchFilterQualityRemoved(quality)
            },
            onContentFilterAdded = { contentFilter ->
                viewModel.onSearchContentFilterAdded(contentFilter)
            },
            onContentFilterRemoved = { contentFilter ->
                viewModel.onSearchContentFilterRemoved(contentFilter)
            },
            onDismiss = { showFilterBottomSheet = false }
        )
    }
}