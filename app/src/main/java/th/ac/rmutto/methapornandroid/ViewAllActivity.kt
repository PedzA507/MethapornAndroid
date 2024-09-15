package th.ac.rmutto.methapornandroid

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ViewAllActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var computerAdapter: ComputerAdapter
    private val computers = mutableListOf<Computer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all)

        recyclerView = findViewById(R.id.recyclerViewComputers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        computerAdapter = ComputerAdapter(computers)
        recyclerView.adapter = computerAdapter

        fetchComputers()
    }

    private fun fetchComputers() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:3000/api/computers")  // แก้ไข URL ให้ตรงกับ API ของคุณ
            .build()

        // เรียก API ใน Thread แยกต่างหาก
        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    Log.d("API_RESPONSE", responseBody)  // พิมพ์ค่า JSON ที่ได้รับจาก API

                    val jsonArray = JSONArray(responseBody)
                    computers.clear()

                    // Parse JSON และเพิ่มข้อมูลใน List
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject: JSONObject = jsonArray.getJSONObject(i)

                        // ใช้ชื่อ field ที่ถูกต้องตาม API
                        val brandName = jsonObject.optString("brandName", "Unknown")
                        val modelName = jsonObject.optString("modelName", "Unknown")
                        val serialNumber = jsonObject.optString("serialNumber", "Unknown")
                        val stockQuantity = jsonObject.optInt("stockQuantity", 0)
                        val price = jsonObject.optString("price", "0")
                        val cpuSpeed = jsonObject.optString("cpuSpeed", "Unknown")
                        val memoryCapacity = jsonObject.optString("memoryCapacity", "Unknown")
                        val hardDiskCapacity = jsonObject.optString("hardDiskCapacity", "Unknown")
                        val image = jsonObject.optString("image", null)

                        val computer = Computer(
                            id = jsonObject.getInt("id"),
                            brandName = brandName,
                            modelName = modelName,
                            serialNumber = serialNumber,
                            stockQuantity = stockQuantity,
                            price = price,
                            cpuSpeed = cpuSpeed,
                            memoryCapacity = memoryCapacity,
                            hardDiskCapacity = hardDiskCapacity,
                            image = image
                        )
                        computers.add(computer)
                    }

                    // อัปเดต UI ใน main thread
                    runOnUiThread {
                        computerAdapter.notifyDataSetChanged()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("ViewAllActivity", "Error fetching computers: ${e.message}")
            }
        }.start()
    }
}
