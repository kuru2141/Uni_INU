package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.myapplication.databinding.ActivityMainBinding

private const val TAG_HOME = "fragment_home"
private const val TAG_CHATBOT = "fragment_chatbot"
private const val TAG_AI = "fragment_ai"
private const val TAG_MEMO = "fragment_memo"

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setFragment(TAG_HOME, HomeFragment())

        binding.navigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.tab_home -> setFragment(TAG_HOME, HomeFragment())
                R.id.tab_chatbot -> setFragment(TAG_CHATBOT, ChatbotFragment())
                R.id.tab_ai-> setFragment(TAG_AI, AiFragment())
                R.id.tab_memo-> setFragment(TAG_MEMO, MemoFragment())
            }
            true
        }
    }

    private fun setFragment(tag: String, fragment: Fragment) {
        val manager: FragmentManager = supportFragmentManager
        val fragTransaction = manager.beginTransaction()

        if (manager.findFragmentByTag(tag) == null){
            fragTransaction.add(R.id.mainFrameLayout, fragment, tag)
        }

        val tagArr = arrayOf(TAG_HOME, TAG_CHATBOT, TAG_AI, TAG_MEMO)

        tagArr.forEach {
            if(manager.findFragmentByTag(it) != null) {
                fragTransaction.hide(manager.findFragmentByTag(it)!!)
            }
        }

        val home = manager.findFragmentByTag(TAG_HOME)
        val chatbot = manager.findFragmentByTag(TAG_CHATBOT)
        val ai = manager.findFragmentByTag(TAG_AI)
        val memo = manager.findFragmentByTag(TAG_MEMO)


        if (tag == TAG_HOME) {
            if (home!=null){
                fragTransaction.show(home)
            }
        }
        else if (tag == TAG_CHATBOT) {
            if (chatbot != null) {
                fragTransaction.show(chatbot)
            }
        }

        else if (tag == TAG_AI) {
            if (ai != null){
                fragTransaction.show(ai)
            }
        }
        else if (tag == TAG_MEMO) {
            if (memo != null){
                fragTransaction.show(memo)
            }
        }

        fragTransaction.commitAllowingStateLoss()
    }
}