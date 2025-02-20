package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySignupBinding
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
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private const val API_KEY = "55e21b32-038d-4380-be34-b5af61de078a"
private var certificateCode = 0
private var success = false

private val retrofit = Retrofit.Builder()
    .baseUrl("https://univcert.com/api/v1/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val retrofit2 = Retrofit.Builder()
    .baseUrl("http://15.165.144.25:8080/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val apiService = retrofit.create(SignUpActivity.ApiService::class.java)
private val apiService2 = retrofit2.create(SignUpActivity.ApiService::class.java)


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnSendVerificationCode.setOnClickListener {
            sendVerificationCode()
        }

        binding.btnVerification.setOnClickListener {
            checkVerificationCode()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            signUp()
        }
    }


    private fun sendVerificationCode() {
        val email = binding.editTextEmail.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this@SignUpActivity, "유효하지 않은 이메일입니다.", Toast.LENGTH_SHORT).show()
            return
        } else if (!email.endsWith("@inu.ac.kr")) {
            Toast.makeText(this@SignUpActivity, "학교 이메일을 사용해야합니다.", Toast.LENGTH_SHORT).show()
            return
        }


        GlobalScope.launch(Dispatchers.IO) {
            try {
                apiService2.sendVerificationRemove(email)
            } catch (e: Exception) {
                Log.e("SignUpActivity", "Failed to send verification request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignUpActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            try {
                val response = apiService2.sendVerificationRequest(email)

                if (response.isSuccessful) {
                    val responseBody: ResponseBody? = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string().toBoolean() // true or false
                        Log.d("", jsonString.toString())
                        if (jsonString) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "인증 번호를 전송했습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "인증 요청에 실패했습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SignUpActivity, "올바르지 않은 응답입니다.", Toast.LENGTH_SHORT)
                            .show()
                        Log.e(
                            "Response",
                            "Unsuccessful response: ${response.code()}, ${response.message()}, ${
                                response.errorBody()?.string()
                            }"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SignUpActivity", "Failed to send verification request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignUpActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }


    private fun checkVerificationCode() {
        val email = binding.editTextEmail.text.toString()
        val code = binding.editTextVerificationCode.text.toString()


        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService2.sendVerificationCode(code, email)

                if (response.isSuccessful) {
                    val responseBody: ResponseBody? = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string().toBoolean() // true or false

                        if (jsonString) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "인증에 성공했습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            certificateCode = 1
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "인증에 실패했습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SignUpActivity, "올바르지 않은 응답입니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e("SignUpActivity", "Failed to send verification request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignUpActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }


    private fun signUp() {
        val name = binding.editTextName.text.toString()
        val student_number = binding.editTextStudentNumber.text.toString()
        val email = binding.editTextEmail.text.toString()
        val code = binding.editTextVerificationCode.text.toString()
        val password = binding.editTextPassword.text.toString()

        // 입력값 유효성 검사
        if (name.isEmpty() || student_number.isEmpty() || email.isEmpty() || code.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService2.sendVerificationCode(code, email)

                if (response.isSuccessful) {
                    val responseBody: ResponseBody? = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string().toBoolean() // true or false

                        if (jsonString) {
                            success = true
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@SignUpActivity,
                                "인증에 실패했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SignUpActivity, "올바른 응답이 아닙니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e("SignUpActivity", "Failed to send verification request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignUpActivity, "네트워크 오류가 생겼습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        if (success || certificateCode != 1) {
            Toast.makeText(
                this@SignUpActivity,
                "인증되지 않은 이메일 입니다.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        val request = SignUp(
            name = name,
            student_number = student_number,
            password = password
        )

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService2.sendSignUp(request)

                if (response.isSuccessful) {
                    val responseBody: ResponseBody? = response.body()
                    if (responseBody != null) {
                        val jsonObject = JSONObject(responseBody.string())
                        val code = jsonObject.optString("code")
                        val message = jsonObject.optString("message")
                        val status = jsonObject.optString("status")


                        if (code == "200") {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            finish()
                        } else if (status == "500") {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "회원 가입에 실패했습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@SignUpActivity,
                            "올바르지 않은 응답입니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("", response.toString())
                    }
                }
            } catch (e: Exception) {
                Log.e("SignUpActivity", "Failed to send verification request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignUpActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    interface ApiService {

        @GET("users/verify")
        suspend fun sendVerificationRequest(@Query("email") email: String): Response<ResponseBody>

        @GET("users/verify-code")
        suspend fun sendVerificationCode(
            @Query("code") code: String,
            @Query("email") email: String
        ): Response<ResponseBody>

        @GET("users/reset")
        suspend fun sendVerificationRemove(@Query("email") email: String): Response<ResponseBody>

        @POST("users/signup")
        suspend fun sendSignUp(@Body request: SignUp): Response<ResponseBody>
    }


    data class SignUp(
        val name: String,
        val student_number: String,
        val password: String
    )
}
