package com.github.lea_ecco.radapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST")
abstract class ApiRAdapter<E>: RecyclerView.Adapter<Holder<E, ViewBinding>>() {
    var filtered = false
    protected val items = ArrayList<E>()
    protected val filteritems = ArrayList<E>()
    protected var comparator: Comparator<E>? = null
    private var onitemclick: ((E) -> Unit)? = null
    private var onitemlongclick: ((E,View) -> Unit)? = null
    private var oncustomevent: ((E, Int) -> Unit)? = null
    private lateinit var inf: LayoutInflater
    private val filters = ArrayList<IFilter<E, *>>()
    private val holders = ArrayList<Class<Holder<E, ViewBinding>>>()

    fun onItemCLick(lis: (E) -> Unit): ApiRAdapter<E> {
        onitemclick = lis
        return this
    }

    fun onItemLongCLick(lis: (E,View) -> Unit): ApiRAdapter<E> {
        onitemlongclick = lis
        return this
    }

    fun onCustomEvent(lis: (E, Int) -> Unit): ApiRAdapter<E> {
        oncustomevent = lis
        return this
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): Holder<E, ViewBinding> {
        val h = getHolder(p, t)
        if (onitemclick != null)
            h.setOnClick(onitemclick!!)
        if (onitemlongclick != null)
            h.setOnLongClick(onitemlongclick!!)
        if (oncustomevent != null)
            h.setOnCustomEvent(oncustomevent!!)
        return h
    }

    private fun getHolder(p: ViewGroup, t: Int) =
        if (holders[t].kotlin.isInner)
            holders[t].constructors[0].newInstance(this, getBinding(p, t)) as Holder<E, ViewBinding>
        else
            holders[t].constructors[0].newInstance(getBinding(p, t)) as Holder<E, ViewBinding>

    private fun getBinding(p: ViewGroup, t: Int): ViewBinding {
        val pt = (holders[t].genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<*>
        val m = pt.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        return m.invoke(null, inf, p, false) as ViewBinding
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        inf = LayoutInflater.from(recyclerView.context)
        for (cl in javaClass.classes)
            if ((cl.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.size == 2)
                holders.add(cl as Class<Holder<E, ViewBinding>>)
        for (f in javaClass.declaredFields) {
            f.isAccessible = true
            (f.get(this) as? IFilter<E, *>)?.apply {
                listener = ::onFilter
                filters.add(this)
            }
        }
    }

    override fun getItemCount() = filteritems.size
    override fun onBindViewHolder(holder: Holder<E, ViewBinding>, position: Int) {
        holder.init(filteritems[position], position)
    }

    fun update(item: E?) {
        if (filteritems.contains(item))
            notifyItemChanged(filteritems.indexOf(item))
    }

    fun addAll(list: Collection<E>, clear: Boolean = true) {
        if (clear) {
            items.clear()
            filteritems.clear()
        }
        items.addAll(list)
        if (filtered)
            onFilter()
        else {
            filteritems.addAll(items)
            if (comparator != null)
                filteritems.sortWith(comparator!!)
            notifyDataSetChanged()
        }
    }

    fun addToFirst(item: E) {
        if (items.contains(item))
            return
        items.add(0, item)
        filteritems.add(0, item)
        notifyItemInserted(0)
    }

    fun add(item: E) {
        if (items.contains(item))
            return
        items.add(item)
        filteritems.add(item)
        if (comparator != null)
            filteritems.sortWith(comparator!!)
        notifyItemInserted(filteritems.indexOf(item))
    }

    fun remove(item: E) {
        notifyItemRemoved(filteritems.indexOf(item))
        if (items.contains(item))
            items.remove(item)
        if (filteritems.contains(item))
            filteritems.remove(item)
    }

    fun getItem(i: Int) = if (i > -1 && i < items.size) items[i] else null
    fun getFilteredItem(i: Int) = if (i > -1 && i < filteritems.size) filteritems[i] else null

    fun clear() {
        val s = filteritems.size
        items.clear()
        filteritems.clear()
        notifyItemRangeRemoved(0, s)
    }

    fun getAll() = items
    fun contains(item: E) = items.contains(item)

    private fun onFilter() {
        if (filters.isNotEmpty()) {
            filteritems.clear()
            filteritems.addAll(items)
            val fil = ArrayList<E>()
            for (f in filters)
                if (f.filter != f.def) {
                    for (i in filteritems)
                        if (f.compare(i))
                            fil.add(i)
                    filteritems.clear()
                    filteritems.addAll(fil)
                    fil.clear()
                }
            filtered = filteritems.size != items.size
            if (comparator != null)
                filteritems.sortWith(comparator!!)
            notifyDataSetChanged()
        }
    }
}
