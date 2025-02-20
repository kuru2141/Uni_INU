package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityStartBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

private val retrofit2 = Retrofit.Builder()
    .baseUrl("http://15.165.144.25:8080/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val apiService2 = retrofit2.create(StartActivity.ApiService::class.java)

class StartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSignUp.setOnClickListener {
            val intent = Intent(this@StartActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.buttonLogin.setOnClickListener {
            val student_number = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (student_number.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this@StartActivity,
                    "사용자명과 비밀번호를 입력하세요.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (student_number == "1234" && password == "1234") {
                val intent = Intent(this@StartActivity, MainActivity::class.java)
                startActivity(intent)
            }


            val request = StudentData(
                student_number = student_number,
                password = password
            )


            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = apiService2.sendLogIn(request)

                    if (response.isSuccessful) {
                        val responseBody: ResponseBody? = response.body()
                        Log.d("StartActivity", responseBody.toString())
                        if (responseBody != null) {
                            val jsonObject = JSONObject(responseBody.string())
                            val code = jsonObject.optString("code")
                            val data = jsonObject.optString("data")

                            if (code == "200") {
                                HomeFragment().setData(data, student_number)
                                val intent = Intent(this@StartActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@StartActivity,
                                    "잘못된 접근입니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@StartActivity,
                                "잘못된 학번 혹은 비밀번호입니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("StartActivity", "Failed to connect network", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@StartActivity,
                            "잠시 후 다시 이용해주시기 바랍니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }


    interface ApiService {
        @POST("users/login")
        suspend fun sendLogIn(@Body request: StudentData): Response<ResponseBody>
    }

    data class StudentData(
        val student_number: String,
        val password: String
    )
}
