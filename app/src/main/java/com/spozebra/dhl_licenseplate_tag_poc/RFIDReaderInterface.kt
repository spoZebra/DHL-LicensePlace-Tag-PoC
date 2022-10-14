package com.spozebra.dhl_licenseplate_tag_poc

import android.content.Context
import com.zebra.rfid.api3.*
import java.util.*


class RFIDReaderInterface(var listener: RfidEventsListener) {

    private lateinit var readers: Readers
    private var availableRFIDReaderList: ArrayList<ReaderDevice>? = null
    private var readerDevice: ReaderDevice? = null
    lateinit var reader: RFIDReader

    fun connect(context : Context): Boolean {
        // Init
        readers = Readers(context, ENUM_TRANSPORT.ALL)

        try {
            if (readers != null) {
                availableRFIDReaderList = readers.GetAvailableRFIDReaderList()
                if (availableRFIDReaderList != null && availableRFIDReaderList!!.size != 0) {
                    // get first reader from list
                    readerDevice = availableRFIDReaderList!![0]
                    reader = readerDevice!!.rfidReader
                    if (!reader!!.isConnected) {
                        reader!!.connect()
                        configureReader()
                        return true
                    }
                }
            }
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        }
        return false
    }

    private fun configureReader() {
        if (reader.isConnected) {
            val triggerInfo = TriggerInfo()
            triggerInfo.StartTrigger.triggerType = START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE
            triggerInfo.StopTrigger.triggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE
            try {
                // receive events from reader
                reader.Events.addEventsListener(listener)
                // HH event
                reader.Events.setHandheldEvent(true)
                // tag event with tag data
                reader.Events.setTagReadEvent(true)
                // application will collect tag using getReadTags API
                reader.Events.setAttachTagDataWithReadEvent(false)
                // set trigger mode as rfid so scanner beam will not come
                //reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
                // set start and stop triggers
                reader.Config.startTrigger = triggerInfo.StartTrigger
                reader.Config.stopTrigger = triggerInfo.StopTrigger
            } catch (e: InvalidUsageException) {
                e.printStackTrace()
            } catch (e: OperationFailureException) {
                e.printStackTrace()
            }
        }
    }

    fun onDestroy() {
        try {
            if (reader != null) {
                reader.Events?.removeEventsListener(listener)
                reader.disconnect()
                reader.Dispose()
                readers.Dispose()
            }
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}