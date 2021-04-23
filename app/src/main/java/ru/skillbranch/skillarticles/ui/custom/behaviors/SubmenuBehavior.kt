package ru.skillbranch.skillarticles.ui.custom.behaviors

import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginRight
import ru.skillbranch.skillarticles.ui.custom.ArticleSubmenu
import ru.skillbranch.skillarticles.ui.custom.Bottombar

class SubmenuBehavior: CoordinatorLayout.Behavior<ArticleSubmenu>() {

    //set view as dependet on bottombar
    override fun layoutDependsOn(
            parent: CoordinatorLayout,
            child: ArticleSubmenu,
            dependency: View
    ): Boolean {
        return dependency is Bottombar
    }

    //will be called if denedent view has been changed
    override fun onDependentViewChanged(
            parent: CoordinatorLayout,
            child: ArticleSubmenu,
            dependency: View
    ): Boolean {
        return if (child.isOpen && dependency is Bottombar && dependency.translationY>=0){
            animate(child, dependency)
            true
        }else false
    }

    private fun animate(child: ArticleSubmenu, dependency: Bottombar) {
        val fraction = dependency.translationY / dependency.minHeight
        child.translationX = (child.width + child.marginRight) * fraction*3
        Log.d("M_SubmenuBehavior","fraction: $fraction translationX: ${child.translationX}")
    }
}