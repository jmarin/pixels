package pixels.common.db.model

case class ImageMetadataDbEntity(
    name: String,
    width: Int = 0,
    height: Int = 0,
    focalLength: Int = 0,
    aperture: Double = 0.0,
    exposure: String = "",
    ISO: Int = 0
)
