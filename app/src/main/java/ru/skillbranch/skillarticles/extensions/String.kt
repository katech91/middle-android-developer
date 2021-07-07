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

