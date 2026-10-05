package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import com.example.ui.theme.AppIcons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.BlogPostEntity
import com.example.data.SiteConfigEntity
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatEditorialDate(millis: Long, lang: AppLanguage): String {
    val locale = if (lang == AppLanguage.BN) Locale.forLanguageTag("bn-BD") else Locale.US
    val sdf = SimpleDateFormat("MMM d, yyyy", locale)
    return sdf.format(Date(millis))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    posts: List<BlogPostEntity>,
    siteConfig: SiteConfigEntity,
    lang: AppLanguage,
    onReadWritingClick: () -> Unit,
    onAboutClick: () -> Unit,
    onPostClick: (Long) -> Unit,
    onCategoryClick: (String) -> Unit
) {
    val publishedPosts = remember(posts) {
        posts.filter { it.status == "published" }.sortedByDescending { it.publishedAt }
    }
    val categories = remember(publishedPosts) {
        publishedPosts.map { it.category }.distinct().ifEmpty {
            listOf("ai", "web-development", "reflections")
        }
    }
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Editorial Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Chitron's Archive study desk illustration",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (lang == AppLanguage.BN) {
                                AppStrings.t("home.heroTitle", lang)
                            } else {
                                siteConfig.heroTitle.ifBlank { AppStrings.t("home.heroTitle", lang) }
                            },
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (lang == AppLanguage.BN) {
                                AppStrings.t("home.heroDesc", lang)
                            } else {
                                siteConfig.heroDescription.ifBlank { AppStrings.t("home.heroDesc", lang) }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Button(
                                onClick = onReadWritingClick,
                                modifier = Modifier.testTag("home_cta_writing")
                            ) {
                                Text(
                                    if (lang == AppLanguage.BN) AppStrings.t("home.ctaPrimary", lang)
                                    else siteConfig.primaryButtonText.ifBlank { AppStrings.t("home.ctaPrimary", lang) }
                                )
                            }
                            OutlinedButton(
                                onClick = onAboutClick,
                                modifier = Modifier.testTag("home_cta_about"),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    if (lang == AppLanguage.BN) AppStrings.t("home.ctaSecondary", lang)
                                    else siteConfig.secondaryButtonText.ifBlank { AppStrings.t("home.ctaSecondary", lang) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Author Sidebar Profile Widget (from index.html)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_chitron_portrait),
                        contentDescription = "Chitron Bhattacharjee portrait",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = AppStrings.t("sidebar.aboutTitle", lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = AppStrings.t("sidebar.aboutRole", lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = AppStrings.t("sidebar.aboutBio", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf("#AI", "#NodeJS", "#WebDev", "#Writing").forEach { tag ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "GitHub ↗",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    uriHandler.openUri("https://github.com/AdiBhaiAlpha")
                                }
                            )
                            Text(
                                text = AppStrings.t("sidebar.viewProfile", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onAboutClick() }
                            )
                        }
                    }
                }
            }
        }

        // Explore Topics Chips
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = AppStrings.t("sidebar.categoriesTitle", lang),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = false,
                            onClick = { onCategoryClick(cat) },
                            label = { Text(cat.uppercase()) },
                            modifier = Modifier.testTag("home_topic_$cat")
                        )
                    }
                }
            }
        }

        // Latest Writing Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == AppLanguage.BN) {
                        AppStrings.t("home.latestWriting", lang)
                    } else {
                        siteConfig.featuredSectionTitle.ifBlank { AppStrings.t("home.latestWriting", lang) }
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(
                    onClick = onReadWritingClick,
                    modifier = Modifier.testTag("home_view_all_writing")
                ) {
                    Text(AppStrings.t("home.viewAll", lang))
                }
            }
        }

        // Latest Posts List
        items(publishedPosts.take(5), key = { it.id }) { post ->
            ArticleCard(
                post = post,
                lang = lang,
                showCover = true,
                onClick = { onPostClick(post.id) },
                onTagClick = null
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleCard(
    post: BlogPostEntity,
    lang: AppLanguage,
    showCover: Boolean,
    onClick: () -> Unit,
    onTagClick: ((String) -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .clickable(onClick = onClick)
            .testTag("article_card_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (showCover && post.coverImage.isNotBlank()) {
                AsyncImage(
                    model = post.coverImage,
                    contentDescription = post.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.img_hero_banner),
                    placeholder = painterResource(id = R.drawable.img_hero_banner)
                )
            }
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = post.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatEditorialDate(post.publishedAt, lang),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = AppStrings.formatReadTime(post.readingTime, lang),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (post.excerpt.isNotBlank()) {
                    Text(
                        text = post.excerpt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (post.labelsList.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        post.labelsList.forEach { label ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp),
                                modifier = if (onTagClick != null) {
                                    Modifier.clickable { onTagClick(label) }
                                } else {
                                    Modifier
                                }
                            ) {
                                Text(
                                    text = "#$label",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WritingScreen(
    posts: List<BlogPostEntity>,
    searchQuery: String,
    selectedCategory: String,
    selectedTag: String,
    sortNewest: Boolean,
    showAdminShortcutModal: Boolean,
    lang: AppLanguage,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onTagSelect: (String) -> Unit,
    onSortToggle: (Boolean) -> Unit,
    onPostClick: (Long) -> Unit,
    onDismissAdminShortcut: () -> Unit,
    onConfirmAdminShortcut: () -> Unit
) {
    val publishedPosts = remember(posts) {
        posts.filter { it.status == "published" }
    }
    val categories = remember(publishedPosts) {
        publishedPosts.map { it.category }.distinct().sorted()
    }
    val filteredPosts = remember(publishedPosts, searchQuery, selectedCategory, selectedTag, sortNewest) {
        var list = publishedPosts
        if (selectedCategory.isNotBlank()) {
            list = list.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        if (selectedTag.isNotBlank()) {
            list = list.filter { post ->
                post.labelsList.any { it.equals(selectedTag, ignoreCase = true) }
            }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                    it.excerpt.lowercase().contains(q) ||
                    it.content.lowercase().contains(q) ||
                    it.labelsCsv.lowercase().contains(q)
            }
        }
        if (sortNewest) {
            list.sortedByDescending { it.publishedAt }
        } else {
            list.sortedBy { it.publishedAt }
        }
    }

    if (showAdminShortcutModal) {
        AlertDialog(
            onDismissRequest = onDismissAdminShortcut,
            title = { Text("Admin Access") },
            text = { Text("Open Chitron's Archive Admin CMS & Live Messenger?") },
            confirmButton = {
                Button(
                    onClick = onConfirmAdminShortcut,
                    modifier = Modifier.testTag("confirm_admin_modal_btn")
                ) {
                    Text("Go to Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissAdminShortcut) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("writing_screen_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = AppStrings.t("writing.title", lang),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("writing_search_input"),
                    placeholder = { Text(AppStrings.t("writing.searchPlaceholder", lang)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search articles")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true
                )

                // Category & Sort Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedCategory.isEmpty(),
                        onClick = { onCategorySelect("") },
                        label = { Text(AppStrings.t("writing.allCategories", lang)) }
                    )
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory.equals(cat, ignoreCase = true),
                            onClick = {
                                onCategorySelect(
                                    if (selectedCategory.equals(cat, ignoreCase = true)) "" else cat
                                )
                            },
                            label = { Text(cat.uppercase()) }
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = { onSortToggle(!sortNewest) },
                        label = {
                            Text(
                                if (sortNewest) AppStrings.t("writing.newestFirst", lang)
                                else AppStrings.t("writing.oldestFirst", lang)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                if (selectedTag.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tag: #$selectedTag",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = { onTagSelect("") }) {
                            Text("Clear tag")
                        }
                    }
                }
            }
        }

        if (filteredPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.t("writing.noPosts", lang),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredPosts, key = { it.id }) { post ->
                ArticleCard(
                    post = post,
                    lang = lang,
                    showCover = false,
                    onClick = { onPostClick(post.id) },
                    onTagClick = { tag -> onTagSelect(tag) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostDetailScreen(
    post: BlogPostEntity?,
    allPublishedPosts: List<BlogPostEntity>,
    lang: AppLanguage,
    onBack: () -> Unit,
    onSelectPost: (Long) -> Unit,
    onTagClick: (String) -> Unit
) {
    BackHandler { onBack() }

    if (post == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Article not found.", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) {
                    Text(AppStrings.t("post.back", lang))
                }
            }
        }
        return
    }

    val sortedPosts = remember(allPublishedPosts) {
        allPublishedPosts.filter { it.status == "published" }.sortedByDescending { it.publishedAt }
    }
    val currentIndex = sortedPosts.indexOfFirst { it.id == post.id }
    val newerPost = if (currentIndex > 0) sortedPosts[currentIndex - 1] else null
    val olderPost = if (currentIndex >= 0 && currentIndex < sortedPosts.size - 1) sortedPosts[currentIndex + 1] else null

    val blocks = remember(post.content) {
        post.content.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("post_detail_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onBack,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.testTag("post_back_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.t("post.back", lang))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = post.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = formatEditorialDate(post.publishedAt, lang),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        AppIcons.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = AppStrings.formatReadTime(post.readingTime, lang),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        AppIcons.RemoveRedEye,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = AppStrings.formatNumber(post.viewCount, lang),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = post.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "By ${post.author}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (post.coverImage.isNotBlank()) {
                    AsyncImage(
                        model = post.coverImage,
                        contentDescription = post.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.img_hero_banner),
                        placeholder = painterResource(id = R.drawable.img_hero_banner)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }

        // Rich Article Content Blocks
        items(blocks) { paragraph ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                when {
                    paragraph.startsWith("### ") -> {
                        Text(
                            text = paragraph.removePrefix("### ").trim(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    paragraph.startsWith("## ") -> {
                        Text(
                            text = paragraph.removePrefix("## ").trim(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    paragraph.startsWith("> ") -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(48.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = paragraph.removePrefix("> ").trim(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = paragraph.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // Labels / Tags
        if (post.labelsList.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        post.labelsList.forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onTagClick(tag) }
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Previous / Next Post Navigation
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .padding(top = 12.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (olderPost != null) {
                    OutlinedButton(
                        onClick = { onSelectPost(olderPost.id) },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = AppStrings.t("post.prev", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = olderPost.title,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (newerPost != null) {
                    OutlinedButton(
                        onClick = { onSelectPost(newerPost.id) },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = AppStrings.t("post.next", lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = newerPost.title,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
