package pl.nowinkitransferowe.core.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.toInstant
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserNewsResourceRepository
import pl.nowinkitransferowe.core.model.NewsCategory
import pl.nowinkitransferowe.core.model.NewsResource
import pl.nowinkitransferowe.core.model.mapToUserNewsResources
import pl.nowinkitransferowe.core.testing.repository.TestNewsRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import kotlin.test.assertEquals

class CompositeUserNewsResourceRepositoryTest {

    private val newsRepository = TestNewsRepository()
    private val userDataRepository = TestUserDataRepository()

    private val userNewsResourceRepository = CompositeUserNewsResourceRepository(
        newsRepository = newsRepository,
        userDataRepository = userDataRepository,
    )

    @Test
    fun whenNoFilters_allNewsResourcesAreReturned() = runTest {
        // Obtain the user news resources flow.
        val userNewsResources = userNewsResourceRepository.observeAll()

        // Send some news resources and user data into the data repositories.
        newsRepository.sendNewsResources(sampleNewsResources)

        // Construct the test user data with bookmarks and followed topics.
        val userData = emptyUserData.copy(
            bookmarkedNewsResources = setOf(sampleNewsResources[0].id, sampleNewsResources[2].id),
        )

        userDataRepository.setUserData(userData)

        // Check that the correct news resources are returned with their bookmarked state.
        assertEquals(
            sampleNewsResources.mapToUserNewsResources(userData),
            userNewsResources.first(),
        )
    }



    @Test
    fun whenFilteredByBookmarkedResources_matchingNewsResourcesAreReturned() = runTest {
        // Obtain the bookmarked user news resources flow.
        val userNewsResources = userNewsResourceRepository.observeAllBookmarked()

        // Send some news resources and user data into the data repositories.
        newsRepository.sendNewsResources(sampleNewsResources)

        // Construct the test user data with bookmarks and followed topics.
        val userData = emptyUserData.copy(
            bookmarkedNewsResources = setOf(sampleNewsResources[0].id, sampleNewsResources[2].id),
        )

        userDataRepository.setUserData(userData)

        // Check that the correct news resources are returned with their bookmarked state.
        assertEquals(
            listOf(sampleNewsResources[0], sampleNewsResources[2]).mapToUserNewsResources(userData),
            userNewsResources.first(),
        )
    }
}



