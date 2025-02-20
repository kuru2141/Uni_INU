package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemSenderChatBinding


class SenderViewHolder(private val binding: ItemSenderChatBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bindItem(senderMessage: MessageModel.SenderMessage) {
        binding.sendMessage.text = senderMessage.message
    }

    companion object {
        fun create(parent: ViewGroup): SenderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = ItemSenderChatBinding.inflate(layoutInflater, parent, false)
            return SenderViewHolder(view)
        }
    }

}