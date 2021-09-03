package com.blogspot.kotlinstudy.graphnote

import org.jfree.chart.*
import org.jfree.chart.annotations.XYTextAnnotation
import org.jfree.chart.axis.ValueAxis
import org.jfree.chart.entity.XYItemEntity
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
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Font
import java.awt.Rectangle
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.io.*
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.JTable
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.table.DefaultTableModel


class GraphViewPanel(infoTable: JTable) : JPanel() {
    private var mGraphPane: ChartPanel
    private var mGraphSlider: JSlider
    private var mSeriesCollection: XYSeriesCollection
    private var mChart: JFreeChart
    private lateinit var mDomainAxis: ValueAxis
    private lateinit var mPlot: XYPlot
    private var mXRange = 80.0
    private var mXRangeMargin = 10.0
    private var mXStart = 0.0
    private var mUpdateInterval = 100
    private val mChangeHandler = ChangeHandler()
    private val mCmdManager = CmdManager.getInstance()
    private val mInfoTable = infoTable

    init {
        layout = BorderLayout()

        mSeriesCollection = XYSeriesCollection()
        mChart = createChartXY(mSeriesCollection)
        mGraphPane = ChartPanel(mChart)
        mGraphPane.addChartMouseListener(object : ChartMouseListener {
            override fun chartMouseClicked(e: ChartMouseEvent) {
                println("AA "+ e.trigger.x + ", " + e.trigger.y + ", width " + mGraphPane.width)
            }

            override fun chartMouseMoved(e: ChartMouseEvent) {}
        })

        class ChartMouseObserver : ChartMouseListener {
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

                    for (series in mSeriesCollection.series) {
                        println(((series as XYSeries).items[0] as XYDataItem).yValue)
                        for (item in series.items) {
                            if ((item as XYDataItem).xValue == xVal) {
                                tableModel.addRow(arrayOf(series.key, item.yValue))
                                break
                            }
                        }
                    }
                }
            }

