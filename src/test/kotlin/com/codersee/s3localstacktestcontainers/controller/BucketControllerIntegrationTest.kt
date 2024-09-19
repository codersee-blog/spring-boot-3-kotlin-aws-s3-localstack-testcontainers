package com.codersee.s3localstacktestcontainers.controller

import com.codersee.s3localstacktestcontainers.LocalStackIntegrationTest
import com.codersee.s3localstacktestcontainers.controller.BucketController.BucketRequest
import com.codersee.s3localstacktestcontainers.util.createBucket
import com.codersee.s3localstacktestcontainers.util.deleteBucket
import com.codersee.s3localstacktestcontainers.util.deleteObject
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BucketControllerIntegrationTest(
  @Autowired private val webTestClient: WebTestClient
) : LocalStackIntegrationTest() {

  @Test
  fun `Given no existing buckets When getting list of buckets Then return an empty array`() {
    val buckets = webTestClient
      .get().uri("/buckets")
      .exchange()
      .expectStatus().isOk()
      .expectBody(object : ParameterizedTypeReference<List<String>>() {})
      .returnResult()
      .responseBody

    assertNotNull(buckets)
    assertTrue(buckets.isEmpty())
  }

  @Test
  fun `Given one existing bucket When getting list of buckets Then return an array with expected bucket name`() {
    val bucketName = "bucket-1"
    localStack.createBucket(bucketName)

    val expectedJson = """
      [ "Bucket #1: $bucketName" ]
    """

    webTestClient
      .get().uri("/buckets")
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .json(expectedJson)

    localStack.deleteBucket(bucketName)
  }

  @Test
  fun `Given no existing buckets When creating bucket Then create bucket successfully`() {
    val bucketName = "bucket-2"

    webTestClient
      .post().uri("/buckets")
      .bodyValue(BucketRequest(bucketName = bucketName))
      .exchange()
      .expectStatus().isOk()

    val execResult = localStack.execInContainer("awslocal", "s3api", "list-buckets").stdout
    // The above returns:
    //
    //{
    //  "Buckets": [
    //    {
    //      "Name": "bucket-2",
    //      "CreationDate": "2024-09-19T05:28:42.000Z"
    //    }
    //  ],
    //  "Owner": {
    //    "DisplayName": "webfile",
    //    "ID": "75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a"
    //  }
    //}

    assertTrue(execResult.contains(bucketName))

    // Alternatively, let's use the other endpoint :)
    val expectedJson = """
      [ "Bucket #1: $bucketName" ]
    """

    webTestClient
      .get().uri("/buckets")
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .json(expectedJson)

    localStack.deleteBucket(bucketName)
  }

  @Test
  fun `Given no objects existing in the bucket When getting objects of a bucket Then return an empty array`() {
    val bucketName = "bucket-3"
    localStack.createBucket(bucketName)

    val objects = webTestClient
      .get().uri("/buckets/$bucketName/objects")
      .exchange()
      .expectStatus().isOk()
      .expectBody(object : ParameterizedTypeReference<List<String>>() {})
      .returnResult()
      .responseBody

    assertNotNull(objects)
    assertTrue(objects.isEmpty())

    localStack.deleteBucket(bucketName)
  }

  @Test
  fun `Given no objects When creating example object Then return created object`() {
    val bucketName = "bucket-4"
    val objectName = "example.json"
    localStack.createBucket(bucketName)

    val expectedJson = """
      {
        "id": "123",
        "name": "Some name"
      }
    """

    webTestClient
      .post().uri("/buckets/$bucketName/objects")
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .json(expectedJson)

    localStack.deleteObject(bucketName, objectName)
    localStack.deleteBucket(bucketName)
  }

  @Test
  fun `Given created object When getting list of objects Then return array with one object`() {
    val bucketName = "bucket-5"
    val objectName = "example.json"
    localStack.createBucket(bucketName)

    val expectedJson = """
      [ $objectName ]
    """

    webTestClient
      .post().uri("/buckets/$bucketName/objects")
      .exchange()
      .expectStatus().isOk()

    webTestClient
      .get().uri("/buckets/$bucketName/objects")
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .json(expectedJson)

    localStack.deleteObject(bucketName, objectName)
    localStack.deleteBucket(bucketName)
  }

  @Test
  fun `Given existing object When getting object by key Then return object content`() {
    val bucketName = "bucket-6"
    val objectName = "example.json"
    localStack.createBucket(bucketName)

    val expected = """
      {
        "id": "123",
        "name": "Some name"
      }
    """

    webTestClient
      .post().uri("/buckets/$bucketName/objects")
      .exchange()

    webTestClient
      .get().uri("/buckets/$bucketName/objects/$objectName")
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .json(expected)

    localStack.deleteObject(bucketName, objectName)
    localStack.deleteBucket(bucketName)
  }

  @Test
  fun `Given existing bucket with object When deleting bucket Then bucket is removed`() {
    val bucketName = "bucket-7"
    localStack.createBucket(bucketName)

    webTestClient
      .post().uri("/buckets/$bucketName/objects")
      .exchange()
      .expectStatus().isOk()

    webTestClient
      .delete().uri("/buckets/$bucketName")
      .exchange()
      .expectStatus().isOk()

    val buckets = webTestClient
      .get().uri("/buckets")
      .exchange()
      .expectStatus().isOk()
      .expectBody(object : ParameterizedTypeReference<List<String>>() {})
      .returnResult()
      .responseBody

    assertNotNull(buckets)
    assertTrue(buckets.isEmpty())
  }

}
