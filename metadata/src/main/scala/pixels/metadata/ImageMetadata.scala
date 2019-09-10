package pixels.metadata

case class ImageMetadata(
    width: Int = 0,
    height: Int = 0,
    focalLength: Int = 0,
    aperture: Double = 0.0,
    exposure: String = "",
    ISO: Int = 0
)
