// MainActivity.kt
package th.ac.rmutto.methapornandroid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ปุ่มสำหรับไปหน้า AddComputerActivity
        val btnAddCom: Button = findViewById(R.id.btnAddCom)
        btnAddCom.setOnClickListener {
            val intent = Intent(this, AddComputerActivity::class.java)
            startActivity(intent)
        }

        // ปุ่มสำหรับไปหน้า ComputerDetailsActivity
        val btnComDetail: Button = findViewById(R.id.btnComDetail)
        btnComDetail.setOnClickListener {
            val intent = Intent(this, ComputerDetailsActivity::class.java)
            startActivity(intent)
        }
    }
}

