package pixels.api
import akka.http.scaladsl.model.Multipart
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes

trait FileUploadUtils {
  def multipartFile(contents: String, fileName: String) =
    Multipart.FormData(
      Multipart.FormData.BodyPart.Strict(
        "file",
        HttpEntity(ContentTypes.`text/plain(UTF-8)`, contents),
        Map("filename" -> fileName)
      )
    )
}
