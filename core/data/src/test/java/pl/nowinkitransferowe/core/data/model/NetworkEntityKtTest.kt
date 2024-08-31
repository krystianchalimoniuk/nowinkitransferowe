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

package pl.nowinkitransferowe.core.data.model

import org.junit.Test
import pl.nowinkitransferowe.core.model.NewsCategory
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource
import kotlin.test.assertEquals

class NetworkEntityKtTest {

    @Test
    fun network_transfers_resource_can_be_mapped_to_transfers_resource_entity() {
        val networkModel =
            NetworkTransferResource(
                id = 15813,
                name = "Raphael Varane",
                footballerImg = "aa11twarz.jpg",
                clubFrom = "Manchester United",
                clubFromImg = "manchesterunited_herbb.png",
                clubTo = "Como 1907",
                clubToImg = "como_herbb.png",
                price = "za darmo",
                link = "11233/Transfery/oficjalnie-raphael-varane-w-como-1907",
                publishDate = "2024-08-30 15:22:04"
            )
        val entity = networkModel.asEntity()

        assertEquals(networkModel.id, entity.id.toInt())
        assertEquals(networkModel.name, entity.name)
        assertEquals(networkModel.clubFrom, entity.clubFrom)
        assertEquals(networkModel.clubFromImg, networkModel.clubFromImg)
        assertEquals(networkModel.clubTo, entity.clubTo)
        assertEquals(networkModel.clubToImg, entity.clubToImg)
        assertEquals(networkModel.price, entity.price)
        assertEquals(networkModel.link, entity.url)
    }

    @Test
    fun network_news_resource_can_be_mapped_to_news_resource_entity() {
        val networkModel =
            NetworkNewsResource(
                id = 11233,
                title = "Oficjalnie: Raphael Varane w Como 1907",
                description = "\"<p><strong>Raphael Varane zgodnie z przewidywaniami trafił do beniaminka Serie A.<\\/strong><\\/p>\\r\\n<p>Como 1907 za pośrednictwem swojej oficjalnej strony internetowej poinformowało o sprowadzeniu na zasadzie wolnego transferu Raphaela Varane'a. Środkowy obrońca po pozytywnym przejściu test&oacute;w medycznych związał się z nowym pracodawcą dwuletnią umową, obowiązującą do 30 czerwca 2026 roku. Co ważne, kontrakt między stronami zawiera opcję przedłużenia o kolejny sezon.<\\/p>\\r\\n<p>93-krotny reprezentant Francji był do wzięcia za darmo, ponieważ wcześniej rozstał się z Manchesterem United. Wychowanek RC Lens koszulkę drużyny popularnych <em>Czerwonych Diabł&oacute;w<\\/em> przywdziewał od sierpnia 2021 roku, gdy przeni&oacute;sł się na Old Trafford za 40 milion&oacute;w euro z Realu Madryt.<\\/p>\\r\\n<p>Raphael Varane w 32 spotkaniach poprzedniej kampanii zdobył 1 bramkę. Fachowy serwis \\\"Transfermarkt\\\" wycenia doświadczonego zawodnika na 20 milion&oacute;w euro. Warto dodać, że 31-letni defensor w 2018 roku został mistrzem świata, a z <em>Kr&oacute;lewskimi<\\/em> cztery razy sięgnął po Ligę Mistrz&oacute;w.<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"fr\\\">Bienvenue Rapha \uD83D\uDC99 <a href=\\\"https:\\/\\/t.co\\/BGQZdvrSYN\\\">pic.twitter.com\\/BGQZdvrSYN<\\/a><\\/p>\\r\\n&mdash; Como1907 (@Como_1907) <a href=\\\"https:\\/\\/twitter.com\\/Como_1907\\/status\\/1817642881332768853?ref_src=twsrc%5Etfw\\\">July 28, 2024<\\/a><\\/blockquote>\\r\\n<\\/center>",
                category = NewsCategory.TRANSFERS,
                isImportant = "Tak",
                time = "13:36:00",
                author = "Redakcja ",
                photoSrc = "Como 1907",

                newsSrc = "Como 1907",
                date = "2024-07-29",
                authorTwitter = "https://twitter.com/Nowinkitransfer",
                authorPicture = "123.jpg",
                link = "Transfery/oficjalnie-raphael-varane-w-como-1907",
                topics = "Raphael Varane, Como 1907, Manchester United",
                picture = "comonew.jpg",
            )
        val entity = networkModel.asEntity()

        assertEquals(networkModel.id, entity.id.toInt())
        assertEquals(networkModel.title, entity.title)
        assertEquals(networkModel.description, entity.description)
        assertEquals(networkModel.category, networkModel.category)
        assertEquals(networkModel.photoSrc, entity.photoSrc)
        assertEquals(networkModel.topics, entity.topics)
    }
}
