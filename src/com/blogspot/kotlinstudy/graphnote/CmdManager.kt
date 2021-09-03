package com.blogspot.kotlinstudy.graphnote

import java.io.IOException

class CmdManager private constructor(){
    var mCmd = "adb"

    companion object {
        const val CMD_START = 1

        private val mInstance: CmdManager = CmdManager()

        fun getInstance(): CmdManager {
            return mInstance
        }
    }

    fun stop() {
        println("Stop process")
        mProcessCmd?.destroy()
        mProcessCmd = null
        mCurrentExecutor?.interrupt()
        mCurrentExecutor = null
    }

    fun start(cmd: String) {
        mCmd = cmd
        execute(makeExecutor(CMD_START))
    }

    private var mCurrentExecutor:Thread? = null
    var mProcessCmd:Process? = null
    private fun execute(cmd:Runnable?) {
        cmd?.run()
    }

    private fun makeExecutor(cmdNum:Int) :Runnable? {
        var executer:Runnable? = null
        when (cmdNum) {
            CMD_START -> executer = Runnable {
                run {
                    mProcessCmd?.destroy()

                    val runtime = Runtime.getRuntime()
                    try {
                        println ("run : $mCmd")
                        mProcessCmd = runtime.exec(mCmd)
                    } catch (e:IOException) {
                        println("Failed run $mCmd")
                        mProcessCmd = null
                        return@run
                    }
                }
            }
        }

        return executer
    }
}