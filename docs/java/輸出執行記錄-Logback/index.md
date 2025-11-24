---
title: 輸出執行記錄-Logback
---
<!--關鍵字: logback, spring boot-->

# Logback是什麼？

Logback 是一個高效能、可擴充的 Java 日誌框架，是 SLF4J 的官方實作，用來取代 Log4j。

它可以分別設定輸出格式、輸出檔案，以及個別輸出log的等級，常用於 Spring Boot 的日誌系統。

# 基本構成

## appender-輸出器

定義一組輸出log的規則，裡面包含了輸出方式，輸出格式等規則

舉個簡單的例子：
```xml
<appender name="MAIN_LOG_FILE" class="ch.qos.logback.core.FileAppender">
    <file>C:/log/myApp.log</file>
    <encoder>
        <pattern>[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%-5level] [%thread] %class{36}.%M\(%line\) - %msg%n</pattern>
    </encoder>
</appender>
```

定義一個appender名稱為`FILE`，使用`ch.qos.logback.core.FileAppender`輸出log，這是個把log輸出到檔案的功能

因為要輸出檔案，需要指定輸出的檔案到`C:/log/myApp.log`

請注意appender只是定義輸出的行為本身，但是什麼時候會使用，以及什麼層級的log要印出，這些事情都不在其中。

關於encoder下面再解釋。

## encoder-編碼器

控制輸出格式和樣式，被夾在appender中，不會單獨存在。

除了程式裡面寫的`log.info("執行成功");`當中的`"執行成功"`用`%msg`控制之外，還有其他參數可用，下面這組是我常用的格式：
```xml
<encoder>
    <pattern>[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%-5level] [%thread] %class{36}.%M\(%line\) - %msg%n</pattern>
</encoder>
```

各參數解釋如下

| Pattern 片段                    | 意義              | 說明                                  |
|-------------------------------|-----------------|-------------------------------------|
| `%d{yyyy-MM-dd HH:mm:ss.SSS}` | 時間資訊            | 指定格式印出，例如 `2025-11-16 10:15:23.451` |
| `%-5level`                    | Log Level       | 靠左對齊、寬度5，右側補空白，例如：`INFO `、`ERROR`   |
| `%thread`                     | 執行緒名稱           | 如 `main`、`http-nio-8080-exec-1`     |
| `%class{36}`                  | 類別名稱(限制最多 36 字) | logger 所在 class；太長會縮短不顯示全名          |
| `%M`                          | 方法名稱            | 呼叫 log 的方法                          |
| `%line`                       | 行數              | 產生 log 的程式碼所在行                      |
| `%msg`                        | Log 訊息內容        | 程式中印出的訊息本身                          |
| `%n`                          | 換行              | log 換行                              |

實際輸出的log會像這樣
```log
[2025-11-17 10:23:45.123] [INFO ] [main] com.example.MyApp.main(20) - 執行成功
```

## logger-紀錄器

logger在程式裡面常見到這兩種寫法：
```java
// 自己定義logger
private static final Logger log = LoggerFactory.getLogger(MyClass.class);

// 使用lombok自動產生logger
@Slf4j
```

``getLogger(MyClass.class)``的意思是「我要根據`MyClass`這個class取得他在logback裡面的設定，看看輸出紀錄的設定是如何。」而`@Slf4j`只是把這段程式編譯時自動生成，實際原理是一樣的。

雖然是用class取得logger，也確實可以對每個class分別設定，但實際使用只有特別的class會單獨定義，其他沒定義logger的class都會預設使用root logger的設定，範例如下：
```xml
<root level="ERROR">
    <appender-ref ref="MAIN_LOG_FILE"/>
    <appender-ref ref="CONSOLE"/>
</root>
```
這段指定了所有class的預設log設定，限制log level為ERROR以上的情況才印出log，印出的對象套用`MAIN_LOG_FILE`和`CONSOLE`兩套規則

----

如果想單獨對`com.example.MyClass`定義輸出到不同的log檔，可以像這樣定義：
```xml
<logger name="com.example.MyClass" level="DEBUG" additivity="false">
    <appender-ref ref="MY_CLASS_LOG_FILE" />
</logger>
```

