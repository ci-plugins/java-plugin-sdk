package com.tencent.bk.devops.plugin.script

import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteException
import org.apache.commons.exec.ExecuteStreamHandler
import org.apache.commons.exec.Executor
import org.slf4j.LoggerFactory

class CommandLineExecutor : DefaultExecutor() {

    /** the first exception being caught to be thrown to the caller  */
    private var exceptionCaught: IOException? = null

    override fun execute(command: CommandLine, environment: MutableMap<String, String>?): Int {
        if (workingDirectory != null && !workingDirectory.exists()) {
            throw IOException(workingDirectory.toString() + " doesn't exist.")
        }

        return executeInternal(command, environment, workingDirectory, streamHandler)
    }

    /**
     * Execute an internal process. If the executing thread is interrupted while waiting for the
     * child process to return the child process will be killed.
     *
     * @param command the command to execute
     * @param environment the execution environment
     * @param dir the working directory
     * @param streams process the streams (in, out, err) of the process
     * @return the exit code of the process
     * @throws IOException executing the process failed
     */
    private fun executeInternal(
        command: CommandLine,
        environment: Map<String, String>?,
        dir: File,
        streams: ExecuteStreamHandler
    ): Int {

        setExceptionCaught(null)

        val process = this.launch(command, environment, dir)

        try {
            streams.setProcessInputStream(process.outputStream)
            streams.setProcessOutputStream(process.inputStream)
            streams.setProcessErrorStream(process.errorStream)
        } catch (e: IOException) {
            process.destroy()
            throw e
        }

        streams.start()
        val executor = Executors.newSingleThreadExecutor()
        try {

            // add the process to the list of those to destroy if the VM exits
            if (this.processDestroyer != null) {
                this.processDestroyer.add(process)
            }

            // associate the watchdog with the newly created process
            if (watchdog != null) {
                watchdog.start(process)
            }

            var exitValue = Executor.INVALID_EXITVALUE

            try {
                exitValue = process.waitFor()
            } catch (e: InterruptedException) {
                process.destroy()
            } finally {
                // see http://bugs.sun.com/view_bug.do?bug_id=6420270
                // see https://issues.apache.org/jira/browse/EXEC-46
                // Process.waitFor should clear interrupt status when throwing InterruptedException
                // but we have to do that manually
                Thread.interrupted()
            }

            if (watchdog != null) {
                watchdog.stop()
            }

            try {
                val future = executor.submit {
                    try {
                        streams.stop()
                    } catch (e: IOException) {
                        setExceptionCaught(e)
                    }

                    closeProcessStreams(process)
                }
                // Wait 3 minute for stopping the stream
                future.get(3, TimeUnit.MINUTES)
            } catch (t: Throwable) {
                logger.error("Fail to close the stream", t)
            }

            if (getExceptionCaught() != null) {
                throw getExceptionCaught()!!
            }

            if (watchdog != null) {
                try {
                    watchdog.checkException()
                } catch (e: IOException) {
                    throw e
                } catch (e: Exception) {
                    throw IOException(e.message)
                }
            }

            if (this.isFailure(exitValue)) {
                throw ExecuteException("Process exited with an error: " + exitValue, exitValue)
            }

            return exitValue
        } finally {
            // remove the process to the list of those to destroy if the VM exits
            if (this.processDestroyer != null) {
                this.processDestroyer.remove(process)
            }
            executor.shutdownNow()
        }
    }

    /**
     * Close the streams belonging to the given Process.
     *
     * @param process the <CODE>Process</CODE>.
     */
    private fun closeProcessStreams(process: Process) {

        try {
            process.inputStream.close()
        } catch (e: IOException) {
            setExceptionCaught(e)
        }

        try {
            process.outputStream.close()
        } catch (e: IOException) {
            setExceptionCaught(e)
        }

        try {
            process.errorStream.close()
        } catch (e: IOException) {
            setExceptionCaught(e)
        }
    }

    /**
     * Keep track of the first IOException being thrown.
     *
     * @param e the IOException
     */
    private fun setExceptionCaught(e: IOException?) {
        if (this.exceptionCaught == null) {
            this.exceptionCaught = e
        }
    }

    /**
     * Get the first IOException being thrown.
     *
     * @return the first IOException being caught
     */
    private fun getExceptionCaught(): IOException? {
        return this.exceptionCaught
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CommandLineExecutor::class.java)
    }
}
