package com.example.myapplication

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentChatbotBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val URL = "https://5huir6qdv2.apigw.ntruss.com/Caps/Caps/"
private const val SECRETKEY = "c2Nvb1ZISnhmYXJYVUJXTGx1WkhIRnlZSlV3ekxCRXc="

class ChatbotFragment : Fragment() {

    private lateinit var binding: FragmentChatbotBinding
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sendButton: Button = binding.sendButton
        val inputEditText: EditText = binding.inputEditText
        chatAdapter = ChatAdapter()

        sendButton.setOnClickListener {
            val userInput = inputEditText.text.toString()
            if (userInput.isNotBlank()) {
                chatAdapter.apply {
                    addItem(MessageModel.SenderMessage(userInput))
                }
                SendMessageTask().execute(userInput)
                inputEditText.text.clear()
                binding.rvChatting.adapter = chatAdapter
            }
        }
    }

    inner class SendMessageTask : AsyncTask<String, Void, String>() {
        private var chatBotMessage: String = ""

        override fun doInBackground(vararg params: String?): String {
            val userInput = params[0]
            return sendMessageToChatbot(userInput)
        }

        override fun onPostExecute(result: String?) {
            try {
                if (!result.isNullOrBlank()) {
                    val jsonResponse = JSONObject(result)
                    val bubblesArray = jsonResponse.optJSONArray("bubbles")

                    if (bubblesArray != null && bubblesArray.length() > 0) {
                        val firstBubble = bubblesArray.getJSONObject(0)
                        val data = firstBubble.optJSONObject("data")

                        if (data != null) {
                            val description = data.optString("description")
                            chatBotMessage = description ?: "No description found."
                        } else {
                            chatBotMessage = "No 'data' field found in the first bubble."
                        }
                    } else {
                        chatBotMessage = "No bubbles found in the response."
                    }
                } else {
                    chatBotMessage = "Empty response."
                }
            } catch (e: Exception) {
                chatBotMessage = "Error extracting description."
            }
            chatAdapter.apply {
                addItem(MessageModel.ReceiverMessage(chatBotMessage))
            }
        }
    }

    private fun sendMessageToChatbot(userInput: String?): String {
        if (userInput.isNullOrBlank()) {
            return "Error: User input is empty."
        }

        val timestamp = System.currentTimeMillis() / 1000
        val requestBody = """
            {
                "version": "v2",
                "userId": "U47b00b58c90f8e47428af8b7bddcda3d1111111",
                "timestamp": $timestamp,
                "bubbles": [{"type": "text", "data": {"description": "$userInput"}}],
                "event": "send"
            }
        """.trimIndent()

        try {
            val signature = makeSignature(SECRETKEY, timestamp.toString(), requestBody)
            val contentType = "application/json; charset=utf-8".toMediaTypeOrNull()

            val request = Request.Builder()
                .url(URL)
                .header("Content-Type", contentType.toString())
                .header("X-NCP-CHATBOT_SIGNATURE", signature)
                .post(requestBody.toRequestBody(contentType))
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            return response.body?.string() ?: "Empty response"
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error occurred: ${e.message}"
        }
    }

    private fun makeSignature(secretKey: String, timestamp: String, message: String): String {
        var encodeBase64String = ""
        try {
            val secreteKeyBytes = secretKey.toByteArray(StandardCharsets.UTF_8)
            val signingKey = SecretKeySpec(secreteKeyBytes, "HmacSHA256")
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(signingKey)
            val rawHmac = mac.doFinal(message.toByteArray(StandardCharsets.UTF_8))
            encodeBase64String = Base64.getEncoder().encodeToString(rawHmac)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return encodeBase64String
    }
}
