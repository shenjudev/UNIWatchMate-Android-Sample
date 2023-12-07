package com.base.sdk.entity.apps

/**
 * Contact(联系人)
 */
class WmContact internal constructor(
    val name: String,
    val number: String,
) {
    companion object {
         const val MAX_NAME_LEN_LIMIT = 32
         const val MAX_NUMBER_LEN_LIMIT = 20

        /**
         * Create a contact that the device can recognize. If the incoming parameter is invalid, it will return null.
         *
         * @param name   Contact name
         * @param number Contact phone number
         * @return
         */
        fun create(name: String?, number: String?): WmContact? {
            if (name == null || number == null) return null
            //去掉number中的空格

            val resultName = subString(name.trim(), MAX_NAME_LEN_LIMIT)
            val resultNumber = subString(number.trim(), MAX_NUMBER_LEN_LIMIT)
            return if (resultName.isNullOrEmpty() || resultNumber.isNullOrEmpty()) {
                null
            } else {
                WmContact(name, number)
            }
        }

        private fun subString(s: String?, bytesNeed: Int): String? {
            if (s == null || s.isEmpty() || bytesNeed <= 0) return null
            val bytes = s.toByteArray()
            if (bytes.size < bytesNeed) return s
            var count = 0
            for (i in s.indices) {
                count += s[i].toString().toByteArray().size
                if (count == bytesNeed) {
                    return s.substring(0, i + 1)
                } else if (count > bytesNeed) {
                    return if (i == 0) {
                        null
                    } else {
                        s.substring(0, i)
                    }
                }
            }
            return s
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WmContact) return false
        val that = other as WmContact
        return name.trim { it <= ' ' } == that.name.trim { it <= ' ' } && number.trim { it <= ' ' } == that.number.trim { it <= ' ' }
    }

    override fun toString(): String {
        return "WmContact(name='$name', number='$number')"
    }


}