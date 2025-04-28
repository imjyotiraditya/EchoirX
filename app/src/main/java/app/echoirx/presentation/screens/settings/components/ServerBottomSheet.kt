package app.echoirx.presentation.screens.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
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
        if (serverUrl.isNotBlank()) {
            val cleanedUrl = cleanUrl(serverUrl)
            onSave(cleanedUrl)
        } else {
            showError = true
            errorMessage = context.getString(R.string.error_empty_server_url)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudQueue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = stringResource(R.string.title_server_settings),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    text = stringResource(R.string.msg_server_subtitle),
                    style = MaterialTheme.typography.bodyMedium
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
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { validateAndSave() }
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
            ) {
                FilledTonalButton(
                    onClick = {
                        serverUrl = defaultServerUrl
                        showError = false
                        onReset()
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.action_reset_to_default))
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { validateAndSave() },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}