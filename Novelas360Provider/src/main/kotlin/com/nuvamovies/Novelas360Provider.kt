package com.nuvamovies

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

/**
 * Novelas360Provider
 * Provider para telenovelas latinas desde novelas360.com
 * 
 * Características:
 * - Telenovelas mexicanas, colombianas, turcas
 * - Series latinas completas
 * - Audio español latino
 * - Calidad HD
 */
class Novelas360Provider : MainAPI() {
    override var mainUrl = "https://novelas360.com"
    override var name = "Novelas360"
    override var lang = "es"
    
    override val hasMainPage = true
    override val hasChromecastSupport = true
    override val hasDownloadSupport = true
    
    override val supportedTypes = setOf(
        TvType.TvSeries,
        TvType.AsianDrama
    )

    override val mainPage = mainPageOf(
        "$mainUrl/page/" to "Telenovelas Recientes",
        "$mainUrl/category/telenovelas-mexicanas/page/" to "Telenovelas Mexicanas",
        "$mainUrl/category/telenovelas-turcas/page/" to "Telenovelas Turcas",
        "$mainUrl/category/telenovelas-colombianas/page/" to "Telenovelas Colombianas",
        "$mainUrl/category/series/page/" to "Series",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val home = document.select("article.post").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2.entry-title a")?.text() ?: return null
        val href = fixUrl(this.selectFirst("h2.entry-title a")?.attr("href") ?: return null)
        val posterUrl = fixUrlNull(
            this.selectFirst("div.post-thumbnail img")?.attr("src") 
            ?: this.selectFirst("div.post-thumbnail img")?.attr("data-src")
        )
        
        // Determinar si es una telenovela turca/asiática o latina
        val isDrama = title.contains("turca", ignoreCase = true) || 
                      title.contains("coreana", ignoreCase = true) ||
                      title.contains("dorama", ignoreCase = true)
        
        return newTvSeriesSearchResponse(
            title,
            href,
            if (isDrama) TvType.AsianDrama else TvType.TvSeries
        ) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()
        
        // Buscar en las primeras 3 páginas
        for (page in 1..3) {
            try {
                val url = "$mainUrl/page/$page/?s=${query.replace(" ", "+")}"
                val document = app.get(url).document
                
                val results = document.select("article.post").mapNotNull {
                    it.toSearchResult()
                }
                
                searchResponse.addAll(results)
                
                // Si no hay resultados, no seguir buscando
                if (results.isEmpty()) break
            } catch (e: Exception) {
                break
            }
        }
        
        return searchResponse.distinctBy { it.url }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title = document.selectFirst("h1.entry-title")?.text()?.trim() 
            ?: throw ErrorLoadingException("No se encontró el título")
        
        val poster = fixUrlNull(
            document.selectFirst("meta[property=og:image]")?.attr("content")
        )
        
        val description = document.select("div.entry-content p").firstOrNull()?.text()?.trim()
        
        val year = Regex("""(\d{4})""").find(
            document.select("div.entry-content").text()
        )?.value?.toIntOrNull()
        
        // Determinar si es drama asiático
        val isDrama = title.contains("turca", ignoreCase = true) || 
                      title.contains("coreana", ignoreCase = true) ||
                      title.contains("dorama", ignoreCase = true) ||
                      document.select("div.entry-content").text().contains("turca", ignoreCase = true)
        
        // Extraer categorías/géneros
        val tags = document.select("span.cat-links a").map { it.text() }
        
        // Buscar episodios relacionados
        val episodes = mutableListOf<Episode>()
        
        // Método 1: Buscar enlaces de episodios en la misma página
        val episodeLinks = document.select("a[href*=capitulo]")
        episodeLinks.forEach { link ->
            val episodeTitle = link.text().trim()
            val episodeUrl = fixUrl(link.attr("href"))
            
            val episodeNum = Regex("""[Cc]apitulo\s*(\d+)""").find(episodeTitle)?.groupValues?.get(1)?.toIntOrNull()
            
            if (episodeNum != null && episodeUrl.isNotEmpty()) {
                episodes.add(
                    newEpisode(episodeUrl) {
                        this.name = episodeTitle
                        this.episode = episodeNum
                    }
                )
            }
        }
        
        // Si el URL actual es de un episodio, agregarlo
        if (url.contains("capitulo", ignoreCase = true)) {
            val episodeNum = Regex("""capitulo-(\d+)""").find(url)?.groupValues?.get(1)?.toIntOrNull() ?: 1
            episodes.add(
                newEpisode(url) {
                    this.name = title
                    this.episode = episodeNum
                }
            )
        }
        
        // Si no se encontraron episodios, agregar la página actual como episodio único
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
            if (isDrama) TvType.AsianDrama else TvType.TvSeries,
            episodes.sortedBy { it.episode }
        ) {
            this.posterUrl = poster
            this.year = year
            this.plot = description
            this.tags = tags
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
        
        // Método 1: Usar fetchUrls como PelisplusHD (extrae URLs del script automáticamente)
        document.select("script").forEach { script ->
            val scriptContent = script.html()
            
            // Usar fetchUrls para extraer todas las URLs del script
            fetchUrls(scriptContent).forEach { url ->
                // Arreglar enlaces con hosts conocidos
                val fixedUrl = fixHostsLinks(url)
                
                // Cargar con loadExtractor (soporta múltiples servidores automáticamente)
                loadExtractor(fixedUrl, data, subtitleCallback, callback)
                linksFound = true
            }
        }
        
        // Método 2: Buscar iframes directos (método estándar)
        document.select("iframe").forEach { iframe ->
            val src = iframe.attr("src").let {
                if (it.startsWith("//")) "https:$it" else it
            }
            
            if (src.isNotEmpty()) {
                val fixedSrc = fixHostsLinks(src)
                loadExtractor(fixedSrc, data, subtitleCallback, callback)
                linksFound = true
            }
        }
        
        // Método 3: Buscar enlaces en el contenido
        document.select("div.entry-content a[href*=player], div.entry-content a[href*=embed], div.entry-content a[href*=stream]").forEach { link ->
            val embedUrl = fixUrl(link.attr("href"))
            val fixedUrl = fixHostsLinks(embedUrl)
            loadExtractor(fixedUrl, data, subtitleCallback, callback)
            linksFound = true
        }
        
        return linksFound
    }
    
    /**
     * Función para arreglar hosts conocidos (como lo hace PelisplusHD)
     * Reemplaza URLs de servidores que cambian de dominio frecuentemente
     */
    private fun fixHostsLinks(url: String): String {
        return url
            .replaceFirst("https://hglink.to", "https://streamwish.to")
            .replaceFirst("https://swdyu.com", "https://streamwish.to")
            .replaceFirst("https://cybervynx.com", "https://streamwish.to")
            .replaceFirst("https://dumbalag.com", "https://streamwish.to")
            .replaceFirst("https://mivalyo.com", "https://vidhidepro.com")
            .replaceFirst("https://dinisglows.com", "https://vidhidepro.com")
            .replaceFirst("https://dhtpre.com", "https://vidhidepro.com")
            .replaceFirst("https://filemoon.link", "https://filemoon.sx")
            .replaceFirst("https://sblona.com", "https://watchsb.com")
            .replaceFirst("https://lulu.st", "https://lulustream.com")
            .replaceFirst("https://uqload.io", "https://uqload.com")
            .replaceFirst("https://do7go.com", "https://dood.la")
    }
}
