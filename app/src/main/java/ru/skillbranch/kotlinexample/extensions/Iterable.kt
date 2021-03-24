package ru.skillbranch.kotlinexample.extensions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T> {
    for ( ind in this.lastIndex..1 ) {
        if ( predicate(this[ind]) ) return this.subList(0,ind-1)
    }
    return listOf()
}