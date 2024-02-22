package com.example.samplev5

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.samplev5.ui.theme.LegacySamplesTheme
import com.sourcepoint.gdpr_cmplibrary.GDPRConsentLib

class MainActivity : ComponentActivity() {

    companion object {
        const val accountId = 22
        const val propertyId = 7639
        const val propertyName = "tcfv2.mobile.webview"
        const val pmId = "122058"
    }

    private lateinit var mainViewGroup: ViewGroup
    private var IABTCF_EnableAdvertiserConsentMode by mutableStateOf("")
    private var IABTCF_TCString by mutableStateOf("")

    private fun showView(view: View) {
        if (view.parent == null) {
            view.layoutParams = ViewGroup.LayoutParams(0, 0)
            view.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            view.bringToFront()
            view.requestLayout()
            mainViewGroup.addView(view)
        }
    }

    private fun removeView(view: View) {
        if (view.parent != null) mainViewGroup.removeView(view)
    }

    private fun buildGDPRConsentLib(): GDPRConsentLib {
        return GDPRConsentLib.newBuilder(accountId, propertyName, propertyId, pmId, this)
            .setOnConsentUIReady { showView(it) }
            .setOnAction { actionType -> Log.i("TAG", "ActionType: $actionType") }
            .setOnConsentUIFinished { removeView(it) }
            .setOnConsentReady { consent ->
                // at this point it's safe to initialize vendors
                consent.toString().split("\n").forEach { line ->
                    Log.i("TAG", line)
                }
                PreferenceManager.getDefaultSharedPreferences(this@MainActivity).apply {
                    IABTCF_EnableAdvertiserConsentMode = getInt("IABTCF_EnableAdvertiserConsentMode", -1).toString()
                    IABTCF_TCString = getString("IABTCF_TCString", "missing") ?: "missing"
                }


            }
            .setOnError { Log.e("TAG", "Something went wrong") }
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LegacySamplesTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = """
                            Version 5
                            
                        """.trimIndent())
                        Text(text = """
                            IABTCF_EnableAdvertiserConsentMode: $IABTCF_EnableAdvertiserConsentMode
                            
                        """.trimIndent())
                        Text(text = """
                            IABTCF_TCString
                            
                            $IABTCF_TCString
                            
                        """.trimIndent())
                    }
                    Buttons(
                        "Review Preferences" to { buildGDPRConsentLib().showPm() },
                        "Clear Preferences" to { PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().clear().apply() },
                        "Load Message" to { executeLoadMessage() },
                    )
                }
            }
        }
        mainViewGroup = findViewById(android.R.id.content)
    }

    override fun onResume() {
        super.onResume()
        executeLoadMessage()
    }

    private fun executeLoadMessage(){
        buildGDPRConsentLib().run()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun Buttons(vararg buttons: Pair<String, () -> Unit>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for ((text, onClick) in buttons) {
            Button(onClick = onClick) {
                Text(text = text)
            }
        }
    }
}