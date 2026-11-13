/*
 * Copyright 2026 The Android Open Source Project
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

package pl.nowinkitransferowe.feature.articledetails.impl

import android.net.Uri

/** The website, the base of the article HTML: the stylesheet and fonts use relative addresses. */
internal const val WEBSITE_URL = "https://nowinkitransferowe.pl/"

/**
 * The article content as a page with the website stylesheet (/static/css/article.css), in the
 * app's light or dark theme. Compose draws the background and the margins, so the page has
 * neither. Embedded posts from X load their script.
 */
internal fun articleHtml(contentHtml: String, darkTheme: Boolean): String = buildString {
    append("<!doctype html><html data-theme=\"")
    append(if (darkTheme) "dark" else "light")
    append("\"><head><meta charset=\"utf-8\">")
    append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
    append("<link rel=\"stylesheet\" href=\"/static/css/article.css\">")
    append("<style>body{background:transparent;padding:0}</style>")
    append("</head><body>")
    append(contentHtml)
    append("<script async src=\"https://platform.twitter.com/widgets.js\"></script>")
    append("</body></html>")
}

/**
 * The id of the website article [uri] points to, for example 11284 for
 * https://nowinkitransferowe.pl/11284/transfery/psg-zarzucilo-siec; null for any other link.
 */
internal fun articleIdOf(uri: Uri): Int? {
    val host = uri.host?.removePrefix("www.") ?: return null
    if (host != "nowinkitransferowe.pl") return null
    return uri.pathSegments.firstOrNull()?.toIntOrNull()
}
