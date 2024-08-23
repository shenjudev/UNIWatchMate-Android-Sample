package com.transsion.oraimohealth.module.device.vdial

class CustomJsonBean {

    var version: String = ""

    var dial: Dial? = null

    var static: ArrayList<Static>? = null

    var active: ArrayList<Active>? = null

    class Dial {
        var name: String = ""
        var provider: String = ""
        var create: String = ""
        var lastupdate: String = ""
        var uuid: String = ""
        var width: IntArray? = null
        var height: IntArray? = null
    }

    class Static {
        var widget: String = ""

        var data: String = ""

        var label: String = ""

        var view: IntArray? = null

        var direction: Int = 0

        var alpha: Int = 0

        var align: Int = 0

        var zorder: Int = 0

        var resource: Array<String>? = null

        var id: Int = 0
    }
    class Active {
        var widget: String = ""

        var data: String = ""

        var label: String = ""

        var view: IntArray? = null

        var direction: Int = 0

        var alpha: Int = 0

        var align: Int = 0

        var text_align: Int = 0

        var zorder: Int = 0

        var font_type: Int = 0

        var font_size: Int = 0

        var font_color: IntArray? = null

        var id: Int = 0
    }

}

