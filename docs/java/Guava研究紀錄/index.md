---
title: Guava簡介
---

<!--關鍵字: gauva, 資料結構-->

# Guava是什麼？

guava是google團隊開發的java開源函式庫，有一些比java原生語法更好用的功能。

理論上guava的誕生是為了google內部需求而生，「先不論底層的算法是否能比JDK提供的原生java更高效，但應該在某個時空背景下是值得使用的」開始看本文之前請先有這樣的概念。

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

在開始講各個class之前，先講點guava的特殊畫風

雖然展示的說明主要是如何簡化程式寫法，但其實guava厲害的不只是簡化寫法而已

guava物件透過函式取得的物件被稱為視圖(view)，意思是底層儲存的資料還是同一批，只是換個角度看這些資料所以變成反向或是篩選後的樣子。

## BiMap-雙向key value查詢結構

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

使用`inverse()`可以取得`<value, key>`的反向視圖(`inverse view`)，轉換視角為用value對應key的查找結構

使用`put()`新增/更新資料，如果碰到傳入value已被對應到其他key會噴錯；使用`forcePut()`則會移除原本的對應再寫入

不論是原本的BiMap還是inverse的反向BiMap都可以用來寫入，並且寫入後的資料一致

### 好處

* 程式碼簡潔
* 不用同時維護兩個map，不會有不一致的問題

### 深入探討

待續

## Multimap-方便收集一對多資料的結構

一個`Multimap<K, V>`的內部實作方法其實是`Map<K, Collection<V>>`，value的部分可以是一個List或Set，能在一些需要整理資料的時候用更簡單的寫法做完同樣的事，不過效能不會差太多。

舉個簡單的例子，查得一個List紀錄每個人員(Person)所屬的部門，要按照部門(Group)整理每個部門下有哪些人的關係，我會這樣處理：

```java
public class ExampleClass {
    public static void main(String[] args) {
        // 要處理的資料
        List<PersonGroup> personGroupList = new ArrayList<>();
        personGroupList.add(new PersonGroup(101L, 2000L));
        personGroupList.add(new PersonGroup(102L, 2000L));
        personGroupList.add(new PersonGroup(103L, 2000L));
        personGroupList.add(new PersonGroup(104L, 2001L));

        Map<Long, Set<Long>> groupIdToPersonIdMap = new HashMap<>();
        for (PersonGroup personGroup : personGroupList) {
            Set<Long> personIdSet = groupIdToPersonIdMap.putIfAbsent(personGroup.groupId, new HashSet<>());
            personIdSet.add(personGroup.personId);
        }
    }

    public static class PersonGroup {
        public Long personId;
        public Long groupId;
    }
}
```

用迴圈依序讀取並寫入對應的set，在寫入前檢查一下這個groupId是否已加入過personId，如果沒有就放個空的set進去，再寫入新的personId

如果使用Multimap，寫法會變成這樣：

```java
public class ExampleClass {
    public static void main(String[] args) {
        List<PersonGroup> personGroupList = new ArrayList<>();
        personGroupList.add(new PersonGroup(101L, 2000L));
        personGroupList.add(new PersonGroup(102L, 2000L));
        personGroupList.add(new PersonGroup(103L, 2000L));
        personGroupList.add(new PersonGroup(104L, 2001L));

        Multimap<Long, Long> groupIdToPersonIdMap = HashMultimap.create();
        for (PersonGroup personGroup : personGroupList) {
            groupIdToPersonIdMap.put(personGroup.groupId, personGroup.personId);
        }
    }

    public static class PersonGroup {
        public Long personId;
        public Long groupId;
    }
}
```

其實和原本不會差太多，Multimap只是把建立新的Collection物件的部分封裝在函式裡面

但是就像Collection<T>有個Collections

除此之外，Multimap某key的元素被刪到沒有的時候，那個key會自動被移除，從keySet()也看不到。

深入探討時會介紹函式會提到的filter類功能可以篩選key或value的值回傳部分的Multimap，看似是新的物件實際上是個`filtered view`，會和原本的Multimap同步更新

### 深入探討

待續

## RangeMap-用物件管理範圍的比較條件

RangeMap是一個key value結構，但是並沒有繼承Map，而且key會是經過Range包裹的物件，物件本身必須是可比較大小，可區分範圍的

從RangeMap透過get傳入key的值(就像一般Map的用法)，並不是「取得相同key的value」而是「取得符合條件範圍的key的value」

舉個例子，以成績和等第的關係為例，某範圍內的分數都是相同的區間，使用RangeMap可以這樣寫

```java
public class RangeMapTest {
    public static void main(String[] args) {
        RangeMap<Integer, String> scoreRatingMap = TreeRangeMap.create();

        scoreRatingMap.put(Range.lessThan(60), "D");
        scoreRatingMap.put(Range.closedOpen(60, 70), "C");
        scoreRatingMap.put(Range.closedOpen(70, 80), "B");
        scoreRatingMap.put(Range.closedOpen(80, 100), "A");
        scoreRatingMap.put(Range.closed(90, 100), "A+");
        scoreRatingMap.put(Range.greaterThan(100), "WTF");

        System.out.println(scoreRatingMap.toString());
        getRating(scoreRatingMap, 59);// D
        getRating(scoreRatingMap, 60);// C
        getRating(scoreRatingMap, 61);// C
        getRating(scoreRatingMap, 70);// B
        getRating(scoreRatingMap, 71);// B
        getRating(scoreRatingMap, 80);// A
        getRating(scoreRatingMap, 81);// A
        getRating(scoreRatingMap, 90);// A+
        getRating(scoreRatingMap, 100);// A+
        getRating(scoreRatingMap, 101);// WTF
        getRating(scoreRatingMap, 10000);// WTF
    }

    private static void getRating(RangeMap<Integer, String> scoreRatingMap, Integer score) {
        String rating = scoreRatingMap.get(score);
        System.out.println("get " + score + " rating: " + rating);
    }
}
```

``closed``是包含，``open``則不包含

### 上面這個例子也可以用if else完成，使用RangeMap有什麼好處？

首先是可讀性，可以在一行看到符合的範圍以及對應的值

使用與switch較為接近的方式後續新增條件也方便擴展

也可以透過``scoreRatingMap.subRangeMap(Range.closed(60, 100)``取得僅限及格範圍的RangeMap進行判斷以及更新

總地來講，RangeMap沒有太特別的超能力，用基礎java就能辦到，但是很方便，可以簡化程式寫法，用來當作函式傳參數的型別也可以增加可讀性
