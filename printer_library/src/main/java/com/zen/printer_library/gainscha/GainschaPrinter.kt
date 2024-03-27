package com.zen.printer_library.gainscha

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import com.gprinter.bean.PrinterDevices
import com.gprinter.command.LabelCommand
import com.gprinter.utils.CallbackListener
import com.gprinter.utils.Command
import com.gprinter.utils.ConnMethod
import com.gprinter.utils.LogUtils
import com.zen.printer_library.CONNECTION_TYPE_BLUETOOTH
import com.zen.printer_library.CONNECTION_TYPE_ETHERNET
import com.zen.printer_library.CONNECTION_TYPE_WIFI
import com.zen.printer_library.Label
import com.zen.printer_library.PrintData
import com.zen.printer_library.R
import com.zen.printer_library.sprt.SprtPrinter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Vector

@SuppressLint("StaticFieldLeak")
object GainschaPrinter {
    private val printScope = CoroutineScope(Dispatchers.IO)
    lateinit var mContext: Context
    const val marginDot = 16

    val gprinterListener = object : CallbackListener {
        override fun onConnecting() {
        }

        override fun onCheckCommand() {
        }

        override fun onSuccess(printerDevices: PrinterDevices?) {
        }

        override fun onReceive(data: ByteArray?) {
        }

        override fun onFailure() {
        }

        override fun onDisconnect() {
        }
    }

    val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                0x00 -> {
                    val tip = msg.obj as String
                    Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show()
                }

                0x01 -> {
                    val status = msg.arg1
                    when (status) {
                        -1 -> { //获取状态失败

                            return
                        }

                        1 -> { // 走纸，打印
                            Toast.makeText(
                                mContext,
                                mContext.getString(R.string.status_feed),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            return
                        }

                        0 -> { //状态正常
                            Toast.makeText(
                                mContext,
                                mContext.getString(R.string.status_normal),
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        -2 -> { //状态缺纸
                            Toast.makeText(
                                mContext,
                                mContext.getString(R.string.status_out_of_paper),
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        -3 -> { //状态开盖
                            Toast.makeText(
                                mContext,
                                mContext.getString(R.string.status_open),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            return
                        }

                        -4 -> {
                            Toast.makeText(
                                mContext,
                                mContext.getString(R.string.status_overheated),
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                    }
                }

                0x02 -> {
                    // 未连接
                    Thread {
                        if (Printer.portManager != null) {
                            Printer.close()
                        }
                    }.start()
                }

                0x03 -> {
                    val message = msg.obj as String

                }
            }
        }
    }

    fun initGainschaPrinter(
        context: Context,
        connectionType: String = CONNECTION_TYPE_BLUETOOTH,
        connectionData: String
    ) {
        mContext = context
        when (connectionType) {
            CONNECTION_TYPE_BLUETOOTH -> {
                val mac = connectionData
                val blueTooth = PrinterDevices.Build()
                    .setContext(context)
                    .setConnMethod(ConnMethod.BLUETOOTH)
                    .setMacAddress(mac)
                    .setCommand(Command.TSC)
                    .setCallbackListener(gprinterListener)
                    .build()
                Printer.connect(blueTooth)
            }

            CONNECTION_TYPE_ETHERNET -> {
                val ip: String = connectionData
                val wifi = PrinterDevices.Build()
                    .setContext(context)
                    .setConnMethod(ConnMethod.WIFI)
                    .setIp(ip)
                    .setPort(9100) //打印唯一端口9100
                    .setCommand(Command.TSC)
                    .setCallbackListener(gprinterListener)
                    .build()
                Printer.connect(wifi)
            }

            CONNECTION_TYPE_WIFI -> {
                val ip: String = connectionData
                val wifi = PrinterDevices.Build()
                    .setContext(context)
                    .setConnMethod(ConnMethod.WIFI)
                    .setIp(ip)
                    .setPort(9100) //打印唯一端口9100
                    .setCommand(Command.TSC)
                    .setCallbackListener(gprinterListener)
                    .build()
                Printer.connect(wifi)
            }
        }
    }

    fun printTest() {
        val testLabel = Label(
            title = "超级珍珠珠...",
            text1 = "制作时间: 23/09/24 17:32",
            text2 = "过期时间: 23/09/24 20:32",
            text3 = "操作人:   王豹豹",
            barCode = "88d9ad376",
            barCodeType = null
        )
        printLabel(testLabel)
    }

    fun print(printData: PrintData) {
        when (printData.type) {
            "label" -> {
                printData.label?.let {
                    SprtPrinter.printLabel(it)
                }
            }
        }
    }

    fun printLabel(label: Label) {
        printScope.launch {
            try {
                val result = Printer.portManager?.writeDataImmediately(getLabelCommand(label))

                if (result == true) {

                } else {

                }
            } catch (e: Exception) {
                LogUtils.e(e.message)
            } finally {
                if (Printer.portManager == null) {
                    Printer.close()
                }
            }

        }
    }

    fun getLabelCommand(label: Label): Vector<Byte> {
        val tsc = LabelCommand().apply {
            // 设置标签尺寸宽高，按照实际尺寸设置 单位mm
            addUserCommand("\r\n")
            addSize(40, 30)
            // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0 单位mm
            addGap(2)
            // 设置打印方向
            addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL)
            // 设置原点坐标
            addReference(0, 0)
            // 清除打印缓冲区
            addCls()

            addText(
                marginDot,
                marginDot,
                LabelCommand.FONTTYPE.SIMPLIFIED_24_CHINESE,
                LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_2,
                LabelCommand.FONTMUL.MUL_2,
                label.title
            )
            addText(
                marginDot,
                marginDot + 48 + 12,
                LabelCommand.FONTTYPE.SIMPLIFIED_24_CHINESE,
                LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1,
                LabelCommand.FONTMUL.MUL_1,
                label.text1
            )
            addText(
                marginDot,
                marginDot + 48 + 12 + 24 + 6,
                LabelCommand.FONTTYPE.SIMPLIFIED_24_CHINESE,
                LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1,
                LabelCommand.FONTMUL.MUL_1,
                label.text2
            )
            addText(
                marginDot,
                marginDot + 48 + 12 + 24 + 6 + 24 + 6,
                LabelCommand.FONTTYPE.SIMPLIFIED_24_CHINESE,
                LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1,
                LabelCommand.FONTMUL.MUL_1,
                label.text3
            )
            // 绘制一维条码
            add1DBarcode(
                marginDot,
                marginDot + 48 + 12 + 24 + 6 + 24 + 6 + 24 + 12,
                LabelCommand.BARCODETYPE.CODE128,
                48,
                LabelCommand.READABEL.EANBEL,
                LabelCommand.ROTATION.ROTATION_0,
                2,
                4,
                label.barCode
            )
            // 打印标签
            addPrint(1, 1)
//            addSound(2, 100)
        }
        return tsc.getCommand()
    }

    fun handleMassage(msg: String) {

    }
}