package app.echoirx.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.echoirx.R
import coil3.compose.AsyncImage

@Composable
fun TrackCover(
    url: String?,
    size: Dp
) {
    AsyncImage(
        model = url,
        contentDescription = stringResource(R.string.cd_track_image),
        modifier = Modifier
            .size(size)
            .clip(MaterialTheme.shapes.extraSmall)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.extraSmall
            ),
        contentScale = ContentScale.Crop
    )
}