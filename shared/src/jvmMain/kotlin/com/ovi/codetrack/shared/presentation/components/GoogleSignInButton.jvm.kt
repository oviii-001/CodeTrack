package com.ovi.codetrack.shared.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Composable
actual fun GoogleSignInButton(
    modifier: Modifier,
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // In a real application, you should securely store the client secret. 
    // For Desktop OAuth, Google allows "Desktop" client types which do not strictly require keeping the secret perfectly hidden, 
    // but typically you'd use PKCE if supported or the desktop client ID.
    val clientId = "764153822908-th4hrbvetusd4bn26e3l1vglcjovca9h.apps.googleusercontent.com"
    // Desktop clients actually don't use the web client ID for the flow, they use a desktop OAuth client.
    // However, since we only have the web client ID right now, we will attempt to use it.
    // Note: Google's OAuth policies restrict loopback IP redirects for Web Client IDs. 
    // A proper implementation requires creating a "Desktop app" OAuth Client ID in Google Cloud Console.

    OutlinedButton(
        onClick = {
            coroutineScope.launch {
                try {
                    val idToken = performDesktopOAuth(clientId)
                    onTokenReceived(idToken)
                } catch (e: Exception) {
                    onError("Desktop Google Sign-In failed: ${e.message}")
                }
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("G", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign in with Google", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private suspend fun performDesktopOAuth(clientId: String): String = withContext(Dispatchers.IO) {
    // 1. Setup local HTTP server to receive the callback
    val port = 8080
    val redirectUri = "http://127.0.0.1:$port/callback"
    var authCode: String? = null
    var errorMsg: String? = null

    val server = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0)
    server.createContext("/callback") { exchange ->
        val query = exchange.requestURI.query ?: ""
        val params = query.split("&").associate {
            val parts = it.split("=")
            parts[0] to if (parts.size > 1) parts[1] else ""
        }
        
        authCode = params["code"]
        errorMsg = params["error"]

        val response = if (authCode != null) {
            "Authentication successful! You can close this window."
        } else {
            "Authentication failed: $errorMsg"
        }
        
        exchange.sendResponseHeaders(200, response.length.toLong())
        exchange.responseBody.use { os ->
            os.write(response.toByteArray())
        }
    }
    server.start()

    try {
        // 2. Open browser
        val authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=$clientId&" +
                "response_type=code&" +
                "scope=email%20profile&" +
                "redirect_uri=$redirectUri"
        
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(authUrl))
        } else {
            throw Exception("Desktop browsing is not supported.")
        }

        // 3. Wait for the callback
        val timeoutMillis = 60000L
        val startTime = System.currentTimeMillis()
        while (authCode == null && errorMsg == null) {
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                throw Exception("Timed out waiting for authentication.")
            }
            Thread.sleep(500)
        }

        if (errorMsg != null) {
            throw Exception(errorMsg)
        }

        val code = authCode ?: throw Exception("No authorization code received.")

        // 4. Exchange code for token
        val tokenUrl = "https://oauth2.googleapis.com/token"
        val clientSecret = "" // Requires desktop client secret or Web Client secret. Note: Web clients don't allow localhost redirect natively without secret.
        // For the sake of this prototype and without a backend, we try to post. 
        // Note: Without a valid secret for Web Client, this will fail in production. We need a Desktop Client ID.
        
        val body = "client_id=$clientId&" +
                   "client_secret=$clientSecret&" +
                   "code=$code&" +
                   "grant_type=authorization_code&" +
                   "redirect_uri=$redirectUri"

        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI(tokenUrl))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw Exception("Token exchange failed: ${response.body()}")
        }

        // 5. Extract id_token
        val jsonElement = Json.parseToJsonElement(response.body()).jsonObject
        return@withContext jsonElement["id_token"]?.jsonPrimitive?.content ?: throw Exception("No id_token in response")
    } finally {
        server.stop(0)
    }
}