            override fun chartMouseMoved(event: ChartMouseEvent) {}
        }
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

        val renderer = mPlot.getRenderer(0) as XYLineAndShapeRenderer
        renderer.useFillPaint = true
        renderer.useOutlinePaint = true
        renderer.defaultShape = Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0)
        renderer.defaultShapesFilled = true
        renderer.defaultShapesVisible = true
        renderer.defaultStroke = BasicStroke(3.0F)
        renderer.autoPopulateSeriesStroke = false

        return chart
    }

    private var mUpdateGraphThread:Thread? = null
    fun startGraph(cmd: String) {
        println("Cmd $cmd")
        if (mUpdateGraphThread == null) {
            mSeriesCollection.removeAllSeries()
            mUpdateGraphThread = Thread(Runnable {
                run {
                    try {
                        mCmdManager.start(cmd)
                        val inputReader = BufferedReader(InputStreamReader(mCmdManager.mProcessCmd?.inputStream))

                        var startTime = 0.0
                        var prevX = -1.0
                        var data = ""
                        var isFistLine = true
                        while (mCmdManager.mProcessCmd?.isAlive == true) {
                            data = inputReader.readLine()
                            if (data == null || data.isEmpty()) {
                                Thread.sleep(mUpdateInterval.toLong())
                                continue
                            }

                            if (isFistLine) {
                                println("First Line : $data")
                                var infoItem = data.split("|")
                                if (infoItem.size == 3) {
                                    mPlot.rangeAxis.label = infoItem[0]
                                    mPlot.domainAxis.label = infoItem[1]
                                    mXRange = infoItem[2].toDouble()
                                }

                                isFistLine = false
                                continue
                            }

                            var dataItem = data.split("|")
                            var time = dataItem[0].toDouble()
                            if (startTime == 0.0) {
                                startTime = time
//                                continue
                            }

                            time -= startTime
                            for (item in dataItem) {

                                val procItem = item.split("#")
                                if (procItem.size != 2) {
                                    continue
                                }

                                if (procItem[1].toFloat() < 1.0) {
                                    continue
                                }
                                var seriesNum = 0
                                var series: XYSeries? = null
                                try {
                                    series = mSeriesCollection.getSeries(procItem[0])
                                } catch (ex : UnknownKeyException) {
//                                    ex.printStackTrace()
                                    series = null
                                }
                                if (series != null) {
                                    while (true) {
                                        seriesNum++
                                        var seriesNext: XYSeries? = null
                                        try {
//                                            println("check x = " + time + ", key = " + procItem[0] + "#" + seriesNum + ", maxX = " + series!!.maxX + ", prevX " + prevX)
                                            seriesNext = mSeriesCollection.getSeries(procItem[0] + "#" + seriesNum)
                                        } catch (ex : UnknownKeyException) {
//                                            ex.printStackTrace()
                                            seriesNext = null
                                        }
                                        if (seriesNext == null) {
                                            if (series!!.maxX != prevX) {
//                                                println("add x = " + time + ", key = " + procItem[0] + "#" + seriesNum + ", maxX = " + series!!.maxX + ", prevX " + prevX)
                                                series = XYSeries(procItem[0] + "#" + seriesNum, true, false)
                                                mSeriesCollection.addSeries(series)
                                            }
                                            break
                                        }
                                        series = seriesNext
                                    }
                                }
                                else {
                                    series = XYSeries(procItem[0], true, false)
                                    mSeriesCollection.addSeries(series)
                                }
                                try {
//                                    println("item x = " + time + ", key = " + procItem[0] + "#" + seriesNum + ", maxX = " + series!!.maxX + ", prevX " + prevX)
                                    series!!.add(time, procItem[1].toFloat())
                                } catch (ex: SeriesException) {
                                    ex.printStackTrace()
                                }
                            }
                            prevX = time

                            val range = mSeriesCollection.getDomainBounds(false)
                            mGraphSlider.minimum = range.lowerBound.toInt()
                            var max = range.upperBound.toInt()
                            if (max > mXRange) {
                                max -= (mXRange - mXRangeMargin).toInt()
                            }
                            mGraphSlider.maximum = max
                            mGraphSlider.value = max

                            mXStart = range.upperBound - mXRange + mXRangeMargin
                            if (mXStart < 0) {
                                mXStart = 0.0
                            }

                            mDomainAxis.setRange(mXStart, mXStart + mXRange)

                            var annotation: XYTextAnnotation? = null
                            val font = Font("SansSerif", Font.PLAIN, 12)

                            mPlot.renderer.removeAnnotations()
                            for (i in 0 until mSeriesCollection.seriesCount) {
                                val series = mSeriesCollection.getSeries(i)
                                if (series.itemCount > 0 && series.getY(series.itemCount - 1).toDouble() > 5) {
                                    annotation = XYTextAnnotation(
                                        series.key as String,
                                        series.getX(series.itemCount - 1).toDouble() + 0.05,
                                        series.getY(series.itemCount - 1).toDouble() + 2
                                    )
                                    annotation.font = font
                                    annotation.textAnchor = TextAnchor.HALF_ASCENT_LEFT
                                    mPlot.renderer.addAnnotation(annotation)
                                }
                            }
                        }
                        val errorReader = BufferedReader(InputStreamReader(mCmdManager.mProcessCmd?.errorStream))
                        while (true) {
                            var errorLine = errorReader.readLine()
                            if (errorLine == null) {
                                break
                            }
                            println(errorLine)
                        }
                        println("Process exit ")
                    } catch (e: FileNotFoundException) {
                        System.err.println(e)
                    } catch (e: IOException) {
                        System.err.println(e)
                    }
                }
            })

            mUpdateGraphThread?.start()
        }
    }

    fun stopGraph() {
        if (mUpdateGraphThread?.isAlive == true) {
            mCmdManager.stop()
            mUpdateGraphThread = null
        }
    }

    internal inner class ChangeHandler() : ChangeListener {
        override fun stateChanged(e: ChangeEvent?) {
            val value: Int = mGraphSlider.getValue()
            mXStart = value.toDouble()
            mDomainAxis.setRange(mXStart, mXStart + mXRange)
        }
    }
}