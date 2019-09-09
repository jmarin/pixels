# pixels
Demo of cloud photo management system built as a set of microservices with Akka Typed

## Configuration

In order to be able to upload images to S3, AWS credentials need to be provided before running the software:

```shell
export AWS_ACCESS_KEY_ID=<access key>
export AWS_SECRET_ACCESS_KEY=<secret key>
export AWS_REGION=<region>
```