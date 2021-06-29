# 插件市场-插件开发 Java SDK（v1.0.9）

```
适用JDK版本：建议1.8
```



**文档修改记录：**

|  版本  | 修改内容 |
| :----: | :------: |
| v1.0.0 |   新建   |
| v1.0.1 |   jackson由2.9.10.3升级到2.9.10.4   |
| v1.0.2 |   AtomContext参数解析由String对象改成Object对象   |
| v1.0.3 |   JsonUtil转换支持kotlin |
| v1.0.4 |   增加Docker相关api |
| v1.0.5 |   更新jackson到2.12.0 |
| v1.0.6 |   兼容gateway网关地址带协议头情况 |
| v1.0.7 |   guava由19.0升级到30.1.1-jre，okhttp由3.12.0升级到4.9.1，ant由1.10.9升级到1.10.10 |
| v1.0.8 |   增加一些工具类，增加一些docker相关代码
| v1.0.9 |   ReportData 增加指定发送邮件的参数功能



[TOC]





## 一、SDK主要类文件介绍

##### 1、AtomBaseParam.java （流水线插件基础参数类）

参数详细信息如下：

|       参数字段名       |        类型        | 是否必需 |            描述            |
| :--------------------: | :----------------: | :------: | :------------------------: |
|    pipelineVersion     |       string       |    是    |        流水线版本号        |
|      projectName       |       string       |    是    |          项目名称          |
|     projectNameCn      |       string       |    是    |        项目中文名称        |
|       pipelineId       |       string       |    是    |          流水线ID          |
|    pipelineBuildNum    |       string       |    是    |       流水线构建序号       |
|    pipelineBuildId     |       string       |    是    |        流水线构建ID        |
|      pipelineName      |       string       |    是    |         流水线名称         |
| pipelineStartTimeMills |       string       |    是    |    流水线启动时间：毫秒    |
| pipelineStartUserName  |       string       |    是    |        流水线触发人        |
|      bkWorkspace       |       string       |    是    |        工作空间地址        |
|    testVersionFlag     |       string       |    是    | 是否是测试版本 Y：是 N：否 |
|  bkSensitiveConfInfo   | Map<String,String> |    否    |        插件敏感信息        |

备注：用户开发插件时定义的参数类需继承AtomBaseParam.java复用这些基础参数（在流水线执行过程中这些基础参数将由agent传递给插件）



##### 2、AtomResult.java （流水线插件输出结果类）

参数详细信息如下：

| 参数字段名 |  类型  | 是否必需 |                          描述                          |
| :--------: | :----: | :------: | :----------------------------------------------------: |
|   status   | string |    是    |      状态  success:成功, failure:失败, error:错误      |
|  message   | string |    否    |                        描述信息                        |
|    type    | string |    是    | 模板类型，目前仅支持default,用于规定data的解析入库方式 |
|    data    |  map   |    否    |                      返回数据信息                      |

 返回数据信息介绍如下：

输出的返回数据类型目前支持string（字符串）、artifact（构件）、report（报告）三种类型，三种类型对应的

java实体对象如下：

- string对应StringData

    输出样例:

  

  ```
  {
   "type": "string",
   "value": "字符串数据"
   }
  ```

- artifact对应ArtifactData

    输出样例:

  

  ```
  {
   "type": "artifact",
   "value": ["file_path_1", "file_path_2"] # 本地文件路径，指定后，agent自动将这些文件归档到仓库
   }
  ```

-   report对应ReportData

   输出样例:

  ```
  {
    "type": "report",
    "label": "",  # 报告别名，用于产出物报告界面标识当前报告
    "path": "",   # 报告目录所在路径，相对于工作空间
    "target": "",  # 报告入口文件
    "enableEmail": true, # 是否开启发送邮件
    "emailReceivers": [], # 邮件接收人
    "emailTitle": "" # 邮件标题
   }
  ```



##### 3、AtomContext.java （流水线插件上下文类）

​      对外提供的方法如下：

