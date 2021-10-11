package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.skillbranch.skillarticles.data.*

interface IArticleRepository {
    fun loadArticleContent(articleId: String): LiveData<List<MarkdownElement>?>
    fun getArticle(articleId: String): LiveData<ArticleData?>
    fun loadArticlePersonalInfo(articleId: String): LiveData<ArticlePersonalInfo?>
    fun getAppSettings(): LiveData<AppSettings>
    fun updateSettings(appSettings: AppSettings)
    fun updateArticlePersonalInfo(info: ArticlePersonalInfo)
}

class ArticleRepository(
    private val local: LocalDataHolder = LocalDataHolder,
    private val network: NetworkDataHolder = NetworkDataHolder,
    private val prefs: PrefManager = PrefManager()
) : IArticleRepository {

    override fun loadArticleContent(articleId: String): LiveData<List<MarkdownElement>?> {
        return network.loadArticleContent(articleId)
            .map { str -> str?.let { MarkdownParser.parse(it) } }   //Transformations.map  extension for LiveData
    }

    override fun getArticle(articleId: String): LiveData<ArticleData?> {
        return local.findArticle(articleId) //2s delay from db
    }

    override fun loadArticlePersonalInfo(articleId: String): LiveData<ArticlePersonalInfo?> {
        return local.findArticlePersonalInfo(articleId) //1s delay from db
    }

    override fun getAppSettings(): LiveData<AppSettings> = prefs.settings //from preferences

    override fun updateSettings(appSettings: AppSettings) {
        prefs.isBigText = appSettings.isBigText
        prefs.isDarkMode = appSettings.isDarkMode
    }

    override fun updateArticlePersonalInfo(info: ArticlePersonalInfo) {
        local.updateArticlePersonalInfo(info)
    }
}