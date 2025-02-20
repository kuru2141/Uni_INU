package com.example.myapplication


import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoItem(category: String, memo: String) {

    var category: String = category
    var memo: String = memo
    var regDate: String

    init {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        val date = Date()
        regDate = formatter.format(date)
    }
}