package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentHomeBinding
import com.github.tlaabs.timetableview.Schedule
import com.github.tlaabs.timetableview.Time
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private lateinit var userId: String
private lateinit var studentNumber: String
private val schedules = ArrayList<Schedule>()
private var myClass = ArrayList<String>()

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding


    private val retrofit2 = Retrofit.Builder()
        .baseUrl("http://15.165.144.25:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService2 = retrofit2.create(HomeFragment.ApiService::class.java)

    fun setData(data: String, student_number: String) {
        userId = data
        studentNumber = student_number
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClassAuto()

        binding.addClass.setOnClickListener {
            showAddClassDialog()
        }

        binding.timetable.setOnStickerSelectEventListener { idx, course ->
            showDeleteConfirmationDialog(
                idx
            )
        }

        binding.btnInu.setOnClickListener {
            openUrl("http://www.inu.ac.kr")
        }
        binding.btnCyber.setOnClickListener {
            openUrl("https://cyber.inu.ac.kr")
        }
        binding.btnPortal.setOnClickListener {
            openUrl("https://portal.inu.ac.kr")
        }
        binding.btnLib.setOnClickListener {
            openUrl("https://lib.inu.ac.kr/")
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun showAddClassDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_class, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("강의 추가")
            .setPositiveButton("확인") { _, _ ->
                val courseEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
                val startSpinner = dialogView.findViewById<Spinner>(R.id.startSpinner)
                val endSpinner = dialogView.findViewById<Spinner>(R.id.endSpinner)
                val daySpinner = dialogView.findViewById<Spinner>(R.id.daySpinner)

                val course = courseEditText.text.toString().trim()
                val startTime = startSpinner.selectedItem.toString()
                val endTime = endSpinner.selectedItem.toString()
                val selectedDay = daySpinner.selectedItem.toString()

                if(!myClass.contains(course)) {
                    if (course.isNotEmpty()) {
                        addClassToTimetable(startTime, endTime, course, selectedDay)
                        val scheduleData = ScheduleData(
                            id = userId,
                            day = selectedDay,
                            course_name = course,
                            start_time = startTime,
                            end_time = endTime
                        )
                        sendTimeTableToServer(scheduleData)
                        myClass.add(course)
                    }
                } else {
                    Toast.makeText(context, "중복된 강의가 있습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소") { _, _ -> }

        val hoursArray = resources.getStringArray(R.array.hours)
        val daysArray = resources.getStringArray(R.array.days)
        val startSpinner = dialogView.findViewById<Spinner>(R.id.startSpinner)
        val endSpinner = dialogView.findViewById<Spinner>(R.id.endSpinner)
        val daySpinner = dialogView.findViewById<Spinner>(R.id.daySpinner)

        startSpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, hoursArray)
        endSpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, hoursArray)
        daySpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, daysArray)

        builder.show()
    }

    private fun addClassToTimetable(
        startTime: String,
        endTime: String,
        name: String,
        selectedDay: String
    ) {

        val schedule = Schedule()
        schedule.classTitle = name
        schedule.startTime = Time(startTime.toInt(), 0)
        schedule.endTime = Time(endTime.toInt(), 0)

        schedule.day = when (selectedDay) {
            "월요일" -> 0
            "화요일" -> 1
            "수요일" -> 2
            "목요일" -> 3
            "금요일" -> 4
            else -> return
        }

        schedules.clear()
        schedules.add(schedule)
        binding.timetable.add(schedules)
    }


    private fun showDeleteConfirmationDialog(index: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("강의 삭제")
        builder.setMessage("선택한 강의를 삭제하시겠습니까?")
        builder.setPositiveButton("확인") { _, _ ->
            binding.timetable.remove(index)
            deleteClassFromServer(myClass[index])
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun deleteClassFromServer(course: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService2.deleteClass(userId, course)

                if (response.isSuccessful) {
                    val responseBody: ResponseBody? = response.body()
                    if (responseBody != null) {
                        val jsonObject = JSONObject(responseBody.string())
                        val code = jsonObject.optString("code")
                        val message = jsonObject.optString("message")

                        if (code == "200") {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "시간표를 삭제하지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to send verification request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addClassAuto() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService2.getCourses(userId)
                if (response.isSuccessful) {
                    val responseBody: ResponseBody? = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string()
                        val jsonObject = JSONObject(jsonString)
                        val code = jsonObject.optString("code")

                        if (code == "200") {
                            val dataArray = jsonObject.optJSONArray("data")
                            if (dataArray != null) {
                                for (i in 0 until dataArray.length()) {
                                    val courseObject = dataArray.optJSONObject(i)
                                    val day = courseObject.optString("day")
                                    val courseName = courseObject.optString("course_name")
                                    val startTime = courseObject.optInt("start_time")
                                    val endTime = courseObject.optInt("end_time")

                                    withContext(Dispatchers.Main) {
                                        myClass.add(courseName)
                                        addClassToTimetable(
                                            startTime.toString(),
                                            endTime.toString(),
                                            courseName,
                                            day
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "시간표를 불러오지 못했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: JSONException) {
                Log.e("HomeFragment", "Failed to send verification request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "시간표를 파싱하지 못했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to send verification request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun sendTimeTableToServer(request: ScheduleData) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService2.addSchedule(userId, request)

                if (response.isSuccessful) {
                    val responseBody: ResponseBody? = response.body()
                    if (responseBody != null) {
                        val jsonObject = JSONObject(responseBody.string())
                        val code = jsonObject.optString("code")
                        val message = jsonObject.optString("message")

                        if (code == "200") {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "시간표 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to send verification request", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    interface ApiService {
        @GET("courses")
        suspend fun getCourses(@Query("userId") userId: String): Response<ResponseBody>

        @POST("courses")
        suspend fun addSchedule(
            @Query("userId") userId: String,
            @Body request: ScheduleData
        ): Response<ResponseBody>

        @DELETE("courses")
        suspend fun deleteClass(
            @Query("userId") userId: String,
            @Query("course") course: String
        ): Response<ResponseBody>
    }


    data class ScheduleData(
        val id: String,
        val day: String,
        val course_name: String,
        val start_time: String,
        val end_time: String
    )
}
