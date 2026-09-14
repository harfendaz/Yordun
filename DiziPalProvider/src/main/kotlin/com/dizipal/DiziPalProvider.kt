package com.dizipal

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import java.net.URI

class DiziPalProvider : MainAPI() {
    override var mainUrl = "https://dizipal.bid"
    override var name = "DiziPal"
    override var lang = "tr"
    override val hasMainPage = true
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.TvSeries, TvType.Movie)

    override val mainPage = mainPageOf(
        "diziler/son-bolumler" to "Son Bölümler",
        "diziler" to "Yeni Diziler",
        "filmler" to "Yeni Filmler",
        "koleksiyon/netflix" to "Netflix",
        "koleksiyon/exxen" to "Exxen",
        "koleksiyon/blutv" to "BluTV"
    )

    private fun updateMainUrl(url: String) {
        if (url.contains("dizipal")) {
            mainUrl = "https://${URI(url).host}"
        }
    }

   override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
    val targetUrl = "$mainUrl/${request.data}"
    val response = app.get(targetUrl)
    updateMainUrl(response.url)
    val document = response.document

    val items = document
        .select("a[title][href]:has(div.poster)")
        .mapNotNull { element ->
            val title = element.attr("title").trim()
            val href = element.attr("href").trim()

            if (title.isBlank() || href.isBlank()) {
                return@mapNotNull null
            }

            val posterUrl = element.selectFirst("img")?.let { img ->
                img.attr("src").ifBlank {
                    img.attr("data-src")
                }
            }

            val isTv = href.contains("/dizi/") ||
                    request.name.contains("Dizi") ||
                    request.name.contains("Bölüm")

            if (isTv) {
                newTvSeriesSearchResponse(title, fixUrl(href), TvType.TvSeries) {
                    this.posterUrl = fixUrlNull(posterUrl)
                }
            } else {
                newMovieSearchResponse(title, fixUrl(href), TvType.Movie) {
                    this.posterUrl = fixUrlNull(posterUrl)
                }
            }
        }

    return newHomePageResponse(request.name, items)
}

    override suspend fun search(query: String): List<SearchResponse> {
        // Fallback to standard HTML search to avoid JSON parsing issues if API changes
        val response = app.get("$mainUrl/?s=$query")
        updateMainUrl(response.url)
        val document = response.document

        return document.select("div.single-item, article.item, div.post-item").mapNotNull { element ->
            val title = element.selectFirst("h2, h3, .title, .name")?.text()?.trim() ?: return@mapNotNull null
            val href = element.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val posterUrl = element.selectFirst("img")?.attr("data-src") ?: element.selectFirst("img")?.attr("src")

            if (href.contains("/dizi/")) {
                newTvSeriesSearchResponse(title, fixUrl(href), TvType.TvSeries) {
                    this.posterUrl = fixUrlNull(posterUrl)
                }
            } else {
                newMovieSearchResponse(title, fixUrl(href), TvType.Movie) {
                    this.posterUrl = fixUrlNull(posterUrl)
                }
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1, .entry-title, .title")?.text()?.trim() ?: "Bilinmeyen Başlık"
        val posterUrl = document.selectFirst("div.poster img, img.cover")?.let { 
            it.attr("data-src").ifEmpty { it.attr("src") } 
        }
        val plot = document.selectFirst("div.description, p.overview, .story")?.text()?.trim()
        val isTv = url.contains("/dizi/")

        if (isTv) {
            val episodes = mutableListOf<Episode>()
            document.select("ul.episodes-list li, div.episode-item").forEachIndexed { index, epEl ->
                val epHref = epEl.selectFirst("a")?.attr("href") ?: return@forEachIndexed
                val epTitle = epEl.text().trim()
                
                val seasonMatch = Regex("(\\d+)\\.?\\s*[Ss]ezon|S(\\d+)").find(epTitle)
                val episodeMatch = Regex("(\\d+)\\.?\\s*[Bb]ölüm|E(\\d+)").find(epTitle)

                val seasonNum = seasonMatch?.groupValues?.drop(1)?.firstOrNull { it.isNotBlank() }?.toIntOrNull() ?: 1
                // Fallback to index if episode number cannot be parsed
                val episodeNum = episodeMatch?.groupValues?.drop(1)?.firstOrNull { it.isNotBlank() }?.toIntOrNull() ?: (index + 1)

                episodes.add(
                    newEpisode(epHref) {
                        this.name = epTitle
                        this.season = seasonNum
                        this.episode = episodeNum
                    }
                )
            }

            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = fixUrlNull(posterUrl)
                this.plot = plot
            }
        } else {
            return newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = fixUrlNull(posterUrl)
                this.plot = plot
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        
        document.select("iframe").forEach { iframe ->
            val iframeUrl = iframe.attr("src").ifEmpty { iframe.attr("data-src") }.ifEmpty { iframe.attr("data-lazy-src") }
            
            if (iframeUrl.isNotBlank() && !iframeUrl.contains("youtube.com/embed/")) {
                loadExtractor(fixUrl(iframeUrl), data, subtitleCallback, callback)
            }
        }
        
        return true
    }
}
