package com.spozebra.dhl_licenseplate_tag_poc

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.spozebra.dhl_licenseplate_tag_poc.view.CircleView
import com.zebra.rfid.api3.*
import java.math.BigInteger


class MainActivity : AppCompatActivity(), RfidEventsListener, IBarcodeScannedListener {

    private val TAG: String = "RFIDReaderInterface"

    private lateinit var progressBar: ProgressBar
    private lateinit var editTextLicensePlate: EditText
    private lateinit var tagDistanceTextView: TextView
    private lateinit var instructionTextView: TextView
    private lateinit var circleView: CircleView

    private var distance = 0

    /*val dataWedgeReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == "com.spozebra.dhl_licenseplate_tag_poc.ACTION") {
                val decodedData: String? = intent.getStringExtra("com.symbol.datawedge.data_string")
                licensePlateTextView.text = decodedData
            }
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        editTextLicensePlate = findViewById(R.id.editTextLicensePlate)
        tagDistanceTextView = findViewById(R.id.tagDistanceTextView)
        instructionTextView = findViewById(R.id.instructionTextView)
        circleView = findViewById(R.id.circleView)

        //var dwConf = DataWedgeInterface(applicationContext);
        //dwConf.configure(packageName)

        // registerReceivers()

        if(Companion.rfidInterface == null){
            progressBar.visibility = ProgressBar.VISIBLE
            Thread {

                if(Companion.scannerInterface == null)
                    Companion.scannerInterface = ScannerInterface(this)

                if(Companion.rfidInterface == null)
                    Companion.rfidInterface = RFIDReaderInterface(this)

                var connectScannerResult = Companion.scannerInterface!!.connect(applicationContext)
                var connectRFIDResult = Companion.rfidInterface!!.connect()

                runOnUiThread {
                    circleView.visibility = View.VISIBLE
                    tagDistanceTextView.visibility = TextView.VISIBLE
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(applicationContext, if(connectRFIDResult && connectScannerResult) "Reader & Scanner are connected!" else "Connection ERROR!", Toast.LENGTH_LONG)
                }
            }.start()
        }
    }

    // Create filter for the broadcast intent
    private fun registerReceivers() {
        // DW OFF
        /*val filter = IntentFilter()
        filter.addAction("com.symbol.datawedge.api.NOTIFICATION_ACTION") // for notification result
        filter.addAction("com.symbol.datawedge.api.RESULT_ACTION") // for error code result
        filter.addCategory(Intent.CATEGORY_DEFAULT) // needed to get version info

        // register to received broadcasts via DataWedge scanning
        filter.addAction("$packageName.ACTION")
        filter.addAction("$packageName.service.ACTION")
        registerReceiver(dataWedgeReceiver, filter)*/
    }

    override fun barcodeScanned(barcode: String) {
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
                        //rfidInterface!!.reader.Actions.Inventory.perform()
                        if(editTextLicensePlate.text.length != 0){
                            var content = editTextLicensePlate.text.toString()
                            if(content.startsWith("JJD")) {
                                val tagId = content.substring(3, content.length)
                                val tagbigInt = BigInteger(tagId)
                                val hex = tagbigInt.toString(16)
                                content = hex.padEnd(32, '0').uppercase()
                            }
                            Companion.rfidInterface!!.reader.Actions.TagLocationing.Perform(content, null, null)

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

    override fun onPause() {
        super.onPause()
       // unregisterReceiver(dataWedgeReceiver);
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Companion.rfidInterface != null) {
            Companion.rfidInterface!!.onDestroy()
        }
    }

    companion object {
        private var rfidInterface : RFIDReaderInterface? = null
        private var scannerInterface : ScannerInterface? = null
    }

}