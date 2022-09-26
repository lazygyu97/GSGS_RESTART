package com.example.gsgs_plus_final.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.gsgs_plus_final.R
import com.example.gsgs_plus_final.vo.pick_list

class PickUpListAdapter(private val items: ArrayList<pick_list>) :
    RecyclerView.Adapter<PickUpListAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView =
            LayoutInflater.from(parent.context).inflate(R.layout.pickup_list_item, parent, false)
        return PickUpListAdapter.ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: PickUpListAdapter.ViewHolder, position: Int) {

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
        fun onItemClick(data: pick_list, pos: Int)
    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }







    // 각 항목에 필요한 기능을 구현
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        fun bind(listener: View.OnClickListener, item: pick_list) {
            view.findViewById<TextView>(R.id.start).text = item.addr_start
            view.findViewById<TextView>(R.id.end).text = item.addr_end
            view.setOnClickListener(listener)
        }
    }
}
