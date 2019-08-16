package pixels.api
import akka.http.scaladsl.model.Multipart
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.Multipart.FormData

trait FileUploadUtils {
  def multipartFile(contents: String, fileName: String): FormData = {
    Multipart.FormData(
      Multipart.FormData.BodyPart.Strict(
        "file",
        HttpEntity(ContentTypes.`text/plain(UTF-8)`, contents),
        Map("filename" -> fileName)
      )
    )
  }

  def multipartFile(contents: Array[Byte], fileName: String): FormData = {
    Multipart.FormData(
      Multipart.FormData.BodyPart.Strict(
        "file",
        HttpEntity(ContentTypes.`application/octet-stream`, contents),
        Map("filename" -> fileName)
      )
    )
  }

}
