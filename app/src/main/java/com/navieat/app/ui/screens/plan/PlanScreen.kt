package com.navieat.app.ui.screens.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.navieat.app.R
import com.navieat.app.domain.model.DayPlan
import java.time.DayOfWeek

@Composable
fun PlanScreen(viewModel: PlanViewModel = hiltViewModel()) {
    val plan by viewModel.plan.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.plan_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        val current = plan
        if (current == null) {
            Text(stringResource(R.string.home_no_plan))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(current.days) { day -> DayCard(day) }
            }
        }
    }
}

@Composable
private fun DayCard(day: DayPlan) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(dayLabel(DayOfWeek.of(day.dayOfWeek.coerceIn(1, 7)))),
                style = MaterialTheme.typography.titleMedium,
            )
            day.meals.forEach { meal ->
                Text(meal.slot.name, style = MaterialTheme.typography.titleSmall)
                meal.dishes.forEach { dish ->
                    Text("• ${dish.name}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun dayLabel(dow: DayOfWeek): Int = when (dow) {
    DayOfWeek.MONDAY -> R.string.plan_day_monday
    DayOfWeek.TUESDAY -> R.string.plan_day_tuesday
    DayOfWeek.WEDNESDAY -> R.string.plan_day_wednesday
    DayOfWeek.THURSDAY -> R.string.plan_day_thursday
    DayOfWeek.FRIDAY -> R.string.plan_day_friday
    DayOfWeek.SATURDAY -> R.string.plan_day_saturday
    DayOfWeek.SUNDAY -> R.string.plan_day_sunday
}
