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
import okhttp3.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class NotifyApi extends BaseApi {


    private static OkHttpClient client = new OkHttpClient
            .Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();
    private static Map<String, String> headers=new HashMap<>();
    /**
     *  发送邮件
     * @param receivers 接收人（多个接收人用英文,号或;号间隔组成字符串）
     * @param ccs       抄送人
     * @param title     邮件的标题
     * @param body      邮件内容
     * @return  Result类
     * @throws IOException
     */
    public Result sendMail(String receivers, String ccs, String title, String body) throws IOException {
        Set<String> Receivers = stringToSet(receivers);
        Set<String> ccsSet = stringToSet(ccs);
        EmailNotifyMessage emailNotifyMessage=new EmailNotifyMessage();
        emailNotifyMessage.addAllReceivers(Receivers);
        emailNotifyMessage.addAllCcs(ccsSet);
        emailNotifyMessage.setTitle(title);
        emailNotifyMessage.setBody(body);
        String rtx = JsonUtil.toJson(emailNotifyMessage);
        Response response = doPost("/notify/api/build/notifies/email", rtx);
        if(response.isSuccessful()){
            return new Result(0,"Email is deliveried successfully!");
        }else{
            return new Result(response.code(),response.body().string());
        }
    }

    /**
     * 发短信
     * @param receivers  接收人（多个接收人用,号或;号间隔组成字符串）
     * @param body       短信内容
     * @return Result类
     * @throws IOException
     */
    public Result sendMessage(String receivers, String body) throws IOException {
        Set<String> Receivers = stringToSet(receivers);
        SmsNotifyMessage smsNotifyMessage = new SmsNotifyMessage();
        smsNotifyMessage.addAllReceivers(Receivers);
        smsNotifyMessage.setBody(body);
        String rtx = JsonUtil.toJson(smsNotifyMessage);
        Response response = doPost("/notify/api/build/notifies/sms", rtx);
        if(response.isSuccessful()){
            return new Result(0,"Email is deliveried successfully!");
        }else{
            return new Result(response.code(),response.body().string());
        }
    }

    /**
     * 发送企业微信到个人
     * @param receivers  接收人（多个接收人用,号或;号间隔组成字符串）
     * @param title      标题
     * @param body       内容
     * @return Result类
     * @throws IOException
     */
    public Result sendEnterPriseWechat(String receivers, String title,String body) throws IOException {
        Set<String> Receivers = stringToSet(receivers);
        RtxMessage rtxMessage = new RtxMessage();
        rtxMessage.setSender("");
        rtxMessage.setPriority(EnumNotifyPriority.LOW);
        rtxMessage.setSource(EnumNotifySource.BUSINESS_LOGIC);
        rtxMessage.setTitle(title);
        rtxMessage.setBody(body);
        rtxMessage.setReceivers(Receivers);
        String rtx = JsonUtil.toJson(rtxMessage);
        Response response = doPost("/notify/api/build/notifies/rtx", rtx);
        if(response.isSuccessful()){
            return new Result(0,"Email is deliveried successfully!");
        }else{
            return new Result(response.code(),response.body().string());
        }
    }

    /**
     * 发送微信
     * @param receivers   接收人（多个接收人用,号或;号间隔组成字符串）
     * @param body        内容
     * @return Result类
     * @throws IOException
     */
    public Result sendWechat(String receivers,String body) throws IOException {
        Set<String> Receivers = stringToSet(receivers);
        WechatNotifyMessage wechatNotifyMessage = new WechatNotifyMessage();
        wechatNotifyMessage.addAllReceivers(Receivers);
        wechatNotifyMessage.setBody(body);
        String wechat = JsonUtil.toJson(wechatNotifyMessage);

        Response response = doPost("/notify/api/build/notifies/wechat",wechat);
        if(response.isSuccessful()){
            return new Result(0,"Email is deliveried successfully!");
        }else{
            return new Result(response.code(),response.body().string());
        }
    }

    private Response doPost(String path, String jsonString) throws IOException {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
        Request request = super.buildPost(path, requestBody, headers);
        Call call = client.newCall(request);
        return call.execute();
    }

    private Response doGet(String path) throws IOException {
        Request request = super.buildGet(path);
        Call call = client.newCall(request);
        return  call.execute();
    }

    private Set<String> stringToSet(String receivers) {
        if(receivers!=null){
            receivers = receivers.replaceAll("\\[|\\]|\\s+|\"", "");
            String[] rs = receivers.split(",");
            Set<String> set = new HashSet<>(Arrays.asList(rs));
            return set;
        }else{
            return new HashSet<>();
        }
    }
}
