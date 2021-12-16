package com.tencent.bk.devops.atom.api;

import com.tencent.bk.devops.plugin.api.impl.RepositoryResourceApi;
import com.tencent.bk.devops.plugin.api.impl.WorkbeeOauthApi;
import com.tencent.bk.devops.plugin.pojo.Result;
import com.tencent.bk.devops.plugin.pojo.repository.Repository;
import com.tencent.bk.devops.plugin.pojo.repository.RepositoryType;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

/**
 * @version 1.0
 */
public class SdkEnvTest {

    @BeforeClass
    public static void setUp() throws IOException {
        String dirKey = "user.dir";
        String dataDir = System.getProperty(dirKey) + "/target/test-classes/";
        System.setProperty(dirKey, dataDir);
        SdkEnv.init();
    }

    @Test
    public void getSdkHeader() {
        RepositoryResourceApi repositoryResourceApi = new RepositoryResourceApi();
        Result<Repository> result = repositoryResourceApi.get(RepositoryType.NAME, null, "bkdevops/bk-ci");
        System.out.println(result);
    }

    @Test
    public void genUrl() {
        String s = SdkEnv.genUrl("x1/2/3");
        System.out.println(s);
        String s1 = SdkEnv.genUrl("/x1/2/3");
        System.out.println(s1);
        assert s.equals(s1);

    }

    @Test
    public void getWorkbeeOauth(){
        WorkbeeOauthApi workbeeOauthApi=new WorkbeeOauthApi();
        Result<Map<String, String>> oauth = workbeeOauthApi.getOauth("101144");
        System.out.println(oauth);
        System.out.println(oauth.getData());
    }
}
