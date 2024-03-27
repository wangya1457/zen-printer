package com.zen.printer_library.epson

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.graphics.scale
import com.epson.epos2.Epos2Exception
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import com.epson.epos2.printer.ReceiveListener
import com.google.gson.Gson
import com.zen.printer_library.PrintData
import com.zen.printer_library.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Arrays

@SuppressLint("StaticFieldLeak")
object EpsonPrinter {
    private lateinit var mContext: Context
    private var mPrinter: Printer? = null
    private var TCP_IP = "TCP:192.168.124.101"
    private const val REQUEST_PERMISSION = 100
    private const val DISCONNECT_INTERVAL = 500 //millseconds
    private val printScope = CoroutineScope(Dispatchers.IO)
    
    fun init(context: Context) {
        mContext = context
        initializeObject()
    }

    fun setPrinterIP(ip: String) {
        TCP_IP = "TCP:".plus(ip)
    }

    fun printTest1() {
        printScope.run {
            runPrintReceiptSequence()
        }
    }

    fun printTest2() {
        printScope.run {
            runPrintCouponSequence()
        }
    }

    fun print(data: String) {
        try {
            val printData = Gson().fromJson(data, PrintData::class.java)
            when (printData.dataType) {
                "label" -> printLabel(printData)
                "receipt" -> printReceipt(printData)
                else -> printReceipt(printData)
            }
        } catch (e: Exception) {
            mPrinter?.clearCommandBuffer()
        }
    }

    private fun printReceipt(printData: PrintData) {
        if (printData.dataType.equals("commandData")) {
            mPrinter?.addCommand(printData.commandData)
            printData()
            return
        }
        when (printData.language) {
            "ZH_CN" -> mPrinter?.addTextLang(Printer.LANG_ZH_CN)
            else -> mPrinter?.addTextLang(Printer.LANG_EN)
        }
        printData.elements?.forEach { element ->
            when (element.type) {
                "style" -> {
                    if (element.FL > 0) {
                        mPrinter?.addFeedLine(element.FL)
                    }
                    if (!element.textAlign.isNullOrEmpty()) {
                        val textAlign = when (element.textAlign) {
                            "left" -> Printer.ALIGN_LEFT
                            "center" -> Printer.ALIGN_CENTER
                            "right" -> Printer.ALIGN_RIGHT
                            else -> Printer.ALIGN_CENTER
                        }
                        mPrinter?.addTextAlign(textAlign)
                    }
                    if (element.lineSpace in 1..255) {
                        mPrinter?.addLineSpace(element.lineSpace)
                    }
                    if (element.textSize in 1..3) {
                        mPrinter?.addTextSize(element.textSize, element.textSize)
                    }
                    if (true.equals(element.ul) || true.equals(element.bold) || (element.color in 1..4)) {
                        mPrinter?.addTextStyle(
                            Printer.PARAM_DEFAULT,
                            if (true.equals(element.ul)) Printer.TRUE else Printer.FALSE,
                            if (true.equals(element.bold)) Printer.TRUE else Printer.FALSE,
                            if (element.color in 1..4) element.color else Printer.PARAM_DEFAULT
                        )
                    }
                    if (true.equals(element.cut)) {
                        mPrinter?.addCut(Printer.CUT_FEED)
                    }
                }

                "text" -> {
                    if (element.FL > 0) {
                        mPrinter?.addFeedLine(element.FL)
                    }
                    if (!element.textAlign.isNullOrEmpty()) {
                        val textAlign = when (element.textAlign) {
                            "left" -> Printer.ALIGN_LEFT
                            "center" -> Printer.ALIGN_CENTER
                            "right" -> Printer.ALIGN_RIGHT
                            else -> Printer.ALIGN_CENTER
                        }
                        mPrinter?.addTextAlign(textAlign)
                    }
                    if (element.textSize in 1..3) {
                        mPrinter?.addTextSize(element.textSize, element.textSize)
                    }
                    if (true.equals(element.ul) || true.equals(element.bold) || (element.color in 1..4)) {
                        mPrinter?.addTextStyle(
                            Printer.PARAM_DEFAULT,
                            if (true.equals(element.ul)) Printer.TRUE else Printer.FALSE,
                            if (true.equals(element.bold)) Printer.TRUE else Printer.FALSE,
                            if (element.color in 1..4) element.color else Printer.PARAM_DEFAULT
                        )
                    }
                    mPrinter?.addText(element.text ?: "")
                }

                "barCode" -> {

                }

                "image" -> {
                    element.image?.let {
                        var image = BitmapFactory.decodeByteArray(it, 0, it.size)
                        if (element.imgW > 0 || element.imgH > 0) {
                            image = image.scale(element.imgW, element.imgH)
                        }
                        mPrinter?.addImage(
                            image, 0, 0,
                            image.width,
                            image.height,
                            Printer.COLOR_1,
                            Printer.MODE_MONO,
                            Printer.HALFTONE_DITHER,
                            Printer.PARAM_DEFAULT.toDouble(),
                            Printer.COMPRESS_AUTO
                        )
                    }
                }
            }
        }
        printData()
    }

