package com.example.samplev6

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.samplev6.ui.theme.LegacySamplesTheme
import com.sourcepoint.cmplibrary.NativeMessageController
import com.sourcepoint.cmplibrary.SpClient
import com.sourcepoint.cmplibrary.core.nativemessage.MessageStructure
import com.sourcepoint.cmplibrary.creation.config
import com.sourcepoint.cmplibrary.creation.delegate.spConsentLibLazy
import com.sourcepoint.cmplibrary.data.network.util.CampaignsEnv
import com.sourcepoint.cmplibrary.exception.CampaignType
import com.sourcepoint.cmplibrary.model.ConsentAction
import com.sourcepoint.cmplibrary.model.MessageLanguage
import com.sourcepoint.cmplibrary.model.exposed.SPConsents
import com.sourcepoint.cmplibrary.model.exposed.SpConfig
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    val cmpConfig : SpConfig = config {
        accountId = 22
        propertyName = "mobile.multicampaign.demo"
        messLanguage = MessageLanguage.ENGLISH // Optional, default ENGLISH
        campaignsEnv = CampaignsEnv.PUBLIC // Optional, default PUBLIC
        messageTimeout = 4000 // Optional, default 3000ms
        +CampaignType.GDPR
    }

    private val spConsentLib by spConsentLibLazy {
        activity = this@MainActivity
        spClient = LocalClient()
        spConfig = cmpConfig
    }

    private var IABTCF_EnableAdvertiserConsentMode by mutableStateOf("")
    private var IABTCF_TCString by mutableStateOf("")

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
                            Version 6
                            
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
                        "Review Preferences" to { spConsentLib.loadPrivacyManager("488393", CampaignType.GDPR) },
                        "Clear Preferences" to { PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().clear().apply() },
                        "Load Message" to { executeLoadMessage() },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        executeLoadMessage()
    }

    private fun executeLoadMessage(){
        spConsentLib.loadMessage()
    }

    internal inner class LocalClient : SpClient {

        override fun onNoIntentActivitiesFound(url: String) { }

        override fun onUIReady(view: View) {
            spConsentLib.showView(view)
        }

        override fun onAction(view: View, consentAction: ConsentAction): ConsentAction {
            return consentAction
        }

        override fun onNativeMessageReady(message: MessageStructure, messageController: NativeMessageController) { }

        override fun onUIFinished(view: View) {
            spConsentLib.removeView(view)
        }

        override fun onError(error: Throwable) {
            error.printStackTrace()
        }

        override fun onSpFinished(sPConsents: SPConsents) {
            PreferenceManager.getDefaultSharedPreferences(this@MainActivity).apply {
                IABTCF_EnableAdvertiserConsentMode = getInt("IABTCF_EnableAdvertiserConsentMode", -1).toString()
                IABTCF_TCString = getString("IABTCF_TCString", "missing") ?: "missing"
            }
        }

        override fun onConsentReady(consent: SPConsents) { }

        override fun onMessageReady(message: JSONObject) {}
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LegacySamplesTheme {
        Greeting("Android")
    }
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