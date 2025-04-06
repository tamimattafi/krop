package com.attafitamim.krop.core.crop

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntRect
import androidx.compose.ui.unit.toSize
import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.utils.constrainOffset
import com.attafitamim.krop.core.utils.constrainResize
import com.attafitamim.krop.core.utils.eq
import com.attafitamim.krop.core.utils.keepAspect
import com.attafitamim.krop.core.utils.next90
import com.attafitamim.krop.core.utils.prev90
import com.attafitamim.krop.core.utils.scaleToFit
import com.attafitamim.krop.core.utils.setAspect
import com.attafitamim.krop.core.utils.setSize
import com.attafitamim.krop.core.utils.toRect

/** State for the current image being cropped */
@Stable
interface CropState {
    val src: ImageSrc
    var transform: ImgTransform
    var region: Rect
    val defaultRegion: Rect
    var aspectLock: Boolean
    var shape: CropShape
    var clipResultToShape: Boolean
    val accepted: Boolean
    fun done(accept: Boolean)
    fun setInitialState(style: CropperStyle)
    fun reset()
}

fun cropState(
    src: ImageSrc,
    onDone: () -> Unit = {},
): CropState = object : CropState {
    val defaultTransform: ImgTransform = ImgTransform.Identity
    private var defaultShape: CropShape = RectCropShape
    private var defaultAspectLock: Boolean = false
    private var defaultClipResultToShape: Boolean = true
    override val src: ImageSrc get() = src
    private var _transform: ImgTransform by mutableStateOf(defaultTransform)
    override var transform: ImgTransform
        get() = _transform
        set(value) {
            onTransformUpdated(transform, value)
            _transform = value
        }

    override val defaultRegion = src.size.toSize().toRect()
    private var firstRegion: Rect = defaultRegion

    private var _region by mutableStateOf(defaultRegion)
    override var region
        get() = _region
        set(value) {
//            _region = value
            _region = updateRegion(
                old = _region, new = value,
                bounds = imgRect, aspectLock = aspectLock
            )
        }

    val imgRect by derivedStateOf { getTransformedImageRect(transform, src.size) }

    override var shape: CropShape by mutableStateOf(defaultShape)
    override var clipResultToShape: Boolean by mutableStateOf(defaultClipResultToShape)
    override var aspectLock by mutableStateOf(defaultAspectLock)

    private fun onTransformUpdated(old: ImgTransform, new: ImgTransform) {
        val unTransform = old.asMatrix(src.size).apply { invert() }
        _region = new.asMatrix(src.size).map(unTransform.map(region))
    }

    /**
     * TODO It would be cleaner to set this state via the constructor of CropState
     * But then we need to know the CropperStyle in advance
     */
    override fun setInitialState(style: CropperStyle) {
        defaultShape = (style.shapes.firstOrNull() ?: defaultShape).also {
            shape = it
        }

        firstRegion = style.aspects.firstOrNull()?.let {
            this.region.setAspect(it)
        } ?: defaultRegion
        _region = firstRegion

        defaultAspectLock = (style.aspects.size == 1).also {
            aspectLock = it
        }
    }

    override fun reset() {
        transform = defaultTransform
        shape = defaultShape
        clipResultToShape = defaultClipResultToShape
        _region = firstRegion
        aspectLock = defaultAspectLock
    }

    override var accepted: Boolean by mutableStateOf(false)

    override fun done(accept: Boolean) {
        accepted = accept
        onDone()
    }
}

fun getTransformedImageRect(transform: ImgTransform, size: IntSize) : Rect {
    val dstMat = transform.asMatrix(size)
    return dstMat.map(size.toIntRect().toRect())
}

fun CropState.rotLeft() {
    transform = transform.copy(angleDeg = transform.angleDeg.prev90())
}

fun CropState.rotRight() {
    transform = transform.copy(angleDeg = transform.angleDeg.next90())
}

fun CropState.flipHorizontal() {
    if ((transform.angleDeg / 90) % 2 == 0) flipX() else flipY()
}

fun CropState.flipVertical() {
    if ((transform.angleDeg / 90) % 2 == 0) flipY() else flipX()
}

fun CropState.flipX() {
    transform = transform.copy(scale = transform.scale.copy(x = -1 * transform.scale.x))
}

fun CropState.flipY() {
    transform = transform.copy(scale = transform.scale.copy(y = -1 * transform.scale.y))
}

fun updateRegion(old: Rect, new: Rect, bounds: Rect, aspectLock: Boolean): Rect {
    val offsetOnly = old.width.eq(new.width) && old.height.eq(new.height)
    return if (offsetOnly) new.constrainOffset(bounds)
    else {
        val result = when {
            aspectLock -> new.keepAspect(old).scaleToFit(bounds, old)
            else -> new.constrainResize(bounds)
        }
        return when {
            result.isEmpty -> result.setSize(old, Size(1f, 1f)).constrainOffset(bounds)
            else -> result
        }
    }
}