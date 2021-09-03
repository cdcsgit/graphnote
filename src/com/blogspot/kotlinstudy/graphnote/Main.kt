package com.blogspot.kotlinstudy.graphnote

import java.awt.Color
import java.awt.Dimension
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.plaf.ColorUIResource
import javax.swing.text.html.HTMLEditorKit
import javax.swing.text.html.StyleSheet

class Main {
    companion object {
        val VERSION: String = "0.0.1"

        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("awt.useSystemAAFontSettings","on")
            System.setProperty("swing.aatext", "true")

            SwingUtilities.invokeLater {
                UIManager.put("ScrollBar.thumb", ColorUIResource(Color(0xE0, 0xE0, 0xE0)))
                UIManager.put("ScrollBar.thumbHighlight", ColorUIResource(Color(0xE5, 0xE5, 0xE5)))
                UIManager.put("ScrollBar.thumbShadow", ColorUIResource(Color(0xE5, 0xE5, 0xE5)))
                UIManager.put("ComboBox.buttonDarkShadow", ColorUIResource(Color.black))

                val mainUI = MainUI("GraphNote")
                mainUI.preferredSize = Dimension(1000, 500)
                mainUI.isVisible = true
            }
        }
    }
}