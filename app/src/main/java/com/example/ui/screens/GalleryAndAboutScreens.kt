package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import com.example.ui.theme.AppIcons
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ChatMessageEntity
import com.example.data.GalleryItemEntity
import com.example.data.SiteConfigEntity
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings

@Composable
fun GalleryPhotoView(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (url == "local://portrait" || url.contains("chitron-bhattacharjee")) {
        Image(
            painter = painterResource(id = R.drawable.img_chitron_portrait),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            error = painterResource(id = R.drawable.img_hero_banner),
            placeholder = painterResource(id = R.drawable.img_hero_banner)
        )
    }
}

@Composable
fun GalleryScreen(
    galleryItems: List<GalleryItemEntity>,
    selectedCategory: String,
    lightboxIndex: Int?,
    lang: AppLanguage,
    onSelectCategory: (String) -> Unit,
    onOpenLightbox: (Int) -> Unit,
    onCloseLightbox: () -> Unit
) {
    val categoriesWithCount = remember(galleryItems) {
        galleryItems.groupBy { it.category }.mapValues { it.value.size }
    }
    val filteredPhotos = remember(galleryItems, selectedCategory) {
        if (selectedCategory == "all" || selectedCategory.isBlank()) {
            galleryItems
        } else {
            galleryItems.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    // Full-screen Lightbox Dialog
    if (lightboxIndex != null && filteredPhotos.isNotEmpty()) {
        val safeIdx = lightboxIndex.coerceIn(0, filteredPhotos.lastIndex)
        val activePhoto = filteredPhotos[safeIdx]
        Dialog(
            onDismissRequest = onCloseLightbox,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xF2121110)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top bar with counter & close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${safeIdx + 1} / ${filteredPhotos.size}",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        IconButton(
                            onClick = onCloseLightbox,
                            modifier = Modifier.testTag("lightbox_close_btn")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close lightbox",
                                tint = Color.White
                            )
                        }
                    }

                    // Center image
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        GalleryPhotoView(
                            url = activePhoto.url,
                            contentDescription = activePhoto.alt.ifBlank { activePhoto.title },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Bottom metadata + Prev/Next controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = activePhoto.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        if (activePhoto.caption.isNotBlank()) {
                            Text(
                                text = activePhoto.caption,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD5CFC7)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activePhoto.category} • ${activePhoto.location}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFC49A6C)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = {
                                        val prev = if (safeIdx > 0) safeIdx - 1 else filteredPhotos.lastIndex
                                        onOpenLightbox(prev)
                                    }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous photo",
                                        tint = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val next = if (safeIdx < filteredPhotos.lastIndex) safeIdx + 1 else 0
                                        onOpenLightbox(next)
                                    }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next photo",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("gallery_screen_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = AppStrings.t("gallery.title", lang),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${AppStrings.formatNumber(filteredPhotos.size, lang)} ${AppStrings.t("gallery.photosCount", lang)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = AppStrings.t("gallery.subtitle", lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Category filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "all",
                        onClick = { onSelectCategory("all") },
                        label = {
                            Text("${AppStrings.t("gallery.all", lang)} (${AppStrings.formatNumber(galleryItems.size, lang)})")
                        }
                    )
                    categoriesWithCount.forEach { (cat, count) ->
                        FilterChip(
                            selected = selectedCategory.equals(cat, ignoreCase = true),
                            onClick = { onSelectCategory(cat) },
                            label = { Text("$cat (${AppStrings.formatNumber(count, lang)})") }
                        )
                    }
                }
            }
        }

        itemsIndexed(filteredPhotos, key = { _, item -> item.id }) { index, photo ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .clickable { onOpenLightbox(index) }
                    .testTag("gallery_card_${photo.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        GalleryPhotoView(
                            url = photo.url,
                            contentDescription = photo.alt.ifBlank { photo.title },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Icon(
                                AppIcons.OpenInFull,
                                contentDescription = AppStrings.t("gallery.viewFullscreen", lang),
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(16.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = photo.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = formatEditorialDate(photo.dateMillis, lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = photo.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (photo.caption.isNotBlank()) {
                            Text(
                                text = photo.caption,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (photo.location.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = photo.location,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutScreen(
    siteConfig: SiteConfigEntity,
    lang: AppLanguage,
    onReadWritingClick: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val roles = if (lang == AppLanguage.BN) AppStrings.rolesBn else AppStrings.rolesEn
    val interests = if (lang == AppLanguage.BN) AppStrings.interestsBn else AppStrings.interestsEn

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("about_screen_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_chitron_portrait),
                            contentDescription = "Chitron Bhattacharjee",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (lang == AppLanguage.BN) "চিত্রন ভট্টাচার্য" else siteConfig.authorName,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = siteConfig.authorTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = siteConfig.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = if (lang == AppLanguage.BN) AppStrings.t("about.headline", lang)
                        else siteConfig.aboutHeadline.ifBlank { AppStrings.t("about.headline", lang) },
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = if (lang == AppLanguage.BN) {
                            AppStrings.t("about.shortBio1", lang) + "\n\n" + AppStrings.t("about.shortBio2", lang)
                        } else {
                            siteConfig.aboutShortBio
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // About & What I Do
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = AppStrings.t("about.secAbout", lang),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = if (lang == AppLanguage.BN) {
                            AppStrings.t("about.bio1", lang) + "\n\n" + AppStrings.t("about.bio2", lang)
                        } else {
                            siteConfig.aboutBiography
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline
                    )

                    Text(
                        text = AppStrings.t("about.secRoles", lang),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    roles.forEach { role ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(
                                text = role,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        // Selected Projects (ShiPu AI, Chitron's Archive)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = AppStrings.t("about.secProjects", lang),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    AppIcons.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (lang == AppLanguage.BN) "শীপু এআই (ShiPu AI)" else "ShiPu AI",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Text(
                                text = if (lang == AppLanguage.BN) {
                                    "শীপু এআই (ShiPu AI) আমার একটি চলমান কনভার্সেশনাল এআই প্রজেক্ট। এর মূল লক্ষ্য হলো একটি কার্যকর ও নমনীয় এআই সিস্টেম তৈরি করা যা স্বাভাবিকভাবে যোগাযোগ করতে পারে এবং বাস্তব জীবনের বিভিন্ন কাজ সম্পন্ন করতে পারে।"
                                } else {
                                    "ShiPu AI is one of my ongoing projects focused on conversational AI. The goal is to build a useful and flexible AI system that can communicate naturally and perform practical tasks."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (lang == AppLanguage.BN) {
                                    "তৈরি: Node.js, JavaScript এবং আধুনিক ওয়েব প্রযুক্তি।"
                                } else {
                                    "Built with: Node.js, JavaScript, LLM APIs, and modern web technologies."
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Technology & Interests
                    Text(
                        text = AppStrings.t("about.secSkills", lang),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = AppStrings.t("about.skillsIntro", lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        siteConfig.skillsList.forEach { skill ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = skill,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = AppStrings.t("about.skillsOutro", lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = AppStrings.t("about.secInterests", lang),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        interests.forEach { interest ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = interest,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Philosophy, Writing, Currently, and Contact
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = AppStrings.t("about.secPhilosophy", lang),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = if (lang == AppLanguage.BN) {
                            AppStrings.t("about.philosophy1", lang) + "\n\n" + AppStrings.t("about.philosophy2", lang)
                        } else {
                            siteConfig.aboutPhilosophy
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Text(
                        text = AppStrings.t("about.secWriting", lang),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = AppStrings.t("about.writingDesc", lang),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onReadWritingClick) {
                        Text(AppStrings.t("about.readWriting", lang))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Text(
                        text = AppStrings.t("about.secCurrently", lang),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = if (lang == AppLanguage.BN) AppStrings.t("about.currently", lang)
                        else siteConfig.aboutCurrently,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Text(
                        text = AppStrings.t("about.secContact", lang),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = AppStrings.t("about.contactIntro", lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            "GitHub" to "https://github.com/AdiBhaiAlpha",
                            "Facebook" to "https://facebook.com/ssfadi",
                            "Instagram" to "https://instagram.com/im.chitron",
                            "Bio Link" to "https://bio.link/chitron"
                        ).forEach { (label, url) ->
                            OutlinedButton(
                                onClick = { uriHandler.openUri(url) },
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(label)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    AppIcons.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VisitorChatBottomSheet(
    visitorName: String,
    messages: List<ChatMessageEntity>,
    lang: AppLanguage,
    onUpdateVisitorName: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var messageDraft by remember { mutableStateOf("") }
    var nameDraft by remember(visitorName) { mutableStateOf(visitorName) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_chitron_portrait),
                    contentDescription = "Chitron Bhattacharjee",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.t("chat.title", lang),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = AppStrings.t("chat.subtitleOnline", lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close chat")
                }
            }

            OutlinedTextField(
                value = nameDraft,
                onValueChange = {
                    nameDraft = it
                    onUpdateVisitorName(it)
                },
                label = { Text(AppStrings.t("chat.visitorNameLabel", lang)) },
                placeholder = { Text(AppStrings.t("chat.visitorNamePlaceholder", lang)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Quick starter chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("chat.starter1", "chat.starter2", "chat.starter3").forEach { key ->
                    val text = AppStrings.t(key, lang)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { onSendMessage(text) }
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // Conversation messages
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Text(
                            text = AppStrings.t("chat.welcomeDesc", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        val isVisitor = msg.sender == "visitor"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isVisitor) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                color = if (isVisitor) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isVisitor) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageDraft,
                    onValueChange = { messageDraft = it },
                    placeholder = { Text(AppStrings.t("chat.placeholder", lang)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("visitor_chat_input"),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (messageDraft.isNotBlank()) {
                            onSendMessage(messageDraft)
                            messageDraft = ""
                        }
                    },
                    modifier = Modifier.testTag("visitor_chat_send_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = AppStrings.t("chat.send", lang))
                }
            }
        }
    }
}
