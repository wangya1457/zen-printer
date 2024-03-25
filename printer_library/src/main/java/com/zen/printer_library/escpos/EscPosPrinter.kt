package com.zen.printer_library.escpos

import android.content.Context
import android.graphics.BitmapFactory
import com.github.anastaciocintra.escpos.EscPos
import com.github.anastaciocintra.escpos.EscPosConst
import com.github.anastaciocintra.escpos.Style
import com.github.anastaciocintra.escpos.image.Bitonal
import com.github.anastaciocintra.escpos.image.BitonalOrderedDither
import com.github.anastaciocintra.escpos.image.EscPosImage
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper
import com.github.anastaciocintra.output.TcpIpOutputStream
import com.zen.printer_library.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class EscPosPrinter(private val context: Context) {
    val CharsetName_CHINISE = "GBK"
    private val printScope = CoroutineScope(Dispatchers.IO)


    fun printTest() {
        printScope.launch {
            val host = "192.168.124.101"
            val port = 9100
            try {
                TcpIpOutputStream(host, port).use { stream ->
                    val escpos = EscPos(stream)
                    escpos.setCharsetName(CharsetName_CHINISE)
                    val title =
                        Style()
                            .setFontSize(
                                Style.FontSize._3,
                                Style.FontSize._3
                            )
                            .setJustification(EscPosConst.Justification.Left_Default)
                    val subtitle =
                        Style(escpos.style)
                            .setBold(true)
                            .setUnderline(Style.Underline.OneDotThick)
                    val bold =
                        Style(escpos.style)
                            .setBold(true)
                    escpos
                        .write(
                            Style()
                                .setFontSize(
                                    Style.FontSize._1,
                                    Style.FontSize._1
                                ).setJustification(EscPosConst.Justification.Left_Default),
                            "FONT left"
                        )

                        .writeLF(
                            Style()
                                .setFontSize(
                                    Style.FontSize._1,
                                    Style.FontSize._1
                                ).setJustification(EscPosConst.Justification.Right),
                            "FONT Size_2 445 你好"
                        )

                        .writeLF(
                            Style()
                                .setFontSize(
                                    Style.FontSize._1,
                                    Style.FontSize._1
                                ), "Right"
                        )
                        .writeLF("")
                        .writeLF(title, "你好 中文")
                        .feed(3)
                        .write("Client: ")
                        .write("你好: ")
                        .write(title, "John Doe")
                        .feed(1)
                        .writeLF("Cup of coffee                      $1.00")
                        .writeLF("Botle of water                     $0.50")
                        .writeLF("┏ (^ω^)=  噵    衟                  $0.50")
                        .writeLF("----------------------------------------")
                        .feed(2)
                        .writeLF(
                            bold,
                            "TOTAL                              $1.50"
                        )
                        .writeLF("----------------------------------------")
//                        .feed(8)
                        .cut(EscPos.CutMode.FULL)
                val options = BitmapFactory.Options()
                options.inScaled = false
                val bitmap =
                    BitmapFactory.decodeResource(context.resources, R.drawable.zenithy_qr_code, options)
                val imageWrapper = RasterBitImageWrapper().setJustification(EscPosConst.Justification.Center)
                escpos.writeLF("BitonalOrderedDither()")
                // using ordered dither for dithering algorithm with default values
                val algorithm: Bitonal = BitonalOrderedDither()
                val escposImage =
                    EscPosImage(EscposImageImpl(bitmap), algorithm)
                escpos.write(imageWrapper, escposImage)
                escpos.feed(5).cut(EscPos.CutMode.FULL)
//                val barcode = BarCode()
//                escpos.writeLF("barcode UPCA system ")
//                barcode.setSystem(BarCode.BarCodeSystem.UPCA)
//                barcode.setHRIPosition(BarCode.BarCodeHRIPosition.BelowBarCode)
//                barcode.setBarCodeSize(2, 100)
//                escpos.feed(2)
//                escpos.write(barcode, "12345678901")
//                escpos.feed(3)
//                escpos.feed(5).cut(EscPos.CutMode.FULL)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
