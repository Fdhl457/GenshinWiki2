package com.example.genshin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.genshin.ui.theme.GenshinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GenshinTheme {
                CatalogApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    GenshinTheme {
        CatalogApp()
    }
}