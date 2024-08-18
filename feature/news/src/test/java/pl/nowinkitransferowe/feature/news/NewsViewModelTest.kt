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

package pl.nowinkitransferowe.feature.news

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.toInstant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserNewsResourceRepository
import pl.nowinkitransferowe.core.model.NewsCategory
import pl.nowinkitransferowe.core.model.NewsResource
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.core.testing.repository.TestNewsRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import pl.nowinkitransferowe.core.testing.util.TestSyncManager
import pl.nowinkitransferowe.core.ui.NewsFeedUiState
import kotlin.test.assertEquals

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class NewsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val syncManager = TestSyncManager()
    private val userDataRepository = TestUserDataRepository()
    private val newsRepository = TestNewsRepository()

    private val userNewsResourceRepository = CompositeUserNewsResourceRepository(
        newsRepository = newsRepository,
        userDataRepository = userDataRepository,
    )

    private val savedStateHandle = SavedStateHandle()
    private lateinit var viewModel: NewsViewModel

    @Before
    fun setup() {
        viewModel = NewsViewModel(
            syncManager = syncManager,
            savedStateHandle = savedStateHandle,
            userDataRepository = userDataRepository,
            userNewsResourceRepository = userNewsResourceRepository,
        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(NewsFeedUiState.Loading, viewModel.feedState.value)
    }

    @Test
    fun stateIsLoadingWhenAppIsSyncingWithNoNews() = runTest {
        syncManager.setSyncing(true)

        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.isSyncing.collect() }

        assertEquals(
            true,
            viewModel.isSyncing.value,
        )

        collectJob.cancel()
    }

    @Test
    fun whenUpdateNewsResourceSavedIsCalled_bookmarkStateIsUpdated() = runTest {
        val newsResourceId = "123"
        viewModel.updateNewsResourceSaved(newsResourceId, true)

        assertEquals(
            expected = setOf(newsResourceId),
            actual = userDataRepository.userData.first().bookmarkedNewsResources,
        )

        viewModel.updateNewsResourceSaved(newsResourceId, false)

        assertEquals(
            expected = emptySet(),
            actual = userDataRepository.userData.first().bookmarkedNewsResources,
        )
    }

    @Test
    fun newsResourceSelectionUpdatesAfterLoading() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }
        val userData = emptyUserData
        newsRepository.sendNewsResources(sampleNewsResources)

        val bookmarkedNewsResourceId = "2"
        viewModel.updateNewsResourceSaved(
            newsResourceId = bookmarkedNewsResourceId,
            isChecked = true,
        )

        val userDataExpected = userData.copy(
            bookmarkedNewsResources = setOf(bookmarkedNewsResourceId),
        )

        val expected = NewsFeedUiState.Success(
            feed = listOf(
                UserNewsResource(newsResource = sampleNewsResources[0], userDataExpected),
                UserNewsResource(newsResource = sampleNewsResources[1], userDataExpected),
                UserNewsResource(newsResource = sampleNewsResources[2], userDataExpected),
                UserNewsResource(newsResource = sampleNewsResources[3], userDataExpected),
                UserNewsResource(newsResource = sampleNewsResources[4], userDataExpected),
            ),
        )
        assertEquals(
            expected,
            viewModel.feedState.value,
        )

        collectJob.cancel()
    }

    @Test
    fun whenLoadNexPageFunctionIsCalled_newsResourcesUpdates() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }
        val userData = emptyUserData
        newsRepository.sendNewsResources(sampleNewsResources)
        val bookmarkedNewsResourceId = "2"
        viewModel.updateNewsResourceSaved(
            newsResourceId = bookmarkedNewsResourceId,
            isChecked = true,
        )

        val userDataExpected = userData.copy(
            bookmarkedNewsResources = setOf(bookmarkedNewsResourceId),
        )
        viewModel.loadNextPage(viewModel.page.value, sampleNewsResources.size)
        assertEquals(
            NewsFeedUiState.Success(
                feed = listOf(
                    UserNewsResource(newsResource = sampleNewsResources[0], userDataExpected),
                    UserNewsResource(newsResource = sampleNewsResources[1], userDataExpected),
                    UserNewsResource(newsResource = sampleNewsResources[2], userDataExpected),
                    UserNewsResource(newsResource = sampleNewsResources[3], userDataExpected),
                    UserNewsResource(newsResource = sampleNewsResources[4], userDataExpected),
                    UserNewsResource(newsResource = sampleNewsResources[5], userDataExpected),
                    UserNewsResource(newsResource = sampleNewsResources[6], userDataExpected),
                    UserNewsResource(newsResource = sampleNewsResources[7], userDataExpected),
                    UserNewsResource(newsResource = sampleNewsResources[8], userDataExpected),
                    UserNewsResource(newsResource = sampleNewsResources[9], userDataExpected),
                ),
            ),
            viewModel.feedState.value,
        )

        collectJob.cancel()
    }

    val sampleNewsResources = listOf(
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
            topics = listOf("Luis Suarez", "Inter Miami CF", "Gremio"),
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
            topics = listOf("Vitor Roque", "FC Barcelona", "Athletico Paranaense"),
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
            topics = listOf("Bryan Zaragoza", "Bayern Monachium", "Granada CF"),
        ),
        NewsResource(
            id = "4",
            title = "Oficjalnie: Moussa Diaby w Al-Ittihad",
            description = "<p><strong>Moussa Diaby zgodnie z przewidywaniami zagra w Arabii Saudyjskiej.<\\/strong><\\/p>\\r\\n<p>Al-Ittihad za pośrednictwem swojej oficjalnej strony internetowej oraz zweryfikowanych medi&oacute;w społecznościowych poinformował o sprowadzeniu na zasadzie transferu definitywnego Moussy Diaby'ego z Aston Villia. Prawy napastnik po pomyślnym przejściu test&oacute;w medycznych związał się z nowym pracodawcą pięcioletnim kontraktem, obowiązującym do 30 czerwca 2029 roku.<\\/p>\\r\\n<p>Kwota tej transakcji wyniosła 60 milion&oacute;w euro, co oznacza, że <em>Tygrysy<\\/em> dokonały najdroższego zakupu w swojej historii. 11-krotny reprezentant Francji z powodzeniem występował w barwach zespołu <em>The Villans<\\/em> od lipca 2023 roku, gdy przeprowadził się na Villa Park za 55 milion&oacute;w euro z Bayeru Leverkusen.<\\/p>\\r\\n<p>25-letni Moussa Diaby w 54 spotkaniach poprzedniego sezonu zdobył 10 bramek i zanotował 9 asyst. Portal \\\"Transfermarkt\\\" wycenia skrzydłowego na 55 milion&oacute;w euro. Przypomnijmy, że koszulkę Al-Ittihad na co dzień przywdziewają takie gwiazdy jak między innymi Karim Benzema, N'Golo Kante oraz Fabinho.<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"qme\\\">\uD83D\uDCF8 \uD83D\uDC9B\uD83D\uDDA4 <a href=\\\"https:\\/\\/t.co\\/SW0fBOAQm2\\\">pic.twitter.com\\/SW0fBOAQm2<\\/a><\\/p>\\r\\n&mdash; نادي الاتحاد السعودي (@ittihad) <a href=\\\"https:\\/\\/twitter.com\\/ittihad\\/status\\/1816241672038351001?ref_src=twsrc%5Etfw\\\">July 24, 2024<\\/a><\\/blockquote>\\r\\n<\\/center>",
            category = NewsCategory.TRANSFERS,
            isImportant = true,
            author = "Redakcja",
            photoSrc = "Al-Ittihad",
            src = "Al-Ittihad",
            publishDate = "2022-10-06T23:00:00.000Z".toInstant(),
            imageUrl = "barcelonanw2.jpg",
            authPic = "123.jpg",
            authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer",
            link = "Transfery\\/oficjalnie-bryan-zaragoza-w-bayernie-monachium",
            topics = listOf("Moussa Diaby", "Al Itihad", "Aston Villa"),
        ),
        NewsResource(
            id = "5",
            title = "Oficjalnie: Amadou Onana w Aston Villi",
            description = "<p><strong><em>Lwy<\\/em> przeprowadziły najdroższy transfer w swojej historii.<\\/strong><\\/p>\\r\\n<p>Aston Villa za pośrednictwem swojej oficjalnej strony internetowej poinformowała o pozyskaniu na zasadzie transferu definitywnego Amadou Onany z Evertonu. Defensywny pomocnik po pozytywnym przejściu test&oacute;w medycznych związał się z nowym pracodawcą długoterminowym pięcioletnim kontraktem, obowiązującym do końca czerwca 2029 roku.<\\/p>\\r\\n<p>Kwota tej transakcji wyniosła niespełna 60 milion&oacute;w euro, a to oznacza, że klub z Birmingham dokonał rekordowego transferu w swojej historii. Etatowy reprezentant Belgii z powodzeniem występował w barwach drużyny popularnych <em>The Toffees<\\/em> od sierpnia 2022 roku, gdy trafił na Goodison Park za prawie 40 milion&oacute;w euro z LOSC Lille.<\\/p>\\r\\n<p>22-letni Amadou Onana w 37 spotkaniach poprzedniego sezonu zdobył 3 bramki i zaliczył 1 asystę. Portal \\\"Transfermarkt\\\" wycenia go na 50 milion&oacute;w euro.<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"en\\\">Aston Villa is delighted to announce the signing of Amadou Onana from Everton. ✍\uFE0F<\\/p>\\r\\n&mdash; Aston Villa (@AVFCOfficial) <a href=\\\"https:\\/\\/twitter.com\\/AVFCOfficial\\/status\\/1815393894621933770?ref_src=twsrc%5Etfw\\\">July 22, 2024<\\/a><\\/blockquote>\\r\\n<\\/center>",
            category = NewsCategory.TRANSFERS,
            isImportant = true,
            author = "Redakcja",
            photoSrc = "Aston Villa",
            src = "Aston Villa",
            publishDate = "2022-10-06T23:00:00.000Z".toInstant(),
            imageUrl = "astonvillanw.jpg",
            authPic = "123.jpg",
            authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer",
            link = "Transfery\\/oficjalnie-amadou-onana-w-aston-villi",
            topics = listOf("Amadou Onana", "Aston Villa", "Everton FC"),
        ),
        NewsResource(
            id = "6",
            title = "Oficjalnie: Leny Yoro w Manchesterze United",
            description = "<p><strong><em>Czerwone Diabły<\\/em> dokonały hitu transferowego.<\\/strong><\\/p>\\r\\n<p>Manchester United za pośrednictwem swojej oficjalnej strony internetowej poinformował o pozyskaniu na zasadzie transferu definitywnego Leny'ego Yoro z LOSC Lille. Środkowy obrońca po pozytywnym przejściu test&oacute;w medycznych związał się z nowym pracodawcą pięcioletnim kontraktem, obowiązującym do końca czerwca 2029 roku.<\\/p>\\r\\n<p>Kwota tej transakcji wyniosła aż 62 miliony euro i co ważne, może jeszcze wzrosnąć o ewentualne bonusy. Młodzieżowy reprezentant Francji z powodzeniem występował w barwach drużyny popularnych <em>Mastif&oacute;w<\\/em> od początku swojej kariery. Bardzo utalentowany osiemnastolatek do pierwszego zespołu Lille został włączony w lipcu 2022 roku.<\\/p>\\r\\n<p>Leny Yoro na przestrzeni poprzedniego sezonu rozegrał 44 spotkania i zdobył 3 bramki. Portal \\\"Transfermarkt\\\" wycenia go na około 50 milion&oacute;w euro. Co ciekawe, o zdolnego defensora zabiegał r&oacute;wnież Real Madryt, jednak Kr&oacute;lewscy nie chcieli spełnić żądań finansowych francuskiego klubu.<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"en\\\">\uD83D\uDD34 This is home.<br \\/><br \\/>Bienvenue a Manchester United, <a href=\\\"https:\\/\\/twitter.com\\/leny_yoro?ref_src=twsrc%5Etfw\\\">@Leny_Yoro<\\/a>! \uD83D\uDE4C<br \\/><a href=\\\"https:\\/\\/twitter.com\\/hashtag\\/MUFC?src=hash&amp;ref_src=twsrc%5Etfw\\\">#MUFC<\\/a><\\/p>\\r\\n&mdash; Manchester United (@ManUtd) <a href=\\\"https:\\/\\/twitter.com\\/ManUtd\\/status\\/1814012288854339622?ref_src=twsrc%5Etfw\\\">July 18, 2024<\\/a><\\/blockquote>\\r\\n<\\/center>",
            category = NewsCategory.TRANSFERS,
            isImportant = true,
            author = "Redakcja",
            photoSrc = "Manchester United",
            src = "Manchester United",
            publishDate = "2022-10-06T23:00:00.000Z".toInstant(),
            imageUrl = "manunitednw.jpg",
            authPic = "123.jpg",
            authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer",
            link = "Transfery\\/oficjalnie-amadou-onana-w-aston-villi",
            topics = listOf("Leny Yoro", "Manchester United", "LOSC Lille"),
        ),
        NewsResource(
            id = "7",
            title = "Oficjalnie: Savio w Manchesterze City",
            description = "<p><strong>Savinho od przyszłego sezonu będzie grał na Etihad Stadium.<\\/strong><\\/p>\\r\\n<p>Manchester City za pośrednictwem swojej oficjalnej strony internetowej poinformował o sprowadzeniu na zasadzie transferu definitywnego Savio z ESTAC Troyes. Prawoskrzydłowy po pozytywnym przejściu test&oacute;w medycznych związał się z nowym pracodawcą pięcioletnim kontraktem, obowiązującym do końca czerwca 2029 roku.<\\/p>\\r\\n<p>Kwota tej transakcji wyniosła 25 milion&oacute;w euro. Warto dodać, że oba kluby uczestniczące w tym transferze należą do jednej sp&oacute;łki - City Football Group. Siedmiokrotny reprezentant Brazylii w ostatnim czasie na podstawie wypożyczenia występował w hiszpańskiej Gironie, gdzie prezentował bardzo wysoką formę.<\\/p>\\r\\n<p>Savinho znany także jako Savio w 41 spotkaniach poprzedniej kampanii strzelił 11 goli i zanotował 10 asyst. Portal \\\"Transfermarkt\\\" wycenia 20-latka na około 50 milion&oacute;w euro.<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"en\\\">Bem-vindo! \uD83D\uDC4B<br \\/><br \\/>We are delighted to announce the signing of Savinho! ✍\uFE0F<\\/p>\\r\\n&mdash; Manchester City (@ManCity) <a href=\\\"https:\\/\\/twitter.com\\/ManCity\\/status\\/1813982783443984432?ref_src=twsrc%5Etfw\\\">July 18, 2024<\\/a><\\/blockquote>\\r\\n<\\/center>",
            category = NewsCategory.TRANSFERS,
            isImportant = true,
            author = "Redakcja",
            photoSrc = "Manchester City",
            src = "Manchester City",
            publishDate = "2022-10-06T23:00:00.000Z".toInstant(),
            imageUrl = "mancitynw.jpg",
            authPic = "123.jpg",
            authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer",
            link = "Transfery\\/oficjalnie-savio-w-manchesterze-city",
            topics = listOf("Savio", "Manchester City", "ESTAC Troyes"),
        ),
        NewsResource(
            id = "8",
            title = "Oficjalnie: Alvaro Morata w Milanie",
            description = "<p><strong>Alvaro Morata zamienił Atletico Madryt na AC Milan.<\\/strong><\\/p>\\r\\n<p>AC Milan za pośrednictwem swojej oficjalnej strony internetowej poinformował o pozyskaniu na zasadzie transferu definitywnego Alvaro Moraty z Atletico Madryt. Środkowy napastnik po pozytywnym przejściu test&oacute;w medycznych związał się z nowym pracodawcą czteroletnią umową, obowiązującą do 30 czerwca 2028 roku.<\\/p>\\r\\n<p>Kwota tej transakcji wyniosła 13 milion&oacute;w euro - tyle wynosiła klauzula odstępnego zawodnika. 80-krotny reprezentant Hiszpanii z powodzeniem występował w barwach zespołu popularnych <em>Rojiblancos<\\/em> od stycznia 2019 roku, jednak po drodze grał r&oacute;wnież dla Juventusu, więc jest to dla niego powr&oacute;t do Włoch.<\\/p>\\r\\n<p>31-latek w przeszłości przywdziewał jeszcze koszulkę Chelsea oraz Realu Madryt. Alvaro Morata w 48 spotkaniach minionej kampanii zdobył 21 bramek i zanotował 5 asyst. Portal \\\"Transfermarkt.de\\\" wycenia go na 16 milion&oacute;w euro. Przypomnijmy, że doświadczony piłkarz na Euro 2024 został mistrzem Europy.<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"en\\\">✍ <a href=\\\"https:\\/\\/twitter.com\\/AlvaroMorata?ref_src=twsrc%5Etfw\\\">@AlvaroMorata<\\/a> is Rossonero \uD83D\uDD34⚫<br \\/><br \\/>Adding a touch of \uD835\uDC45\uD835\uDC5C\uD835\uDC57\uD835\uDC5C to our <a href=\\\"https:\\/\\/twitter.com\\/hashtag\\/DNACMilan?src=hash&amp;ref_src=twsrc%5Etfw\\\">#DNACMilan<\\/a> \uD83E\uDDEC<a href=\\\"https:\\/\\/twitter.com\\/hashtag\\/SempreMilan?src=hash&amp;ref_src=twsrc%5Etfw\\\">#SempreMilan<\\/a><\\/p>\\r\\n&mdash; AC Milan (@acmilan) <a href=\\\"https:\\/\\/twitter.com\\/acmilan\\/status\\/1814254164119261641?ref_src=twsrc%5Etfw\\\">July 19, 2024<\\/a><\\/blockquote>\\r\\n<\\/center>",
            category = NewsCategory.TRANSFERS,
            isImportant = true,
            author = "Redakcja",
            photoSrc = "AC Milan",
            src = "AC Milan",
            publishDate = "2022-10-06T23:00:00.000Z".toInstant(),
            imageUrl = "milannw.jpg",
            authPic = "123.jpg",
            authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer",
            link = "Transfery\\/oficjalnie-alvaro-morata-w-milanie",
            topics = listOf("Alvaro Morata", "AC Milan", "Atletico Madryt"),
        ),
        NewsResource(
            id = "9",
            title = "Oficjalnie: Kacper Kozłowski w Gaziantep FK\"",
            description = "<p><strong>Kacper Kozłowski definitywnie rozstał się z Brighton &amp; Hove Albion.<\\/strong><\\/p>\\r\\n<p>Gaziantep FK za pośrednictwem swojej oficjalnej strony internetowej poinformował o pozyskaniu na zasadzie transferu definitywnego Kacpra Kozłowskiego z Brighton &amp; Hove Albion. Środkowy pomocnik po pozytywnym przejściu niezbędnych test&oacute;w medycznych związał się z nowym pracodawcą trzyletnim kontraktem, obowiązującym do czerwca 2027 roku.<\\/p>\\r\\n<p>Kwota tej transakcji nie została podana do wiadomości publicznej. Sześciokrotny reprezentant Polski zawodnikiem drużyny popularnych <em>Mew<\\/em> był od stycznia 2022 roku, gdy przeprowadził się na Amex Stadium za aż 11 milion&oacute;w euro z Pogoni Szczecin, z kt&oacute;rej piłkarz wypłynął na szerokie piłkarskie wody.<\\/p>\\r\\n<p>Kacper Kozłowski, kt&oacute;ry ostatnio przebywał na wypożyczeniu w holenderskim Vitesse Arnhem, w 33 spotkaniach poprzedniego sezonu zdobył 3 bramki i zanotował 5 asyst. Portal \\\"Transfermarkt.de\\\" wycenia go na 5 milion&oacute;w euro. Co ciekawe, urodzony w Koszalinie 20-latek nawet nie zadebiutował w pierwszym zespole Brighton &amp; Hove Albion.<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"tr\\\">\uD83D\uDD34⚫\uFE0F Gaziantep Futbol Kul&uuml;b&uuml;m&uuml;z, İngiltere Premier Ligi takımlarından <a href=\\\"https:\\/\\/twitter.com\\/OfficialBHAFC?ref_src=twsrc%5Etfw\\\">@officialbhafc<\\/a> forması giyen gen&ccedil; yıldız Kacper Kozlowski&rsquo;nin transferi konusunda oyuncu ve kul&uuml;b&uuml; ile anlaşmaya varmıştır.<br \\/><br \\/>\uD83D\uDC49 <a href=\\\"https:\\/\\/t.co\\/sgUP39grK7\\\">https:\\/\\/t.co\\/sgUP39grK7<\\/a> <a href=\\\"https:\\/\\/t.co\\/1xUtDwoI21\\\">pic.twitter.com\\/1xUtDwoI21<\\/a><\\/p>\\r\\n&mdash; Gaziantep FK (@GaziantepFK) <a href=\\\"https:\\/\\/twitter.com\\/GaziantepFK\\/status\\/1814314693843992989?ref_src=twsrc%5Etfw\\\">July 19, 2024<\\/a><\\/blockquote>\\r\\n<\\/center>",
            category = NewsCategory.TRANSFERS,
            isImportant = true,
            author = "Redakcja",
            photoSrc = "Gaziantep FK",
            src = "Gaziantep FK",
            publishDate = "2022-10-06T23:00:00.000Z".toInstant(),
            imageUrl = "gaziantepnw.jpg",
            authPic = "123.jpg",
            authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer",
            link = "Transfery\\/oficjalnie-kacper-kozlowski-w-gaziantep-fk",
            topics = listOf("Kacper Kozłowski", "Gaziantep FK", "Brighton & Hove Albion"),
        ),
        NewsResource(
            id = "10",
            title = "Oficjalnie: Kylian Mbappe w Realu Madryt",
            description = "<p><strong>Kylian Mbappe został przedstawiony jako nowy piłkarz Realu Madryt.<\\/strong><\\/p>\\r\\n<p>Real Madryt za pośrednictwem swojej oficjalnej strony internetowej poinformował o pozyskaniu na zasadzie wolnego transferu Kyliana Mbappe. Napastnik po pozytywnym przejściu test&oacute;w medycznych związał się z nowym pracodawcą pięcioletnim kontraktem, obowiązującym do 30 czerwca 2029 roku.<\\/p>\\r\\n<p><em>Kr&oacute;lewscy<\\/em> nie musieli nic płacić PSG, ponieważ wcześniej wygasła umowa gwiazdora z francuskim klubem. 84-krotny reprezentant Francji z powodzeniem występował w barwach zespołu z Paryża od sierpnia 2017 roku, kiedy trafił na Parc des Princes za 180 milion&oacute;w euro z AS Monaco.<\\/p>\\r\\n<p>Kylian Mbappe w 48 spotkaniach poprzedniego sezonu strzelił 44 gole i zanotował 10 asyst. Portal \\\"Transfermarkt\\\" wycenia 25-latka na 180 milion&oacute;w euro. Mistrz świata z 2018 roku z przeprowadzką na Santiago Bernabeu był łączony od wielu lat, ale przenosiny dopiero teraz doszły do skutku.<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"qme\\\">\uD83D\uDCAA <a href=\\\"https:\\/\\/twitter.com\\/KMbappe?ref_src=twsrc%5Etfw\\\">@KMbappe<\\/a> \uD83D\uDCAA<a href=\\\"https:\\/\\/twitter.com\\/hashtag\\/WelcomeMbapp%C3%A9?src=hash&amp;ref_src=twsrc%5Etfw\\\">#WelcomeMbapp&eacute;<\\/a> <a href=\\\"https:\\/\\/t.co\\/ILrh0YMaVx\\\">pic.twitter.com\\/ILrh0YMaVx<\\/a><\\/p>\\r\\n&mdash; Real Madrid C.F. (@realmadrid) <a href=\\\"https:\\/\\/twitter.com\\/realmadrid\\/status\\/1814661539900571894?ref_src=twsrc%5Etfw\\\">July 20, 2024<\\/a><\\/blockquote>\\r\\n<\\/center>",
            category = NewsCategory.TRANSFERS,
            isImportant = true,
            author = "Redakcja",
            photoSrc = "Real Madryt",
            src = "Real Madryt",
            publishDate = "2022-10-06T23:00:00.000Z".toInstant(),
            imageUrl = "gaziantepnw.jpg",
            authPic = "123.jpg",
            authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer",
            link = "Transfery\\/oficjalnie-kylian-mbappe-w-realu-madryt",
            topics = listOf("Kylian Mbappe", "Real Madryt", "Paris Saint-Germain"),
        ),
    )
}
