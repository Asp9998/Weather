package com.example.weather.core.design.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CommonTextField(valueState: MutableState<String>,
                    onQueryChanged: (String) -> Unit,
                    placeHolder: String,
                    keyboardType: KeyboardType = KeyboardType.Text,
                    imeAction: ImeAction = ImeAction.Next,
                    onAction: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(value = valueState.value,
        onValueChange = {
            valueState.value = it
            onQueryChanged.invoke(it)
                        },
        label = { Text(text = placeHolder)},
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = onAction,
        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = Color.White),
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth()

    )
}