package com.tencent.bk.devops.atom.pojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.tencent.bk.devops.atom.common.DataType;
import java.util.Set;
import org.junit.Test;

/** @version 1.0 */
public class ArtifactDataTest {

  @Test
  public void test() {
    Set<String> values = Sets.newHashSet("artifact1");
    ArtifactData data = new ArtifactData(values);
    assertEquals(values, data.getValue());
    values.forEach(key -> assertTrue(data.getValue().contains(key)));
    assertEquals(DataType.artifact, data.getType());
  }
}
