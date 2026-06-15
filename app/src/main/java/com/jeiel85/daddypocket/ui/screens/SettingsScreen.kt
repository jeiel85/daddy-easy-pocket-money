package com.jeiel85.daddypocket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeiel85.daddypocket.ui.AppViewModel
import com.jeiel85.daddypocket.ui.HomeStats
import com.jeiel85.daddypocket.ui.theme.AlertRed
import com.jeiel85.daddypocket.ui.theme.SavingGreen
import com.jeiel85.daddypocket.ui.theme.NormalBlue
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AppViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val stats by viewModel.homeStatsStream.collectAsState()
    val budget by viewModel.monthlyBudget.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState(initial = emptyList())
    val cards by viewModel.allCards.collectAsState(initial = emptyList())

    // Budget Setup states
    var isEditingBudget by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }

    // Start Day setup state (Placeholder for MVP, typical standard is 1st of the month)
    var startingDay by remember { mutableStateOf("1일") }

    // MoM (Month over Month) Comparison State
    var lastMonthSpend by remember { mutableStateOf<Long?>(null) }

    val yearStr = currentMonth.take(4)
    val monthStr = currentMonth.takeLast(2)

    // Calculate Last Month's Spend
    LaunchedEffect(currentMonth, allExpenses) {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        try {
            val date = sdf.parse(currentMonth) ?: return@LaunchedEffect
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MONTH, -1)
            val lastMonthStr = sdf.format(cal.time)

            // Aggregate last month's records
            val lastMonthExpenses = allExpenses.filter { it.date.startsWith(lastMonthStr) }
            lastMonthSpend = lastMonthExpenses.sumOf { it.amount }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(budget) {
        budget?.let {
            budgetInput = it.amount.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("지출 통계 및 장부 설정", fontWeight = FontWeight.Bold, fontSize = 21.sp) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================= SECTION 1: STATISTICS SUMMARY =================
            Text("📊 이번 달 지출 분석 (${monthStr}월)", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)

            // 1) MoM Comparison Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("지난달 대비 총지출 비교", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    val comparisonVal = lastMonthSpend
                    if (comparisonVal == null || comparisonVal == 0L) {
                        Text("지난달 지출 장부 기록이 없어 비교를 생성할 수 없습니다.\n아직 첫 달 사용 중이십니다! 힘내세요!", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        val diff = stats.totalSpent - comparisonVal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (diff <= 0) Icons.Default.ThumbUp else Icons.Default.TrendingUp,
                                contentDescription = "지출률",
                                tint = if (diff <= 0) SavingGreen else AlertRed,
                                modifier = Modifier.size(32.dp)
                            )
                            Column {
                                Text(
                                    text = if (diff <= 0) {
                                        "지난달보다 " + formatCurrency(Math.abs(diff)) + " 아꼈습니다!"
                                    } else {
                                        "지난달보다 " + formatCurrency(diff) + " 더 지출했습니다!"
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (diff <= 0) SavingGreen else AlertRed
                                )
                                Text(
                                    text = "지난달 지출: ${formatCurrency(comparisonVal)} | 이번달: ${formatCurrency(stats.totalSpent)}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 2) Category percentage breakdown list card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("카테고리별 지출 금액", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (stats.topCategories.isEmpty()) {
                        Text("이번 달 등록된 분류별 지출 정보가 없습니다.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        val total = stats.totalSpent.coerceAtLeast(1L)
                        stats.topCategories.forEach { (cat, amount) ->
                            val pct = (amount.toFloat() / total.toFloat() * 100).toInt()
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cat, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${formatCurrency(amount)} ($pct%)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { amount.toFloat() / total.toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }
            }

            // ================= SECTION 2: BUDGET SETTING =================
            Text("⚙️ 지출 예산 및 장부 관리", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Budget Control Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${monthStr}월 기준 예산금액 설정", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "현재: " + formatCurrency(budget?.amount ?: 500000L),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { isEditingBudget = !isEditingBudget },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEditingBudget) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primaryContainer,
                                contentColor = if (isEditingBudget) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(if (isEditingBudget) "설정 중" else "수정", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isEditingBudget) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = budgetInput,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() }) budgetInput = input
                                },
                                label = { Text("원 단위 숫자로만 작성") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    val amountLong = budgetInput.toLongOrNull() ?: 500000L
                                    viewModel.updateBudget(currentMonth, amountLong)
                                    isEditingBudget = false
                                },
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("확인", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    // Month Starting Day Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("매월 시작일 설정 (MVP)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("매월 1일 고정으로 장부 월정산이 시작됩니다.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("1일", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ================= SECTION 3: SYSTEM REMINDALERT =================
            Text("🔔 카드 결제일 안내 알림 서비스", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "카드별로 기입해두신 카드정산일 기준 0~3일 전에 휴대전화 시스템 상태바에 리마인더 Push 알람을 자동 발송합니다. 정상 발송되는지 테스트해보실 수 있습니다.", 
                        fontSize = 14.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )

                    Button(
                        onClick = { viewModel.triggerTestNotification() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = "벨")
                            Text("리마인더 알림 시뮬레이션 (상태바 확인)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Footer info
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "아빠 용돈 지출정리 v1.0 • 오프라인 로컬 우선 안전 모드\n등록하신 개인 지출 원장은 기기에만 암호화 저장되며 서버나 금융기관으로 영구히 전송되지 않습니다.",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp)
            )
        }
    }
}
