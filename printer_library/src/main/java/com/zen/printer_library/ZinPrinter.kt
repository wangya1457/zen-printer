package com.zen.printer_library

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.printer.sdk.PrinterInstance
import com.zen.printer_library.epson.EpsonPrinter
import com.zen.printer_library.gainscha.GainschaPrinter
import com.zen.printer_library.gainscha.GainschaPrinter.initGainschaPrinter
import com.zen.printer_library.sprt.SprtPrinter
import com.zen.printer_library.sprt.SprtPrinter.initSprintPrinter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

const val CONNECTION_TYPE_BLUETOOTH = "bluetooth"
const val CONNECTION_TYPE_WIFI = "wifi"
const val CONNECTION_TYPE_ETHERNET = "ethernet"
const val PRINTER_TYPE_EPSON = "epson"
const val PRINTER_TYPE_SPRT = "sprt"
const val PRINTER_TYPE_GAINSCHA = "gainscha"


@SuppressLint("StaticFieldLeak")
object ZinPrinter {
    const val TAG = "ZinPrinter"
    const val testTCP = "192.168.124.101"
    const val TEST_BLUETOOTH_ADDRESS = "00:12:5B:00:97:F5"

    private var PrinterIP = testTCP
    private lateinit var mContext: Context
    private const val REQUEST_PERMISSION = 100
    private const val DISCONNECT_INTERVAL = 500 //millseconds
    private val printScope = CoroutineScope(Dispatchers.IO)

    fun init(context: Context, printerType: String, connectionType: String, connectionData: String) {
        mContext = context
        when(printerType) {
            PRINTER_TYPE_EPSON -> initEpsonPrinter()
            PRINTER_TYPE_SPRT -> initSprintPrinter(context, connectionType, connectionData)
            PRINTER_TYPE_GAINSCHA -> initGainschaPrinter(context, connectionType, connectionData)
        }

    }

    private fun initEpsonPrinter() {
        EpsonPrinter.init(mContext)
        EpsonPrinter.setPrinterIP(PrinterIP)
    }

    fun setPrinterIP(ip: String) {
        PrinterIP = ip
    }

    fun print(data: String) {
        try {
            val printData = Gson().fromJson(data, PrintData::class.java)
            when (printData.printerType) {
                PRINTER_TYPE_EPSON -> EpsonPrinter.print(data)
                PRINTER_TYPE_SPRT -> SprtPrinter.print(printData)
                PRINTER_TYPE_GAINSCHA -> GainschaPrinter.print(printData)
                else -> EpsonPrinter.print(data)
            }
        } catch (e: Exception) {

        }
    }

    fun sprtBluetoothLabelPrintTest() {
        val printer =  PrinterInstance.getPrinterInstance()
    }

    fun printTest1() {
        EpsonPrinter.printTest1()
    }

    fun printTest2() {
        SprtPrinter.printTest()
    }

    fun printTest3() {
        GainschaPrinter.printTest()
    }
}