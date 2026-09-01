package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PreferencesManager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DisguiseCalculatorScreen(
    preferencesManager: PreferencesManager,
    onUnlockSecretSettings: () -> Unit
) {
    val context = LocalContext.current
    val disguiseConfig = preferencesManager.disguiseConfig.value

    var displayValue by remember { mutableStateOf("0") }
    var expressionHistory by remember { mutableStateOf("") }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var isNewNumber by remember { mutableStateOf(true) }

    fun handleNumber(digit: String) {
        if (isNewNumber || displayValue == "0") {
            displayValue = digit
            isNewNumber = false
        } else {
            if (displayValue.length < 12) {
                displayValue += digit
            }
        }
    }

    fun handleOperator(op: String) {
        val current = displayValue.toDoubleOrNull() ?: 0.0
        firstOperand = current
        pendingOp = op
        expressionHistory = "$displayValue $op"
        isNewNumber = true
    }

    fun handleEquals() {
        // Secret PIN check!
        if (displayValue == disguiseConfig.secretPin || expressionHistory.startsWith(disguiseConfig.secretPin)) {
            Toast.makeText(context, "🔓 SafeLink Hub Unlocked", Toast.LENGTH_SHORT).show()
            preferencesManager.updateDisguiseConfig(disguiseConfig.copy(isDisguiseActive = false))
            onUnlockSecretSettings()
            return
        }

        val secondOperand = displayValue.toDoubleOrNull() ?: 0.0
        val first = firstOperand ?: return
        val op = pendingOp ?: return

        val result = when (op) {
            "+" -> first + secondOperand
            "-" -> first - secondOperand
            "×" -> first * secondOperand
            "÷" -> if (secondOperand != 0.0) first / secondOperand else Double.NaN
            else -> secondOperand
        }

        expressionHistory = "$first $op $secondOperand ="
        displayValue = if (result.isNaN()) {
            "Error"
        } else if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            "%.6f".format(result).trimEnd('0').trimEnd('.')
        }
        firstOperand = null
        pendingOp = null
        isNewNumber = true
    }

    fun handleClear() {
        displayValue = "0"
        expressionHistory = ""
        firstOperand = null
        pendingOp = null
        isNewNumber = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF17181A))
            .padding(16.dp)
            .testTag("disguise_calculator_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Calculator Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = expressionHistory,
                    color = Color(0xFF7C7F86),
                    fontSize = 20.sp,
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = displayValue,
                    color = Color.White,
                    fontSize = if (displayValue.length > 8) 42.sp else 54.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keypad Grid
            val buttonSpacing = 12.dp

            // Row 1: AC, +/-, %, ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalcButton(
                    text = "AC",
                    bgColor = Color(0xFFA5A5A5),
                    textColor = Color.Black,
                    modifier = Modifier.weight(1f),
                    onClick = { handleClear() }
                )
                CalcButton(
                    text = "±",
                    bgColor = Color(0xFFA5A5A5),
                    textColor = Color.Black,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val num = displayValue.toDoubleOrNull()
                        if (num != null) {
                            val flipped = num * -1
                            displayValue = if (flipped % 1.0 == 0.0) flipped.toLong().toString() else flipped.toString()
                        }
                    }
                )
                CalcButton(
                    text = "%",
                    bgColor = Color(0xFFA5A5A5),
                    textColor = Color.Black,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val num = displayValue.toDoubleOrNull()
                        if (num != null) {
                            displayValue = (num / 100.0).toString()
                        }
                    }
                )
                CalcButton(
                    text = "÷",
                    bgColor = Color(0xFFFF9F0A),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = { handleOperator("÷") }
                )
            }

            Spacer(modifier = Modifier.height(buttonSpacing))

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalcButton(text = "7", bgColor = Color(0xFF333333), textColor = Color.White, modifier = Modifier.weight(1f), onClick = { handleNumber("7") })
                CalcButton(text = "8", bgColor = Color(0xFF333333), textColor = Color.White, modifier = Modifier.weight(1f), onClick = { handleNumber("8") })
                CalcButton(text = "9", bgColor = Color(0xFF333333), textColor = Color.White, modifier = Modifier.weight(1f), onClick = { handleNumber("9") })
                CalcButton(text = "×", bgColor = Color(0xFFFF9F0A), textColor = Color.White, modifier = Modifier.weight(1f), onClick = { handleOperator("×") })
            }

            Spacer(modifier = Modifier.height(buttonSpacing))

            // Row 3: 4, 5, 6, -
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalcButton(text = "4", bgColor = Color(0xFF333333), textColor = Color.White, modifier = Modifier.weight(1f), onClick = { handleNumber("4") })
                CalcButton(text = "5", bgColor = Color(0xFF333333), textColor = Color.White, modifier = Modifier.weight(1f), onClick = { handleNumber("5") })
                CalcButton(text = "6", bgColor = Color(0xFF333333), textColor = Color.White, modifier = Modifier.weight(1f), onClick = { handleNumber("6") })
                CalcButton(text = "-", bgColor = Color(0xFFFF9F0A), textColor = Color.White, modifier = Modifier.weight(1f), onClick = { handleOperator("-") })
            }

            Spacer(modifier = Modifier.height(buttonSpacing))

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalcButton(text = "1", bgColor = Color(0xFF333333), textColor = Color.White, modifier = Modifier.weight(1f), onClick = { handleNumber("1") })
                CalcButton(text = "2", bgColor = Color(0xFF333333), textColor = Color.White, modifier = Modifier.weight(1f), onClick = { handleNumber("2") })
                CalcButton(text = "3", bgColor = Color(0xFF333333), textColor = Color.White, modifier = Modifier.weight(1f), onClick = { handleNumber("3") })
                CalcButton(text = "+", bgColor = Color(0xFFFF9F0A), textColor = Color.White, modifier = Modifier.weight(1f), onClick = { handleOperator("+") })
            }

            Spacer(modifier = Modifier.height(buttonSpacing))

            // Row 5: 0, ., =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalcButton(
                    text = "0",
                    bgColor = Color(0xFF333333),
                    textColor = Color.White,
                    modifier = Modifier.weight(2f),
                    onClick = { handleNumber("0") }
                )
                CalcButton(
                    text = ".",
                    bgColor = Color(0xFF333333),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (!displayValue.contains(".")) {
                            displayValue += "."
                            isNewNumber = false
                        }
                    }
                )
                CalcButton(
                    text = "=",
                    bgColor = Color(0xFFFF9F0A),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = { handleEquals() },
                    onLongClick = {
                        // Long press unlock secret backdoor
                        Toast.makeText(context, "🔓 Backdoor Unlocked", Toast.LENGTH_SHORT).show()
                        preferencesManager.updateDisguiseConfig(disguiseConfig.copy(isDisguiseActive = false))
                        onUnlockSecretSettings()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalcButton(
    text: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(bgColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