    private fun printLabel(printData: PrintData) {

    }

    private fun runPrintReceiptSequence(): Boolean {
        if (!createReceiptData()) {
            return false
        }
        return if (!printData()) {
            false
        } else true
    }

    private fun createReceiptData(): Boolean {
        var method = ""
        val logoData =
            BitmapFactory.decodeResource(mContext.getResources(), R.drawable.zenithy_qr_code)
                .scale(200, 200, true)
        var textData = StringBuilder()
        val barcodeWidth = 2
        val barcodeHeight = 100
        if (mPrinter == null) {
            return false
        }
        val ops = ByteArrayOutputStream()
        logoData.compress(Bitmap.CompressFormat.PNG, 100, ops)
        val test = ops.toByteArray()
        Log.d("imageArray", Arrays.toString(test))
        try {
            mPrinter?.addTextLang(Printer.LANG_ZH_CN)
            method = "addTextAlign"
//            mPrinter?.addTextAlign(Printer.ALIGN_CENTER)
            method = "addImage"
            mPrinter?.addImage(
                logoData, 0, 0,
                logoData.width,
                logoData.height,
                Printer.COLOR_1,
                Printer.MODE_MONO,
                Printer.HALFTONE_DITHER,
                Printer.PARAM_DEFAULT.toDouble(),
                Printer.COMPRESS_AUTO
            )
            method = "addFeedLine"
            mPrinter?.addFeedLine(1)
            mPrinter?.addTextFont(Printer.FONT_A)
            textData.append("你好，中文 123")
            textData.append(" (555) 555 – 5555\n")
            mPrinter?.addText(textData.toString())

            textData.delete(0, textData.length)
            mPrinter?.addTextFont(Printer.FONT_A)
            mPrinter?.addTextAlign(Printer.ALIGN_LEFT)
            mPrinter?.addTextSize(1, 1)
            textData.append("STORE DIRECTOR – John Smith\n")
            textData.append("\n")
            textData.append("7/01/07 16:58 6153 05 0191 134\n")
            textData.append("ST# 21 OP# 001 TE# 01 TR# 747\n")
            textData.append("------------------------------\n")
            method = "addText"
            mPrinter?.addText(textData.toString())

            textData.delete(0, textData.length)
            mPrinter?.addTextSize(1, 1)
            mPrinter?.addTextStyle(
                Printer.PARAM_DEFAULT,
                Printer.PARAM_DEFAULT,
                Printer.PARAM_DEFAULT,
                Printer.COLOR_1
            )
            textData.append("一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十")
            mPrinter?.addText(textData.toString())
            mPrinter?.addFeedLine(1)

            textData.delete(0, textData.length)
//            mPrinter?.addTextSize(2,2)
            mPrinter?.addTextStyle(
                Printer.PARAM_DEFAULT,
                Printer.TRUE,
                Printer.PARAM_DEFAULT,
                Printer.COLOR_2
            )
            textData.append("一二三四五六七八九十一二三四五六七八九十一二三四五六七八九十")
            mPrinter?.addText(textData.toString())
            mPrinter?.addFeedLine(1)

            textData.delete(0, textData.length)
//            mPrinter?.addTextSize(3,3)
            mPrinter?.addTextStyle(
                Printer.PARAM_DEFAULT,
                Printer.PARAM_DEFAULT,
                Printer.TRUE,
                Printer.COLOR_3
            )
            textData.append("一二三四五六七八九十一二三四五六七八九十")
            mPrinter?.addText(textData.toString())
            mPrinter?.addFeedLine(1)

            textData.delete(0, textData.length)
//            mPrinter?.addTextSize(4,4)
//            mPrinter?.addLineSpace(0)
            mPrinter?.addTextStyle(
                Printer.PARAM_DEFAULT,
                Printer.PARAM_DEFAULT,
                Printer.PARAM_DEFAULT,
                Printer.COLOR_4
            )
            textData.append("一二三四五六七八九十一二三四五六七八九十")
            mPrinter?.addText(textData.toString())
            mPrinter?.addFeedLine(1)

            textData.delete(0, textData.length)
            mPrinter?.addTextFont(Printer.FONT_A)
            mPrinter?.addTextAlign(Printer.ALIGN_CENTER)
            mPrinter?.addTextSize(1, 1)
            textData.append("438 CANDYMAKER ASSORT   4.99 R\n")
            textData.append("474 TRIPOD              8.99 R\n")
            textData.append("433 BLK LOGO PRNTED ZO  7.99 R\n")
            textData.append("458 AQUA MICROTERRY SC  6.99 R\n")
            textData.append("493 30L BLK FF DRESS   16.99 R\n")
            textData.append("407 LEVITATING DESKTOP  7.99 R\n")
            textData.append("441 **Blue Overprint P  2.99 R\n")
            textData.append("476 REPOSE 4PCPM CHOC   5.49 R\n")
            textData.append("461 WESTGATE BLACK 25  59.99 R\n")
            textData.append("------------------------------\n")
            method = "addText"
            mPrinter?.addText(textData.toString())
            textData.delete(0, textData.length)
            textData.append("SUBTOTAL                160.38\n")
            textData.append("TAX                      14.43\n")
            method = "addText"
            mPrinter?.addText(textData.toString())
            textData.delete(0, textData.length)
            method = "addTextSize"
            mPrinter?.addTextSize(2, 2)
            method = "addText"
            mPrinter?.addText("TOTAL    174.81\n")
            method = "addTextSize"
            mPrinter?.addTextSize(1, 1)
            method = "addFeedLine"
            mPrinter?.addFeedLine(1)
            textData.append("CASH                    200.00\n")
            textData.append("CHANGE                   25.19\n")
            textData.append("------------------------------\n")
            method = "addText"
            mPrinter?.addText(textData.toString())
            textData.delete(0, textData.length)
            textData.append("Purchased item total number\n")
            textData.append("Sign Up and Save !\n")
            textData.append("With Preferred Saving Card\n")
            method = "addText"
            mPrinter?.addText(textData.toString())
            textData.delete(0, textData.length)
            method = "addFeedLine"
            mPrinter?.addFeedLine(2)
            method = "addBarcode"
            mPrinter?.addBarcode(
                "01209457",
                Printer.BARCODE_CODE39,
                Printer.HRI_BELOW,
                Printer.FONT_A,
                barcodeWidth,
                barcodeHeight
            )
            method = "addCut"
            mPrinter?.addCut(Printer.CUT_FEED)
        } catch (e: Exception) {
            mPrinter?.clearCommandBuffer()
            ShowMsg.showException(e, method, mContext)
            return false
        }
        textData.clear()
        return true
    }

