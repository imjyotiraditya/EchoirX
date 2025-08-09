package app.echoirx.presentation.screens.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.echoirx.R
import app.echoirx.domain.model.SearchItem
import app.echoirx.presentation.components.TrackCover
import app.echoirx.presentation.components.preferences.PreferencePosition
import java.util.Locale

@Composable
fun SearchResultItem(
    modifier: Modifier = Modifier,
    result: SearchItem,
    position: PreferencePosition = PreferencePosition.Single,
    onClick: () -> Unit
) {
    val formatsDisplay = result.formats?.let { formats ->
        formats.mapTo(mutableSetOf()) {
            when (it) {
                "HIRES_LOSSLESS" -> stringResource(R.string.quality_label_hires)
                "LOSSLESS" -> stringResource(R.string.quality_label_cdq)
                "DOLBY_ATMOS" -> stringResource(R.string.label_dolby)
                "HIGH", "LOW" -> stringResource(R.string.label_aac)
                else -> stringResource(R.string.label_unknown)
            }
        }.apply {
            if (formats.any { it == "HIRES_LOSSLESS" || it == "LOSSLESS" }) {
                add(stringResource(R.string.label_aac))
            }
        }.joinToString(" / ").uppercase(Locale.getDefault())
    } ?: stringResource(R.string.label_unknown).uppercase(Locale.getDefault())

    val shape = when (position) {
        PreferencePosition.Single -> MaterialTheme.shapes.large
        PreferencePosition.Top -> MaterialTheme.shapes.large.copy(
            bottomStart = MaterialTheme.shapes.extraSmall.bottomStart,
            bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd
        )

        PreferencePosition.Bottom -> MaterialTheme.shapes.large.copy(
            topStart = MaterialTheme.shapes.extraSmall.topStart,
            topEnd = MaterialTheme.shapes.extraSmall.topEnd
        )

        PreferencePosition.Middle -> MaterialTheme.shapes.extraSmall
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TrackCover(
                url = result.cover,
                size = 60.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatsDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = result.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = result.artists.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    result.formats?.let { formats ->
                        if ("DOLBY_ATMOS" in formats) {
                            Icon(
                                painter = painterResource(R.drawable.ic_dolby),
                                contentDescription = stringResource(R.string.label_dolby_atmos),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Text(
                    text = result.duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (result.explicit) {
                    Icon(
                        painter = painterResource(R.drawable.ic_explicit),
                        contentDescription = stringResource(R.string.label_explicit),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}