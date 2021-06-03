package com.tencent.bk.devops.atom.pojo;

import com.tencent.bk.devops.atom.common.DataType;
import com.tencent.bk.devops.atom.common.ReportType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 报告数据单元测试类
 *
 * @version 1.0
 */
public class ReportDataTest {

    @Test
    public void test() {
        String label = "la1";
        String path = "/data/1";
        String target = "xxx.tgz";
        ReportData data = new ReportData(label, path, target);
        assertEquals(label, data.getLabel());
        assertEquals(path, data.getPath());
        assertEquals(target, data.getTarget());
        assertEquals(ReportType.INTERNAL, data.getReportType());
        assertEquals(DataType.report, data.getType());
        data = ReportData.createLocalReport(label, path, target);
        assertEquals(label, data.getLabel());
        assertEquals(path, data.getPath());
        assertEquals(target, data.getTarget());
        assertEquals(ReportType.INTERNAL, data.getReportType());
        assertEquals(DataType.report, data.getType());

        String url = "http://www.tencent.com";
        data = ReportData.createUrlReport(label, url);
        assertEquals(label, data.getLabel());
        assertEquals(url, data.getUrl());
        assertEquals(ReportType.THIRDPARTY, data.getReportType());
        assertEquals(DataType.report, data.getType());
        data = new ReportData(label, url);
        assertEquals(label, data.getLabel());
        assertEquals(url, data.getUrl());
        assertEquals(ReportType.THIRDPARTY, data.getReportType());
        assertEquals(DataType.report, data.getType());
    }
}
