package app.echoirx.presentation.common

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import com.bobbyesp.foundation.ui.motion.materialSharedAxisXIn
import com.bobbyesp.foundation.ui.motion.materialSharedAxisXOut

object Motion {
    val AnimatedTextContentTransformation =
        ContentTransform(
            materialSharedAxisXIn(initialOffsetX = { it / 5 }),
            materialSharedAxisXOut(targetOffsetX = { -it / 10 }),
            sizeTransform = SizeTransform(clip = false),
        )
}