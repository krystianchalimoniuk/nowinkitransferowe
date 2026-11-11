/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.nowinkitransferowe.core.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.NewsCategory
import pl.nowinkitransferowe.core.model.NewsResource
import pl.nowinkitransferowe.core.model.UserData
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.core.ui.PreviewParameterData.newsResources

/**
 * This [PreviewParameterProvider](https://developer.android.com/reference/kotlin/androidx/compose/ui/tooling/preview/PreviewParameterProvider)
 * provides list of [UserNewsResource] for Composable previews.
 */
class UserNewsResourcePreviewParameterProvider : PreviewParameterProvider<List<UserNewsResource>> {

    override val values: Sequence<List<UserNewsResource>> = sequenceOf(newsResources)
}

object PreviewParameterData {
    private val userData: UserData = UserData(
        bookmarkedNewsResources = setOf("1", "3"),
        viewedNewsResources = setOf("1", "2", "4"),
        bookmarkedTransferResources = setOf("2", "3"),
        viewedTransferResources = setOf("2,3"),
        darkThemeConfig = DarkThemeConfig.DARK,
        useDynamicColor = false,
        isNewsNotificationsAllowed = true,
        isTransfersNotificationsAllowed = true,
        isGeneralNotificationAllowed = true,
    )

    val newsResources = listOf(
        UserNewsResource(
            newsResource = NewsResource(
                id = "1",
                title = "Oficjalnie: Luis Suarez w Interze Miami",
                description = "<p><strong>Imię i nazwisko:<\\/strong> Vitor Roque<\\/p>\\r\\n<p><strong>Poprzedni klub:<\\/strong> Athletico Paranaense<\\/p>\\r\\n<p><strong>Nowy klub:<\\/strong> FC Barcelona<\\/p>\\r\\n<p><strong>Kwota transferu:<\\/strong> 40 mln euro<\\/p>\\r\\n<p><strong>Długość kontraktu:<\\/strong> do 30 czerwca 2027 roku<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"en\\\">Lookin good, Vitor \uD83D\uDC4D <a href=\\\"https:\\/\\/t.co\\/qkFbK9sdBD\\\">pic.twitter.com\\/qkFbK9sdBD<\\/a><\\/p>\\r\\n&mdash; FC Barcelona (@FCBarcelona) <a href=\\\"https:\\/\\/twitter.com\\/FCBarcelona\\/status\\/1740002656180216260?ref_src=twsrc%5Etfw\\\">December 27, 2023<\\/a><\\/blockquote>\\r\\n<\\/center>",
                category = NewsCategory.ITALY,
                isImportant = true,
                publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
                author = "Redakcja",
                photoSrc = "Inter Miami CF",
                src = "Inter Miami CF",
                imageUrl = "intermiaminw.jpg",
                authPic = "123.jpg",
                authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer", link = "Transfery\\/oficjalnie-luis-suarez-w-interze-miami",
                topics = listOf("Luis Suarez", "Inter Miami CF", "Gremio"),
            ),
            userData = userData,
        ),
        UserNewsResource(
            newsResource = NewsResource(
                id = "2",
                title = "Oficjalnie: Vitor Roque w Barcelonie",
                description = "<p><strong>Imię i nazwisko:<\\/strong> Vitor Roque<\\/p>\\r\\n<p><strong>Poprzedni klub:<\\/strong> Athletico Paranaense<\\/p>\\r\\n<p><strong>Nowy klub:<\\/strong> FC Barcelona<\\/p>\\r\\n<p><strong>Kwota transferu:<\\/strong> 40 mln euro<\\/p>\\r\\n<p><strong>Długość kontraktu:<\\/strong> do 30 czerwca 2027 roku<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"en\\\">Lookin good, Vitor \uD83D\uDC4D <a href=\\\"https:\\/\\/t.co\\/qkFbK9sdBD\\\">pic.twitter.com\\/qkFbK9sdBD<\\/a><\\/p>\\r\\n&mdash; FC Barcelona (@FCBarcelona) <a href=\\\"https:\\/\\/twitter.com\\/FCBarcelona\\/status\\/1740002656180216260?ref_src=twsrc%5Etfw\\\">December 27, 2023<\\/a><\\/blockquote>\\r\\n<\\/center>",
                category = NewsCategory.SPAIN,
                isImportant = true,
                author = "Redakcja",
                photoSrc = "FC Barcelona",
                src = "FC Barcelona",
                publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
                imageUrl = "barcelonanw2.jpg",
                authPic = "123.jpg",
                authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer", link = "Transfery\\/oficjalnie-vitor-roque-w-barcelonie",
                topics = listOf("Vitor Roque", "FC Barcelona", "Athletico Paranaense"),
            ),
            userData = userData,
        ),
        UserNewsResource(
            newsResource = NewsResource(
                id = "3",
                title = "Oficjalnie: Bryan Zaragoza w Bayernie Monachium",
                description = "<p><strong>Imię i nazwisko:<\\/strong> Bryan Zaragoza<\\/p>\\r\\n<p><strong>Poprzedni klub:<\\/strong> Granada<\\/p>\\r\\n<p><strong>Nowy klub:<\\/strong> Bayern Monachium<\\/p>\\r\\n<p><strong>Data transferu:<\\/strong> 1 lipca 2024 roku<\\/p>\\r\\n<p><strong>Kwota transferu:<\\/strong> 13 mln euro<\\/p>\\r\\n<p><strong>Długość kontraktu:<\\/strong> do 30 czerwca 2029 roku<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"fi\\\">Bryan Zaragoza is set to join FC Bayern! \uD83D\uDD34⚪<br \\/><br \\/>\uD83D\uDD17 <a href=\\\"https:\\/\\/t.co\\/ShPCo6oQ5u\\\">https:\\/\\/t.co\\/ShPCo6oQ5u<\\/a><a href=\\\"https:\\/\\/twitter.com\\/hashtag\\/MiaSanMia?src=hash&amp;ref_src=twsrc%5Etfw\\\">#MiaSanMia<\\/a> <a href=\\\"https:\\/\\/t.co\\/QTry3HR6hK\\\">pic.twitter.com\\/QTry3HR6hK<\\/a><\\/p>\\r\\n&mdash; FC Bayern Munich (@FCBayernEN) <a href=\\\"https:\\/\\/twitter.com\\/FCBayernEN\\/status\\/1732378344509870571?ref_src=twsrc%5Etfw\\\">December 6, 2023<\\/a><\\/blockquote>\\r\\n<\\/center>",
                category = NewsCategory.ENGLAND,
                isImportant = true,
                author = "Redakcja",
                photoSrc = "Bayern Monachium",
                src = "Bayern Monachium",
                publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
                imageUrl = "barcelonanw2.jpg",
                authPic = "123.jpg",
                authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer",
                link = "Transfery\\/oficjalnie-bryan-zaragoza-w-bayernie-monachium",
                topics = listOf("Bryan Zaragoza", "Bayern Monachium", "Granada CF"),
            ),
            userData = userData,
        ),
    )
}
