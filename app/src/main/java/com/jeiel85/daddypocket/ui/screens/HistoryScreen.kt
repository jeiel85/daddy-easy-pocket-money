package com.jeiel85.daddypocket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeiel85.daddypocket.data.Expense
import com.jeiel85.daddypocket.ui.AppViewModel
import com.jeiel85.daddypocket.ui.theme.AlertRed
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: AppViewModel,
    onNavigateToEdit: (Int) -> Unit
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val expenses by viewModel.filteredExpenses.collectAsState()

    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedCardId by viewModel.selectedCardFilter.collectAsState()
    val cards by viewModel.allCards.collectAsState(initial = emptyList())

    val categories = listOf("전체", "식비", "카페", "주유", "경조사", "용돈", "기타")

    // Date Strings for Header
    val yearStr = currentMonth.take(4)
    val monthStr = currentMonth.takeLast(2)

    // Option Dialog States
    var selectedExpenseForOptions by remember { mutableStateOf<Expense?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${yearStr}년 ${monthStr}월 지출 대장", fontWeight = FontWeight.Bold, fontSize = 21.sp) },
                actions = {
                    IconButton(onClick = { viewModel.exportToCSV() }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "CSV 내보내기")
                            Text("CSV", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Screen Filters Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(0.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Filters Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = (cat == "전체" && selectedCategory == null) || (selectedCategory == cat)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (cat == "전체") {
                                        viewModel.setCategoryFilter(null)
                                    } else {
                                        viewModel.setCategoryFilter(cat)
                                    }
                                },
                                label = { Text(cat, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    // Divider
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    // Cards Filter Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("결제처: ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        // All Cards filter pill
                        val isAllSelected = selectedCardId == null
                        FilterChip(
                            selected = isAllSelected,
                            onClick = { viewModel.setCardFilter(null) },
                            label = { Text("전체 수단", fontSize = 14.sp) }
                        )

                        // Particular Card filters
                        cards.forEach { card ->
                            val isSelected = selectedCardId == card.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setCardFilter(card.id) },
                                label = { Text(card.name, fontSize = 14.sp) }
                            )
                        }
                    }
                }
            }

            // Total spent info subheader
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "총 ${expenses.size}건의 기록",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "합계: " + formatCurrency(expenses.sumOf { it.amount }),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Expenses items list
            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = "비어있음",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "조회 조건에 맞는 지출 내역이 없습니다.",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 64.dp)
                ) {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseItemCard(
                            expense = expense,
                            cards = cards,
                            onOptionClick = { selectedExpenseForOptions = expense }
                        )
                    }
                }
            }
        }
    }

    // --- Action Dialog triggered by Tapping an Expense ---
    if (selectedExpenseForOptions != null) {
        val exp = selectedExpenseForOptions!!
        val cardMap = cards.associate { it.id to it.name }
        val cardLabel = if (exp.cardId != null) cardMap[exp.cardId] ?: "알 수 없는 카드" else "현금/계좌이체"

        AlertDialog(
            onDismissRequest = { selectedExpenseForOptions = null },
            title = {
                Text(
                    text = "지출 상세 및 관리",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("날짜: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), fontSize = 16.sp)
                        Text(exp.date, fontSize = 16.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("카테고리: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), fontSize = 16.sp)
                        Text(exp.category, fontSize = 16.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("결제처: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), fontSize = 16.sp)
                        Text(cardLabel, fontSize = 16.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("메모: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), fontSize = 16.sp)
                        Text(exp.memo.ifEmpty { "(메모 없음)" }, fontSize = 16.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("금액: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), fontSize = 16.sp)
                        Text(formatCurrency(exp.amount), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val targetId = exp.id
                            selectedExpenseForOptions = null
                            onNavigateToEdit(targetId)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Edit, contentDescription = "수정")
                            Text("비밀장부 내용 수정하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            showDeleteConfirmDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Delete, contentDescription = "삭제")
                            Text("이 지출 지우기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(
                        onClick = { selectedExpenseForOptions = null },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("돌아가기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // --- Double Confirm Delete Dialog ---
    if (showDeleteConfirmDialog && selectedExpenseForOptions != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("지출 내역 삭제", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text("정말로 이 지출 기록을 원장에서 지우시겠습니까? 삭제한 정보는 복구할 수 없습니다.", fontSize = 16.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(selectedExpenseForOptions!!)
                        showDeleteConfirmDialog = false
                        selectedExpenseForOptions = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("지우기", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("취소", fontSize = 15.sp)
                }
            }
        )
    }
}

// Single Expense visual card Composable
@Composable
fun ExpenseItemCard(
    expense: Expense,
    cards: List<com.jeiel85.daddypocket.data.CardInfo>,
    onOptionClick: () -> Unit
) {
    val cardMap = cards.associate { it.id to it.name }
    val cardName = if (expense.cardId != null) cardMap[expense.cardId] ?: "신용/체크" else null

    // Determine leading icon based on category/paymentType
    val leadingIcon = when (expense.category) {
        "식비" -> Icons.Default.Restaurant
        "카페" -> Icons.Default.LocalCafe
        "주유" -> Icons.Default.LocalGasStation
        "경조사" -> Icons.Default.CardGiftcard
        "용돈" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.Payments
    }

    val paymentMethodLabel = when (expense.paymentType) {
        "CASH" -> "현금"
        "TRANSFER" -> "이체"
        "CHECK_CARD" -> "${cardName ?: "체크카드"}"
        "CREDIT_CARD" -> "${cardName ?: "신용카드"}"
        else -> expense.paymentType
    }

    val displayDate = try {
        // Extract day "14" from "2026-06-14"
        expense.date.takeLast(2) + "일"
    } catch (e: Exception) {
        expense.date
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOptionClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Date Indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayDate,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }

            // Central Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        leadingIcon,
                        contentDescription = expense.category,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = expense.category,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (expense.memo.startsWith("[고정비]")) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("고정지출", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }

                Text(
                    text = expense.memo.ifEmpty { "내역 상세 설명 없음" },
                    fontSize = 15.sp,
                    color = if (expense.memo.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

                Text(
                    text = paymentMethodLabel,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            // Right price info
            Text(
                text = formatCurrency(expense.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
