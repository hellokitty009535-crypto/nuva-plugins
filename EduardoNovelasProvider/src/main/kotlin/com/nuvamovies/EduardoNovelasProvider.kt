package com.nuvamovies

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

/**
 * EduardoNovelasProvider
 * Provider para telenovelas latinas desde eduardonovelas.com
 * 
 * Características:
 * - Telenovelas mexicanas, colombianas, venezolanas, brasileñas
 * - Clásicas y actuales
 * - Audio español latino
 * - Todas las décadas (1970-2020)
 */
class EduardoNovelasProvider : MainAPI() {
    override var mainUrl = "https://www.eduardonovelas.com"
    override var name = "Eduardo Novelas"
    override var lang = "es"
    
    override val hasMainPage = true
    override val hasChromecastSupport = true
    override val hasDownloadSupport = true
    
    override val supportedTypes = setOf(
        TvType.TvSeries
    )

    override val mainPage = mainPageOf(
        "$mainUrl/search/label/Década%202020" to "Telenovelas 2020s",
        "$mainUrl/search/label/Década%202010" to "Telenovelas 2010s",
        "$mainUrl/search/label/Década%202000" to "Telenovelas 2000s",
        "$mainUrl/search/label/Década%201990" to "Telenovelas 1990s",
        "$mainUrl/search/label/México" to "Telenovelas Mexicanas",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data).document
        val home = document.select("article.post").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2.entry-title")?.text() ?: return null
        val href = fixUrl(this.selectFirst("h2.entry-title a")?.attr("href") ?: return null)
        val posterUrl = fixUrlNull(
            this.selectFirst("img")?.attr("src") 
            ?: this.selectFirst("img")?.attr("data-src")
        )
        
        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/search?q=${query.replace(" ", "+")}"
        val document = app.get(url).document
        
        return document.select("article.post").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title = document.selectFirst("h1, h2.entry-title, h3.entry-title")?.text()?.trim() 
            ?: throw ErrorLoadingException("No se encontró el título")
        
        val poster = fixUrlNull(
            document.selectFirst("meta[property=og:image]")?.attr("content")
            ?: document.selectFirst("img")?.attr("src")
        )
        
        val description = document.select("div.entry-content p").firstOrNull()?.text()?.trim()
        
        // Extraer año del título o contenido
        val year = Regex("""(19\d{2}|20\d{2})""").find(
            title + " " + document.select("div.entry-content").text()
        )?.value?.toIntOrNull()
        
        // Extraer capítulos
        val episodes = mutableListOf<Episode>()
        
        // Buscar enlaces de capítulos en el contenido
        val episodeLinks = document.select("div.entry-content a:matches(Capítulo|Capitulo)")
        episodeLinks.forEach { link ->
            val episodeTitle = link.text().trim()
            val episodeUrl = fixUrl(link.attr("href"))
            
            val episodeNum = Regex("""[Cc]ap[íi]tulo\s*(\d+)""").find(episodeTitle)
                ?.groupValues?.get(1)?.toIntOrNull()
            
            if (episodeNum != null && episodeUrl.isNotEmpty() && !episodeUrl.contains("#")) {
                episodes.add(
                    newEpisode(episodeUrl) {
                        this.name = episodeTitle
                        this.episode = episodeNum
                    }
                )
            }
        }
        
        // Si no se encontraron episodios con el método anterior, buscar patrones alternativos
        if (episodes.isEmpty()) {
            // Buscar texto de capítulos y construir URLs
            val content = document.select("div.entry-content").text()
            val chaptersText = Regex("""Capítulo\s+(\d+)""").findAll(content)
            
            chaptersText.forEach { match ->
                val episodeNum = match.groupValues[1].toIntOrNull()
                if (episodeNum != null) {
                    episodes.add(
                        newEpisode(url) {
                            this.name = "Capítulo $episodeNum"
                            this.episode = episodeNum
                        }
                    )
                }
            }
        }
        
        // Si aún no hay episodios, agregar la página actual como episodio único
        if (episodes.isEmpty()) {
            episodes.add(
                newEpisode(url) {
                    this.name = title
                    this.episode = 1
                }
            )
        }

        return newTvSeriesLoadResponse(
            title,
            url,
            TvType.TvSeries,
            episodes.sortedBy { it.episode }
        ) {
            this.posterUrl = poster
            this.year = year
            this.plot = description
            this.showStatus = ShowStatus.Completed
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        
        var linksFound = false
        
        // Método 1: Buscar iframes directos
        document.select("iframe").forEach { iframe ->
            val src = iframe.attr("src").let {
                when {
                    it.startsWith("//") -> "https:$it"
                    it.startsWith("/") -> "$mainUrl$it"
                    else -> it
                }
            }
            
            if (src.isNotEmpty() && (src.contains("ok.ru") || src.contains("vk.com") || 
                src.contains("dailymotion") || src.contains("youtube"))) {
                loadExtractor(src, data, subtitleCallback, callback)
                linksFound = true
            }
        }
        
        // Método 2: Buscar enlaces en el contenido
        document.select("div.entry-content a[href*=ok.ru], div.entry-content a[href*=vk.com], div.entry-content a[href*=dailymotion]").forEach { link ->
            val embedUrl = fixUrl(link.attr("href"))
            loadExtractor(embedUrl, data, subtitleCallback, callback)
            linksFound = true
        }
        
        // Método 3: Usar fetchUrls para extraer URLs del HTML
        document.select("script, div.entry-content").forEach { element ->
            val content = element.html()
            fetchUrls(content).forEach { url ->
                if (url.contains("ok.ru") || url.contains("vk.com") || url.contains("dailymotion")) {
                    loadExtractor(url, data, subtitleCallback, callback)
                    linksFound = true
                }
            }
        }
        
        return linksFound
    }
}
