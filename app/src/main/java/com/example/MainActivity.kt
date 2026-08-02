package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.surveycad.ui.MainScreen
import com.example.surveycad.ui.theme.SurveyCADTheme
import com.example.surveycad.viewmodel.SurveyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SurveyCADTheme {
                val viewModel: SurveyViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
