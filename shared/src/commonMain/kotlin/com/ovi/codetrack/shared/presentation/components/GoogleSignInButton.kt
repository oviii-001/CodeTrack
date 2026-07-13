package com.ovi.codetrack.shared.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit
)
