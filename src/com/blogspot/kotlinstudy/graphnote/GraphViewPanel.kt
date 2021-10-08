package com.blogspot.kotlinstudy.graphnote

import org.jfree.chart.*
import org.jfree.chart.annotations.XYTextAnnotation
import org.jfree.chart.axis.ValueAxis
import org.jfree.chart.entity.XYItemEntity
import org.jfree.chart.labels.XYToolTipGenerator
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.ui.TextAnchor
import org.jfree.data.UnknownKeyException
import org.jfree.data.general.SeriesException
import org.jfree.data.xy.XYDataItem
import org.jfree.data.xy.XYDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.io.*
import java.util.*
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.table.DefaultTableModel
import kotlin.Comparator
import kotlin.collections.ArrayList


class GraphViewPanel(infoTable: JTable) : JPanel() {
    private var mGraphPane: ChartPanel
    private var mGraphSlider: JSlider
    private var mSeriesCollection: XYSeriesCollection
    private var mChart: JFreeChart
    private lateinit var mDomainAxis: ValueAxis
    private lateinit var mPlot: XYPlot
    private var mXRange = 80.0
    private var mXRangeMargin = 5.0
    private var mXStart = 0.0
    private var mUpdateInterval = 100
    private val mChangeHandler = ChangeHandler()
    private val mCmdManager = CmdManager.getInstance()
    private val mInfoTable = infoTable
    private var mMinYVal = 1.0
    private var mMinAnnotation = 5.0
    private var mStartXVal = 0.0
    private var mPrevX = 0.0

    init {
        layout = BorderLayout()

        mSeriesCollection = XYSeriesCollection()
        mChart = createChartXY(mSeriesCollection)
        mGraphPane = ChartPanel(mChart)
        mGraphPane.setMouseZoomable(true)
        mGraphPane.isRangeZoomable = true

        mGraphPane.addChartMouseListener(ChartMouseObserver())
        mGraphSlider = JSlider()
        mGraphSlider.addChangeListener(mChangeHandler)
        mGraphSlider.minimum = 0
        mGraphSlider.maximum = 0
        mGraphSlider.value = mXStart.toInt()

        add(mGraphPane, BorderLayout.CENTER)
        add(mGraphSlider, BorderLayout.SOUTH)
    }

    private fun createChartXY(dataset: XYDataset): JFreeChart {
        val chart: JFreeChart = ChartFactory.createXYLineChart(
            null,
            "",
            "",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        )

        mPlot = chart.plot as XYPlot
        mPlot.isDomainPannable = true
        mPlot.isRangePannable =true

        mDomainAxis = mPlot.domainAxis
        mDomainAxis.isAutoRange = false
        mDomainAxis.setRange(mXStart, mXStart + mXRange)
        println("createChartXY - Domain range " + mXStart + ", " + (mXStart + mXRange))

        val renderer = mPlot.getRenderer(0) as XYLineAndShapeRenderer
        renderer.useFillPaint = true
        renderer.useOutlinePaint = true
        renderer.defaultShape = Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0)
        renderer.defaultShapesFilled = true
        renderer.defaultShapesVisible = true
        renderer.defaultStroke = BasicStroke(3.0F)
        renderer.autoPopulateSeriesStroke = false

        val xyToolTipGenerator =
            XYToolTipGenerator { dataset, series, item ->
                val x1 = dataset.getX(series, item)
                val y1 = dataset.getY(series, item)
                val stringBuilder = StringBuilder()
                stringBuilder.append(
                    String.format(
                        "<html><p style='color:#0000ff;'>%s",
                        dataset.getSeriesKey(series)
                    )
                )
                stringBuilder.append("[$x1, $y1]")
                val customItem = ((dataset as XYSeriesCollection).getSeries(series)  as XYSeries).getDataItem(item) as CustomXYDataItem
                if (customItem.mDesc.isNotEmpty()) {
                    stringBuilder.append(String.format("<br/>%s", customItem.mDesc))
                }
                stringBuilder.append("</p></html>")
                stringBuilder.toString()
            }

