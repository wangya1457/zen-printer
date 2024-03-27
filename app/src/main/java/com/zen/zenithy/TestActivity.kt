package com.zen.zenithy

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.zen.printer_library.CONNECTION_TYPE_BLUETOOTH
import com.zen.printer_library.Element
import com.zen.printer_library.PRINTER_TYPE_SPRT
import com.zen.printer_library.ZinPrinter
import com.zen.printer_library.ZinPrinter.TEST_BLUETOOTH_ADDRESS
import com.zen.printer_library.ZinPrinter.printTest1
import com.zen.printer_library.ZinPrinter.printTest2
import com.zen.printer_library.ZinPrinter.printTest3
import com.zen.printer_library.escpos.PrintTest.TestJsonData

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ZinPrinter.init(applicationContext, PRINTER_TYPE_SPRT, CONNECTION_TYPE_BLUETOOTH, TEST_BLUETOOTH_ADDRESS)

        findViewById<Button>(R.id.button).setOnClickListener {
            printTest1()
        }
        findViewById<Button>(R.id.button2).setOnClickListener {
            (0..5).forEach {
                printTest2()
            }
        }
        findViewById<Button>(R.id.button3).setOnClickListener {
            printTest3()
        }
    }

    private fun printTest() {
        val elements = arrayListOf<Element>()
        elements.add(
            Element(
                type = "style",
                text = "",
                image = null,
                textAlign = "center",
                lineSpace = 0,
                textSize = 0,
                ul = null,
                bold = null,
                color = 0,
                FL = 0,
                cut = null,
                clearBuffer = null,
                imgW = 0,
                imgH = 0
            )
        )
        elements.add(
            Element(
                type = "image",
                text = "",
                image = null,
                textAlign = "center",
                lineSpace = 0,
                textSize = 0,
                ul = null,
                bold = null,
                color = 0,
                FL = 0,
                cut = null,
                clearBuffer = null,
                imgW = 0,
                imgH = 0
            )
        )
        elements.add(
            Element(
                type = "style",
                text = "",
                image = null,
                textAlign = "",
                lineSpace = 0,
                textSize = 0,
                ul = null,
                bold = null,
                color = 0,
                FL = 0,
                cut = true,
                clearBuffer = null,
                imgW = 0,
                imgH = 0
            )
        )

//        val gsonData = Gson().toJson(TestJsonData)
        print(TestJsonData)
    }
}