package com.rendly.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Highlight(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    val title: String = "",
    @SerialName("cover_url")
    val coverUrl: String? = null,
    val category: String = "CUSTOM",
    @SerialName("frame_style")
    val frameStyle: String = "CLASSIC",
    @SerialName("frame_color")
    val frameColor: String = "CATEGORY",
    @SerialName("background_color")
    val backgroundColor: String = "DEFAULT",
    val icon: String = "Star",
    @SerialName("stories_count")
    val storiesCount: Int = 0,
    @SerialName("is_new")
    val isNew: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)

@Serializable
data class HighlightStory(
    val id: String = "",
    @SerialName("highlight_id")
    val highlightId: String = "",
    @SerialName("story_id")
    val storyId: String? = null,
    @SerialName("media_url")
    val mediaUrl: String = "",
    @SerialName("media_type")
    val mediaType: String = "image",
    val position: Int = 0,
    @SerialName("created_at")
    val createdAt: String = ""
)
