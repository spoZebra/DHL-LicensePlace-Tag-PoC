package com.spozebra.dhl_licenseplate_tag_poc

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.spozebra.dhl_licenseplate_tag_poc.view.CircleView
import com.zebra.rfid.api3.*

class MainActivity : AppCompatActivity(), RfidEventsListener {

    private val TAG: String = "RFIDReaderInterface"

    private lateinit var progressBar: ProgressBar
    private lateinit var licensePlateTextView: TextView
    private lateinit var tagDistanceTextView: TextView
    private lateinit var instructionTextView: TextView
    private lateinit var circleView: CircleView

    private var rfidInterface : RFIDReaderInterface? = null

    private var distance = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        licensePlateTextView = findViewById(R.id.licensePlateTextView)
        tagDistanceTextView = findViewById(R.id.tagDistanceTextView)
        instructionTextView = findViewById(R.id.instructionTextView)
        circleView = findViewById(R.id.circleView)

        if(rfidInterface == null){
            progressBar.visibility = ProgressBar.VISIBLE
            Thread {
                rfidInterface = RFIDReaderInterface(this)
                var result = rfidInterface!!.connect()

                runOnUiThread {
                    circleView.visibility = View.VISIBLE
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(this.baseContext, if(result) "Reader connected!" else "Connection ERROR!", Toast.LENGTH_LONG)
                }
            }.start()
        }
    }
    // Read Event Notification
    override fun eventReadNotify(e: RfidReadEvents) {
        // Recommended to use new method getReadTagsEx for better performance in case of large tag population
        val myTags: Array<TagData> = rfidInterface!!.reader.Actions.getReadTags(100)
        if (myTags != null) {
            for (tag in myTags) {
                Log.d(TAG, "Tag ID " + tag.tagID)
                if (tag.isContainsLocationInfo) {
                    val dist = tag.LocationInfo.relativeDistance.toInt()
                    Log.d(TAG, "Tag relative distance $dist")
                    updateUI(dist)
                }
            }
        }
    }

    private fun updateUI(newDist : Int){
        this@MainActivity.runOnUiThread {
            if(newDist != distance) {
                tagDistanceTextView.text = newDist.toString()
                distance = newDist
                circleView.updateRadius(distance)
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
                        rfidInterface!!.reader.Actions.TagLocationing.Perform("3039606343AFCE40002BD2FA", null, null)
                    } catch (e: InvalidUsageException) {
                        e.printStackTrace()
                    } catch (e: OperationFailureException) {
                        e.printStackTrace()
                    }
                }.start()
            }
            if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.handheldEvent === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {

                Thread {
                    try {
                        rfidInterface!!.reader.Actions.Inventory.stop()
                        this@MainActivity.runOnUiThread {
                            tagDistanceTextView.text = "0"
                            circleView.updateRadius(0)
                        }
                    } catch (e: InvalidUsageException) {
                        e.printStackTrace()
                    } catch (e: OperationFailureException) {
                        e.printStackTrace()
                    }
                }.start()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (rfidInterface != null) {
            rfidInterface!!.onDestroy()
        }
    }
}