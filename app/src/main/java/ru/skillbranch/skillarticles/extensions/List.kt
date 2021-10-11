package ru.skillbranch.skillarticles.extensions

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