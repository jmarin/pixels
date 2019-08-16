import BuildSettings._

lazy val pixels = (project in file("."))
  .settings(pixelsBuildSettings:_*)
  .aggregate(api)



lazy val api = (project in file("api"))
  .settings(pixelsBuildSettings:_*)
