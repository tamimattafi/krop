package com.attafitamim.krop.core.crop

import androidx.compose.animation.core.animate
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.lerp
import com.attafitamim.krop.core.utils.lerpAngle

@Composable
fun animateImgTransform(target: ImgTransform): State<ImgTransform> {
    var prev by remember { mutableStateOf<ImgTransform?>(null) }
    val current = remember { mutableStateOf(target) }
    LaunchedEffect(target) {
        val a = prev
        try {
            if (a != null) animate(0f, 1f) { p, _ ->
                current.value = (a.lerp(target, p))
            }
        } finally {
            current.value = (target)
            prev = target
        }
    }
    return current
}

fun ImgTransform.lerp(target: ImgTransform, p: Float): ImgTransform {
    if (p == 0f) return this
    if (p == 1f) return target
    return ImgTransform(
        angleDeg = lerpAngle(angleDeg, target.angleDeg, p),
        scale = lerp(scale, target.scale, p),
        pivotRel = lerp(pivotRel, target.pivotRel, p)
    )
}