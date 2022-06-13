package com.tencent.bk.devops.atom.api;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

/** @version 1.0 */
public class SdkEnvTest {

  @BeforeClass
  public static void setUp() throws IOException {
    String dirKey = "user.dir";
    String dataDir = System.getProperty(dirKey) + "/target/test-classes/";
    System.setProperty(dirKey, dataDir);
    SdkEnv.init();
  }

  @Test
  public void genUrl() {
    String s = SdkEnv.genUrl("x1/2/3");
    System.out.println(s);
    String s1 = SdkEnv.genUrl("/x1/2/3");
    System.out.println(s1);
    assert s.equals(s1);
  }
}
