package com.example.gsgs_plus_final.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.vo.pick_list2

class UseListAdapter(private val items: ArrayList<pick_list2>) :
    RecyclerView.Adapter<UseListAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView =
            LayoutInflater.from(parent.context).inflate(R.layout.use_list_item, parent, false)
        return UseListAdapter.ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: UseListAdapter.ViewHolder, position: Int) {

        val item = items[position]
        Log.d("data!!!!:",item.toString())
        val listener = View.OnClickListener { it ->
            if(position!= RecyclerView.NO_POSITION)
            {
                listener?.onItemClick(item,position)
            }
        }

        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }

    }

    interface OnItemClickListener {
        fun onItemClick(data: pick_list2, pos: Int)
    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }



    // 각 항목에 필요한 기능을 구현
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        fun bind(listener: View.OnClickListener, item: pick_list2) {
            val split = item.addr_start.toString().split("!")
            val split2 = item.addr_end.toString().split("!")

            val start=split[0].substring(8)
            val end=split2[0].substring(8)

            view.findViewById<TextView>(R.id.addr_S).text = start
            view.findViewById<TextView>(R.id.addr_E).text = end
            view.setOnClickListener(listener)

//            view.findViewById<TextView>(R.id.start).text = item.addr_start
//            view.findViewById<TextView>(R.id.end).text = item.addr_end
//            view.setOnClickListener(listener)
        }
    }
}
