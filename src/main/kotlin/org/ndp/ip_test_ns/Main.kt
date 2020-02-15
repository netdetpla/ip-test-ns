package org.ndp.ip_test_ns

import org.ndp.ip_test_ns.bean.IP
import org.ndp.ip_test_ns.bean.MQResult
import org.ndp.ip_test_ns.utils.Logger.logger
import org.ndp.ip_test_ns.utils.OtherTools
import org.ndp.ip_test_ns.utils.RedisHandler
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object Main {

    private val task = RedisHandler.consumeTaskParam(
        RedisHandler.generateNonce(5)
    )

    private fun parseParam() {
        val param = task!!.param
        val input = File("/input_file")
        logger.debug("params: ")
        logger.debug(param)
        val ips = ArrayList<String>()
        for (i in param.split(",")) {
            when {
                i.contains('-') -> ips.addAll(OtherTools.splitINetSegment(i))
                i.contains('/') -> ips.addAll(OtherTools.splitMaskedINet(i))
                else -> ips.add(i)
            }
        }
        input.writeText(ips.joinToString("\n"))
    }

    private fun execute(): List<IP> {
        logger.info("ping start")
        val fpingBuilder = ProcessBuilder(
            ("fping -b 64 -f input_file -H 64 -e -a").split(" ")
        )
        val fping = fpingBuilder.start()
        fping.waitFor()
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(fping.inputStream.available())
        if (buffer.isEmpty()) return ArrayList()
        var length: Int
        while (fping.inputStream.read(buffer).also { length = it } != -1) {
            result.write(buffer, 0, length)
        }
        logger.info("ping end")
        val reg = "([.0-9]+) \\(([0-9]+).*"
        val matcher = Pattern.compile(reg)
        val results = ArrayList<IP>()
        for (r in result.toString(StandardCharsets.UTF_8.name()).split("\n")) {
            val groups = matcher.matcher(r)
            while (groups.find()) {
                results.add(
                    IP(
                        groups.group(1),
                        Integer.parseInt(groups.group(2))
                    )
                )
            }
        }
        return results
    }

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("ip-test started")
        if (task == null || task.taskID == 0) {
            logger.warn("no task, exiting...")
            return
        }
        // 执行
        try {
            parseParam()
            // 获取配置
            val results = execute()
            RedisHandler.produceResult(
                MQResult(task.taskID, results, 0, "")
            )
            // 写结果
        } catch (e: Exception) {
            logger.error(e.toString())
            val stringWriter = StringWriter()
            e.printStackTrace(PrintWriter(stringWriter))
            RedisHandler.produceResult(
                MQResult(task.taskID, ArrayList(), 1, stringWriter.buffer.toString())
            )
        }
        // 结束
        logger.info("ip-test end successfully")
    }
}