        renderer.defaultToolTipGenerator = xyToolTipGenerator
        return chart
    }

    private var mUpdateGraphThread:Thread? = null
    fun startGraphCmd(cmd: String) {
        println("Cmd $cmd")
        if (mUpdateGraphThread == null) {
            mSeriesCollection.removeAllSeries()
            mUpdateGraphThread = Thread(Runnable {
                run {
                    try {
                        mCmdManager.start(cmd)
                        val inputReader = BufferedReader(InputStreamReader(mCmdManager.mProcessCmd?.inputStream))

                        var dataLine: String? = ""
                        var firstLine = ""
                        var secondLine = ""
                        mStartXVal = 0.0
                        mPrevX = 0.0
                        while (mCmdManager.mProcessCmd?.isAlive == true) {
                            dataLine = inputReader.readLine()
                            if (dataLine == null || dataLine.isEmpty()) {
                                Thread.sleep(mUpdateInterval.toLong())
                                continue
                            }

                            if (firstLine.isEmpty()) {
                                firstLine = dataLine
                                println("First Line : $firstLine")
                            } else if (secondLine.isEmpty()) {
                                secondLine = dataLine
                                println("Second Line : $secondLine")
                            }

                            if (firstLine.isNotEmpty() && secondLine.isNotEmpty()) {
                                setGraphInfo(firstLine, secondLine)
                                break
                            }
                        }

                        while (mCmdManager.mProcessCmd?.isAlive == true) {
                            dataLine = inputReader.readLine()
                            if (dataLine == null || dataLine.isEmpty()) {
                                Thread.sleep(mUpdateInterval.toLong())
                                continue
                            }
                            updateGraph(dataLine)
                        }

                        val errorReader = BufferedReader(InputStreamReader(mCmdManager.mProcessCmd?.errorStream))
                        while (true) {
                            var errorLine = errorReader.readLine()
                            if (errorLine == null) {
                                break
                            }
                            println(errorLine)
                        }
                    } catch (e: FileNotFoundException) {
                        System.err.println(e)
                    } catch (e: IOException) {
                        System.err.println(e)
                    } finally {
                        println("Process exit ")
                        mUpdateGraphThread = null
                    }
                }
            })

            mUpdateGraphThread?.start()
        }
    }

    fun startGraphFile(file: String) {
        println("file $file")
        if (mUpdateGraphThread == null) {
            mSeriesCollection.removeAllSeries()
            mUpdateGraphThread = Thread(Runnable {
                run {
                    try {
                        val inputReader = BufferedReader(FileReader(File(file)))

                        var dataLine: String? = ""
                        var firstLine = ""
                        var secondLine = ""
                        mStartXVal = 0.0
                        mPrevX = 0.0
                        while (true) {
                            dataLine = inputReader.readLine()
                            if (firstLine.isEmpty()) {
                                firstLine = dataLine
                                println("First Line : $firstLine")
                            } else if (secondLine.isEmpty()) {
                                secondLine = dataLine
                                println("Second Line : $secondLine")
                            }
                            if (firstLine.isNotEmpty() && secondLine.isNotEmpty()) {
                                setGraphInfo(firstLine, secondLine)
                                break
                            }
                        }

                        val mainUI = SwingUtilities.windowForComponent(this) as MainUI
                        val autoRefresh = mainUI.mFileRefreshBtn.isSelected
                        while (true) {
                            dataLine = inputReader.readLine()
                            if (dataLine == null || dataLine.isEmpty()) {
                                if (!autoRefresh) {
                                    break
                                }
                                Thread.sleep(mUpdateInterval.toLong())
                                continue
                            }
                            updateGraph(dataLine)
                        }

                    } catch (e: FileNotFoundException) {
                        System.err.println(e)
                    } catch (e: IOException) {
                        System.err.println(e)
                    } finally {
                        println("Process exit ")
                        mUpdateGraphThread = null
                    }
                }
            })

            mUpdateGraphThread?.start()
        }
    }

    private fun updateGraph(dataLine: String) {
        var dataItems = dataLine.split("|")
        var xVal = dataItems[0].toDouble()
        if (mStartXVal == 0.0) {
            mStartXVal = xVal
        }

        xVal -= mStartXVal
        for (dataItem in dataItems) {
            val itemValues = dataItem.split("#")
            if (itemValues.size != 2 && itemValues.size != 3) {
                continue
            }

            if (itemValues[1].toFloat() < mMinYVal) {
                continue
            }
            var seriesNum = 0
            var series: XYSeries? = null
            try {
                series = mSeriesCollection.getSeries(itemValues[0])
            } catch (ex : UnknownKeyException) {
                series = null
            }

            if (series != null) {
                while (true) {
                    seriesNum++
                    var seriesNext: XYSeries? = null
                    try {
                        seriesNext = mSeriesCollection.getSeries(itemValues[0] + "#" + seriesNum)
                    } catch (ex : UnknownKeyException) {
                        seriesNext = null
                    }
                    if (seriesNext == null) {
                        if (series!!.maxX != mPrevX) {
                            series = XYSeries(itemValues[0] + "#" + seriesNum, true, false)
                            mSeriesCollection.addSeries(series)
                        }
                        break
                    }
                    series = seriesNext
                }
            }
            else {
                series = XYSeries(itemValues[0], true, false)
                mSeriesCollection.addSeries(series)
            }
            try {
                if (itemValues.size == 3) {
                    series!!.add(CustomXYDataItem(xVal, itemValues[1].toDouble(), itemValues[2]))
                } else {
                    series!!.add(CustomXYDataItem(xVal, itemValues[1].toDouble(), ""))
                }
            } catch (ex: SeriesException) {
                ex.printStackTrace()
            }
        }
        mPrevX = xVal

        val range = mSeriesCollection.getDomainBounds(false)
        if (range != null) {
            mGraphSlider.minimum = range.lowerBound.toInt()
            var max = range.upperBound.toInt()
            if (max > mXRange) {
                max -= (mXRange - mXRangeMargin).toInt()
            }

            mChangeHandler.active = false
            if (mGraphSlider.maximum == mGraphSlider.value) {
                mGraphSlider.maximum = max
                mGraphSlider.value = max
                mXStart = range.upperBound - mXRange + mXRangeMargin
                if (mXStart < 0) {
                    mXStart = 0.0
                }
            }
            else {
                mGraphSlider.maximum = max
            }
            mChangeHandler.active = true
        }

        mDomainAxis.setRange(mXStart, mXStart + mXRange)
        println("updateGraph - Domain range " + mXStart + ", " + (mXStart + mXRange))

        var annotation: XYTextAnnotation? = null
        val font = Font("SansSerif", Font.PLAIN, 12)

        mPlot.renderer.removeAnnotations()
        for (i in 0 until mSeriesCollection.seriesCount) {
            val series = mSeriesCollection.getSeries(i)
            if (series.itemCount > 0 && series.getY(series.itemCount - 1).toDouble() > mMinAnnotation) {
                annotation = XYTextAnnotation(
                    series.key as String,
                    series.getX(series.itemCount - 1).toDouble() + 0.05,
                    series.getY(series.itemCount - 1).toDouble()
                )
                annotation.font = font
                annotation.textAnchor = TextAnchor.HALF_ASCENT_LEFT
                mPlot.renderer.addAnnotation(annotation)
            }
        }
    }

    private fun setGraphInfo(firstLine: String, secondLine: String) {
        if (firstLine.startsWith("TITLE")) {
            var title = firstLine.substring(6).trim()
            val frame = SwingUtilities.windowForComponent(this) as MainUI
            if (title.isNotEmpty()) {
                frame.mGraphTitleTF.text = title
            }
            else {
                frame.mGraphTitleTF.text = "No Title"
                println("setGraphInfo : There are no title")
            }
        }

        if (secondLine.startsWith("SETTINGS")) {
            var items = secondLine.split("|")
            if (items.size >= 4) {
                mPlot.rangeAxis.label = items[1]
                mPlot.domainAxis.label = items[2]
                mXRange = items[3].toDouble()
                if (items.size >= 5) {
                    mMinYVal = items[4].toDouble()
                }
                if (items.size >= 6) {
                    mMinAnnotation = items[5].toDouble()
                }
            }
            else {
                println("setGraphInfo : There are no settings")
            }
        }
    }

    fun stopGraph() {
        if (mUpdateGraphThread?.isAlive == true) {
            mUpdateGraphThread?.interrupt()
            mCmdManager.stop()
            mUpdateGraphThread = null
        }
    }

    internal inner class ChangeHandler() : ChangeListener {
        var active = true
        override fun stateChanged(e: ChangeEvent?) {
            if (active) {
                println("TEST TEST " + (mGraphSlider.maximum - mGraphSlider.minimum)  + ", " + mXRange)
                if ((mGraphSlider.maximum - mGraphSlider.minimum) > mXRange) {
                    val value: Int = mGraphSlider.value
                    mXStart = value.toDouble()
                    mDomainAxis.setRange(mXStart, mXStart + mXRange)
                }
                else {
                    mGraphSlider.value = mGraphSlider.maximum
                }
                println("stateChanged - Domain range " + mXStart + ", " + (mXStart + mXRange))
            }
            else {
                println("stateChanged - disabled")
            }
        }
    }

    internal inner class ChartMouseObserver : ChartMouseListener {
        internal inner class TableItem(key: String, value: Double) {
            val mKey = key
            val mValue = value
        }
        override fun chartMouseClicked(event: ChartMouseEvent) {
            var entity = event.entity
            val point2d: Point2D = Point2D.Double(
                ((event.trigger.x - mGraphPane.insets.left) / mGraphPane.scaleX),
                ((event.trigger.y - mGraphPane.insets.top) / mGraphPane.scaleY)
            )
            var minDistance = Int.MAX_VALUE.toDouble()
            val entities: Collection<*> = mGraphPane.chartRenderingInfo.entityCollection.entities

            if (entity !is XYItemEntity) {
                for (item in entities) {
                    if (item is XYItemEntity) {
                        val rect: Rectangle = item.area.bounds
                        val centerPoint: Point2D = Point2D.Double(rect.centerX, rect.centerY)
                        if (point2d.distance(centerPoint) < minDistance) {
                            minDistance = point2d.distance(centerPoint)
                            entity = item
                        }
                    }
                }
            }

            if (entity is XYItemEntity) {
                val xVal = entity.dataset.getX(entity.seriesIndex, entity.item)
                val tableModel = mInfoTable.model as DefaultTableModel
                while (tableModel.rowCount > 0) {
                    tableModel.removeRow(0)
                }

                mInfoTable.tableHeader.columnModel.getColumn(1).headerValue = xVal
                mInfoTable.tableHeader.repaint()

                val itemList: ArrayList<TableItem> = ArrayList<TableItem>()
                for (series in mSeriesCollection.series) {
                    for (item in (series as XYSeries).items) {
                        if ((item as XYDataItem).xValue == xVal) {
                            itemList.add(TableItem(series.key as String, item.yValue))
                            break
                        }
                    }
                }


                Collections.sort(itemList,
                    Comparator<TableItem> { item1, item2 -> item2.mValue.compareTo(item1.mValue) })

                for (item in itemList) {
                    tableModel.addRow(arrayOf(item.mKey, item.mValue))
                }
            }
        }

        override fun chartMouseMoved(event: ChartMouseEvent) {}
    }

    class CustomXYDataItem(x: Double, y: Double, desc: String) : XYDataItem(x, y) {
        var mDesc = desc
    }
}