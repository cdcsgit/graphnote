package com.blogspot.kotlinstudy.graphnote

import java.awt.*
import java.awt.event.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableModel
import kotlin.system.exitProcess


class MainUI(title: String) : JFrame() {
    private lateinit var mMenuBar: JMenuBar
    private lateinit var mMenuFile: JMenu
    private lateinit var mItemFileOpen: JMenuItem
    private lateinit var mItemFileOpenRecents: JMenu
    private lateinit var mItemFileExit: JMenuItem
    private lateinit var mMenuView: JMenu
    private lateinit var mMenuHelp: JMenu
    private lateinit var mItemHelp: JMenuItem
    private lateinit var mItemAbout: JMenuItem

    private lateinit var mMainPane: JPanel
    private lateinit var mGraphViewPane: GraphViewPanel
    private lateinit var mInfoPane: JPanel

    internal lateinit var mGraphTitleTF: JTextField

    private lateinit var mCtrlPane: JPanel
    private lateinit var mCtrlTabbedPane: JTabbedPane
    private lateinit var mCmdPane: JPanel
    private lateinit var mCmdStartBtn: ColorButton
    private lateinit var mCmdStopBtn: ColorButton
    private lateinit var mCmdClearBtn: ColorButton
    private lateinit var mCmdClearSaveBtn: ColorButton
    private lateinit var mCmdDirLabel: JLabel
    private lateinit var mCmdDirTF: JTextField
    private lateinit var mCmdDirBtn: ColorButton
    private lateinit var mCmdCombo: ColorComboBox<String>

    private lateinit var mFilePane: JPanel
    private lateinit var mFileStartBtn: ColorButton
    private lateinit var mFileStopBtn: ColorButton
    private lateinit var mFileClearBtn: ColorButton
    private lateinit var mFileClearSaveBtn: ColorButton
    private lateinit var mFilePathLabel: JLabel
    private lateinit var mFilePathTF: JTextField
    private lateinit var mFilePathBtn: ColorButton
    internal lateinit var mFileRefreshBtn: JCheckBox

    private lateinit var mInfoModel: DefaultTableModel
    private lateinit var mInfoTable: JTable
    private lateinit var mInfoScrollPane: JScrollPane

    private val mActionHandler = ActionHandler()
    private val mMouseHandler = MouseHandler()

    private val mConfigManager = ConfigManager()
    private var mIsCreatingUI = true

    private var mFrameX = 0
    private var mFrameY = 0
    private var mFrameWidth = 1280
    private var mFrameHeight = 720
    private var mFrameExtendedState = Frame.MAXIMIZED_BOTH

    init {
        mConfigManager.loadConfig()

        var prop = mConfigManager.mProperties[mConfigManager.ITEM_FRAME_X] as? String
        if (!prop.isNullOrEmpty()) {
            mFrameX = prop.toInt()
        }
        prop = mConfigManager.mProperties[mConfigManager.ITEM_FRAME_Y] as? String
        if (!prop.isNullOrEmpty()) {
            mFrameY = prop.toInt()
        }
        prop = mConfigManager.mProperties[mConfigManager.ITEM_FRAME_WIDTH] as? String
        if (!prop.isNullOrEmpty()) {
            mFrameWidth = prop.toInt()
        }
        prop = mConfigManager.mProperties[mConfigManager.ITEM_FRAME_HEIGHT] as? String
        if (!prop.isNullOrEmpty()) {
            mFrameHeight = prop.toInt()
        }
        prop = mConfigManager.mProperties[mConfigManager.ITEM_FRAME_EXTENDED_STATE] as? String
        if (!prop.isNullOrEmpty()) {
            mFrameExtendedState = prop.toInt()
        }

        createUI(title)
    }

    private fun exit() {
        mConfigManager.saveConfig()
        exitProcess(0)
    }

