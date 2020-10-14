# Ufo

#### 介绍
一款傻瓜式的Android网络访问框架，比Retrofit更简洁易使用。

#### Ufo网络框架使用SoEasy，4步轻松搞定：
1. 引入Ufo插件；
````
由于目前插件和源码还未上传到jcenter，本地引用配置如下：
拷贝源码根目录的repo插件到你的工程根目录下.然后在工程的根目录gradle配置文件中进行引用
buildscript {
    repositories {
        maven {url uri('repo')}
    }
    dependencies {
        classpath 'com.androidufo.aop:plugin:1.0.0'
    }
}
````
2. 在使用的工程gradle文件下引入；
````
apply plugin: 'ufo-aspectj'
android {
    // 必须使用java8及以上版本
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
dependencies {
    implementation project(':api')
    annotationProcessor project(':api-compiler')
}
````
3. 定义Api接口类和请求方法；
4. 调用请求方法；

#### 最基本Api接口类定义及使用如下：
````
定义代码：
@Api(baseUrl = "http://192.168.1.23:8080")
public interface MyApi {
    @Get
    ResultCall<String> getInfos();
}

调用方式一：
public class MainActivity extends AppCompatActivity {
    @Autowired // 使用该注解直接注入MyApi对象
    private MyApi myApi;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myApi.getInfos().execute(new ResultListener<String>() {
            @Override
            public void onError(Error error) {
            }
            @Override
            public void onResult(String result) {
            }
        });
    }
}
调用方式二：
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Provider.api(MyApi.class).getInfos().execute(new ResultListener<String>() {
            @Override
            public void onError(Error error) {
            }
            @Override
            public void onResult(String result) {
            }
        });
    }
}

调用方式一和调用方式二本质上没有区别，Api接口类定义后，拿到的实例为全局单例
````
##### 定义时注意5个关键点：
1. 该类必须是interface；
2. 必须使用@Api注解标识；
3. 单个url环境必须配置baseUrl；
4. 接口方法必须使用对应的http请求方法注解标识；
5. 请求数据方法返回值必须是ResultCall类型及其子类型；

````
目前支持的请求方法对应注解有：@Get、@Post、@Put、@Patch、@Delete、@Download、@Upload。
其中@Download代表下载文件，@Upload代表上传文件，框架内部将上传和下载单独列出，使用更加方便。
````

##### PS：ResultCall支持泛型，可自动将请求响应数据解析为指定的类型，为了方便，后面的示例都使用String类型。

#### 自动绑定生命周期，拒绝内存泄漏
该框架提供了网络请求绑定生命周期功能，无需担心Activity或Fragment被销毁后请求依旧还在执行导致内存泄露。
在执行前调用bindLifecycle方法即可，代码如下：
````
Provider.api(MyApi.class)
        .getInfos()
        .bindLifecycle(this) // 其中this代表当前Activity或Fragment对象
        .execute(new ResultListener<String>() {
            @Override
            public void onError(Error error) {
            }
            @Override
            public void onResult(String result) {
            }
        });
````
#### 携带请求参数
框架将请求参数分为7种，具体如下：
1. @Query参数：拼接在url上的参数，必须使用QueryParams类型，且使用该注解标识；
2. @Body参数：请求体参数，必须使用BodyParams类型，且使用该注解标识；
3. @Header参数：请求头参数，必须使用HeaderParams类型，且使用该注解标识；
4. @Path参数：用于动态拼接RestFull风格url的变量，必须使用String类型，且使用该注解标识，格式必须为{paramsName}，且参数名必须与@Path注解的key属性值相同；
5. @UploadFile参数：该参数只能用于添加上传的文件，必须使用UploadFileParams类型，且使用该注解标识；
6. @DownloadPath参数：该参数只能用于下载文件时制定文件所在的目录和文件名字，必须使用DownloadPathParams类型，且使用该注解标识；
7. @Url参数：该参数替换Api接口类中定义的baseUrl地址，动态使用指定的地址值；

##### 使用示例：
````
Api接口定义：
@Api(baseUrl = "http://192.168.1.23:8080")
public interface MyApi {
    @Get
    ResultCall<String> getInfos(@Query QueryParams queryParams);
    
    @Post(restUrl = "/goods/{startDate}/{endDate}", format = BodyFormat.JSON)
    ResultCall<String> searchGoods(
            @Body BodyParams bodyParams,
            @Path(key = "startDate") String startDate,
            @Path(key = "endDate") String endDate
    );

    @Download
    DownloadCall downloadApk(@Url String apkUrl);

    @Upload
    UploadCall<String> uploadMyFile(@UploadFile UploadFileParams fileParams);
}

方法getInfos调用：
QueryParams queryParams =
                new QueryParams.Builder()
                .param("name", "ZhangSan")
                .param("age", "20")
                .build();
myApi.getInfos(queryParams)
        .bindLifecycle(this)
        .execute(new ResultListener<String>() {
            @Override
            public void onError(Error error) {
            }
            @Override
            public void onResult(String result) {
            }
        });
使用@Query参数后得到的url为：
http://192.168.1.23:8080?name=ZhangSan&age=20

方法searchGoods调用：
BodyParams bodies =
        new BodyParams.Builder()
                .param("goodsName", "Apple")
                .build();
HeaderParams headers =
        new HeaderParams.Builder()
                .header("Content-Encoding", "gzip")
                .build();
myApi.searchGoods(bodies, headers, "2020-01-01", "2020-03-08")
        .bindLifecycle(this)
        .execute(new ResultListener<String>() {
            @Override
            public void onError(Error error) {
            }
            @Override
            public void onResult(String result) {
            }
        });
