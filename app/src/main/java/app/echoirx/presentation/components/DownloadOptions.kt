package app.echoirx.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.echoirx.R
import app.echoirx.domain.model.QualityConfig

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DownloadOptions(
    formats: List<String>?,
    modes: List<String>?,
    onOptionSelected: (QualityConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val downloadOptions = buildList {
        if (!formats.isNullOrEmpty() && !modes.isNullOrEmpty()) {
            val hasDolbyAtmos = "DOLBY_ATMOS" in modes && "DOLBY_ATMOS" in formats
            val hasStereo = "STEREO" in modes

            if (hasDolbyAtmos) {
                add(QualityConfig.DolbyAtmosAC3)
                add(QualityConfig.DolbyAtmosAC4)
            }

            if (hasStereo && "HIRES_LOSSLESS" in formats) {
                add(QualityConfig.HiRes)
            }

            if (hasStereo) {
                if ("LOSSLESS" in formats) {
                    add(QualityConfig.Lossless)
                }
                add(QualityConfig.AAC320)
                add(QualityConfig.AAC96)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            downloadOptions.forEach { config ->
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Text(
                                text = stringResource(config.summary)
                            )
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    AssistChip(
                        onClick = { onOptionSelected(config) },
                        label = {
                            Text(
                                text = stringResource(config.shortLabel),
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_download),
                                contentDescription = stringResource(
                                    R.string.cd_download_option,
                                    config.shortLabel
                                ),
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    )
                }
            }
        }
    }
}