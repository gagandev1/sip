package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.ui.components.glass.GlassSurface
import com.example.ui.theme.SipAquaPrimary
import com.example.viewmodel.SipViewModel

@Composable
fun CoachScreen(
    viewModel: SipViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    val quickQuestions = remember {
        listOf(
            "How much water should I drink during a workout?",
            "Does tea and coffee count towards my water goal?",
            "What are the best times of day to hydrate?",
            "How does hydration impact deep sleep and recovery?"
        )
    }

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("coach_screen_column")
    ) {
        // Top Floating Glass Header
        GlassSurface(
            shape = RoundedCornerShape(24.dp),
            glassOpacity = profile.glassOpacity,
            reduceTransparency = profile.reduceTransparency,
            elevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SipAquaPrimary.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Sip Coach",
                        tint = SipAquaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sip Coach",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = if (isThinking) "Refining advice..." else "Hydration and Habit Specialist",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isThinking) SipAquaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Messages thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                GlassChatBubble(
                    message = msg,
                    glassOpacity = profile.glassOpacity,
                    reduceTransparency = profile.reduceTransparency
                )
            }

            if (isThinking && messages.lastOrNull()?.text?.isEmpty() == true) {
                item {
                    // Three bouncing droplet dots typing indicator
                    BouncingDropletTypingIndicator()
                }
            }
        }

        // Quick Suggestion Glass Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickQuestions.forEach { question ->
                GlassSurface(
                    shape = RoundedCornerShape(18.dp),
                    glassOpacity = (profile.glassOpacity - 0.1f).coerceAtLeast(0.3f),
                    reduceTransparency = profile.reduceTransparency,
                    elevation = 1.dp,
                    onClick = { viewModel.sendChatMessage(question) }
                ) {
                    Text(
                        text = question,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Floating Glass Input Bar
        GlassSurface(
            shape = RoundedCornerShape(28.dp),
            glassOpacity = (profile.glassOpacity + 0.15f).coerceAtMost(0.95f),
            reduceTransparency = profile.reduceTransparency,
            elevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 80.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = { Text("Ask your hydration coach...", fontSize = 14.sp) },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputPrompt.isNotBlank()) {
                            viewModel.sendChatMessage(inputPrompt)
                            inputPrompt = ""
                            focusManager.clearFocus()
                        }
                    }),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("coach_input_field")
                )
                IconButton(
                    onClick = {
                        if (inputPrompt.isNotBlank()) {
                            viewModel.sendChatMessage(inputPrompt)
                            inputPrompt = ""
                            focusManager.clearFocus()
                        }
                    },
                    enabled = inputPrompt.isNotBlank() && !isThinking,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (inputPrompt.isNotBlank() && !isThinking) SipAquaPrimary else Color.White.copy(alpha = 0.1f))
                        .testTag("coach_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send",
                        tint = if (inputPrompt.isNotBlank() && !isThinking) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GlassChatBubble(
    message: ChatMessage,
    glassOpacity: Float,
    reduceTransparency: Boolean
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        GlassSurface(
            shape = RoundedCornerShape(
                topStart = 22.dp,
                topEnd = 22.dp,
                bottomStart = if (isUser) 22.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 22.dp
            ),
            glassOpacity = if (isUser) (glassOpacity + 0.2f).coerceAtMost(0.95f) else glassOpacity,
            reduceTransparency = reduceTransparency,
            elevation = 2.dp,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (isUser) SipAquaPrimary.copy(alpha = 0.32f) else Color.Transparent
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
fun BouncingDropletTypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "BouncingDroplets")
    val d1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(animation = tween(400, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "D1"
    )
    val d2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(animation = tween(400, delayMillis = 150, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "D2"
    )
    val d3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(animation = tween(400, delayMillis = 300, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "D3"
    )

    Row(
        modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(d1, d2, d3).forEach { offset ->
            Box(
                modifier = Modifier
                    .offset(y = offset.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(SipAquaPrimary)
            )
        }
    }
}
