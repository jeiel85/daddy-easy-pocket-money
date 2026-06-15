package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FixedExpense
import com.example.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedExpensesScreen(
    viewModel: AppViewModel
) {
    val fixedExpenses by viewModel.allFixedExpenses.collectAsState(initial = emptyList())
    val currentMonth by viewModel.currentMonth.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var fixedToEdit by remember { mutableStateOf<FixedExpense?>(null) }

    // Form states
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("통신비") }
    var paymentDay by remember { mutableStateOf("25") }
    var autoApply by remember { mutableStateOf(true) }

    val categories = listOf("통신비", "보험", "구독료", "교통비", "용돈", "기타 고정비")

    val yearStr = currentMonth.take(4)
    val monthStr = currentMonth.takeLast(2)

    LaunchedEffect(fixedToEdit) {
        if (fixedToEdit != null) {
            val f = fixedToEdit!!
            title = f.title
            amountStr = f.amount.toString()
            category = f.category
            paymentDay = f.paymentDay.toString()
            autoApply = f.autoApply
        } else {
            title = ""
            amountStr = ""
            category = "통신비"
            paymentDay = "25"
            autoApply = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("매월 나가는 고정 지출 목록", fontWeight = FontWeight.Bold, fontSize = 21.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    fixedToEdit = null
                    showAddEditDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "등록", modifier = Modifier.size(24.dp)) },
                text = { Text("고정 지출 추가하기", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
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
            // Informative advice block for fathers
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "정보", tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "보험료, 통신요금, 교통비, 넷플릭스 등 매달 반복해 빠져나가는 자동이체를 등록하세요. '매월 자동 반영'을 체크하시면 새롭게 매월 장부를 시작할 때 알아서 지출 내역서에 기록되어 예산이 차감됩니다.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (fixedExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "아직 등록된 정기 고정비가 없습니다.\n우측 하단 버튼을 눌러 정기 지출을 등록해보세요.",
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
                    items(fixedExpenses, key = { it.id }) { fixed ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (fixed.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = fixed.title,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (fixed.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(fixed.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                    }

                                    Text(
                                        text = "정산일: 매월 ${fixed.paymentDay}일 납부",
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "매월 정기이체",
                                            modifier = Modifier.size(16.dp),
                                            tint = if (fixed.autoApply) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = if (fixed.autoApply) "매월 ${monthStr}월 장부에 자동 이체 반영됨" else "자동 고정 처리를 사용하지 않음",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (fixed.autoApply) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = formatCurrency(fixed.amount),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (fixed.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }

                                // Actions
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            fixedToEdit = fixed
                                            showAddEditDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Outlined.Edit, contentDescription = "수정", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteFixedExpense(fixed)
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

    // --- Fixed Expense Register/Edit Input Dialog ---
    if (showAddEditDialog) {
        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = {
                Text(
                    text = if (fixedToEdit == null) "정기 고정 지출 추가" else "정기 고정 지출 수정",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("고정 지출 적요명 (예: SKT 통신비, 종신 보험료 등)", fontSize = 14.sp) },
                        placeholder = { Text("통합 고정비명") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                amountStr = input
                            }
                        },
                        label = { Text("매월 정기 이체금액 (원)", fontSize = 14.sp) },
                        placeholder = { Text("금액 작성 (예: 65000)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Simple category selector dropdown logic
                    var categoryExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { categoryExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("보일 분류: $category", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.Add, contentDescription = "펼치기")
                            }
                        }
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.81f)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        category = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = paymentDay,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                val value = input.toIntOrNull() ?: 1
                                if (value in 1..31) {
                                    paymentDay = input
                                }
                            }
                        },
                        label = { Text("이체 결산일 (매월 1~31일 지정)", fontSize = 14.sp) },
                        placeholder = { Text("예: 25") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("매월 자동 반영설정", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("매월 장부를 새로 열 때 자동으로 이 지출 내역 생성", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoApply,
                            onCheckedChange = { autoApply = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountLong = amountStr.toLongOrNull() ?: 0L
                        val paymentDayInt = paymentDay.toIntOrNull() ?: 25
                        if (title.trim().isEmpty() || amountLong <= 0 || paymentDayInt !in 1..31) {
                            return@Button
                        }

                        viewModel.saveFixedExpense(
                            id = fixedToEdit?.id ?: 0,
                            title = title.trim(),
                            amount = amountLong,
                            category = category,
                            paymentDay = paymentDayInt,
                            isActive = true,
                            autoApply = autoApply
                        )
                        showAddEditDialog = false
                    }
                ) {
                    Text("저장", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}
