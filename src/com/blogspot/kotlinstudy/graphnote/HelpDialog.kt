package com.blogspot.kotlinstudy.graphnote

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.plaf.basic.BasicScrollBarUI

class HelpDialog(parent: JFrame) : JDialog(parent, "Help", true), ActionListener {
    private var mHelpTextPane: JTextPane
    private var mCloseBtn : ColorButton

    init {
        mCloseBtn = ColorButton("Close")
        mCloseBtn.addActionListener(this)

        mHelpTextPane = JTextPane()
        mHelpTextPane.contentType = "text/html"
        mHelpTextPane.text = HelpText.text

        val scrollPane = JScrollPane(mHelpTextPane)
        val aboutPanel = JPanel()
        scrollPane.preferredSize = Dimension(850, 800)
        scrollPane.verticalScrollBar.ui = BasicScrollBarUI()
        scrollPane.horizontalScrollBar.ui = BasicScrollBarUI()
        aboutPanel.add(scrollPane)

        val panel = JPanel()
        panel.layout = BorderLayout()
        panel.add(aboutPanel, BorderLayout.CENTER)

        val btnPanel = JPanel()
        btnPanel.add(mCloseBtn)
        panel.add(btnPanel, BorderLayout.SOUTH)

        contentPane.add(panel)
        pack()
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == mCloseBtn) {
            dispose()
        }
    }
}

private class HelpText() {
    companion object {
        val text =
            """
        <html>
        <body>
        <center><font size=7>graph view utility</font><br>
        <font size=5>==================================================================================</font></center>

        </body>
        </html>
    """.trimIndent()
    }
}
