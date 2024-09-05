package th.ac.rmutto.methapornandroid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class ComputerDetailsActivity : AppCompatActivity() {

    private lateinit var computerIdEditText: EditText
    private lateinit var brandNameEditText: EditText
    private lateinit var modelNameEditText: EditText
    private lateinit var serialNumberEditText: EditText
    private lateinit var stockQuantityEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var cpuSpeedEditText: EditText
    private lateinit var memoryCapacityEditText: EditText
    private lateinit var hardDiskCapacityEditText: EditText
    private lateinit var imageView: ImageView
    private lateinit var editButton: Button
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var changeImageButton: Button

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private var isEditing = false  // Flag to track editing state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_computer_details)

        computerIdEditText = findViewById(R.id.editComputerId)
        brandNameEditText = findViewById(R.id.editBrandName)
        modelNameEditText = findViewById(R.id.editModelName)
        serialNumberEditText = findViewById(R.id.editSerialNumber)
        stockQuantityEditText = findViewById(R.id.editStockQuantity)
        priceEditText = findViewById(R.id.editPrice)
        cpuSpeedEditText = findViewById(R.id.editCpuSpeed)
        memoryCapacityEditText = findViewById(R.id.editMemoryCapacity)
        hardDiskCapacityEditText = findViewById(R.id.editHardDiskCapacity)
        imageView = findViewById(R.id.imageComputer)
        editButton = findViewById(R.id.buttonEdit)
        saveButton = findViewById(R.id.buttonSave)
        deleteButton = findViewById(R.id.buttonDelete)
        changeImageButton = findViewById(R.id.buttonChangeImage)

        // Initially disable editing
        setEditingEnabled(false)

        findViewById<Button>(R.id.buttonFetch).setOnClickListener {
            val computerId = computerIdEditText.text.toString()
            fetchComputerDetails(computerId) { computer ->
                runOnUiThread {
                    computer?.let {
                        populateFields(it)
                        setEditingEnabled(false)
                    } ?: run {
                        Toast.makeText(this, "ไม่พบข้อมูลคอมพิวเตอร์", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        editButton.setOnClickListener {
            isEditing = !isEditing
            setEditingEnabled(isEditing)

            if (isEditing) {
                editButton.text = "Cancel"
            } else {
                editButton.text = "Edit"
            }
        }

        saveButton.setOnClickListener {
            val computerId = computerIdEditText.text.toString()
            val updatedComputer = createComputerFromFields(computerId)

            updateComputerDetails(computerId, updatedComputer) { success, updatedImageUrl ->
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, "บันทึกข้อมูลสำเร็จ", Toast.LENGTH_SHORT).show()
                        setEditingEnabled(false)
                        updatedImageUrl?.let {
                            loadImage(it, imageView)
                        }
                        isEditing = false
                        editButton.text = "Edit"
                    } else {
                        Toast.makeText(this, "บันทึกข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        deleteButton.setOnClickListener {
            val computerId = computerIdEditText.text.toString()
            deleteComputerDetails(computerId) { success ->
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, "ลบข้อมูลสำเร็จ", Toast.LENGTH_SHORT).show()
                        clearFields()
                    } else {
                        Toast.makeText(this, "ลบข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        changeImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            imageView.setImageURI(selectedImageUri) // Display selected image
        }
    }

    private fun fetchComputerDetails(id: String, callback: (Computer?) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.1.49:3000/api/computers/$id")
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val computer = parseComputerJson(responseBody)
                        callback(computer)
                    }
                } else {
                    callback(null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }

    private fun updateComputerDetails(id: String, computer: Computer, callback: (Boolean, String?) -> Unit) {
        val client = OkHttpClient()
        val requestBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("brandName", computer.brandName)
            .addFormDataPart("modelName", computer.modelName)
            .addFormDataPart("serialNumber", computer.serialNumber)
            .addFormDataPart("stockQuantity", computer.stockQuantity.toString())
            .addFormDataPart("price", computer.price.toString())
            .addFormDataPart("cpuSpeed", computer.cpuSpeed.toString())
            .addFormDataPart("memoryCapacity", computer.memoryCapacity)
            .addFormDataPart("hardDiskCapacity", computer.hardDiskCapacity)

        selectedImageUri?.let {
            val file = getFileFromUri(it)
            if (file != null && file.exists()) {
                val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
                requestBuilder.addFormDataPart("image", file.name, requestBody)
            }
        }

        val requestBody = requestBuilder.build()
        val request = Request.Builder()
            .url("http://192.168.1.49:3000/api/computers/$id")
            .put(requestBody)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                val updatedImageUrl = if (success) {
                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody ?: "")
                    jsonObject.optString("image", null)
                } else null
                callback(success, updatedImageUrl)
            } catch (e: IOException) {
                e.printStackTrace()
                callback(false, null)
            }
        }.start()
    }

    private fun deleteComputerDetails(id: String, callback: (Boolean) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.1.49:3000/api/computers/$id")
            .delete()
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                callback(response.isSuccessful)
            } catch (e: IOException) {
                e.printStackTrace()
                callback(false)
            }
        }.start()
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("image", ".jpg", cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) {
            Log.e("ComputerDetailsActivity", "Failed to create file from Uri", e)
            null
        }
    }

    private fun loadImage(url: String, imageView: ImageView) {
        runOnUiThread {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Toast.makeText(this@ComputerDetailsActivity, "เกิดข้อผิดพลาดในการโหลดรูปภาพ", Toast.LENGTH_SHORT).show()
                        Log.e("ComputerDetailsActivity", "Glide error: ${e?.message}")
                        return false
                    }

                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(imageView)
        }
    }

    private fun parseComputerJson(json: String): Computer {
        val jsonObject = JSONObject(json)
        return Computer(
            id = jsonObject.getInt("id"),
            brandName = jsonObject.getString("brandName"),
            modelName = jsonObject.getString("modelName"),
            serialNumber = jsonObject.getString("serialNumber"),
            stockQuantity = jsonObject.getInt("stockQuantity"),
            price = jsonObject.getString("price"),
            cpuSpeed = jsonObject.getString("cpuSpeed"),
            memoryCapacity = jsonObject.getString("memoryCapacity"),
            hardDiskCapacity = jsonObject.getString("hardDiskCapacity"),
            image = jsonObject.optString("image", null)
        )
    }

    private fun setEditingEnabled(enabled: Boolean) {
        brandNameEditText.isEnabled = enabled
        modelNameEditText.isEnabled = enabled
        serialNumberEditText.isEnabled = enabled
        stockQuantityEditText.isEnabled = enabled
        priceEditText.isEnabled = enabled
        cpuSpeedEditText.isEnabled = enabled
        memoryCapacityEditText.isEnabled = enabled
        hardDiskCapacityEditText.isEnabled = enabled
        saveButton.isEnabled = enabled
        changeImageButton.isEnabled = enabled
    }

    private fun populateFields(computer: Computer) {
        brandNameEditText.setText(computer.brandName)
        modelNameEditText.setText(computer.modelName)
        serialNumberEditText.setText(computer.serialNumber)
        stockQuantityEditText.setText(computer.stockQuantity.toString())
        priceEditText.setText(computer.price.toString())
        cpuSpeedEditText.setText(computer.cpuSpeed.toString())
        memoryCapacityEditText.setText(computer.memoryCapacity)
        hardDiskCapacityEditText.setText(computer.hardDiskCapacity)
        computer.image?.let { imageUrl ->
            Log.d("ComputerDetailsActivity", "Image URL: $imageUrl")
            loadImage(imageUrl, imageView)
        }
    }

    private fun clearFields() {
        brandNameEditText.text.clear()
        modelNameEditText.text.clear()
        serialNumberEditText.text.clear()
        stockQuantityEditText.text.clear()
        priceEditText.text.clear()
        cpuSpeedEditText.text.clear()
        memoryCapacityEditText.text.clear()
        hardDiskCapacityEditText.text.clear()
        imageView.setImageResource(R.drawable.placeholder)
    }

    private fun createComputerFromFields(computerId: String): Computer {
        return Computer(
            id = computerId.toInt(),
            brandName = brandNameEditText.text.toString(),
            modelName = modelNameEditText.text.toString(),
            serialNumber = serialNumberEditText.text.toString(),
            stockQuantity = stockQuantityEditText.text.toString().toInt(),
            price = priceEditText.text.toString(),
            cpuSpeed = cpuSpeedEditText.text.toString(),
            memoryCapacity = memoryCapacityEditText.text.toString(),
            hardDiskCapacity = hardDiskCapacityEditText.text.toString(),
            image = null // Set this to the URL if there's a need to update image
        )
    }
}
