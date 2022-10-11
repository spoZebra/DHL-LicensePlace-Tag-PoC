package com.spozebra.dhl_licenseplate_tag_poc

import android.util.Log
import com.zebra.rfid.api3.*


class RFIDReaderEventHandler  : RfidEventsListener {
    val TAG = "ZebraRfidEventHandler"
    val reader : RFIDReader;

    constructor(reader : RFIDReader){
        this.reader = reader;
    }

    // Read Event Notification
    override fun eventReadNotify(e: RfidReadEvents) {
        // Recommended to use new method getReadTagsEx for better performance in case of large tag population
        val myTags: Array<TagData> = reader.Actions.getReadTags(100)
        if (myTags != null) {
            for (tag in myTags) {
                Log.d(TAG, "Tag ID " + tag.tagID)
                if (tag.isContainsLocationInfo) {
                    val dist = tag.LocationInfo.relativeDistance
                    Log.d(TAG, "Tag relative distance $dist")
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
                        //reader.Actions.Inventory.perform()
                        reader.Actions.TagLocationing.Perform("3039606343AFCE40002BD2FA","", null)
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
                        reader.Actions.Inventory.stop()
                    } catch (e: InvalidUsageException) {
                        e.printStackTrace()
                    } catch (e: OperationFailureException) {
                        e.printStackTrace()
                    }
                }.start()
            }
        }
    }
}