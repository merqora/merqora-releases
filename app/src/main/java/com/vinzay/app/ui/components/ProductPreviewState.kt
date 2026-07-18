package com.vinzay.app.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import com.vinzay.app.data.model.Post

data class ProductPreviewConfig(
    val post: Post,
    val onContactSeller: ((Post) -> Unit)? = null
)

val LocalOpenProductPreview = staticCompositionLocalOf<(ProductPreviewConfig) -> Unit> { { } }