    private fun runPrintCouponSequence(): Boolean {
        if (!createCouponData()) {
            return false
        }
        return if (!printData()) {
            false
        } else true
    }

    private fun createCouponData(): Boolean {
        var method = ""
        val coffeeData =
            BitmapFactory.decodeResource(mContext.resources, R.drawable.zenithy_qr_code)
                .scale(300, 300)
        val wmarkData = BitmapFactory.decodeResource(mContext.resources, R.drawable.wmark)
        val barcodeWidth = 2
        val barcodeHeight = 64
        if (mPrinter == null) {
            return false
        }
        try {
            method = "addImage"
            mPrinter?.addImage(
                wmarkData,
                0,
                0,
                wmarkData.width,
                wmarkData.height,
                Printer.PARAM_DEFAULT,
                Printer.PARAM_DEFAULT,
                Printer.PARAM_DEFAULT,
                Printer.PARAM_DEFAULT.toDouble(),
                Printer.PARAM_DEFAULT
            )
            method = "addImage"
            mPrinter?.addImage(
                coffeeData,
                0,
                0,
                coffeeData.width,
                coffeeData.height,
                Printer.PARAM_DEFAULT,
                Printer.PARAM_DEFAULT,
                Printer.PARAM_DEFAULT,
                3.0,
                Printer.COMPRESS_DEFLATE
            )
            method = "addBarcode"
            mPrinter?.addBarcode(
                "01234567890",
                Printer.BARCODE_UPC_A,
                Printer.PARAM_DEFAULT,
                Printer.PARAM_DEFAULT,
                barcodeWidth,
                barcodeHeight
            )
            method = "addCut"
            mPrinter?.addCut(Printer.CUT_FEED)
        } catch (e: Exception) {
            mPrinter?.clearCommandBuffer()
            ShowMsg.showException(e, method, mContext)
            return false
        }
        return true
    }

