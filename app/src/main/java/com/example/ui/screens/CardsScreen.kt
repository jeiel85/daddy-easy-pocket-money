package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardInfo
import com.example.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    viewModel: AppViewModel
) {
    val cards by viewModel.allCards.collectAsState(initial = emptyList())
    val currentMonth by viewModel.currentMonth.collectAsState()
    val monthlySpendByCard by viewModel.cardMonthlySpend.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<CardInfo?>(null) }

    // Form states
    var cardName by remember { mutableStateOf("") }
    var cardPaymentDay by remember { mutableStateOf("14") }
    var cardActive by remember { mutableStateOf(true) }

    LaunchedEffect(cardToEdit) {
        if (cardToEdit != null) {
            val card = cardToEdit!!
            cardName = card.name
            cardPaymentDay = card.paymentDay.toString()
            cardActive = card.isActive
        } else {
            cardName = ""
            cardPaymentDay = "14"
            cardActive = true
        }
    }

    val yearStr = currentMonth.take(4)
    val monthStr = currentMonth.takeLast(2)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 지출 카드 관리", fontWeight = FontWeight.Bold, fontSize = 21.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    cardToEdit = null
                    showAddEditDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "카드 등록", modifier = Modifier.size(24.dp)) },
                text = { Text("카드 새로 등록하기", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 16.dp).height(56.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Description banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 카드 정보를 등록해두시면 각 카드별 결제일 알림을 드리고 이번 달 실시간 결제 누적 금액을 계산해 보여드립니다.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        lineHeight = 22.sp
                    )
                }
            }

            if (cards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "등록된 카드가 없습니다\n오른쪽 아래 버튼을 눌러 소유하신 카드를 추가해 주세요.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(cards, key = { it.id }) { card ->
                        val cardSpend = monthlySpendByCard[card.id] ?: 0L
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (card.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = "신용카드",
                                    tint = if (card.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                )

                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = card.name,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (card.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        if (!card.isActive) {
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("사용 안함", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Text(
                                        text = "결제 정산일: 매월 ${card.paymentDay}일",
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (card.isActive) {
                                        Text(
                                            text = "${monthStr}월 사용 카드값: " + formatCurrency(cardSpend),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                // Edit or Delete controls
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            cardToEdit = card
                                            showAddEditDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Outlined.Edit, contentDescription = "수정", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteCard(card)
                                        }
                                    ) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "삭제", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Card Details Input Dialog ---
    if (showAddEditDialog) {
        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = {
                Text(
                    text = if (cardToEdit == null) "새 카드 추가하기" else "카드 정보 수정",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        label = { Text("카드 별칭 (예: 국민 탄탄대로, 삼성 아멕스, 신한 등)", fontSize = 14.sp) },
                        placeholder = { Text("카드명 입력") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cardPaymentDay,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                val value = input.toIntOrNull() ?: 0
                                if (value in 0..31) {
                                    cardPaymentDay = input
                                }
                            }
                        },
                        label = { Text("결제 결산일 (매월 지정일: 1~31)", fontSize = 14.sp) },
                        placeholder = { Text("예: 14") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("이 카드를 지출 장부에서 활성화 사용", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = cardActive,
                            onCheckedChange = { cardActive = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val paymentDayInt = cardPaymentDay.toIntOrNull() ?: 14
                        if (cardName.trim().isEmpty() || paymentDayInt !in 1..31) {
                            return@Button
                        }

                        viewModel.saveCard(
                            id = cardToEdit?.id ?: 0,
                            name = cardName.trim(),
                            paymentDay = paymentDayInt,
                            isActive = cardActive
                        )
                        showAddEditDialog = false
                    }
                ) {
                    Text("저장", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text("취소", fontSize = 15.sp)
                }
            }
        )
    }
}
