package com.tencent.bk.devops.plugin.pojo.artifactory

import com.tencent.bk.devops.plugin.utils.JacksonUtil

data class JfrogFilesData(
    val uri: String = "",
    val created: String = "",
    val files: List<JfrogFile> = listOf()
)

fun main() {
    val s = "{\n" +
            "  \"uri\" : \"http://bk.artifactory.oa.com:80/api/storage/generic-local/bk-custom/landunplugins\",\n" +
            "  \"created\" : \"2019-08-16T12:16:00.484+08:00\",\n" +
            "  \"files\" : [ {\n" +
            "    \"uri\" : \"/data\",\n" +
            "    \"size\" : -1,\n" +
            "    \"lastModified\" : \"2019-07-24T10:54:25.002+08:00\",\n" +
            "    \"folder\" : true\n" +
            "  }, {\n" +
            "    \"uri\" : \"/data/a.md\",\n" +
            "    \"size\" : 23,\n" +
            "    \"lastModified\" : \"2019-07-24T10:54:25.113+08:00\",\n" +
            "    \"folder\" : false,\n" +
            "    \"sha1\" : \"90b09dcd2199c62f00335f884ebf6667c746cd7c\"\n" +
            "  }]}"
    println(">>>>" + JacksonUtil.createObjectMapper().readValue(s, JfrogFilesData::class.java))
}
