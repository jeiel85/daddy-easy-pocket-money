package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.HomeStats
import com.example.ui.theme.AlertRed
import com.example.ui.theme.NormalBlue
import com.example.ui.theme.SavingGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigateToInput: (Int) -> Unit,
    onNavigateToStats: () -> Unit
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val stats by viewModel.homeStatsStream.collectAsState()
    val alarms by viewModel.paymentDayAlarms.collectAsState()

    val yearStr = currentMonth.take(4)
    val monthStr = currentMonth.takeLast(2)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.prevMonth() }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "이전 달", modifier = Modifier.size(36.dp))
                        }
                        Text(
                            text = "${yearStr}년 ${monthStr}월",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            fontSize = 24.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { viewModel.nextMonth() }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "다음 달", modifier = Modifier.size(36.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToInput(0) }, // 0 means new expense
                icon = { Icon(Icons.Default.Add, contentDescription = "지출 입력", modifier = Modifier.size(28.dp)) },
                text = { Text("빠른 지출 입력", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .height(64.dp)
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
            // 1. 카드 정산일 및 경고 알림 알람 배너
            if (alarms.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "경고",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "다가오는 카드 결제일 안내",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        alarms.forEach { alert ->
                            Text(
                                text = alert,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 32.dp)
                            )
                        }
                    }
                }
            }

            // 2. 남은 지출 예산 (내 남은 돈) 크게 표시
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (stats.remaining >= 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "이번 달 남은 내 돈",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (stats.remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatCurrency(stats.remaining),
                        style = MaterialTheme.typography.displayLarge,
                        color = if (stats.remaining >= 0) SavingGreen else AlertRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar for Budget Spent
                    val spentRatio = if (stats.budget > 0) stats.totalSpent.toFloat() / stats.budget.toFloat() else 0f
                    val clampedRatio = spentRatio.coerceIn(0f, 1f)
                    val progressColor = if (spentRatio > 1.0f) AlertRed else if (spentRatio > 0.8f) MaterialTheme.colorScheme.tertiary else NormalBlue

                    LinearProgressIndicator(
                        progress = { clampedRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "지출률 ${(spentRatio * 100).toInt()}%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                        Text(
                            text = "예산: " + formatCurrency(stats.budget),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3. 오늘 쓴 돈 & 이번 달 쓴 돈 요약
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Today, contentDescription = "오늘", tint = NormalBlue, modifier = Modifier.size(28.dp))
                        Text("오늘 사용액", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = formatCurrency(stats.todaySpent),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "사용", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Text("지금까지 사용액", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = formatCurrency(stats.totalSpent),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 4. 이번 달 주요 지출 카테고리 TOP 3
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.TrendingUp, contentDescription = "분석", tint = MaterialTheme.colorScheme.primary)
                            Text(
                                "이번 달 지출 순위 TOP 3",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "더보기",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .clickable { onNavigateToStats() }
                                .padding(4.dp)
                        )
                    }

                    if (stats.topCategories.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "등록된 지출 내역이 없습니다.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        stats.topCategories.forEachIndexed { index, pair ->
                            val catName = pair.first
                            val catAmount = pair.second
                            val maxAmount = stats.topCategories.firstOrNull()?.second ?: 1L
                            val ratio = catAmount.toFloat() / maxAmount.toFloat()

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    when (index) {
                                                        0 -> AlertRed.copy(alpha = 0.2f)
                                                        1 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "${index + 1}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (index) {
                                                    0 -> AlertRed
                                                    1 -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.secondary
                                                }
                                            )
                                        }
                                        Text(
                                            text = catName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Text(
                                        text = formatCurrency(catAmount),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ratio.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = when (index) {
                                        0 -> AlertRed
                                        1 -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.secondary
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Helpful Tip Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "팁", tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "지출 관리는 무조건 아끼는 목표보다, 내 돈의 흐름을 정확히 기록하고 흐름을 보는 것에서 시작합니다.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// Global Clean Currency Formatter helper
fun formatCurrency(amount: Long): String {
    return String.format("%,d원", amount)
}
