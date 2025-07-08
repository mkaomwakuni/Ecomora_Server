package est.ecomora.server

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testHealthEndpoint() = testApplication {
        application {
            module()
        }
        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(bodyAsText().contains("healthy"))
        }
    }

    @Test
    fun testRootEndpoint() = testApplication {
        application {
            module()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(bodyAsText().contains("Welcome to Ecomora API"))
        }
    }

    @Test
    fun testInvalidLoginRequest() = testApplication {
        application {
            module()
        }
        client.post("/api/v1/auth/login") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody("email=&password=")
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            assertTrue(bodyAsText().contains("Email is required"))
        }
    }
}