    private fun createUI(title: String) {
        setTitle(title)

        val img = ImageIcon(this.javaClass.getResource("/images/logo.png"))
        iconImage = img.image

        defaultCloseOperation = EXIT_ON_CLOSE
        setLocation(mFrameX, mFrameY)
        setSize(mFrameWidth, mFrameHeight)
        extendedState = mFrameExtendedState
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(p0: ComponentEvent?) {
                revalidate()
                super.componentResized(p0)
            }
        })

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                exit()
            }
        })

        mMenuBar = JMenuBar()
        mMenuFile = JMenu(Strings.FILE)

        mItemFileOpen = JMenuItem(Strings.OPEN)
        mItemFileOpen.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileOpen)

        mItemFileOpenRecents = JMenu(Strings.OPEN_RECENTS)
        mItemFileOpenRecents.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileOpenRecents)

        mItemFileExit = JMenuItem(Strings.EXIT)
        mItemFileExit.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileExit)
        mMenuBar.add(mMenuFile)

        mMenuView = JMenu(Strings.VIEW)
        mMenuBar.add(mMenuView)

        mMenuHelp = JMenu(Strings.HELP)

        mItemHelp = JMenuItem(Strings.HELP)
        mItemHelp.addActionListener(mActionHandler)
        mMenuHelp.add(mItemHelp)

        mItemAbout = JMenuItem(Strings.ABOUT)
        mItemAbout.addActionListener(mActionHandler)
        mMenuHelp.add(mItemAbout)
        mMenuBar.add(mMenuHelp)

        jMenuBar = mMenuBar

        layout = BorderLayout()

        mCmdStartBtn = ColorButton("Start")
        mCmdStartBtn.addActionListener(mActionHandler)
        mCmdStartBtn.addMouseListener(mMouseHandler)

        mCmdStopBtn = ColorButton("Stop")
        mCmdStopBtn.addActionListener(mActionHandler)
        mCmdStopBtn.addMouseListener(mMouseHandler)

        mCmdClearBtn = ColorButton("Clear")
        mCmdClearBtn.addActionListener(mActionHandler)
        mCmdClearBtn.addMouseListener(mMouseHandler)

        mCmdClearSaveBtn = ColorButton("ClearSave")
        mCmdClearSaveBtn.addActionListener(mActionHandler)
        mCmdClearSaveBtn.addMouseListener(mMouseHandler)

        mCmdPane = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        mCmdPane.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
        mCmdPane.add(mCmdStartBtn)
        mCmdPane.add(mCmdStopBtn)
        mCmdPane.add(mCmdClearBtn)
        mCmdPane.add(mCmdClearSaveBtn)

        addVSeparator(mCmdPane)
        mCmdDirBtn = ColorButton("Select")
        mCmdDirBtn.addActionListener(mActionHandler)

        mCmdDirLabel = JLabel("Cmds Dir ")

        mCmdDirTF = JTextField()
        mCmdDirTF.preferredSize = Dimension(300, 28)
        mCmdDirTF.isEditable = false

        mCmdPane.add(mCmdDirLabel)
        mCmdPane.add(mCmdDirTF)
        mCmdPane.add(mCmdDirBtn)

        addVSeparator(mCmdPane)

        mCmdCombo = ColorComboBox<String>()
        mCmdCombo.isEditable = true
        mCmdCombo.renderer = ColorComboBox.ComboBoxRenderer()
