package com.mercora.app.feed

/**
 * Feed item types matching the C++ enum FeedItemType.
 * Order MUST match the C++ enum exactly.
 */
enum class FeedItemType {
    POST_IMAGE,         // 0 - Image post
    POST_VIDEO,         // 1 - Video post (inline)
    REND_VIDEO,         // 2 - Short-form rend video
    SUGGESTED_ACCOUNTS, // 3 - Suggested accounts carousel
    SPECIAL_MODULE,     // 4 - Special module / promotion
    AD_SLOT             // 5 - Future ad placeholder
}

/**
 * Represents one item in the rendered feed.
 * The C++ engine decides order; Kotlin only renders.
 */
sealed class FeedItem {
    abstract val feedIndex: Int // Position in the feed

    data class PostItem(
        override val feedIndex: Int,
        val postIndex: Int // Index into the posts list
    ) : FeedItem()

    data class RendItem(
        override val feedIndex: Int,
        val rendIndex: Int // Index into the rends list
    ) : FeedItem()

    data class SuggestedAccountsItem(
        override val feedIndex: Int
    ) : FeedItem()

    data class SpecialModuleItem(
        override val feedIndex: Int,
        val moduleId: String = ""
    ) : FeedItem()
}
