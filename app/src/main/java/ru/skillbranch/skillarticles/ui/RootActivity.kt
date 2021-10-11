package ru.skillbranch.skillarticles.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.hideKeyboard
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.*

class RootActivity : AppCompatActivity(), IArticleView {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var viewModelFactory: ViewModelProvider.Factory = ViewModelFactory(this, "0")

    private val viewModel: ArticleViewModel by viewModels { viewModelFactory }
    private val vb: ActivityRootBinding by viewBinding(ActivityRootBinding::inflate)

    private val vbBottombar
        get() = vb.bottombar.binding
    private val vbSubmenu
        get() = vb.submenu.binding
    private lateinit var searchView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()
        setupBottombar()
        setupSubmenu()
        setupCopyListener()

        viewModel.observeState(this, ::renderUi)
        viewModel.observeSubState(this, ArticleState::toBottombarData, ::renderBotombar)
        viewModel.observeSubState(this, ArticleState::toSubmenuData, ::renderSubmenu)

        viewModel.observeNotifications(this){
            renderNotification(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_search , menu)
        val menuItem = menu.findItem(R.id.action_search)
        searchView = menuItem?.actionView as SearchView
        searchView.queryHint = getString(R.string.article_search_placeholder)

        //restore SearchView
        if( viewModel.currentState.isSearch ) {
            menuItem.expandActionView()
            searchView.setQuery(viewModel.currentState.searchQuery, false)
            searchView.requestFocus()
        } else {
            searchView.clearFocus()
        }

        menuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        viewModel.saveState()
        super.onSaveInstanceState(outState)
    }

    private fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(vb.coordinatorContainer, notify.message, Snackbar.LENGTH_LONG)
                .setAnchorView(vb.bottombar)

        when (notify) {
            is Notify.ActionMessage -> {
                val (_, label, handler) = notify

                with(snackbar) {
                    setActionTextColor(getColor(R.color.color_accent_dark))
                    setAction(label) { handler.invoke() }
                }
            }

            is Notify.ErrorMessage -> {
                val (_, label, handler) = notify

                with(snackbar){
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(R.color.white))
                    setActionTextColor(getColor(R.color.white))
                    setAction(label) { handler?.invoke() }
                }
            }

            else -> { /* nothing */ }
        }

        snackbar.show()
    }

    override fun setupSubmenu() {
        with(vbSubmenu) {
            btnTextUp.setOnClickListener { viewModel.handleUpText() }
            btnTextDown.setOnClickListener { viewModel.handleDownText() }
            switchMode.setOnClickListener { viewModel.handleNightMode() }
        }
    }

    override fun setupBottombar() {
        with(vbBottombar) {
            btnLike.setOnClickListener { viewModel.handleLike() }
            btnBookmark.setOnClickListener { viewModel.handleBookmark() }
            btnShare.setOnClickListener { viewModel.handleShare() }
            btnSettings.setOnClickListener { viewModel.handleToggleMenu() }

            btnResultUp.setOnClickListener{
                if (!vb.tvTextContent.hasFocus()) vb.tvTextContent.requestFocus()
                hideKeyboard(it)
                viewModel.handleUpResult()
            }
            btnResultDown.setOnClickListener{
                if (!vb.tvTextContent.hasFocus()) vb.tvTextContent.requestFocus()
                hideKeyboard(it)
                viewModel.handleDownResult()
            }
            btnSearchClose.setOnClickListener{
                viewModel.handleSearchMode(false)
                invalidateOptionsMenu()  //toolbar сбросится до начального состояния
            }
        }
    }

    override fun renderBotombar(data: BottombarData) {
        with(vbBottombar) {
            btnSettings.isChecked = data.isShowMenu
            btnLike.isChecked = data.isLike
            btnBookmark.isChecked = data.isBookmark
        }

        if (data.isSearch) {
            showSearchBar(data.resultsCount, data.searchPosition)
            with(vb.toolbar) {
                (layoutParams as AppBarLayout.LayoutParams).scrollFlags =
                    AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
            }
        } else {
            hideSearchBar()
            with(vb.toolbar) {
                (layoutParams as AppBarLayout.LayoutParams).scrollFlags =
                    AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
            }
        }
    }

    override fun renderSubmenu(data: SubmenuData) {
        with(vbSubmenu){
            switchMode.isChecked = data.isDarkMode
            btnTextUp.isChecked = data.isBigText
            btnTextDown.isChecked = ! data.isBigText
        }

        if (data.isShowMenu) vb.submenu.open() else vb.submenu.close()
    }

    override fun renderUi(data: ArticleState) {
        delegate.localNightMode =
                if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        with(vb.tvTextContent){
            textSize = if (data.isBigText) 18f else 14f
            isLoading = data.content.isEmpty()
            setContent(data.content)
        }

        //bind toolbar
        with(vb.toolbar){
            title = data.title ?: "loading"
            subtitle = data.category ?: "loading"
            if (data.categoryIcon != null) {
                logo = ContextCompat.getDrawable(context, data.categoryIcon as Int)
            }
        }

        if (data.isLoadingContent) return

        if (data.isSearch){
            renderSearchResult(data.searchResults)
            renderSearchPosition(data.searchPosition, data.searchResults)
        } else clearSearchResult()
    }

    override fun setupToolbar() {
        setSupportActionBar(vb.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val logo = vb.toolbar.children.find { it is AppCompatImageView } as? ImageView
        logo ?: return
        logo.scaleType =  ImageView.ScaleType.CENTER_CROP
        //check toolbar imports
        (logo.layoutParams as? Toolbar.LayoutParams)?.let {
            it.width = this.dpToIntPx(40)
            it.height = this.dpToIntPx(40)
            it.marginEnd = this.dpToIntPx(16)
            logo.layoutParams = it
        }
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        vb.tvTextContent.renderSearchResult(searchResult)
    }

    override fun renderSearchPosition(searchPosition: Int, searchResult: List<Pair<Int, Int>>) {
        vb.tvTextContent.renderSearchPosition(searchResult.getOrNull(searchPosition))
    }

    override fun clearSearchResult() {
        vb.tvTextContent.clearSearchResult()
    }

    override fun showSearchBar(resultsCount: Int, searchPosition: Int) {
        with(vb.bottombar){
            setSearchState(true)
            setSearchInfo(resultsCount, searchPosition)
        }
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        with(vb.bottombar){
            setSearchState(false)
        }
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(0))
    }

    override fun setupCopyListener() {
        vb.tvTextContent.setCopyListener { copy ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied code", copy)
            clipboard.setPrimaryClip(clip)
            viewModel.handleCopyCode()
        }
    }
}