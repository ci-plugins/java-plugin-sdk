package com.tencent.bk.devops.atom;

import com.tencent.bk.devops.atom.api.SdkEnv;
import com.tencent.bk.devops.atom.common.Constants;
import com.tencent.bk.devops.atom.pojo.AtomBaseParam;
import com.tencent.bk.devops.atom.pojo.DataField;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import com.tencent.bk.devops.plugin.api.impl.KubernetesBuildApi;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.Assert.*;

public class AtomFileIoTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private String originalDir;
    private String originalDataKey;
    private String originalInputKey;
    private String originalOutputKey;
    private Object originalSdkEnv;
    private Field sdkInstance;
    private Path directory;

    @Before
    public void setUp() throws Exception {
        originalDir = System.getProperty("user.dir");
        originalDataKey = Constants.DATA_DIR_ENV;
        originalInputKey = Constants.INPUT_FILE_ENV;
        originalOutputKey = Constants.OUTPUT_FILE_ENV;
        sdkInstance = SdkEnv.class.getDeclaredField("instance");
        sdkInstance.setAccessible(true);
        originalSdkEnv = sdkInstance.get(null);
        String unset = "SDK_FILE_TEST_" + UUID.randomUUID().toString().replace("-", "");
        Constants.DATA_DIR_ENV = unset + "_DIR";
        Constants.INPUT_FILE_ENV = unset + "_INPUT";
        Constants.OUTPUT_FILE_ENV = unset + "_OUTPUT";
        directory = folder.newFolder("workspace").toPath();
        System.setProperty("user.dir", directory.toString());
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty("user.dir", originalDir);
        Constants.DATA_DIR_ENV = originalDataKey;
        Constants.INPUT_FILE_ENV = originalInputKey;
        Constants.OUTPUT_FILE_ENV = originalOutputKey;
        sdkInstance.set(null, originalSdkEnv);
    }

    @Test
    public void readsUtf8ParametersThroughBothEntryPoints() throws Exception {
        String name = "caf\u00e9 \u97d3\ud83d\ude00";
        Files.writeString(directory.resolve("input.json"), "{\"BK_CI_PROJECT_NAME\":\"" + name + "\"}",
                StandardCharsets.UTF_8);
        AtomContext<AtomBaseParam> context = new AtomContext<>(AtomBaseParam.class);
        assertEquals(name, context.getParam().getProjectName());
        assertEquals(name, context.getAllParameters().get("BK_CI_PROJECT_NAME"));
    }

    @Test(expected = IOException.class)
    public void missingInputStillFails() throws Exception {
        new AtomContext<>(AtomBaseParam.class);
    }

    @Test
    public void createsMissingParentDirectoryForOutput() throws Exception {
        AtomContext<AtomBaseParam> context = context();
        Files.delete(directory.resolve("input.json"));
        Files.delete(directory);
        context.persistent();
        assertEquals(JsonUtil.toJson(context.getResult()),
                Files.readString(directory.resolve("output.json"), StandardCharsets.UTF_8));
    }

    @Test
    public void overwritesExistingOutputWithUtf8() throws Exception {
        AtomContext<AtomBaseParam> context = context();
        context.getResult().setMessage("\u97d3\ud83d\ude00");
        Files.writeString(directory.resolve("output.json"), "old content".repeat(1000));
        context.persistent();
        assertEquals(JsonUtil.toJson(context.getResult()),
                Files.readString(directory.resolve("output.json"), StandardCharsets.UTF_8));
    }

    @Test
    public void serializationFailureStillWritesEmptyContent() throws Exception {
        AtomContext<AtomBaseParam> context = context();
        context.getResult().getData().put("broken", new BrokenData());
        assertNull(JsonUtil.toJson(context.getResult()));
        Files.writeString(directory.resolve("output.json"), "old content");
        context.persistent();
        assertEquals(0L, Files.size(directory.resolve("output.json")));
    }

    @Test(expected = IOException.class)
    public void directoryAsOutputStillFails() throws Exception {
        AtomContext<AtomBaseParam> context = context();
        Files.createDirectory(directory.resolve("output.json"));
        context.persistent();
    }

    @Test
    public void sdkEnvironmentReadRetainsDefaultCharsetAndDeletesFile() throws Exception {
        Path file = directory.resolve(".sdk.json");
        Files.write(file, "{\"gateway\":\"caf\u00e9.example\"}".getBytes(Charset.defaultCharset()));
        SdkEnv.init();
        assertEquals("caf\u00e9.example", SdkEnv.getGatewayHost());
        assertFalse(Files.exists(file));
    }

    @Test(expected = IOException.class)
    public void missingSdkEnvironmentStillFails() throws Exception {
        SdkEnv.init();
    }

    @Test
    public void kotlinReadRetainsDefaultCharsetAndMissingFileFallback() throws Exception {
        Path file = directory.resolve("input.json");
        Files.write(file, "{\"BK_CI_START_USER_ID\":\"caf\u00e9\"}".getBytes(Charset.defaultCharset()));
        KubernetesBuildApi api = new KubernetesBuildApi();
        Method method = KubernetesBuildApi.class.getDeclaredMethod("getUserId");
        method.setAccessible(true);
        assertEquals("caf\u00e9", method.invoke(api));
        Files.delete(file);
        assertEquals("", method.invoke(api));
    }

    private AtomContext<AtomBaseParam> context() throws IOException {
        Files.writeString(directory.resolve("input.json"), "{}", StandardCharsets.UTF_8);
        return new AtomContext<>(AtomBaseParam.class);
    }

    public static class BrokenData extends DataField {
        public BrokenData() {
            super(null);
        }

        public String getBroken() {
            throw new IllegalStateException("deliberate serialization failure");
        }
    }
}
