package com.spozebra.dhl_licenseplate_tag_poc

import android.content.Context
import com.zebra.scannercontrol.*
import java.util.*


class ScannerInterface: IDcsSdkApiDelegate {
    private var sdkHandler: SDKHandler? = null

    fun connect(context : Context) : Boolean {
        if(sdkHandler == null)
            sdkHandler = SDKHandler(context)
        sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL)

        val notifications_mask = DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value
        sdkHandler!!.dcssdkSubsribeForEvents(notifications_mask)

        val scannerInfoList : ArrayList<DCSScannerInfo> = ArrayList()
        sdkHandler!!.dcssdkGetAvailableScannersList(scannerInfoList)

        try {
            val scanner = scannerInfoList[0]
            if(scanner.isActive)
                return true

            // Connect
            var result = sdkHandler!!.dcssdkEstablishCommunicationSession(scanner.scannerID)

            return result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS

        } catch (e: Exception) {
            return false
        }

    }

    override fun dcssdkEventScannerAppeared(p0: DCSScannerInfo?) {
    }

    override fun dcssdkEventScannerDisappeared(p0: Int) {
    }

    override fun dcssdkEventCommunicationSessionEstablished(p0: DCSScannerInfo?) {
    }

    override fun dcssdkEventCommunicationSessionTerminated(p0: Int) {
    }

    override fun dcssdkEventBarcode(p0: ByteArray?, p1: Int, p2: Int) {
        val barcode = p0!!.toString()

    }

    override fun dcssdkEventImage(p0: ByteArray?, p1: Int) {
    }

    override fun dcssdkEventVideo(p0: ByteArray?, p1: Int) {
    }

    override fun dcssdkEventBinaryData(p0: ByteArray?, p1: Int) {
    }

    override fun dcssdkEventFirmwareUpdate(p0: FirmwareUpdateEvent?) {
    }

    override fun dcssdkEventAuxScannerAppeared(p0: DCSScannerInfo?, p1: DCSScannerInfo?) {
    }
}