    private fun printData(): Boolean {
        if (mPrinter == null) {
            return false
        }
        if (!connectPrinter()) {
            mPrinter?.clearCommandBuffer()
            return false
        }
        try {
            mPrinter?.sendData(Printer.PARAM_DEFAULT)
        } catch (e: Exception) {
            mPrinter?.clearCommandBuffer()
            ShowMsg.showException(e, "sendData", mContext)
            try {
                mPrinter?.disconnect()
            } catch (ex: Exception) {
                // Do nothing
            }
            return false
        }
        return true
    }

    private fun initializeObject(): Boolean {
        try {
            mPrinter = Printer(
                Printer.TM_T83III,
                Printer.MODEL_CHINESE,
                mContext
            )
        } catch (e: Exception) {
            ShowMsg.showException(e, "Printer", mContext)
            return false
        }
        mPrinter?.setReceiveEventListener(object : ReceiveListener {
            override fun onPtrReceive(p0: Printer?, p1: Int, p2: PrinterStatusInfo?, p3: String?) {
                printScope.launch {
                    disconnectPrinter()
                }
            }
        })
        return true
    }

    private fun finalizeObject() {
        if (mPrinter == null) {
            return
        }
        mPrinter?.setReceiveEventListener(null)
        mPrinter = null
    }

    private fun connectPrinter(): Boolean {
        if (mPrinter == null) {
            return false
        }
        try {
            mPrinter?.connect(
                TCP_IP,
                Printer.PARAM_DEFAULT
            )
        } catch (e: Exception) {
            ShowMsg.showException(e, "connect", mContext)
            return false
        }
        return true
    }

    private suspend fun disconnectPrinter() {
        if (mPrinter == null) {
            return
        }
        while (true) {
            try {
                mPrinter?.disconnect()
                break
            } catch (e: Exception) {
                if (e is Epos2Exception) {
                    //Note: If printer is processing such as printing and so on, the disconnect API returns ERR_PROCESSING.
                    if (e.errorStatus == Epos2Exception.ERR_PROCESSING) {
                        try {
                            delay(DISCONNECT_INTERVAL.toLong())
                        } catch (ex: Exception) {
                        }
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
        }
        mPrinter?.clearCommandBuffer()
    }
}