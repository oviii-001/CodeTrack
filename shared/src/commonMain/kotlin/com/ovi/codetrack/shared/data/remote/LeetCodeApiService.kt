package com.ovi.codetrack.shared.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class LeetCodeProblem(
    val questionId: String,
    val title: String,
    val titleSlug: String,
    val difficulty: String,
    val topicTags: List<String>
)

@Serializable
data class LeetCodeUserStats(
    val totalSolved: Int,
    val easySolved: Int,
    val mediumSolved: Int,
    val hardSolved: Int
)

class LeetCodeApiService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetch problem details from LeetCode GraphQL API by title slug.
     * Example slug: "two-sum", "valid-parentheses"
     */
    suspend fun fetchProblemBySlug(titleSlug: String): Result<LeetCodeProblem> {
        return try {
            val query = """
                {
                    "query": "query questionData(${'$'}titleSlug: String!) { question(titleSlug: ${'$'}titleSlug) { questionId title titleSlug difficulty topicTags { name } } }",
                    "variables": {"titleSlug": "$titleSlug"}
                }
            """.trimIndent()

            val response: HttpResponse = client.post("https://leetcode.com/graphql") {
                contentType(ContentType.Application.Json)
                setBody(query)
            }

            val body = response.bodyAsText()
            val jsonElement = json.parseToJsonElement(body)
            val questionData = jsonElement
                .jsonObject["data"]
                ?.jsonObject?.get("question")
                ?.jsonObject ?: return Result.failure(Exception("Problem not found"))

            val tags = questionData["topicTags"]
                ?.jsonArray
                ?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.contentOrNull }
                ?: emptyList()

            Result.success(
                LeetCodeProblem(
                    questionId = questionData["questionId"]?.jsonPrimitive?.content ?: "",
                    title = questionData["title"]?.jsonPrimitive?.content ?: "",
                    titleSlug = questionData["titleSlug"]?.jsonPrimitive?.content ?: "",
                    difficulty = questionData["difficulty"]?.jsonPrimitive?.content ?: "",
                    topicTags = tags
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch user's solved problem stats from the alfa-leetcode-api.
     */
    suspend fun fetchUserStats(username: String): Result<LeetCodeUserStats> {
        return try {
            val response: HttpResponse = client.get("https://alfa-leetcode-api.onrender.com/$username/solved")
            val body = response.bodyAsText()
            val jsonElement = json.parseToJsonElement(body)
            val obj = jsonElement.jsonObject

            // Check if there's an error (user not found)
            if (obj.containsKey("errors")) {
                return Result.failure(Exception("LeetCode user '$username' not found"))
            }

            Result.success(
                LeetCodeUserStats(
                    totalSolved = obj["solvedProblem"]?.jsonPrimitive?.int ?: 0,
                    easySolved = obj["easySolved"]?.jsonPrimitive?.int ?: 0,
                    mediumSolved = obj["mediumSolved"]?.jsonPrimitive?.int ?: 0,
                    hardSolved = obj["hardSolved"]?.jsonPrimitive?.int ?: 0
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Look up a problem by its numeric ID.
     * Since LeetCode GraphQL requires a title slug, we first try matching against
     * our local roadmap, then fall back to the alfa-leetcode-api problemset endpoint.
     */
    suspend fun fetchProblemById(problemId: Int): Result<LeetCodeProblem> {
        return try {
            // Use the alfa-leetcode-api to get problem list and find by ID
            val response: HttpResponse = client.get("https://alfa-leetcode-api.onrender.com/problems?limit=1&skip=${problemId - 1}")
            val body = response.bodyAsText()
            val jsonElement = json.parseToJsonElement(body)
            val problems = jsonElement.jsonObject["problemsetQuestionList"]?.jsonArray

            if (problems != null && problems.isNotEmpty()) {
                val problem = problems.first().jsonObject
                val titleSlug = problem["titleSlug"]?.jsonPrimitive?.content
                if (titleSlug != null) {
                    // Now fetch full details using the slug
                    return fetchProblemBySlug(titleSlug)
                }
            }
            
            Result.failure(Exception("Problem #$problemId not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
