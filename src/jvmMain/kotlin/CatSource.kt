import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

object CatSource {

    suspend fun postPasswds(): Result<String> = runCatching {
        httpClient.post {
            url("Passwd/passwd/passwds.do")
            setBody(MultiPartFormDataContent(partData()))
            contentType(ContentType.Application.Json)
        }.body()
    }

    private fun HttpRequestBuilder.partData() = formData {
        headers {
            append(
                "access_token",
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoxLCJleHAiOjE2OTI0MjYzNDksInVzZXJuYW1lIjoibHVjYXMifQ.2lapTgjCqbXRZF28ICeUhxnpvtMzqTpzr9xN7xqOWao"
            )
        }
        append("user_id", 1)
        append("secret_key", "SkGk5x4IqWs0HC5w9b5Fcak8NX0lgBmMrvVRFxg3nAQ=")
    }

}