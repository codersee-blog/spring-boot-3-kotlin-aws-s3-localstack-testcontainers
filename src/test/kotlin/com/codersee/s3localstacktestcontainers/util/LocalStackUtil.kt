package com.codersee.s3localstacktestcontainers.util

import org.testcontainers.containers.localstack.LocalStackContainer

fun LocalStackContainer.createBucket(bucketName: String) {
  this.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", bucketName)
}

fun LocalStackContainer.deleteBucket(bucketName: String) {
  this.execInContainer("awslocal", "s3api", "delete-bucket", "--bucket", bucketName)
}

fun LocalStackContainer.deleteObject(bucketName: String, objectName: String) {
  this.execInContainer("awslocal", "s3api", "delete-object", "--bucket", bucketName, "--key", objectName)
}
