package com.github.lea_ecco.radapter

abstract class IFilter<E, T>(var def : T) {
    var filter : T = def
    lateinit var listener : () -> Unit
    fun set(f : T, need: Boolean = true) {
        filter = f
        if (need)
            listener.invoke()
    }
    abstract fun compare(item : E) : Boolean
}
