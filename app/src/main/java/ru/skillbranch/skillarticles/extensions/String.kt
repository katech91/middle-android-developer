package ru.skillbranch.skillarticles.extensions

import android.util.Log

fun String?.indexesOf(
    substr: String,
    ignoreCase: Boolean = true
): List<Int> {
    if(substr.isBlank() || substr.isEmpty()){
        return emptyList()
    }

    val regex = if(ignoreCase){
        Regex(substr, RegexOption.IGNORE_CASE)
    }else{
        Regex(substr)
    }
    val matches = regex.findAll(this.toString(), 0)
    var result: MutableList<Int> = mutableListOf()
    matches.forEach {
        result.add(it.range.first)
    }
    Log.d("M_String","result: $result")
    return result
}

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): MutableList<List<Pair<Int, Int>>> {
    val results = mutableListOf<List<Pair<Int, Int>>>()
    this.sortedBy { it.second }
    bounds.sortedBy { it.second }
    bounds.forEach {bound ->
        val filttred = this.filter {
            (it.first >= bound.first) && (it.second <= bound.second)
        }
        results.add(filttred)
    }
    return results
}