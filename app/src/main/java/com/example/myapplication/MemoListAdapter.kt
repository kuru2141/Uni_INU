package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MemoListAdapter(
    private val context: Context,
    private val resource: Int,
    private val itemList: ArrayList<MemoItem>,
    private val memoDeleteListener: MemoDeleteListener

) :
    RecyclerView.Adapter<MemoListAdapter.ViewHolder>() {

    init {
        notifyDataSetChanged()
    }

    fun addItem(item: MemoItem) {
        itemList.add(0, item)
        notifyDataSetChanged()
    }

    fun addItemList(newItemList: ArrayList<MemoItem>) {
        itemList.addAll(newItemList)
        notifyDataSetChanged()
    }

    fun getItemList(): ArrayList<MemoItem> {
        return itemList
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(resource, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        holder.categoryText.text = item.category
        holder.memoText.text = item.memo
        holder.dateText.text = item.regDate

        holder.itemView.setOnClickListener {
            showDeleteDialog(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var categoryText: TextView = itemView.findViewById(R.id.category)
        var memoText: TextView = itemView.findViewById(R.id.memo)
        var dateText: TextView = itemView.findViewById(R.id.regdate)
    }

    private fun showDeleteDialog(item: MemoItem) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("메모 삭제")
        alertDialogBuilder.setMessage("메모를 제거하시겠습니까?")
        alertDialogBuilder.setPositiveButton("확인") { _, _ ->
            removeMemoItem(item)
            Toast.makeText(context, "메모가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("취소") { _, _ ->
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun removeMemoItem(item: MemoItem) {
        itemList.remove(item)
        notifyDataSetChanged()
        memoDeleteListener.onMemoDeleted(itemList)
    }

    interface MemoDeleteListener {
        fun onMemoDeleted(memoList: List<MemoItem>)
    }
}
