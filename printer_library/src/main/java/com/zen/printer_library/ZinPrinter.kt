package com.zen.printer_library

import android.annotation.SuppressLint
import android.content.Context
import com.epson.epos2.printer.Printer
import com.google.gson.Gson
import com.printer.sdk.PrinterInstance
import com.zen.printer_library.epson.EpsonPrinter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


@SuppressLint("StaticFieldLeak")
object ZinPrinter {
    const val TAG = "ZinPrinter"
    var testTCP = "TCP:192.168.124.101"
    private var PrinterIP = testTCP
    private lateinit var mContext: Context
    private var mPrinter: Printer? = null
    private const val REQUEST_PERMISSION = 100
    private const val DISCONNECT_INTERVAL = 500 //millseconds
    private val printScope = CoroutineScope(Dispatchers.IO)

    fun init(context: Context) {
        mContext = context
        initEpsonPrinter()
    }

    private fun initEpsonPrinter() {
        EpsonPrinter.init(mContext)
        EpsonPrinter.setPrinterIP(PrinterIP)
    }

    fun setPrinterIP(ip: String) {
        PrinterIP = "TCP:".plus(ip)
    }

    fun print(data: String) {
        try {
            val printData = Gson().fromJson(data, PrintData::class.java)
            when (printData.printerType) {
                "epson" -> EpsonPrinter.print(data)
                else -> EpsonPrinter.print(data)
            }
        } catch (e: Exception) {
            mPrinter?.clearCommandBuffer()
        }
    }

    fun sprtBluetoothLabelPrintTest() {
        val printer =  PrinterInstance.getPrinterInstance()

    }

    fun printTest1() {
        EpsonPrinter.printTest1()
    }

    fun printTest2() {
        EpsonPrinter.printTest2()
    }

}