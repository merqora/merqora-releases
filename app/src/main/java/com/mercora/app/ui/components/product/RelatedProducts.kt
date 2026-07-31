package com.mercora.app.ui.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.data.model.Post
import com.mercora.app.data.repository.PostRepository
import com.mercora.app.ui.components.UnifiedProductCard
import com.mercora.app.ui.components.toProductCardData
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RelatedProductsSection(
    posts: List<Post>,
    onPostClick: (Post) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Productos relacionados",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(posts.take(8)) { post ->
                Box(modifier = Modifier.width(170.dp).height(330.dp)) {
                    UnifiedProductCard(
                        data = post.toProductCardData(),
                        onClick = { onPostClick(post) },
                        imageHeight = 150.dp
                    )
                }
            }
        }
    }
}

@Composable
fun YouMightLikeSectionInfinite(
    currentPostId: String,
    onPostClick: (Post) -> Unit
) {
    val scope = rememberCoroutineScope()
    var allPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasMorePosts by remember { mutableStateOf(true) }
    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 20

    LaunchedEffect(currentPostId) {
        isLoading = true
        currentPage = 0
        allPosts = emptyList()
        hasMorePosts = true

        scope.launch {
            try {
                val posts = PostRepository.getPostsByCategory(
                    category = null,
                    excludePostId = currentPostId,
                    limit = pageSize,
                    offset = 0
                )
                allPosts = posts
                hasMorePosts = posts.size >= pageSize
                currentPage = 1
            } catch (e: Exception) {
                android.util.Log.e("ProductPage", "Error loading posts: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun loadMorePosts() {
        if (isLoadingMore || !hasMorePosts) return

        isLoadingMore = true
        scope.launch {
            try {
                val newPosts = PostRepository.getPostsByCategory(
                    category = null,
                    excludePostId = currentPostId,
                    limit = pageSize,
                    offset = currentPage * pageSize
                )

                if (newPosts.isNotEmpty()) {
                    allPosts = allPosts + newPosts
                    currentPage++
                    hasMorePosts = newPosts.size >= pageSize
                } else {
                    hasMorePosts = false
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductPage", "Error loading more posts: ${e.message}")
            } finally {
                isLoadingMore = false
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "también podráa gustarte",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (allPosts.isNotEmpty()) {
                Text(
                    text = "${allPosts.size}+ productos",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceElevated)
                    )
                }
            }
        } else if (allPosts.isNotEmpty()) {
            val chunkedPosts = allPosts.chunked(2)
            chunkedPosts.forEachIndexed { index, rowPosts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowPosts.forEach { post ->
                        Box(modifier = Modifier.weight(1f).height(330.dp)) {
                            UnifiedProductCard(
                                data = post.toProductCardData(),
                                onClick = { onPostClick(post) },
                                imageHeight = 150.dp
                            )
                        }
                    }
                    if (rowPosts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (index == chunkedPosts.size - 2 && hasMorePosts && !isLoadingMore) {
                    LaunchedEffect(index) {
                        loadMorePosts()
                    }
                }
            }

            if (isLoadingMore) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = PrimaryPurple,
                        strokeWidth = 3.dp
                    )
                }
            }

            if (hasMorePosts && !isLoadingMore) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable { loadMorePosts() },
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryPurple.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ver Más productos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryPurple
                        )
                    }
                }
            }

            if (!hasMorePosts && allPosts.size > pageSize) {
                Text(
                    text = "Has visto todos los productos",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
