package app.echoirx.presentation.screens.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.echoirx.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ServerBottomSheet(
    currentServer: String,
    onSave: (server: String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    var serverUrl by remember { mutableStateOf(currentServer) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val defaultServerUrl = "https://example.com/api/echoir"
    val context = LocalContext.current

    fun cleanUrl(url: String): String {
        return url.trim().replace("\\s+".toRegex(), "")
    }

    fun validateAndSave() {
        focusManager.clearFocus()
        if (serverUrl.isBlank()) {
            showError = true
            errorMessage = context.getString(R.string.error_empty_server_url)
        } else {
            val cleanedUrl = cleanUrl(serverUrl)
            onSave(cleanedUrl)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.title_server),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            OutlinedTextField(
                value = serverUrl,
                onValueChange = {
                    serverUrl = it
                    showError = false
                },
                label = { Text(stringResource(R.string.label_server_url)) },
                placeholder = { Text(defaultServerUrl) },
                singleLine = true,
                isError = showError,
                supportingText = {
                    if (showError) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (serverUrl.contains(" ")) {
                        Text(
                            text = stringResource(R.string.msg_spaces_will_be_removed),
                        )
                    }
                },
                trailingIcon = {
                    if (serverUrl.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                serverUrl = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = stringResource(R.string.cd_clear)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { validateAndSave() }
                )
            )

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = {
                        serverUrl = defaultServerUrl
                        showError = false
                        onReset()
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(
                        text = stringResource(R.string.action_reset)
                    )
                }

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Button(
                    onClick = { validateAndSave() },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(
                        text = stringResource(R.string.action_save)
                    )
                }
            }
        }
    }
}