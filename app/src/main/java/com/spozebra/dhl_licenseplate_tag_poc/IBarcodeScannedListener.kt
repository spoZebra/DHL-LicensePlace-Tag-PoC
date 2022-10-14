package com.spozebra.dhl_licenseplate_tag_poc

interface IBarcodeScannedListener {
    fun barcodeScanned(barcode : String?)
}