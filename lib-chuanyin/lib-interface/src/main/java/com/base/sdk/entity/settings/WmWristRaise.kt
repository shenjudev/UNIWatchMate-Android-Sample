package com.base.sdk.entity.settings

/**
 * 抬腕亮屏设置
 */
data class WmWristRaise(
    /**
     * Whether to enable wrist lift(是否开启抬腕)
     */
    var isScreenWakeEnabled: Boolean = false
) {
    override fun toString(): String {
        return "WmWristRaise(isScreenWakeEnabled=$isScreenWakeEnabled)"
    }
}