package pl.nowinkitransferowe.core.network

import JvmUnitTestDemoAssetManager
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Before
import pl.nowinkitransferowe.core.model.NewsCategory
import pl.nowinkitransferowe.core.network.demo.DemoNtNetworkDataSource
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource

class DemoNtNetworkDataSourceTest {

    private lateinit var subject: DemoNtNetworkDataSource

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        subject = DemoNtNetworkDataSource(
            ioDispatcher = testDispatcher,
            networkJson = Json { ignoreUnknownKeys = true },
            assets = JvmUnitTestDemoAssetManager,
        )
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun testDeserializationOfTransfers() = runTest(testDispatcher) {
        kotlin.test.assertEquals(
            NetworkTransferResource(
                id = 15813,
                name = "Raphael Varane",
                footballerImg = "aa11twarz.jpg",
                clubFrom = "Manchester United",
                clubFromImg = "manchesterunited_herbb.png",
                clubTo = "Como 1907",
                clubToImg = "como_herbb.png",
                price = "za darmo",
                link = "11233/Transfery/oficjalnie-raphael-varane-w-como-1907"
            ),
            subject.getTransferResources().first(),
        )
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun testDeserializationOfNewsResources() = runTest(testDispatcher) {
        kotlin.test.assertEquals(
            NetworkNewsResource(
                id = 11233,
                title = "Oficjalnie: Raphael Varane w Como 1907",
                category = NewsCategory.TRANSFERS,
                isImportant = "Tak",
                description = "<p><strong>Raphael Varane zgodnie z przewidywaniami trafił do beniaminka Serie A.</strong></p>\r\n<p>Como 1907 za pośrednictwem swojej oficjalnej strony internetowej poinformowało o sprowadzeniu na zasadzie wolnego transferu Raphaela Varane'a. Środkowy obrońca po pozytywnym przejściu test&oacute;w medycznych związał się z nowym pracodawcą dwuletnią umową, obowiązującą do 30 czerwca 2026 roku. Co ważne, kontrakt między stronami zawiera opcję przedłużenia o kolejny sezon.</p>\r\n<p>93-krotny reprezentant Francji był do wzięcia za darmo, ponieważ wcześniej rozstał się z Manchesterem United. Wychowanek RC Lens koszulkę drużyny popularnych <em>Czerwonych Diabł&oacute;w</em> przywdziewał od sierpnia 2021 roku, gdy przeni&oacute;sł się na Old Trafford za 40 milion&oacute;w euro z Realu Madryt.</p>\r\n<p>Raphael Varane w 32 spotkaniach poprzedniej kampanii zdobył 1 bramkę. Fachowy serwis \"Transfermarkt\" wycenia doświadczonego zawodnika na 20 milion&oacute;w euro. Warto dodać, że 31-letni defensor w 2018 roku został mistrzem świata, a z <em>Kr&oacute;lewskimi</em> cztery razy sięgnął po Ligę Mistrz&oacute;w.</p>\r\n<p>&nbsp;</p>\r\n<center>\r\n<blockquote class=\"twitter-tweet\">\r\n<p dir=\"ltr\" lang=\"fr\">Bienvenue Rapha 💙 <a href=\"https://t.co/BGQZdvrSYN\">pic.twitter.com/BGQZdvrSYN</a></p>\r\n&mdash; Como1907 (@Como_1907) <a href=\"https://twitter.com/Como_1907/status/1817642881332768853?ref_src=twsrc%5Etfw\">July 28, 2024</a></blockquote>\r\n</center>",
                newsSrc = "Como 1907",
                photoSrc = "Como 1907",
                date = "2024-07-29",
                time = "13:36:00",
                picture = "comonew.jpg",
                author = "Redakcja ",
                authorTwitter = "https://twitter.com/Nowinkitransfer",
                authorPicture = "123.jpg",
                link = "Transfery/oficjalnie-raphael-varane-w-como-1907",
                topics = "Raphael Varane, Como 1907, Manchester United"
            ),
            subject.getNewsResources().find { it.id == 11233 },
        )
    }
}
