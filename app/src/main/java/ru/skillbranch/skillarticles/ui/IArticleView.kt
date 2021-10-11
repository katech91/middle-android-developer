package ru.skillbranch.skillarticles.ui

import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.BottombarData
import ru.skillbranch.skillarticles.viewmodels.SubmenuData

interface IArticleView {
    fun setupSubmenu()
    fun setupBottombar()
    fun renderBotombar(data: BottombarData)
    fun renderSubmenu(data: SubmenuData)
    fun renderUi(data: ArticleState)
    fun setupToolbar()
    fun renderSearchResult(searchResult: List<Pair<Int, Int>>)
    fun renderSearchPosition(searchPosition: Int, searchResult: List<Pair<Int, Int>>)
    fun clearSearchResult()
    fun showSearchBar(resultsCount: Int, searchPosition: Int)
    fun hideSearchBar()
    fun setupCopyListener()
}
