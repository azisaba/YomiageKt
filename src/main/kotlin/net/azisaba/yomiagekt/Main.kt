package net.azisaba.yomiagekt

import net.azisaba.yomiagekt.bot.YomiageBot

fun main() {
    // headless mode
    System.setProperty("java.awt.headless", "true")

    YomiageBot().main()
}
