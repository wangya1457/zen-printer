package com.zen.printer_library.sprt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.widget.Toast
import androidx.core.graphics.scale
import com.printer.sdk.PrinterConstants
import com.printer.sdk.PrinterInstance
import com.zen.printer_library.Label
import com.zen.printer_library.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak")
object SprtPrinter {
    private val printScope = CoroutineScope(Dispatchers.IO)

    lateinit var mContext: Context
    lateinit var mPrinter: PrinterInstance
    const val TEST_BLUETOOTH_ADDRESS = "00:12:5B:00:97:F5"
    private var bluetooth_address = TEST_BLUETOOTH_ADDRESS
    const val marginDot = 16

    fun initSprintPrinter(context: Context) {
        mContext = context
        try {
            val device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(bluetooth_address)
            mPrinter =
                PrinterInstance.getPrinterInstance(device, object : Handler(context.mainLooper) {
                    override fun handleMessage(msg: Message) {
                        when (msg.what) {
                            PrinterConstants.Connect.SUCCESS -> {
                                showToast("蓝牙打印机连接成功！")
                            }

                            PrinterConstants.Connect.FAILED -> {
                                showToast("蓝牙打印机连接失败！")
                            }

                            PrinterConstants.Connect.CLOSED -> {
                                showToast("蓝牙打印机连接关闭！")
                            }

                            PrinterConstants.Connect.NODEVICE -> {
                                showToast("无设备！")
                            }
                        }
                    }
                })
            mPrinter.openConnection()
        } catch (e: Exception) {
            showToast("printTestErr:$e")
        }
    }

    fun printTest() {

        val testLabel = Label(
            title = "超级珍珠珠...",
            text1 = "制作时间: 23/09/24 17:32",
            text2 = "制作时间: 23/09/24 17:32",
            text3 = "制作时间: 23/09/24 17:32",
            barCode = "88d9ad376",
            barCodeType = null
        )
        printLabel(testLabel)
    }

    fun printImageTest() {
        var image = BitmapFactory.decodeResource(mContext.resources, R.drawable.my_monochrome_image)
        image = image.scale(320, 240)
        printScope.launch {
            try {
                mPrinter.run {
                    // 设置标签纸大小
                    pageSetupTSPL(PrinterConstants.SIZE_58mm, 320, 240)
                    sendStrToPrinterTSPL("CLS\r\n")
                    drawBitmapTSPL(0, 0, 0, image)
                    printTSPL(1, 1)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("printTestErr:$e")
                }
            }
        }

    }

    fun printLabel(label: Label) {
        printScope.launch {
            try {
                mPrinter.run {

                    // 设置标签纸大小
                    pageSetupTSPL(PrinterConstants.SIZE_58mm, 320, 240)
//                    sendStrToPrinterTSPL("GAP 2 mm,0 mm")
//                    sendStrToPrinterTSPL("SIZE 40 mm,30 mm")
                    // 清除缓存区内容
                    sendStrToPrinterTSPL("CLS\r\n")
                    drawTextTSPL(
                        marginDot,
                        marginDot,
                        true,
                        2,
                        2,
                        PrinterConstants.PRotate.Rotate_0,
                        label.title
                    )
                    drawTextTSPL(
                        marginDot,
                        marginDot + 48 + 12,
                        true,
                        1,
                        1,
                        PrinterConstants.PRotate.Rotate_0,
                        label.text1
                    )
                    drawTextTSPL(
                        marginDot,
                        marginDot + 48 + 12 + 24 + 6,
                        true,
                        1,
                        1,
                        PrinterConstants.PRotate.Rotate_0,
                        label.text2
                    )
                    drawTextTSPL(
                        marginDot,
                        marginDot + 48 + 12 + 24 + 6 + 24 + 6,
                        true,
                        1,
                        1,
                        PrinterConstants.PRotate.Rotate_0,
                        label.text3
                    )

                    drawBarCodeTSPL(
                        marginDot,
                        marginDot + 48 + 12 + 24 + 6 + 24 + 6 + 24 + 12,
                        PrinterConstants.PBarcodeType.CODE128,
                        48,
                        true,
                        PrinterConstants.PRotate.Rotate_0,
                        2,
                        4,
                        label.barCode
                    )
                    printTSPL(1, 1)
//                closeConnection()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("printTestErr:$e")
                }
            }
        }
    }

    fun showToast(msg: String) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show()
    }

    fun setBlueToothAddress(address: String) {
        bluetooth_address = address
    }
}

