package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentAiBinding
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class AiFragment : Fragment() {
    private val REQUEST_IMAGE_PICK = 1
    private lateinit var binding: FragmentAiBinding

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://180.226.49.210:5000/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    interface ApiService {
        @Multipart
        @POST("predict")
        fun uploadImage(
            @Part imageFile : MultipartBody.Part
        ): Call<String>
    }

    private val service = retrofit.create(ApiService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAiBinding.inflate(inflater, container, false)

        binding.imageView.setOnClickListener {
            pickFromGallery()
        }
        return binding.root
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let { uri ->
                try {
                    val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
                    val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)

                    binding.imageView.setImageBitmap(bitmap)

                    bitmap?.let {
                        uploadImage(it)
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun uploadImage(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
        val body = MultipartBody.Part.createFormData("file", "image.jpg", requestBody)

        val call = service.uploadImage(body)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val jsonObject = JsonParser.parseString(responseBody).asJsonObject

                    val predictedClass: String? = jsonObject["predicted_class"]?.asString
                    val building_name: String? = jsonObject["building_name"]?.asString
                    val building_description: String? = jsonObject["building_description"]?.asString

                    activity?.runOnUiThread {
                        binding.nameBd.text = "${predictedClass} 호관"
                        binding.majorBd.text = building_name
                        binding.explainBd.text = building_description
                    }
                    Log.d("AiFragment", "Predicted class: $predictedClass")
                } else {
                    Log.e("AiFragment", "Failed to upload image: ${response.message()}")
                }
            }


            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("AiFragment", "Failed to upload image", t)
            }
        })
    }
}

