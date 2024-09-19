package com.codersee.s3localstacktestcontainers.controller

import io.awspring.cloud.s3.S3Template
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.services.s3.S3Client
import kotlin.text.Charsets.UTF_8

@RestController
@RequestMapping("/buckets")
class BucketController(
  private val s3Template: S3Template,
  private val s3Client: S3Client,
) {

  @GetMapping
  fun listBuckets(): List<String> {
    val response = s3Client.listBuckets()

    return response.buckets()
      .mapIndexed { index, bucket ->
        "Bucket #${index + 1}: ${bucket.name()}"
      }
  }

  @PostMapping
  fun createBucket(@RequestBody request: BucketRequest) {
    s3Template.createBucket(request.bucketName)
  }

  @GetMapping("/{bucketName}/objects")
  fun listObjects(@PathVariable bucketName: String): List<String> =
    s3Template.listObjects(bucketName, "")
      .map { s3Resource -> s3Resource.filename }

  @PostMapping("/{bucketName}/objects")
  fun createExampleObject(@PathVariable bucketName: String): Example {
    val example = Example(id = "123", name = "Some name")

    s3Template.store(bucketName, "example.json", example)

    return s3Template.read(bucketName, "example.json", Example::class.java)
  }

  @GetMapping("/{bucketName}/objects/{objectName}")
  fun getObject(@PathVariable bucketName: String, @PathVariable objectName: String): String =
    s3Template.download(bucketName, objectName).getContentAsString(UTF_8)


  @DeleteMapping("/{bucketName}")
  fun deleteBucket(@PathVariable bucketName: String) {
    s3Template.listObjects(bucketName, "")
      .forEach { s3Template.deleteObject(bucketName, it.filename) }

    s3Template.deleteBucket(bucketName)
  }

  data class BucketRequest(val bucketName: String)

  data class Example(val id: String, val name: String)

}

