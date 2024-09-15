package th.ac.rmutto.methapornandroid

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ComputerAdapter(private val computerList: List<Computer>) : RecyclerView.Adapter<ComputerAdapter.ComputerViewHolder>() {

    class ComputerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val brandName: TextView = itemView.findViewById(R.id.textViewBrandName)
        val modelName: TextView = itemView.findViewById(R.id.textViewModelName)
        val serialNumber: TextView = itemView.findViewById(R.id.textViewSerialNumber)
        val price: TextView = itemView.findViewById(R.id.textViewPrice)
        val imageView: ImageView = itemView.findViewById(R.id.imageViewComputer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComputerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_computer, parent, false)
        return ComputerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComputerViewHolder, position: Int) {
        val computer = computerList[position]
        holder.brandName.text = computer.brandName
        holder.modelName.text = computer.modelName
        holder.serialNumber.text = computer.serialNumber
        holder.price.text = computer.price

        // ตรวจสอบ URL ของรูปภาพ
        Log.d("IMAGE_URL", "Image URL: ${computer.image}")

        val imageUrl = computer.image ?: ""  // ถ้าไม่มี URL ให้เป็นค่าว่าง

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder)  // รูปภาพที่แสดงก่อนโหลดเสร็จ
            .error(R.drawable.error)  // รูปภาพที่แสดงเมื่อเกิดข้อผิดพลาด
            .into(holder.imageView)
    }



    override fun getItemCount(): Int {
        return computerList.size
    }
}
