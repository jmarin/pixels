package pixels.model

import pixels.metadata.ImageMetadata

case class Image(data: Option[ImageData] = None, metadata: Option[ImageMetadata] = None)
