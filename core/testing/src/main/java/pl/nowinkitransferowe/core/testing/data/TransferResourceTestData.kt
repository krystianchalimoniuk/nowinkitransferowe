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

package pl.nowinkitransferowe.core.testing.data

import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.model.TransferResource

val transferResourceTestData: List<TransferResource> = listOf(
    TransferResource(
        id = "1",
        name = "Phil Bardsley",
        footballerImg = "aa11twarz.jpg",
        clubTo = "Burnley FC",
        clubToImg = "burnley_herbb.png",
        clubFrom = "Stockport",
        clubFromImg = "stockportcounty_herbb.png",
        price = "za darmo",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
    TransferResource(
        id = "2",
        name = "Derek Cornelius",
        footballerImg = "aa11twarz.jpg",
        clubTo = "Vancouver",
        clubToImg = "vancouverwhitecaps_herbb.png",
        clubFrom = "Malmo FF",
        clubFromImg = "malmo_herbb.png",
        price = "nie ujawniono",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
    TransferResource(
        id = "3",
        name = "Allan Saint-Maximeu",
        footballerImg = "aa11twarz.jpg",
        clubTo = "AJ Auxerre",
        clubToImg = "auxerre_herbb.png",
        clubFrom = "Saint-Etienne",
        clubFromImg = "saintetiennenw2_herbb.png",
        price = "0,5 mln \u20ac",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
    TransferResource(
        id = "4",
        name = "Robin Le Normand",
        footballerImg = "aa11twarz.jpg",
        clubTo = "Atletico Madryt",
        clubToImg = "atletico2_herbb.png",
        clubFrom = "Real Sociedad",
        clubFromImg = "sociedad_herbb.png",
        price = "34,5 mln €",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
    TransferResource(
        id = "5",
        name = "Jonathan Clauss",
        footballerImg = "aa11twarz.jpg",
        clubTo = "OGC Nice",
        clubToImg = "auxerre_herbb.png",
        clubFrom = "Olympique Marsylia",
        clubFromImg = "marsylia_herbb.png",
        price = "5 mln €",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
    TransferResource(
        id = "6",
        name = "Lucas Piazon",
        footballerImg = "aa11twarz.jpg",
        clubTo = "Avs FS",
        clubToImg = "avsfs_herbb.png",
        clubFrom = "SC Braga",
        clubFromImg = "braga_herbb.png",
        price = "za darmo",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
    TransferResource(
        id = "7",
        name = "Malang Sarr",
        footballerImg = "aa11twarz.jpg",
        clubTo = "RC Lens",
        clubToImg = "lens_herbb.png",
        clubFrom = "Chelsea FC",
        clubFromImg = "chelsea_herbb.png",
        price = "za darmo",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
    TransferResource(
        id = "8",
        name = "Jesper Lindstrom",
        footballerImg = "aa11twarz.jpg",
        clubTo = "Everton FC",
        clubToImg = "everton_herbb.png",
        clubFrom = "SSC Napoli",
        clubFromImg = "napolinowe_herbb.png",
        price = "wypożyczenie",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
    TransferResource(
        id = "9",
        name = "Fabian Mrozek",
        footballerImg = "aa11twarz.jpg",
        clubTo = "IF Brommapojkarna",
        clubToImg = "ifbromma_herbb.png",
        clubFrom = "Liverpool FC",
        clubFromImg = "liverpool_herbb.png",
        price = "wypożyczenie",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
    TransferResource(
        id = "10",
        name = "Kristopher Vida",
        footballerImg = "aa11twarz.jpg",
        clubTo = "Tatabanyai SC",
        clubToImg = "tatabanya_herbb.png",
        clubFrom = "Kisvarda FC",
        clubFromImg = "kisvarda_herbb.jpg",
        price = "za darmo",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
)
