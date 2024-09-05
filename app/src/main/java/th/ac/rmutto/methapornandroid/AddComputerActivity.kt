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
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class AddComputerActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private lateinit var imageViewSelected: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_computer)

        val brandNameEditText: EditText = findViewById(R.id.editBrandName)
        val modelNameEditText: EditText = findViewById(R.id.editModelName)
        val serialNumberEditText: EditText = findViewById(R.id.editSerialNumber)
        val stockQuantityEditText: EditText = findViewById(R.id.editStockQuantity)
        val priceEditText: EditText = findViewById(R.id.editPrice)
        val cpuSpeedEditText: EditText = findViewById(R.id.editCpuSpeed)
        val memoryCapacityEditText: EditText = findViewById(R.id.editMemoryCapacity)
        val hardDiskCapacityEditText: EditText = findViewById(R.id.editHardDiskCapacity)
        val submitButton: Button = findViewById(R.id.buttonSubmit)
        val selectImageButton: Button = findViewById(R.id.buttonSelectImage)
        imageViewSelected = findViewById(R.id.imageViewSelected)

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        submitButton.setOnClickListener {
            val brandName = brandNameEditText.text.toString()
            val modelName = modelNameEditText.text.toString()
            val serialNumber = serialNumberEditText.text.toString()
            val stockQuantity = stockQuantityEditText.text.toString()
            val price = priceEditText.text.toString()
            val cpuSpeed = cpuSpeedEditText.text.toString()
            val memoryCapacity = memoryCapacityEditText.text.toString()
            val hardDiskCapacity = hardDiskCapacityEditText.text.toString()

            if (brandName.isEmpty() || modelName.isEmpty() || serialNumber.isEmpty() ||
                stockQuantity.isEmpty() || price.isEmpty() ||
                cpuSpeed.isEmpty() || memoryCapacity.isEmpty() ||
                hardDiskCapacity.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Thread {
                val result = sendComputerData(
                    brandName, modelName, serialNumber, stockQuantity,
                    price, cpuSpeed, memoryCapacity, hardDiskCapacity, selectedImageUri
                )
                runOnUiThread {
                    if (result) {
                        Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            imageViewSelected.setImageURI(selectedImageUri)
            Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show()
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Image selection canceled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
        }
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
            Log.e("AddComputerActivity", "Failed to create file from Uri", e)
            null
        }
    }

    private fun sendComputerData(
        brandName: String, modelName: String, serialNumber: String,
        stockQuantity: String, price: String, cpuSpeed: String,
        memoryCapacity: String, hardDiskCapacity: String,
        imageUri: Uri?
    ): Boolean {
        return try {
            val client = OkHttpClient()
            val requestBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("brandName", brandName)
                .addFormDataPart("modelName", modelName)
                .addFormDataPart("serialNumber", serialNumber)
                .addFormDataPart("stockQuantity", stockQuantity)
                .addFormDataPart("price", "$price Baht")
                .addFormDataPart("cpuSpeed", "$cpuSpeed GHz")
                .addFormDataPart("memoryCapacity", "$memoryCapacity GB")
                .addFormDataPart("hardDiskCapacity", "$hardDiskCapacity TB")

            if (imageUri != null) {
                val file = getFileFromUri(imageUri)
                if (file != null && file.exists()) {
                    val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
                    requestBuilder.addFormDataPart("image", file.name, requestBody)
                } else {
                    Log.e("AddComputerActivity", "File does not exist: ${file?.path}")
                    return false
                }
            }

            val requestBody = requestBuilder.build()
            val request = Request.Builder()
                .url("http://192.168.1.49:3000/api/computers") // เปลี่ยนเป็น URL จริง
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("AddComputerActivity", "Response failed: ${response.code} ${response.message}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("AddComputerActivity", "Exception: ${e.message}")
            false
        }
    }
}
