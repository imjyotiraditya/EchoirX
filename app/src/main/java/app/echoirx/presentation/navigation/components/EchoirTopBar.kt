package app.echoirx.presentation.navigation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.echoirx.R
import app.echoirx.presentation.common.Motion.AnimatedTextContentTransformation
import app.echoirx.presentation.navigation.Route
import app.echoirx.presentation.navigation.Route.Companion.navigationLabel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EchoirTopBar(
    currentTopLevelRoute: Route.TopLevelRoute,
    isSearchDetails: Boolean,
    onNavigateBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {

    val transitionState = remember { MutableTransitionState(currentTopLevelRoute) }

    LaunchedEffect(currentTopLevelRoute) {
        transitionState.targetState = currentTopLevelRoute
    }

    val transition = rememberTransition(transitionState)

    TopAppBar(
        title = {
            transition.AnimatedContent(
                modifier = Modifier
                    .fillMaxWidth(),
                transitionSpec = { AnimatedTextContentTransformation }
            ) { topLevelRoute ->
                Text(
                    text = stringResource(topLevelRoute.navigationLabel),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        navigationIcon = {
            AnimatedContent(
                targetState = isSearchDetails
            ) { condition ->
                if (condition) {
                    FilledTonalIconButton(
                        onClick = onNavigateBack,
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button)
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        scrollBehavior = scrollBehavior
    )
}