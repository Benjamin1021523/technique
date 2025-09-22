---
title: Guava簡介
---

<!--關鍵字: gauva, 資料結構-->

# Guava是什麼？

guava是google團隊開發的java開源函式庫，有一些比java原生語法更好用的功能。

理論上guava的誕生是為了google內部需求而生，先不論底層的算法是否能比JDK提供的原生java更高效，但應該在某個時空背景下是值得使用的。

## 套件引用

目前最新版為``33.5.0``版，非安卓專案使用的版本為`-jre`，安卓則不同
```xml
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>33.5.0-jre</version>
</dependency>
```

## 使用說明

先不附上使用範例，因為guava並不是一套用於特定情境或是解決特定工作的工具，後續再補充

可以確定的是，使用guava的時候，請搞清楚**使用的目的以及實際達到的效果**

# 功能介紹

## BiMap

BiMap繼承了原生java的Map，因此他可以支援map的所有功能，作為一種key value結構儲存

只是相比於原本Map的結構，BiMap特別適合用在一些特定的情境

> BiMap不只是key是唯一值，可以取得對應的value，反過來value之間也不會重複，可以用value的值取得對應的key
> 簡單來說，使用BiMap可以獲得一個雙向的key value結構
> 
> 在BiMap裡面不會出現重複的key或重複的value，這是大前提

BiMap繼承了Map，想要建立物件需要透過BiMap的實作物件，例如`BiMap<String, String> biMap = HashBiMap.create()`

可以把BiMap當成是多了些規則的另一種Map使用

### 寫法差異

使用原生java.util.Map建立雙向查詢機制
```java
public class ExampleClass {
    public static void main(String[] args) {
        Map<String, Integer> statusNameCodeMap = new HashMap<>();
        statusNameCodeMap.put("OK", 200);
        statusNameCodeMap.put("NOT_FOUND", 404);
    
        Map<Integer, String> statusCodeNameMap = new HashMap<>();
        statusCodeNameMap.put(200, "OK");
        statusCodeNameMap.put(404, "NOT_FOUND");
    
        // get by status name
        System.out.println(statusNameCodeMap.get("OK"));
        // get by status code
        System.out.println(statusCodeNameMap.get(200));
    }
}
```
使用BiMap建立雙向查詢
```java
public class ExampleClass {
    public static void main(String[] args) {
        BiMap<String, Integer> statusBiMap = HashBiMap.create();
        statusBiMap.put("OK", 200);
        statusBiMap.put("NOT_FOUND", 404);

        Map<String, Integer> statusNameCodeMap = statusBiMap;
        Map<Integer, String> statusCodeNameMap = statusBiMap.inverse();

        // get by status name
        System.out.println(statusNameCodeMap.get("OK"));
        // get by status code
        System.out.println(statusCodeNameMap.get(200));
    }
}
```

使用`inverse()`可以取得<value, key>的反向查找結構

使用`put()`新增/更新資料，如果碰到傳入value已被對應到其他key會噴錯；使用`forcePut()`則會移除原本的對應再寫入

不論是原本的BiMap還是inverse的反向BiMap都可以用來寫入，並且寫入後的資料一致

### 好處

* 程式碼簡潔
* 不用同時維護兩個map，不會有不一致的問題

### 深入探討

<iframe src="./BiMap-必定一對一的map結構.md" width="600" height="400"></iframe>