```
    /**
     * 获取请求参数
     * @return 请求参数
     */
    public T getParam() {
        return param;
    }
    
    /**
     * 获取敏感信息参数
     * @param filedName 字段名
     * @return 敏感信息参数
     */
    public String getSensitiveConfParam(String filedName){
        Map<String,String> bkSensitiveConfInfo = param.getBkSensitiveConfInfo();
        if(null != bkSensitiveConfInfo){
            return bkSensitiveConfInfo.get(filedName);
        }else{
            return null;
        }
    }

    /**
     * 获取结果对象
     * @return 结果对象
     */
    public AtomResult getResult() {
        return result;
    }
```



##### 4、支持http调用的工具类OkHttpUtils.java

   支持常用的GET、POST、PUT和DELETE的restful请求提交方式，以下是post提交方法 的介绍（其它请求方法详细信息请查看OkHttpUtils.java）:

```
   /**
    * http post方式请求，返回json格式响应报文
    *
    * @param url            请求路径
    * @param jsonParam      json格式参数
    * @param headers        请求头
    * @param connectTimeout 连接超时时间
    * @param writeTimeout   写超时时间
    * @param readTimeout    读超时时间
    * @return json格式响应报文
    */
    public static String doPost(String url, String jsonParam, Map<String, String> headers, long     connectTimeout, long writeTimeout, long readTimeout)；
```



##### 5、支持json操作的工具类JsonUtil.java

​     包含的主要方法如下：

```
    /**
     * 序列化时忽略bean中的某些字段,字段需要用注解SkipLogFields包括
     *
     * @param bean 对象
     * @param <T>  对象类型
     * @return Json字符串
     * @see SkipLogField
     */
    public static <T> String skipLogFields(T bean);

    /**
     * 从Json串中解析成bean对象,支持参数泛型
     *
     * @param jsonString    Json字符串
     * @param typeReference 对象类
     * @param <T>           对象类型
     * @return  对象
     */
    public static <T> T fromJson(String jsonString, TypeReference<T> typeReference);
	
    /**
     * 从Json串中解析成bean对象
     *
     * @param jsonString Json字符串
     * @param beanClass  对象类
     * @param <T>        对象类型
     * @return 对象
     */
    public static <T> T fromJson(String jsonString, Class<T> beanClass);

    /**
     * 创建输出所有字段的Json，不管字段值是默认值 还是等于 null 还是空集合的字段，全输出,可用于外部接口协议输出
     *
     * @param bean 对象
     * @param <T>  对象类型
     * @return Json字符串
     */
    public static <T> String toJson(T bean);

    /**
     * 注意，此只输出一个bean对象中字段值不为null的字段值才会序列到json，可用于外部系统协议输出。
     *
     * @param bean 对象
     * @param <T>  对象类型
     * @return Json字符串
     */
    public static <T> String toNonEmptyJson(T bean);

```



##### 6、BaseApi.java（支持sdk调用蓝盾后台接口的基类，可以继承该类使用该类封装的调用蓝盾后台接口的方法）

​     包含的主要方法如下：

