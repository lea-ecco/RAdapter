package com.github.lea_ecco.radapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class Holder<E, VB: ViewBinding>(protected val binding: VB) : RecyclerView.ViewHolder(binding.root) {
    protected var item : E? = null
    protected var pos = 0
    protected var customevent: ((E, Int) -> Unit)? = null

    fun setOnClick(lis : ((E) -> Unit)) {
        binding.root.setOnClickListener {
            if (item != null)
                lis.invoke(item!!)
        }
    }

    fun setOnLongClick(lis : ((E,View) -> Unit)) {
        binding.root.setOnLongClickListener {
            if (item != null) {
                lis.invoke(item!!, itemView)
                true
            } else
                false
        }
    }

    fun init(i : E, p : Int) {
        item = i
        pos = p
        update()
    }
    abstract fun update()

    fun setOnCustomEvent(oncustomevent: (E, Int) -> Unit) {
        customevent = oncustomevent
    }
}
