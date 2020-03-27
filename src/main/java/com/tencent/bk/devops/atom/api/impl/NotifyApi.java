package com.tencent.bk.devops.atom.api.impl;

import com.tencent.bk.devops.atom.api.BaseApi;
import com.tencent.bk.devops.atom.pojo.notify.EnumNotifyPriority;
import com.tencent.bk.devops.atom.pojo.notify.EnumNotifySource;
import com.tencent.bk.devops.atom.pojo.notify.RtxMessage;
import com.tencent.bk.devops.atom.pojo.Result;
import com.tencent.bk.devops.atom.utils.json.JsonUtil;
import com.tencent.bk.devops.plugin.pojo.notify.EmailNotifyMessage;
import com.tencent.bk.devops.plugin.pojo.notify.SmsNotifyMessage;
import com.tencent.bk.devops.plugin.pojo.notify.WechatNotifyMessage;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NotifyApi extends BaseApi {

    private static OkHttpClient client = new OkHttpClient
        .Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build();

    private static Map<String, String> headers = new HashMap<>();

    /**
     * 发送邮件
     *
     * @param receivers 接收人（多个接收人用英文,号或;号间隔组成字符串）
     * @param ccs       抄送人
     * @param title     邮件的标题
     * @param body      邮件内容
     * @return Result类
     * @throws IOException
     */
    public Result sendMail(String receivers, String ccs, String title, String body) throws IOException {
        Set<String> Receivers = stringToSet(receivers);
        Set<String> ccsSet = stringToSet(ccs);
        EmailNotifyMessage emailNotifyMessage = new EmailNotifyMessage();
        emailNotifyMessage.addAllReceivers(Receivers);
        emailNotifyMessage.addAllCcs(ccsSet);
        emailNotifyMessage.setTitle(title);
        emailNotifyMessage.setBody(body);
        String email = JsonUtil.toJson(emailNotifyMessage);
        try (Response response = doPost("/notify/api/build/notifies/email", email)) {
            if (response.isSuccessful()) {
                return new Result(0, "Email is deliveried successfully!");
            } else {
                return new Result(response.code(), response.body() != null ? response.body().string() : null);
            }
        }
    }

    /**
     * 发短信
     *
     * @param receivers 接收人（多个接收人用,号或;号间隔组成字符串）
     * @param body      短信内容
     * @return Result类
     * @throws IOException
     */
    public Result sendMessage(String receivers, String body) throws IOException {
        Set<String> Receivers = stringToSet(receivers);
        SmsNotifyMessage smsNotifyMessage = new SmsNotifyMessage();
        smsNotifyMessage.addAllReceivers(Receivers);
        smsNotifyMessage.setBody(body);
        String sms = JsonUtil.toJson(smsNotifyMessage);
        try (Response response = doPost("/notify/api/build/notifies/sms", sms)) {
            if (response.isSuccessful()) {
                return new Result(0, "Sms is deliveried successfully!");
            } else {
                return new Result(response.code(), response.body() != null ? response.body().string() : null);
            }
        }
    }

    /**
     * 发送企业微信到个人
     *
     * @param receivers 接收人（多个接收人用,号或;号间隔组成字符串）
     * @param title     标题
     * @param body      内容
     * @return Result类
     * @throws IOException
     */
    public Result sendEnterPriseWechat(String receivers, String title, String body) throws IOException {
        Set<String> Receivers = stringToSet(receivers);
        RtxMessage rtxMessage = new RtxMessage();
        rtxMessage.setSender("");
        rtxMessage.setPriority(EnumNotifyPriority.LOW);
        rtxMessage.setSource(EnumNotifySource.BUSINESS_LOGIC);
        rtxMessage.setTitle(title);
        rtxMessage.setBody(body);
        rtxMessage.setReceivers(Receivers);
        String rtx = JsonUtil.toJson(rtxMessage);
        try (Response response = doPost("/notify/api/build/notifies/rtx", rtx)) {
            if (response.isSuccessful()) {
                return new Result(0, "RTX is deliveried successfully!");
            } else {
                return new Result(response.code(), response.body() != null ? response.body().string() : null);
            }
        }
    }

    /**
     * 发送微信
     *
     * @param receivers 接收人（多个接收人用,号或;号间隔组成字符串）
     * @param body      内容
     * @return Result类
     * @throws IOException
     */
    public Result sendWechat(String receivers, String body) throws IOException {
        Set<String> Receivers = stringToSet(receivers);
        WechatNotifyMessage wechatNotifyMessage = new WechatNotifyMessage();
        wechatNotifyMessage.addAllReceivers(Receivers);
        wechatNotifyMessage.setBody(body);
        String wechat = JsonUtil.toJson(wechatNotifyMessage);
        try (Response response = doPost("/notify/api/build/notifies/wechat", wechat)) {
            if (response.isSuccessful()) {
                return new Result(0, "Wechat is deliveried successfully!");
            } else {
                return new Result(response.code(), response.body() != null ? response.body().string() : null);
            }
        }
    }

    private Response doPost(String path, String jsonString) throws IOException {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
        Request request = super.buildPost(path, requestBody, headers);
        Call call = client.newCall(request);
        return call.execute();
    }

    private Set<String> stringToSet(String receivers) {
        if (receivers != null) {
            receivers = receivers.replaceAll("\\[|\\]|\\s+|\"", "");
            String[] rs = receivers.split(",");
            return new HashSet<>(Arrays.asList(rs));
        } else {
            return new HashSet<>();
        }
    }
}