```
    /**
     * request请求，返回json格式响应报文
     *
     * @param request request对象
     * @param errorMessage 请求错误信息
     * @return json格式响应报文
     */
    protected String request(Request request, String errorMessage) throws IOException; 

    /**
     * get请求，返回request对象
     *
     * @param path 请求路径
     * @param headers 请求头
     * @return request对象
     */
    public Request buildGet(String path, Map<String, String> headers);
	
    /**
     * get请求，返回request对象
     *
     * @param path 请求路径
     * @return request对象
     */
    public Request buildGet(String path);

    /**
     * post请求，返回request对象
     *
     * @param path 请求路径
     * @return request对象
     */
    public Request buildPost(String path);

    /**
     * post请求，返回request对象
     *
     * @param path 请求路径
     * @param headers 请求头
     * @return request对象
     */
    public Request buildPost(String path, Map<String, String> headers);
    
    /**
     * post请求，返回request对象
     *
     * @param path 请求路径
     * @param requestBody 请求报文体
     * @param headers 请求头
     * @return request对象
     */
    public Request buildPost(String path, RequestBody requestBody, Map<String, String>  headers);
    
    /**
     * put请求，返回request对象
     *
     * @param path 请求路径
     * @return request对象
     */
    public Request buildPut(String path);
    
    /**
     * put请求，返回request对象
     *
     * @param path 请求路径
     * @param headers 请求头
     * @return request对象
     */
    public Request buildPut(String path, Map<String, String> headers);
    
    /**
     * post请求，返回request对象
     *
     * @param path 请求路径
     * @param requestBody 请求报文体
     * @param headers 请求头
     * @return request对象
     */
    public Request buildPut(String path, RequestBody requestBody, Map<String, String>   headers);
    
    /**
     * delete请求，返回request对象
     *
     * @param path 请求路径
     * @param headers 请求头
     * @return request对象
     */
    public Request buildDelete(String path, Map<String, String> headers);
    
    /**
     * 生成json形式请求报文体，返回请求报文体
     *
     * @param data 请求数据对象
     * @return json形式请求报文体
     */
    public RequestBody getJsonRequest(Object data);
    

```


##### 7、DockerApi.kt（统一的docker run入口api）

​     包含的主要方法如下：

```
    /**
     * 执行镜像run相关操作，屏蔽构建资源差异
     *
     */
    fun dockerRunCommand(projectId: String, pipelineId: String, buildId: String, param: DockerRunRequest): Result<DockerRunResponse> 

    /**
     * 获取镜像日志和状态
     *
     */
    fun dockerRunGetLog(projectId: String, pipelineId: String, buildId: String, param: DockerRunLogRequest): Result<DockerRunLogResponse>

```

使用示例：

```
// 启动镜像
val param = DockerRunRequest(
    userId = commandParam.landunParam.userId,
    imageName = imageParam.imageName,
    command = imageParam.command,
    dockerLoginUsername = imageParam.registryUser,
    dockerLoginPassword = imageParam.registryPwd,
    workspace = File(commandParam.landunParam.streamCodePath),
    extraOptions = imageParam.env.plus(mapOf(
        "devCloudAppId" to commandParam.devCloudAppId,
        "devCloudUrl" to commandParam.devCloudUrl,
        "devCloudToken" to commandParam.devCloudToken
    ))

)
val dockerRunResponse = api.dockerRunCommand(
    projectId = commandParam.landunParam.devopsProjectId,
    pipelineId = commandParam.landunParam.devopsPipelineId,
    buildId = commandParam.landunParam.buildId,
    param = param
).data!!
```

```
// 轮询镜像日志和状态
// extraOptions取自前面“启动镜像”步骤
// 轮询镜像日志和状态
// extraOptions取自前面“启动镜像”步骤
for (i in 1..100000000) {
        Thread.sleep(timeGap)

        val runLogResponse = getRunLogResponse(api, commandParam, extraOptions, timeGap)

        extraOptions = runLogResponse.extraOptions

        var isBlank = false
        runLogResponse.log?.forEachIndexed { index, s ->
            if (s.isBlank()) {
                isBlank = true
                LogUtils.printStr(".")
            } else {
                if (isBlank) {
                    isBlank = false;
                    LogUtils.printLog("")
                }
                LogUtils.printLog("[docker]: $s")
            }
        }

        when (runLogResponse.status) {
            DockerStatus.success -> {
                LogUtils.printLog("docker run success: $runLogResponse")
                return
            }
            DockerStatus.failure, DockerStatus.error -> {
                throw RuntimeException("docker run fail: $runLogResponse")
            }
            else -> {
                if (i % 16 == 0) LogUtils.printLog("docker run status: $runLogResponse")
            }
        }
    }
}


```




## 二、SDK提供的服务介绍

##### 1、引入了slf4j-simple日志打印框架提供日志打印服务

   使用方法详见slf4j-simple的官网：https://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html
