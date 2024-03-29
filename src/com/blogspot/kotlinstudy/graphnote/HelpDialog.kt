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
        <b>Data format</b><br>
         TITLE|title string <br>
         SETTINGS|Y Name|X Name|X Range|Y Min value|Min annotation <br>
         Xval|Line name#Yval#Description1(Can be omitted)|Line name2#Yval2#Description2 ...
<pre>
Example
TITLE|Show CPU Usage : top -b -d 1
SETTINGS|CPU|TIME|160|0.1|0.5
1631165418|Total#7.3|999982-top#2.3|6558-Xorg#0.8|6889-cinnamon#0.8|8784-terminator#0.8
1631165419|Total#0.9|688631-java#0.4|999230-chrome#0.2|6889-cinnamon#0.1
</pre>
        </body>
        </html>
    """.trimIndent()
    }
}
