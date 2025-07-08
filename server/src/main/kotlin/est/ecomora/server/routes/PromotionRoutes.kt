package est.ecomora.server.routes

import est.ecomora.server.domain.repository.promotions.PromotionsRepositoryImpl
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.text.SimpleDateFormat

fun Route.promotionRoutes(
    db: PromotionsRepositoryImpl
) {
    route("v1/promotions") {
        post {
            val multipart = call.receiveMultipart()
            var title: String? = null
            var description: String? = null
            var imageUrl: String? = null
            var startDate: Long? = null
            var endDate: Long? = null
            var enable: Boolean? = null
            val uploadDir = File("upload/products/promotions")
            if (!uploadDir.exists()){
                uploadDir.mkdirs()
            }
            val dateFormat = SimpleDateFormat("MM/dd/yyyy")

            multipart.forEachPart { partData ->
                when (partData) {
                    is PartData.FileItem -> {
                        val fileName = partData.originalFileName?.replace(" ","_") ?: "name/${System.currentTimeMillis()}"
                        val file = File(uploadDir,fileName)
                        partData.streamProvider().use { input->
                            file.outputStream().use { output->
                                input.copyTo(output)
                            }
                        }
                        imageUrl = "/upload/products/promotions/$fileName"
                    }

                    is PartData.FormItem -> {
                        when(partData.name){
                            "title" -> title = partData.value
                            "description" -> description = partData.value
                            "startDate" ->startDate= partData.value?.let { dateFormat.parse(it)?.time }
                            "endDate" -> endDate = partData.value?.let { dateFormat.parse(it)?.time }
                            "enable" -> enable = partData.value.toBooleanStrictOrNull()
                        }
                    }
                    else -> {}
                }
            }
            try {
                val products = db.insertPromo(
                    title  = title ?: return@post call.respond(HttpStatusCode.BadRequest, "Title Missing"),
                    description = description ?: return@post call.respond(HttpStatusCode.BadRequest, "Description Missing"),
                    imageUrl = imageUrl ?: "/upload/products/promotions/default.jpg",
                    startDate = startDate ?: return@post call.respond(HttpStatusCode.BadRequest, "Start Date Missing"),
                    endDate = endDate ?: return@post call.respond(HttpStatusCode.BadRequest,"End Date Missing"),
                    enabled = enable ?: return@post call.respond(HttpStatusCode.BadRequest,"Enabled Missing")
                )
                products.let {
                    call.respond(
                        HttpStatusCode.Created,
                        "Promotion Product Added Successfully... $products"
                    )
                }

            }catch (e: Exception){
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    "Error While Uploading Promotions Products : ${e.message}"
                )
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            try {
                val deletedCount = db.deletePromotionById(id)
                if (deletedCount != null && deletedCount > 0) {
                    call.respond(HttpStatusCode.OK, "Promotion with ID $id deleted successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Promotion with ID $id not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to delete promotion: ${e.message}")
            }
        }

        get("{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            try {
                val promotion = db.getPromotionById(id)
                if (promotion != null) {
                    call.respond(HttpStatusCode.OK, promotion)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Promotion with ID $id not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to retrieve promotion: ${e.message}")
            }
        }

        put("{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                "Id Missing"
            )
            val multipart = call.receiveMultipart()
            var title : String? = null
            var description: String? = null
            var imageUrl: String? = null
            var startDate: Long? = null
            var endDate: Long? = null
            var enable: Boolean? = null
            val uploadDir = File("upload/products/promotions")
            if (!uploadDir.exists()){
                uploadDir.mkdirs()
            }
            val dateFormat = SimpleDateFormat("MM/dd/yyyy")

            multipart.forEachPart { partData ->
                when (partData) {
                    is PartData.FileItem -> {
                        val fileName = partData.originalFileName?.replace(" ","_") ?: "name/${System.currentTimeMillis()}"
                        val file = File(uploadDir,fileName)
                        partData.streamProvider().use { input->
                            file.outputStream().use { output->
                                input.copyTo(output)
                            }
                        }
                        imageUrl = "/upload/products/promotions/$fileName"
                    }

                    is PartData.FormItem -> {
                        when(partData.name){
                            "title" -> title = partData.value
                            "description" -> description = partData.value
                            "startDate" ->startDate= partData.value?.let { dateFormat.parse(it)?.time }
                            "endDate" -> endDate = partData.value?.let { dateFormat.parse(it)?.time }
                            "enable" -> enable = partData.value.toBooleanStrictOrNull()
                        }
                    }
                    else ->{}
                }
            }

            try {
                // Get existing promotion to use as defaults for missing fields
                val existingPromotion = db.getPromotionById(id.toLong())
                    ?: return@put call.respond(
                        HttpStatusCode.NotFound,
                        "Promotion with ID $id not found"
                    )

                val services = db.updatePromo(
                    id = id.toLong(),
                    title = title ?: existingPromotion.title,
                    description = description ?: existingPromotion.description,
                    imageUrl = imageUrl ?: existingPromotion.imageUrl,
                    startDate = startDate ?: existingPromotion.startDate,
                    endDate = endDate ?: existingPromotion.endDate,
                    enabled = enable ?: existingPromotion.enabled
                )
                if (services != null) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        "Promotion Updated Successfully"
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "Promotion with ID $id not found"
                    )
                }

            }catch (e: Exception){
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    "Error While Updating Promotion: ${e.message}"
                )
            }
        }

        get {
            try {
                val promotions = db.getAllPromotions()
                if (promotions.isNullOrEmpty() == true ) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        "No Promotion Items Available inside the Database."
                    )
                } else {
                    call.respond(HttpStatusCode.OK, promotions)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to retrieve promotion: ${e.message}")
            }
        }
    }
}