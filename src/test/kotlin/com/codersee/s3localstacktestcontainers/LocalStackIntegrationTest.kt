package com.codersee.s3localstacktestcontainers

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.S3
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = RANDOM_PORT)
class LocalStackIntegrationTest {

  companion object {
    val localStack: LocalStackContainer = LocalStackContainer(
      DockerImageName.parse("localstack/localstack:3.7.2")
    ).apply {
      start()
    }

    @JvmStatic
    @DynamicPropertySource
    fun overrideProperties(registry: DynamicPropertyRegistry) {
      registry.add("spring.cloud.aws.region.static") { localStack.region }
      registry.add("spring.cloud.aws.credentials.access-key") { localStack.accessKey }
      registry.add("spring.cloud.aws.credentials.secret-key") { localStack.secretKey }
      registry.add("spring.cloud.aws.s3.endpoint") { localStack.getEndpointOverride(S3).toString() }
    }

  }
}

