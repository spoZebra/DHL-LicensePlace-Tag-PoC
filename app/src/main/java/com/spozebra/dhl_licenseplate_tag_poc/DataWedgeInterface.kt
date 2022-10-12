package com.spozebra.dhl_licenseplate_tag_poc

import android.content.Context
import android.content.Intent
import android.os.Bundle

class DataWedgeInterface(val context: Context) {

    private val EXTRA_PROFILENAME = "DHL_POC"

    // DataWedge Extras
    private val EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE"
    private val EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG"

    // DataWedge Actions
    private val ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION"

    fun configure(packageName : String){

        // Send DataWedge intent with extra to create profile
        // Use CREATE_PROFILE: http://techdocs.zebra.com/datawedge/latest/guide/api/createprofile/
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_CREATE_PROFILE, EXTRA_PROFILENAME)

        // Configure created profile to apply to this app
        val profileConfig = Bundle()

        // Configure barcode input plugin
        profileConfig.putString("PROFILE_NAME", EXTRA_PROFILENAME)
        profileConfig.putString("PROFILE_ENABLED", "true")
        profileConfig.putString("CONFIG_MODE", "UPDATE") // Update specified settings in profile

        // PLUGIN_CONFIG bundle properties
        val rfidConfig = Bundle()
        rfidConfig.putString("PLUGIN_NAME", "RFID")
        rfidConfig.putString("RESET_CONFIG", "true")

        // PARAM_LIST bundle properties
        val rfidProps = Bundle()
        rfidProps.putString("rfid_input_enabled", "false")
        rfidConfig.putBundle("PARAM_LIST", rfidProps)
        profileConfig.putBundle("PLUGIN_CONFIG", rfidConfig)

        // Apply configs
        // Use SET_CONFIG: http://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig)

        // Configure intent output for captured data to be sent to this app
        val intentConfig = Bundle()
        intentConfig.putString("PLUGIN_NAME", "INTENT")
        intentConfig.putString("RESET_CONFIG", "true")
        val intentProps = Bundle()
        intentProps.putString("intent_output_enabled", "true")
        intentProps.putString("intent_action", "$packageName.ACTION")
        intentProps.putString("intent_delivery", "2")
        intentConfig.putBundle("PARAM_LIST", intentProps)
        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig)
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig)
    }

    fun p2(){

        // Main bundle properties

        // Main bundle properties
        val profileConfig = Bundle()
        profileConfig.putString("PROFILE_NAME", EXTRA_PROFILENAME)
        profileConfig.putString("PROFILE_ENABLED", "true")
        profileConfig.putString("CONFIG_MODE", "UPDATE") // Update specified settings in profile


        // PLUGIN_CONFIG bundle properties

        // PLUGIN_CONFIG bundle properties
        val barcodeConfig = Bundle()
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
        barcodeConfig.putString("RESET_CONFIG", "true")

        // PARAM_LIST bundle properties

        // PARAM_LIST bundle properties
        val barcodeProps = Bundle()
        barcodeProps.putString("scanner_selection", "auto")
        barcodeProps.putString("scanner_input_enabled", "false")

        // Bundle "barcodeProps" within bundle "barcodeConfig"

        // Bundle "barcodeProps" within bundle "barcodeConfig"
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
        // Place "barcodeConfig" bundle within main "profileConfig" bundle
        // Place "barcodeConfig" bundle within main "profileConfig" bundle
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)

        // Create APP_LIST bundle to associate app with profile

        // Create APP_LIST bundle to associate app with profile
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig)
    }
    private fun sendDataWedgeIntentWithExtra(action: String, extraKey: String, extras: Bundle) {
        val dwIntent = Intent()
        dwIntent.action = action
        dwIntent.putExtra(extraKey, extras)
        context.sendBroadcast(dwIntent)
    }


    private fun sendDataWedgeIntentWithExtra(action: String, extraKey: String, extraValue: String) {
        val dwIntent = Intent()
        dwIntent.action = action
        dwIntent.putExtra(extraKey, extraValue)
        context.sendBroadcast(dwIntent)
    }

}