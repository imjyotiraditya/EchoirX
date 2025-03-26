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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
import app.echoirx.data.utils.extensions.formatErrorMessage
import app.echoirx.data.utils.extensions.showSnackbar
import app.echoirx.domain.model.SearchResult
import app.echoirx.presentation.components.EmptyStateMessage
import app.echoirx.presentation.components.TrackBottomSheet
import app.echoirx.presentation.navigation.Route
import app.echoirx.presentation.screens.search.components.FilterBottomSheet
import app.echoirx.presentation.screens.search.components.SearchResultItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

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
    val sameTapActionFlow = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getMutableStateFlow("triggerSameTap", false)
        ?: remember { MutableStateFlow(false) }

    var selectedTrack by remember { mutableStateOf<SearchResult?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showFilterBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(sameTapActionFlow) {
        sameTapActionFlow.collectLatest {
            if (it) {
                focusRequester.requestFocus()
                sameTapActionFlow.value = false
                navController.currentBackStackEntry?.savedStateHandle
                    ?.remove<Boolean>("triggerSameTap")
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
            TextField(
                value = state.query,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = MaterialTheme.shapes.small,
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
                        contentDescription = stringResource(R.string.cd_search),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchType.entries.forEach { type ->
                    FilterChip(
                        selected = state.searchType == type,
                        onClick = {
                            viewModel.onSearchTypeChange(type)
                            focusManager.clearFocus()
                        },
                        label = {
                            Text(
                                text = stringResource(type.title),
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = state.searchType == type,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
                Spacer(
                    modifier = Modifier.weight(1f)
                )
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(
                                text = stringResource(R.string.tooltip_filter)
                            )
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = { showFilterBottomSheet = true }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterAlt,
                            contentDescription = stringResource(R.string.cd_filter_button)
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        when (state.status) {
            SearchStatus.Empty, SearchStatus.Ready -> {
                EmptyStateMessage(
                    title = stringResource(R.string.msg_search_empty),
                    description = stringResource(R.string.msg_search_empty_desc),
                    painter = painterResource(R.drawable.ic_search)
                )
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
                        items(state.filteredResults) { result ->
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
                            buttonText = stringResource(R.string.action_retry),
                            onButtonClick = { viewModel.search() }
                        )
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
            onDismiss = { showBottomSheet = false }
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