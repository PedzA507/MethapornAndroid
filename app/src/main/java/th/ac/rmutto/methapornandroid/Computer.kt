package th.ac.rmutto.methapornandroid

data class Computer(
    val id: Int,
    val brandName: String,
    val modelName: String,
    val serialNumber: String,
    val stockQuantity: Int,
    val price: String,
    val cpuSpeed: String,
    val memoryCapacity: String,
    val hardDiskCapacity: String,
    val image: String?
)

