package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentMemoBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MemoFragment : Fragment(), MemoListAdapter.MemoDeleteListener {
    private lateinit var binding: FragmentMemoBinding
    private lateinit var context: Context
    private lateinit var memoList: RecyclerView
    private lateinit var memoListAdapter: MemoListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var categorySpinner: Spinner
    private lateinit var memoEdit: EditText
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context = requireContext()
        sharedPreferences = context.getSharedPreferences("memo_data", Context.MODE_PRIVATE)
        categorySpinner = binding.root.findViewById(R.id.category)
        memoEdit = binding.root.findViewById(R.id.memo)
        memoList = binding.root.findViewById(R.id.recyclerView)
        memoListAdapter = MemoListAdapter(context, R.layout.row_memo_item, ArrayList(getMemoList()), this)

        setRecyclerView()
        setMemoListItem()

        val registerButton: ImageButton = binding.root.findViewById(R.id.register)
        registerButton.setOnClickListener {
            registerMemo()
        }
    }

    private fun setRecyclerView() {
        layoutManager = LinearLayoutManager(context)
        memoList.layoutManager = layoutManager
        memoList.adapter = memoListAdapter
    }

    private fun setMemoListItem() {
        val list = ArrayList<MemoItem>()
        memoListAdapter.addItemList(list)
    }

    private fun registerMemo() {
        val category = categorySpinner.selectedItem as String
        val memo = memoEdit.text.toString()

        if (TextUtils.isEmpty(memo)) {
            Toast.makeText(context, R.string.msg_memo_input, Toast.LENGTH_SHORT).show()
            return
        }

        addMemoItem(category, memo)

        categorySpinner.setSelection(0)
        memoEdit.setText("")

        hideKeyboard()
    }

    private fun hideKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun addMemoItem(category: String, memo: String) {
        val item = MemoItem(category, memo)
        memoListAdapter.addItem(item)
        saveMemoList(memoListAdapter.getItemList()) // 메모 추가시 저장
    }

    override fun onMemoDeleted(memoList: List<MemoItem>) {
        saveMemoList(memoList) // 삭제된 메모 목록을 저장
    }

    private fun saveMemoList(memoList: List<MemoItem>) {
        val editor = sharedPreferences.edit()
        val memoJson = Gson().toJson(memoList)
        editor.putString("memo_list", memoJson)
        editor.apply()
    }

    private fun getMemoList(): List<MemoItem> {
        val memoJson = sharedPreferences.getString("memo_list", "")
        return if (memoJson.isNullOrEmpty()) {
            ArrayList()
        } else {
            val type = object : TypeToken<List<MemoItem>>() {}.type
            Gson().fromJson(memoJson, type)
        }
    }
}


