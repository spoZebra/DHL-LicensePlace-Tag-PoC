package com.spozebra.dhl_licenseplate_tag_poc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.spozebra.dhl_licenseplate_tag_poc.view.CircleView
import com.zebra.rfid.api3.*
import java.math.BigInteger


class MainActivity : AppCompatActivity(), RfidEventsListener, IBarcodeScannedListener {

    private val TAG: String = "MainActivity"

    private lateinit var progressBar: ProgressBar
    private lateinit var editTextLicensePlate: EditText
    private lateinit var tagDistanceTextView: TextView
    private lateinit var instructionTextView: TextView
    private lateinit var circleView: CircleView

    private var distance = 0

    private val dataWedgeReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == "com.spozebra.dhl_licenseplate_tag_poc.ACTION") {
                val decodedData: String? = intent.getStringExtra("com.symbol.datawedge.data_string")
                this@MainActivity.barcodeScanned(decodedData)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        editTextLicensePlate = findViewById(R.id.editTextLicensePlate)
        tagDistanceTextView = findViewById(R.id.tagDistanceTextView)
        instructionTextView = findViewById(R.id.instructionTextView)
        circleView = findViewById(R.id.circleView)
        // Register DW receiver
        registerReceivers()
        // Setup RFID & Scanner
        configureReader()

        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.page_1 -> {
                    // Respond to navigation item 1 click
                    true
                }
                R.id.page_2 -> {
                    InventoryFragment()
                    // Respond to navigation item 2 click
                    true
                }
                else -> false
            }
        }
    }

    private fun configureReader(){
        progressBar.visibility = ProgressBar.VISIBLE
        Thread {

            // Configure datawedge
            var dwConf = DataWedgeInterface(applicationContext);
            dwConf.configure(packageName)

            // Configure BT Scanner
            /*if(Companion.scannerInterface == null)
                Companion.scannerInterface = ScannerInterface(this)

            var connectScannerResult = Companion.scannerInterface!!.connect(applicationContext)*/

            // Configure RFID
            if(Companion.rfidInterface == null)
                Companion.rfidInterface = RFIDReaderInterface(this)

            var connectRFIDResult = Companion.rfidInterface!!.connect(applicationContext)

            runOnUiThread {
                circleView.visibility = View.VISIBLE
                tagDistanceTextView.visibility = TextView.VISIBLE
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(applicationContext, if(connectRFIDResult) "Reader & Scanner are connected!" else "Connection ERROR!", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    // Create filter for the broadcast intent
    private fun registerReceivers() {
        val filter = IntentFilter()
        filter.addAction("com.symbol.datawedge.api.NOTIFICATION_ACTION") // for notification result
        filter.addAction("com.symbol.datawedge.api.RESULT_ACTION") // for error code result
        filter.addCategory(Intent.CATEGORY_DEFAULT) // needed to get version info

        // register to received broadcasts via DataWedge scanning
        filter.addAction("$packageName.ACTION")
        filter.addAction("$packageName.service.ACTION")
        registerReceiver(dataWedgeReceiver, filter)
    }

    override fun barcodeScanned(barcode: String?) {
        runOnUiThread {
            editTextLicensePlate.setText(barcode)
        }
    }

    // Read Event Notification
    override fun eventReadNotify(e: RfidReadEvents) {
        // Recommended to use new method getReadTagsEx for better performance in case of large tag population
        val myTags: Array<TagData> = Companion.rfidInterface!!.reader.Actions.getReadTags(100)
        if (myTags != null) {
            for (tag in myTags) {
                Log.d(TAG, "Tag ID " + tag.tagID)
                if (tag.isContainsLocationInfo) {
                    val dist = tag.LocationInfo.relativeDistance.toInt()
                    Log.d(TAG, "Tag relative distance $dist")

                    this@MainActivity.runOnUiThread {
                        if(dist != distance) {
                            tagDistanceTextView.text = dist.toString()
                            distance = dist
                            circleView.updateRadius(distance)
                        }
                    }
                }
            }
        }
    }

    // Status Event Notification
    override fun eventStatusNotify(rfidStatusEvents: RfidStatusEvents) {
        Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.statusEventType)
        if (rfidStatusEvents.StatusEventData.statusEventType === STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
            if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.handheldEvent === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {

                Thread {
                    try {
                        if(editTextLicensePlate.text.isNotEmpty()){
                            val tagIdToSearch = getTagContentFromLicensePlate(editTextLicensePlate.text.toString())
                            Companion.rfidInterface!!.reader.Actions.TagLocationing.Perform(tagIdToSearch, null, null)
                        }
                        else{
                            Toast.makeText(applicationContext, "Choose a License Plate before to start searching", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }
            if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.handheldEvent === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {

                Thread {
                    try {
                        Companion.rfidInterface!!.reader.Actions.Inventory.stop()
                        this@MainActivity.runOnUiThread {
                            tagDistanceTextView.text = "0"
                            circleView.updateRadius(0)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }
        }
    }

    private fun getTagContentFromLicensePlate(licensePlate : String) : String{
        var tagContent = licensePlate

        if(licensePlate.startsWith("JJD")) {
            val tagSubstring = licensePlate.substring(3, licensePlate.length)
            val tagHexContent = BigInteger(tagSubstring).toString(16)
            tagContent = tagHexContent.padEnd(32, '0').uppercase()
        }

        return tagContent
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(dataWedgeReceiver);
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Companion.rfidInterface != null) {
            Companion.rfidInterface!!.onDestroy()
        }
    }

    companion object {
        var rfidInterface : RFIDReaderInterface? = null
        private var scannerInterface : ScannerInterface? = null
    }

}