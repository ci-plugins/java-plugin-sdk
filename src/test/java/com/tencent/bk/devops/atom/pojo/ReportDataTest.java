package com.tencent.bk.devops.atom.pojo;

import com.tencent.bk.devops.atom.common.DataType;
import org.junit.Test;

import static org.junit.Assert.*;

/**
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
        assertEquals(DataType.report, data.getType());
    }
}
