package com.zen.zenithy

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.zen.printer_library.Element
import com.zen.printer_library.ZinPrinter
import com.zen.printer_library.ZinPrinter.printTest1
import com.zen.printer_library.escpos.EscPosPrinter
import com.zen.printer_library.escpos.PrintTest.TestJsonData
import com.zen.printer_library.sprt.SprtPrinter

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ZinPrinter.init(this)
        SprtPrinter.initSprintPrinter(this)

        findViewById<Button>(R.id.button).setOnClickListener {
            printTest1()
        }
        findViewById<Button>(R.id.button2).setOnClickListener {
            EscPosPrinter(this).printTest()
        }
        findViewById<Button>(R.id.button3).setOnClickListener {
            SprtPrinter.printTest()
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