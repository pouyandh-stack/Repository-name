package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.ui.theme.*
import com.example.viewmodel.JaarchiUiState
import com.example.viewmodel.JaarchiViewModel
import kotlinx.coroutines.launch

@Composable
fun SupportChatScreen(
    uiState: JaarchiUiState,
    viewModel: JaarchiViewModel,
    onBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralLight)
            .testTag("support_chat_screen")
    ) {
        // Chat Header
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = Color.White)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = CrimsonRed)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("پشتیبانی و چت آنلاین ${uiState.appName}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text("پاسخگویی هوشمند ۲۴ ساعته", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.chatMessages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    onQuickReplyClick = { reply -> viewModel.sendUserChatMessage(reply) }
                )
            }

            if (uiState.isBotTyping) {
                item {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = CrimsonRed)
                        Text("کارشناس هوشمند در حال نوشتن پاسخ...", fontSize = 11.sp, color = NeutralMedium)
                    }
                }
            }
        }

        // Message Input Row
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("سوال خود را بپرسید...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendUserChatMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("send_message_button")
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "ارسال", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onQuickReplyClick: (String) -> Unit
) {
    val isUser = message.isFromUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary else Color.White,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .widthIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = if (isUser) Color.White else NeutralDark
                )
                Text(
                    text = message.timestamp,
                    fontSize = 10.sp,
                    color = if (isUser) Color.White.copy(alpha = 0.7f) else NeutralMedium,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        if (message.quickReplies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                message.quickReplies.forEach { reply ->
                    SuggestionChip(
                        onClick = { onQuickReplyClick(reply) },
                        label = { Text(reply, fontSize = 11.sp) }
                    )
                }
            }
        }
    }
}