val sampleNewsResources: List<NewsResource> = listOf(
    NewsResource(
        id = "1",
        title = "Oficjalnie: Luis Suarez w Interze Miami",
        description = "<p><strong>Imię i nazwisko:<\\/strong> Vitor Roque<\\/p>\\r\\n<p><strong>Poprzedni klub:<\\/strong> Athletico Paranaense<\\/p>\\r\\n<p><strong>Nowy klub:<\\/strong> FC Barcelona<\\/p>\\r\\n<p><strong>Kwota transferu:<\\/strong> 40 mln euro<\\/p>\\r\\n<p><strong>Długość kontraktu:<\\/strong> do 30 czerwca 2027 roku<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"en\\\">Lookin good, Vitor \uD83D\uDC4D <a href=\\\"https:\\/\\/t.co\\/qkFbK9sdBD\\\">pic.twitter.com\\/qkFbK9sdBD<\\/a><\\/p>\\r\\n&mdash; FC Barcelona (@FCBarcelona) <a href=\\\"https:\\/\\/twitter.com\\/FCBarcelona\\/status\\/1740002656180216260?ref_src=twsrc%5Etfw\\\">December 27, 2023<\\/a><\\/blockquote>\\r\\n<\\/center>",
        category = NewsCategory.ITALY,
        isImportant = true,
        publishDate = "2022-10-06T23:00:00.000Z".toInstant(),
        author = "Redakcja",
        photoSrc = "Inter Miami CF",
        src = "Inter Miami CF",
        imageUrl = "intermiaminw.jpg",
        authPic = "123.jpg",
        authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer",
        link = "Transfery\\/oficjalnie-luis-suarez-w-interze-miami",
        topics = listOf("Luis Suarez", "Inter Miami CF", "Gremio")
    ),

    NewsResource(
        id = "2",
        title = "Oficjalnie: Vitor Roque w Barcelonie",
        description = "<p><strong>Imię i nazwisko:<\\/strong> Vitor Roque<\\/p>\\r\\n<p><strong>Poprzedni klub:<\\/strong> Athletico Paranaense<\\/p>\\r\\n<p><strong>Nowy klub:<\\/strong> FC Barcelona<\\/p>\\r\\n<p><strong>Kwota transferu:<\\/strong> 40 mln euro<\\/p>\\r\\n<p><strong>Długość kontraktu:<\\/strong> do 30 czerwca 2027 roku<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"en\\\">Lookin good, Vitor \uD83D\uDC4D <a href=\\\"https:\\/\\/t.co\\/qkFbK9sdBD\\\">pic.twitter.com\\/qkFbK9sdBD<\\/a><\\/p>\\r\\n&mdash; FC Barcelona (@FCBarcelona) <a href=\\\"https:\\/\\/twitter.com\\/FCBarcelona\\/status\\/1740002656180216260?ref_src=twsrc%5Etfw\\\">December 27, 2023<\\/a><\\/blockquote>\\r\\n<\\/center>",
        category = NewsCategory.SPAIN,
        isImportant = true,
        author = "Redakcja",
        photoSrc = "FC Barcelona",
        src = "FC Barcelona",
        publishDate = "2022-10-06T23:00:00.000Z".toInstant(),
        imageUrl = "barcelonanw2.jpg",
        authPic = "123.jpg",
        authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer",
        link = "Transfery\\/oficjalnie-vitor-roque-w-barcelonie",
        topics = listOf("Vitor Roque", "FC Barcelona", "Athletico Paranaense")
    ),
    NewsResource(
        id = "3",
        title = "Oficjalnie: Bryan Zaragoza w Bayernie Monachium",
        description = "<p><strong>Imię i nazwisko:<\\/strong> Bryan Zaragoza<\\/p>\\r\\n<p><strong>Poprzedni klub:<\\/strong> Granada<\\/p>\\r\\n<p><strong>Nowy klub:<\\/strong> Bayern Monachium<\\/p>\\r\\n<p><strong>Data transferu:<\\/strong> 1 lipca 2024 roku<\\/p>\\r\\n<p><strong>Kwota transferu:<\\/strong> 13 mln euro<\\/p>\\r\\n<p><strong>Długość kontraktu:<\\/strong> do 30 czerwca 2029 roku<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"fi\\\">Bryan Zaragoza is set to join FC Bayern! \uD83D\uDD34⚪<br \\/><br \\/>\uD83D\uDD17 <a href=\\\"https:\\/\\/t.co\\/ShPCo6oQ5u\\\">https:\\/\\/t.co\\/ShPCo6oQ5u<\\/a><a href=\\\"https:\\/\\/twitter.com\\/hashtag\\/MiaSanMia?src=hash&amp;ref_src=twsrc%5Etfw\\\">#MiaSanMia<\\/a> <a href=\\\"https:\\/\\/t.co\\/QTry3HR6hK\\\">pic.twitter.com\\/QTry3HR6hK<\\/a><\\/p>\\r\\n&mdash; FC Bayern Munich (@FCBayernEN) <a href=\\\"https:\\/\\/twitter.com\\/FCBayernEN\\/status\\/1732378344509870571?ref_src=twsrc%5Etfw\\\">December 6, 2023<\\/a><\\/blockquote>\\r\\n<\\/center>",
        category = NewsCategory.ENGLAND,
        isImportant = true,
        author = "Redakcja",
        photoSrc = "Bayern Monachium",
        src = "Bayern Monachium",
        publishDate = "2022-10-06T23:00:00.000Z".toInstant(),
        imageUrl = "barcelonanw2.jpg",
        authPic = "123.jpg",
        authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer",
        link = "Transfery\\/oficjalnie-bryan-zaragoza-w-bayernie-monachium",
        topics = listOf("Bryan Zaragoza", "Bayern Monachium", "Granada CF")
    ),)

