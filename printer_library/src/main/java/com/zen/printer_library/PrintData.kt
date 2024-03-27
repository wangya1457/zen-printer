package com.zen.printer_library

data class PrintData(
    var type: String?, // label receipt
    var printerType: String?, // epson sprt gainscha
    var dataType: String?, // zinData(自定义打印数据结构) commandData(ESC-POS或TSC 打印指令)
    var language: String?, // ZH_CN EN 打印语言
    var charSet: String?, // GBK 打印字符集
    var elements: ArrayList<Element>?, // 自定义打印元素
    var printCommandType: String?, // ESC-POS TSC (dataType == commandData 时的指令集类型)
    var commandData: ByteArray?, // dataType == commandData 时的打印指令。
    var label: Label? // type == label 时的label数据
)

data class Label(
    var title: String?,
    var text1: String?,
    var text2: String?,
    var text3: String?,
    var barCode: String?,
    var barCodeType: String?
)

data class Element(
    var type: String?, // style text barCode image
    var text: String?, // 文字
    var image: ByteArray?, // 图片(二维码)数组
    var textAlign: String?, // left center right 只在行首生效
    var lineSpace: Int, // 1..255  不设置，默认似乎是30？
    var textSize: Int, // 1..3
    var ul: Boolean?, // 下划线
    var bold: Boolean?, // 粗体
    var color: Int, // 1..4
    var FL: Int, // feedLine 换行
    var cut: Boolean?, // 切纸
    var clearBuffer: Boolean?, // 清除已输入指令
    var imgW: Int = 0, // 指定后对图片宽度进行缩放至imgW,不指定使用图片原宽度
    var imgH: Int = 0, // 指定后对图片高度进行缩放至imgH,不指定使用图片原高度
)