searchGoods方法定义时，@Post注解使用了属性format = BodyFormat.JSON，代表请求体提交时会以json数据格式提交。
该属性还支持表单形式提交（FORM，默认值）和文本类型提交（TEXT）。
当使用BodyFormat.TEXT文本类型时提交的请求体参数需要调用stringBody()方法传入，如下代码：
BodyParams bodies = new BodyParams.Builder()
                        .stringBody("字符串文本请求体")
                        .build();

文件下载方法downloadApk调用：
String downloadUrl = "https://s9.pstatp.com/package/apk/aweme/120801/aweme_aweGW_v120801_41b4_1600163228.apk?v=1600163232";
myApi.downloadApk(downloadUrl)
        .execute(new DownloadListener() {
            @Override
            public void onDownloading(State downloadState, String fileName, Progress progress) {
                // State有三种状态：START开始下载，IN_PROGRESS下载中，COMPLETE下载完成，回调是UI线程，可以进行UI更新
                // fileName参数表示文件下载后的存储路径
                // progress参数表示当前下载进度参数
                switch (downloadState) {
                    case START:// 开始下载
                        break;
                    case IN_PROGRESS:// 正在下载中，可更新进度值
                        break;
                    case COMPLETE:// 下载完成
                        break;
                }
            }
            @Override
            public void onDownloadFailed(Error error) {
            }
        });
下载路径默认目录为sdcard下，需要申请sdcard的权限:
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

文件上传方法uploadMyFile调用：
UploadFileParams fileParams =
        new UploadFileParams.Builder()
                .file("/sdcard/myfile.txt")
                .file("idFile", "/sdcard/card.jpg")
                .build();
可以同时上传多个文件，如果没有设置文件上传的key值，默认会赋值一个key
上传文件时，也可以携带数据参数，参考前面的示例即可
myApi.uploadMyFile(fileParams)
        .execute(new UploadListener<String>() {
            @Override
            public void onUploading(State uploadState, Progress progress) {
                上传文件与下载文件一样，这里不再举例说明
            }
            @Override
            public void onError(Error error) {
            }
            @Override
            public void onResult(String result) {
                若上传文件接口会返回数据，result即是返回结果
            }
        });
````
#### PS:以上示例的使用方法都可以结合，如果有使用错误，编译时会报错，并提示相应的错误点。

#### Https及UrlEncode支持
框架默认已经支持了无证书不安全的的https请求，要配置证书则直接将证书放入assets文件夹下，然后配置@Api注解的属性，示例如下：
````
@Api(
        baseUrl = "http://192.168.1.23:8080",
        assetsSslCer = "xxx.cer",
        assetsBks = "xxx.bks",
        bksPassword = "123456",
        urlEncode = true // 表示url的@Query参数是否开启UrlEncode，默认false不开启
)

assetsSslCer：填写cer证书在assets文件夹下的完整路径
assetsBks：填写bks证书在assets文件夹下的完整路径，没有可不填写
bksPassword：bks证书的密码
将证书路径配置正确，无需其他操作即可生效！
````

#### 动态多环境Url支持
该框架还支持多环境url自由切换，要实现多环境url需要将定义的Api接口类继承MultipleUrlEnvConfigs接口，并实现方法initUrlEnvConfigs的默认行为。
````
@Api
public interface MultipleUrlApi extends MultipleUrlEnvConfigs {
    @Override
    default List<UrlEnvConfig> initUrlEnvConfigs() {
        必须至少返回一个环境的url配置
        List<UrlEnvConfig> list = new ArrayList<>();
        例如开发环境配置:baseUrl填写公司访问的地址
        UrlEnvConfig devEnv = new UrlEnvConfig("dev", "http://192.168.1.22:8080");
        list.add(devEnv);
        正式环境配置:baseUrl填写公司访问的地址
        UrlEnvConfig proEnv = new UrlEnvConfig("pro", "http://baidu.com");
        list.add(proEnv);
        return list;
    }
}
````
多url环境可以动态新增环境和切换环境，具体调用如下：
````
动态添加北京服务器的环境示例：
Provider.api(MultipleUrlApi.class)
        .addUrlEnvConfig(new UrlEnvConfig("BeiJingServer", "https://ufo-beijing.cn"));

将当前环境切换至北京服务器环境示例：
try {
    Provider.api(MultipleUrlApi.class).switchUrlEnvConfig("BeiJingServer");
} catch (Exception e) {
    e.printStackTrace();
}
````

#### 自定义解析请求响应返回结果
````
自定义请求结果，只需要配置响应转换器即可，代码如下：
myApi.getInfos(queryParams)
    .bindLifecycle(this)
    .responseConverter(new Converter<String>() {
        @Override
        public String convert(String response, Type resultType) throws Exception {
            response：表示请求响应数据的字符串
            resultType：表示当前定义方法传入的泛型对应的type类型，用于json解析
            同样，可以在这里进行解密操作，这里就简单返回一个固定字符串
            return "我是Ufo网络访问框架";
        }
    })
    .execute(new ResultListener<String>() {
        @Override
        public void onError(Error error) {
        }
        @Override
        public void onResult(String result) {
            对应这里的result值就会变成：我是Ufo网络访问框架
        }
    });
````

#### 其他更多用法请参考Demo源码，后续会考虑新增功能：
1. 支持配置拦截器；
2. 断点续传下载；
3. 网络缓存；
4. Rx支持；