指定`com.example.MyClass`範圍輸出的log套用`MY_CLASS_LOG_FILE`的規則，level在DEBUG以上印出。

``additivity="false"``這個參數定義的是logger範圍內的log是否要往上傳給範圍更大的logger(包含root在內)

設為true以這次的例子的情況，會導致`com.example.MyClass`的log在`MY_CLASS_LOG_FILE`之後又在`MAIN_LOG_FILE`和`CONSOLE`再次印出

重複的log除了浪費額外的空間之外，多數使用情境在單獨定義logger的目的就是希望把特定class的紀錄排除在主要log檔外，因此這項設定通常設為false。

## property-設定檔讀取、設定值設定

``<property>``可以用來定義讀取的設定檔，以及宣告變數

把前面舉例的appender輸出格式加上讀取設定之後會變成像這樣：
```xml
<!--執行到這行的時候載入指定設定檔內容-->
<property file="C:\conf\myApp.properties"/>
<!--嘗試取得環境變數或設定擋中的設定值MAIN_LOG_FILE_PATTERN，如果不存在就用冒號右邊的值-->
<property name="MAIN_LOG_FILE_PATTERN" value="${MAIN_LOG_FILE_PATTERN:-[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%-5level] [%thread] %class{36}.%M\(%line\) - %msg%n}"/>

<appender name="MAIN_LOG_FILE" class="ch.qos.logback.core.FileAppender">
    <file>C:/log/myApp.log</file>
    <encoder>
        <pattern>${MAIN_LOG_FILE_PATTERN}</pattern>
    </encoder>
</appender>
```

## 其他項目

``statusListener``可以在程式啟動過程印出log，得到各項參數的實際值，以此確認property的值，是預設值還是確實讀到設定檔

```xml
<statusListener class="ch.qos.logback.core.status.OnConsoleStatusListener"/>
```

# 進階功能

## 從logback到logback spring

logback spring是經過改良的logback，目的是為了spring boot專案更方便使用。

特色是他可以使用spring boot專案啟動時的`核心`設定，也就是application.properties內容。

### 讀取spring boot核心設定

與property不同，不用指定讀取的設定檔路徑與名稱，不會受限於一定要指定檔案存在才能順利讀取設定啟動。

使用方式如下，讀取`project.name`和`filelog.level`兩個設定參數，並且賦予`filelog.level`不存在時的預設值為`DEBUG`

```xml
<springProperty name="PROJECT_NAME" source="project.name"/>
<springProperty name="FILE_LOG_LEVEL" source="filelog.level" defaultValue="DEBUG"/>
```

請注意！程式裡透過`@PropertySource`引用的其他設定檔無法生效，這些非核心設定的參數會晚於logback spring才載入，想使用這個功能讀取外部設定檔需要使用`spring.config.import`將其他檔案也加入為核心設定的範圍，用法如下：

```properties
spring.config.import=optional:classpath:MyApp.properties,\
    file:${MYAPP_HOME}/conf/MyApp.properties
```

使用`classpath`引用內部設定檔，`file`引用外部設定檔，`optional`說明該檔案取不到也是正常的，不要因為缺一個設定檔就出錯啟動失敗。

如此就能與spring boot使用的設定值一致，即使發生設定值重複而覆蓋的情況也會一起複製，不會因為指定設定檔而出現落差。

### 使用spring profile控制log內容

這是logback spring的另一個方便功能，根據profile做logback內的邏輯判斷

和設定檔定義`level=DEBUG`這類不同，profile可以決定一段appender是否被加入

```xml
<!--當profile為dev的時候，level設為DEBUG並輸出log到檔案和log檔-->
<springProfile name="dev">
    <root level="DEBUG">
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="MAIN_LOG_FILE"/>
    </root>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        </encoder>
    </appender>
</springProfile>

<!--當profile為prod的時候，level設為ERROR並且輸出到log檔-->
<springProfile name="prod">
    <root level="ERROR">
        <appender-ref ref="MAIN_LOG_FILE"/>
    </root>
</springProfile>
```

為了方便說明，level都寫為綁定profile的寫法，但這不是好的做法，不論哪個環境執行，印出的log等級都還是由設定檔控制比較好，畢竟像是線上環境需要觀察正常log的情況，總不能都要人修改xml。

<!--Finish-->