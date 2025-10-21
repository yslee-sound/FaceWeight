package com.sweetapps.faceweight.feature.settings

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.sweetapps.faceweight.core.ui.AppElevation
import com.sweetapps.faceweight.core.ui.BaseActivity
import com.sweetapps.faceweight.core.util.Constants
import com.sweetapps.faceweight.core.ui.LocalSafeContentPadding

class SettingsActivity : BaseActivity() {
    override fun getScreenTitle(): String = "설정"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BaseScreen { SettingsScreen() } }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val (initialCost, initialFrequency, initialDuration) = Constants.getUserSettings(context)
    val sharedPref = context.getSharedPreferences(Constants.USER_SETTINGS_PREFS, Context.MODE_PRIVATE)

    var selectedCost by remember { mutableStateOf(initialCost) }
    var selectedFrequency by remember { mutableStateOf(initialFrequency) }
    var selectedDuration by remember { mutableStateOf(initialDuration) }

    val safePadding = LocalSafeContentPadding.current

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).padding(safePadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsCard(title = "음주 비용", titleColor = colorResource(id = R.color.color_indicator_money)) {
            SettingsOptionGroup(
                selectedOption = selectedCost,
                options = listOf("저", "중", "고"),
                labels = listOf("저 (1만원 이하)", "중 (1~5만원)", "고 (5만원 이상)"),
                onOptionSelected = { newValue -> selectedCost = newValue; sharedPref.edit { putString("selected_cost", newValue) } }
            )
        }
        SettingsCard(title = "음주 빈도", titleColor = colorResource(id = R.color.color_progress_primary)) {
            SettingsOptionGroup(
                selectedOption = selectedFrequency,
                options = listOf("주 1회 이하", "주 2~3회", "주 4회 이상"),
                labels = listOf("주 1회 이하", "주 2~3회", "주 4회 이상"),
                onOptionSelected = { newValue -> selectedFrequency = newValue; sharedPref.edit { putString("selected_frequency", newValue) } }
            )
        }
        SettingsCard(title = "음주 시간", titleColor = colorResource(id = R.color.color_indicator_hours)) {
            SettingsOptionGroup(
                selectedOption = selectedDuration,
                options = listOf("짧음", "보통", "길게"),
                labels = listOf("짧음 (2시간 이하)", "보통 (3~5시간)", "길게 (6시간 이상)"),
                onOptionSelected = { newValue -> selectedDuration = newValue; sharedPref.edit { putString("selected_duration", newValue) } }
            )
        }
    }
}

@Composable
fun SettingsCard(title: String, titleColor: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.CARD), // lowered from CARD_HIGH
        border = BorderStroke(1.dp, colorResource(id = R.color.color_border_light)) // added subtle border for depth
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = titleColor, modifier = Modifier.padding(bottom = 12.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsOptionItem(isSelected: Boolean, label: String, onSelected: () -> Unit) {
    Card(
        onClick = onSelected,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) colorResource(id = R.color.color_accent_blue).copy(alpha = 0.1f) else colorResource(id = R.color.color_bg_card_light)),
        border = if (isSelected) BorderStroke(2.dp, colorResource(id = R.color.color_accent_blue)) else BorderStroke(1.dp, colorResource(id = R.color.color_border_light))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isSelected, onClick = onSelected, colors = RadioButtonDefaults.colors(selectedColor = colorResource(id = R.color.color_accent_blue), unselectedColor = colorResource(id = R.color.color_radio_unselected)))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = if (isSelected) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold) else MaterialTheme.typography.bodyLarge, color = if (isSelected) colorResource(id = R.color.color_indicator_days) else colorResource(id = R.color.color_text_primary_dark))
        }
    }
}

@Composable
fun SettingsOptionGroup(selectedOption: String, options: List<String>, labels: List<String>, onOptionSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, option ->
            SettingsOptionItem(isSelected = selectedOption == option, label = labels[index], onSelected = { onOptionSelected(option) })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() { SettingsScreen() }
