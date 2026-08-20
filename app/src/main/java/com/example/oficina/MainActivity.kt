package com.example.oficina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficina.controle.OficinaApplication
import com.example.oficina.controle.auth.AuthRepository
import com.example.oficina.controle.ui.navigation.AppNavGraph
import com.example.oficina.controle.ui.theme.OficinaAppTheme
import com.example.oficina.controle.viewmodel.AppViewModel
import com.example.oficina.controle.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as OficinaApplication).repository
        
        setContent {
            OficinaAppTheme {
                val viewModel: AppViewModel = viewModel(
                    factory = AppViewModelFactory(repository)
                )
                AppNavGraph(vm = viewModel)
            }
        }
    }
}