//        mCmdCombo.editor.editorComponent.addKeyListener(mKeyHandler)
//        mCmdCombo.addItemListener(mItemHandler)
        mCmdCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mCmdCombo.preferredSize = Dimension(200, 28)
        mCmdCombo.border = BorderFactory.createEmptyBorder(0, 0, 0, 5)

        mCmdPane.add(mCmdCombo)

        mFileStartBtn = ColorButton("Start")
        mFileStartBtn.addActionListener(mActionHandler)
        mFileStartBtn.addMouseListener(mMouseHandler)

        mFileStopBtn = ColorButton("Stop")
        mFileStopBtn.addActionListener(mActionHandler)
        mFileStopBtn.addMouseListener(mMouseHandler)

        mFileClearBtn = ColorButton("Clear")
        mFileClearBtn.addActionListener(mActionHandler)
        mFileClearBtn.addMouseListener(mMouseHandler)

        mFileClearSaveBtn = ColorButton("ClearSave")
        mFileClearSaveBtn.addActionListener(mActionHandler)
        mFileClearSaveBtn.addMouseListener(mMouseHandler)

        mFilePane = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        mFilePane.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
        
        mFilePane.add(mFileStartBtn)
        mFilePane.add(mFileStopBtn)
        mFilePane.add(mFileClearBtn)
        mFilePane.add(mFileClearSaveBtn)
        
        addVSeparator(mFilePane)

        mFilePathBtn = ColorButton("Select")
        mFilePathBtn.addActionListener(mActionHandler)

        mFilePathLabel = JLabel("Files Path ")

        mFilePathTF = JTextField()
        mFilePathTF.preferredSize = Dimension(300, 28)
        mFilePathTF.isEditable = false

        mFilePane.add(mFilePathLabel)
        mFilePane.add(mFilePathTF)
        mFilePane.add(mFilePathBtn)

        addVSeparator(mFilePane)

        mFileRefreshBtn = JCheckBox("Auto Refresh", false)
        mFilePane.add(mFileRefreshBtn)

        mCtrlTabbedPane = JTabbedPane()
        mCtrlTabbedPane.addTab("Run Cmd", mCmdPane);
        mCtrlTabbedPane.addTab("Open File", mFilePane);
        mCtrlTabbedPane.selectedComponent.foreground = Color.red

        mCtrlPane = JPanel(GridLayout(1, 1))
        mCtrlPane.add(mCtrlTabbedPane)

        val insets = UIManager.getInsets("TabbedPane.contentBorderInsets")
        insets.top = -1
        insets.bottom = -1
        insets.left = -1
        insets.right = -1
        UIManager.put("TabbedPane.contentBorderInsets", insets)

        mInfoPane = JPanel(BorderLayout())
        mInfoPane.preferredSize = Dimension(200, 100)

        val header = arrayOf("Series", "Value")
        mInfoModel = DefaultTableModel(header, 0)
        mInfoTable = JTable(mInfoModel)
        mInfoScrollPane = JScrollPane(mInfoTable)
        mInfoTable.columnModel.getColumn(0).preferredWidth = 150;
        mInfoPane.add(mInfoScrollPane)

        mMainPane = JPanel(BorderLayout())
        mGraphViewPane = GraphViewPanel(mInfoTable)

        mGraphTitleTF = JTextField("Graph View Utility")
        mGraphTitleTF.isEditable = false
        mGraphTitleTF.preferredSize = Dimension(10, 40)
        mGraphTitleTF.horizontalAlignment = JTextField.CENTER
        var font = Font(mGraphTitleTF.font.fontName, Font.BOLD, 20)
        mGraphTitleTF.font = font

        add(mCtrlPane, BorderLayout.NORTH)
        add(mMainPane, BorderLayout.CENTER)

        mMainPane.add(mGraphTitleTF, BorderLayout.NORTH)
        mMainPane.add(mGraphViewPane, BorderLayout.CENTER)
        mMainPane.add(mInfoPane, BorderLayout.EAST)

        mCmdDirTF.text = mConfigManager.mProperties[mConfigManager.ITEM_CMD_DIR] as? String

        if (mCmdDirTF.text.isNotEmpty()) {
            val dir = File(mCmdDirTF.text)
            val listFiles = dir.listFiles { param -> param.isFile && param.canExecute() }

            mCmdCombo.removeAllItems()
            for (item in listFiles) {
                mCmdCombo.addItem(item.name)
            }
        }
        mCmdCombo.selectedItem = mConfigManager.mProperties[mConfigManager.ITEM_CMD_FILE] as? String
        mFilePathTF.text = mConfigManager.mProperties[mConfigManager.ITEM_OPEN_FILE] as? String

        mIsCreatingUI = false
    }

    inner class ConfigManager {
        val CONFIG_FILE = "graphnote.xml"
        var mProperties = Properties()

        val ITEM_FRAME_X = "FRAME_X"
        val ITEM_FRAME_Y = "FRAME_Y"
        val ITEM_FRAME_WIDTH = "FRAME_WIDTH"
        val ITEM_FRAME_HEIGHT = "FRAME_HEIGHT"
        val ITEM_FRAME_EXTENDED_STATE = "FRAME_EXTENDED_STATE"

        val ITEM_CMD_DIR = "CMD_DIR"
        val ITEM_CMD_FILE = "CMD_FILE"

        val ITEM_OPEN_FILE = "OPEM_FILE"

        private fun setDefaultConfig() {
        }

        fun loadConfig() {
            var fileInput: FileInputStream? = null

            try {
                fileInput = FileInputStream(CONFIG_FILE)
                mProperties.loadFromXML(fileInput)
            } catch (ex: Exception) {
                ex.printStackTrace()
                setDefaultConfig()
            } finally {
                if (null != fileInput) {
                    try {
                        fileInput.close()
                    } catch (ex: IOException) {
                    }
                }
            }
        }

        fun saveConfig() {
            var fileOutput: FileOutputStream? = null

            try {
                mProperties[ITEM_FRAME_X] = location.x.toString()
            } catch (e: NullPointerException) {
                mProperties[ITEM_FRAME_X] = "0"
            }

            try {
                mProperties[ITEM_FRAME_Y] = location.y.toString()
            } catch (e: NullPointerException) {
                mProperties[ITEM_FRAME_Y] = "0"
            }

            try {
                mProperties[ITEM_FRAME_WIDTH] = size.width.toString()
            } catch (e: NullPointerException) {
                mProperties[ITEM_FRAME_WIDTH] = "1280"
            }

            try {
                mProperties[ITEM_FRAME_HEIGHT] = size.height.toString()
            } catch (e: NullPointerException) {
                mProperties[ITEM_FRAME_HEIGHT] = "720"
            }

            mProperties[ITEM_FRAME_EXTENDED_STATE] = extendedState.toString()
            mProperties[ITEM_CMD_DIR] = mCmdDirTF.text
            mProperties[ITEM_CMD_FILE] = mCmdCombo.selectedItem?.toString() ?: ""
            mProperties[ITEM_OPEN_FILE] = mFilePathTF.text


            try {
                fileOutput = FileOutputStream(CONFIG_FILE)
                mProperties.storeToXML(fileOutput, "")
            } finally {
                if (null != fileOutput) {
                    try {
                        fileOutput.close()
                    } catch (ex: IOException) {
                    }
                }
            }
        }
    }

    private fun addVSeparator(panel:JPanel) {
        val separator1 = JSeparator(SwingConstants.VERTICAL)
        separator1.preferredSize = Dimension(separator1.preferredSize.width, 20)
        separator1.foreground = Color.DARK_GRAY
        separator1.background = Color.DARK_GRAY
        val separator2 = JSeparator(SwingConstants.VERTICAL)
        separator2.preferredSize = Dimension(separator2.preferredSize.width, 20)
        separator2.background = Color.DARK_GRAY
        separator2.foreground = Color.DARK_GRAY
        panel.add(Box.createHorizontalStrut(5))
        panel.add(separator1)
        panel.add(separator2)
        panel.add(Box.createHorizontalStrut(5))
    }

    internal inner class ActionHandler() : ActionListener {
        override fun actionPerformed(p0: ActionEvent?) {
            if (p0?.source == mItemAbout) {
                val aboutDialog = AboutDialog(this@MainUI)
                aboutDialog.setLocationRelativeTo(this@MainUI)
                aboutDialog.setVisible(true)
            } else if (p0?.source == mItemHelp) {
                val helpDialog = HelpDialog(this@MainUI)
                helpDialog.setLocationRelativeTo(this@MainUI)
                helpDialog.setVisible(true)
            }else if (p0?.source == mCmdStartBtn) {
                val cmd = "${mCmdDirTF.text}/${mCmdCombo.selectedItem.toString()}"
                mGraphViewPane.startGraphCmd(cmd)
            } else if (p0?.source == mCmdStopBtn) {
                mGraphViewPane.stopGraph()
            } else if (p0?.source == mCmdDirBtn) {
                val chooser = JFileChooser()
                chooser.currentDirectory = File(".")
                chooser.dialogTitle = "Cmds Dir"
                chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                chooser.isAcceptAllFileFilterUsed = false

                if (chooser.showOpenDialog(this@MainUI) == JFileChooser.APPROVE_OPTION) {
                    println("getSelectedFile() : " + chooser.selectedFile)
                    mCmdDirTF.text = chooser.selectedFile.absolutePath
                    val dir = File(mCmdDirTF.text)
                    val listFiles = dir.listFiles { param -> param.isFile && param.canExecute() }

                    mCmdCombo.removeAllItems()
                    for (item in listFiles) {
                        mCmdCombo.addItem(item.name)
                    }
                } else {
                    println("No Selection ")
                }
            } else if (p0?.source == mFileStartBtn) {
                mGraphViewPane.startGraphFile(mFilePathTF.text)
            } else if (p0?.source == mFileStopBtn) {
                mGraphViewPane.stopGraph()
            } else if (p0?.source == mFilePathBtn) {
                val fileDialog = FileDialog(this@MainUI, "Data file", FileDialog.LOAD)
                fileDialog.isVisible = true
                if (fileDialog.file != null) {
                    val file = File(fileDialog.directory + fileDialog.file)
                    println("adb command : " + file.absolutePath)
                    mFilePathTF.text = file.absolutePath
                } else {
                    println("Cancel Open")
                }
            }
        }
    }

    internal inner class MouseHandler : MouseAdapter() {
        override fun mouseClicked(p0: MouseEvent?) {
            super.mouseClicked(p0)
        }

        override fun mouseReleased(p0: MouseEvent?) {
            super.mouseReleased(p0)
        }
    }
}
