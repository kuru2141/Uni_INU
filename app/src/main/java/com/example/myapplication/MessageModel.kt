package com.example.myapplication

interface MessageModel {
    data class SenderMessage(
        val message: String
    ): MessageModel

    data class ReceiverMessage(
        val message: String
    ): MessageModel
}