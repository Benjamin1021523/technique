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

後續介紹函式會提到的filter類功能可以篩選key或value的值回傳部分的Multimap，看似是新的物件實際上是個`filtered view`，會和原本的Multimap同步更新

### 好用函式

<details>
<summary>Multimap.entries()</summary>

可以想像Multimap會是個二維結構，如果要依序經過每個key底下value的每個元素，傳統的map需要經過兩層迴圈

`Map<Long, Set<Long>>`使用`Map.entrySet()`取得key value的對應，再依序取得每個value(`Set<String>`)裡面的每個元素

`HashMultimap<Long, Long>`使用Multimap.entries()可以得到key對應value當中每個元素的entry，依照key的順序與value的順序組成

等同把這個二維結構拉平為一維結構
</details>

<details>
<summary>Multimaps.asMap <b>謹慎使用！</b></summary>

Multimaps.asMap可以將一個`Multimap<K, V>`轉為`Map<K, Collection<V>>`，Collection具體是哪一種端看Multimap建立時的類型

看似可以就此當map加入新的key value組合或是更新資料，但其實還是有些限制

對已經存在的key使用add加入新的元素是可以的，但是想要插入新的key value是沒辦法的，畢竟前面也看到了，雖然底層是HashSet但是並不是加個HashSet就能讓Multimap這套規則正常運作。
```java
// 這個可以有
Multimaps.asMap(groupIdToPersonIdMap).get(2000L).add(106L);
// 這個真的不行
Set<Long> set = new HashSet<>();
set.add(106L);
Multimaps.asMap(groupIdToPersonIdMap).put(2002L, set);
```

> 既然擔心混淆誤用，什麼時候適合使用asMap?

Multimap不是標準的物件，無法序列化。因此如Gson和Jackson等套件沒辦法將它轉為json

此時使用asMap就能得到看起來是Map的物件，照著實作的資料結構產生json字串

總而言之，asMap產生的Map請避免用於修改，當作唯讀的物件使用就好，因此個人建議 **雖然asMap建立的Map會因為`filter view`的特性和Multimap更新的內容同步，但是最好還是要用再呼叫asMap，不要增加誤用的機會**。
</details>

<details>
<summary>Multimaps.filterXXX</summary>

這段說明包含了`filterKeys()`和`filterValues()`，兩種方法傳的參數幾乎一樣，傳入一個Multimap物件，再加上`com.google.common.base.Predicate`或是lambda物件指定過濾資料的規則。只不過一個是過濾key，一個是過濾value

過濾之後的資料會回以同樣的Multimap物件類型回傳，就像前面說的`filtered view`和原本的物件共用儲存空間，你可以更新任一方達到資料同步

推薦的使用方法是定義一個函式接收Multimap參數，作為其中處理邏輯時的讀取資料，像這樣<br>
``public void completeData(Multimap<String, String> viewMultimap, List<MyData> dataList)``<br>
可以使用不同的條件過濾Multimap之後傳進去，對函式來說他都是同樣的用法，查詢Multimap的資料確認有無，也不用知道你的過濾方式是什麼。<br>
做到情境邏輯分開處理的同時，也能共用資料的處理邏輯。

此外，搭配前面的`entries()`方法的話就可以先過濾再以一維結構輕易的逐一取得資料。

目前還沒有實際在專案上用過，但是值得期待。
</details>
