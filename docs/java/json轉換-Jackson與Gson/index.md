---
title: json轉換-Jackson與Gson
---
<!--關鍵字: Jackson, Gson-->

# json轉換-Jackson與Gson

這篇會介紹java最常用的兩種將物件與json字串之間轉換的套件，比較兩者的用法與適合的情境

## Jackson-與spring框架高度整合的套件

jackson被包含在`spring-boot-starter-web`的套件之中，也是spring mvc預設使用的json轉換工具，意思就是定義API收到的json轉為輸入物件，以及輸出物件轉為json的工作，如果沒特別設定的話預設都是使用jackson完成。

jackson的基本語法是這樣的：
```java
private static ObjectMapper objectMapper = new ObjectMapper();

public static void setObjectMapper(ObjectMapper objectMapper) {
    JacksonUtil.objectMapper = objectMapper;
}

public static String toJson(Object obj) {
    try {
        return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
        log.error("jackson to json fail: " + e.getMessage());
    }
    return null;
}

public static <T> T fromJson(String json, Class<T> clazz) {
    try {
        return objectMapper.readValue(json, clazz);
    } catch (Exception e) {
        log.error("jackson from json fail: " + e.getMessage());
    }
    return null;
}

public static <T> T fromJson(String json, TypeReference<T> typeReference) {
    try {
        return objectMapper.readValue(json, typeReference);
    } catch (Exception e) {
        log.error("jackson from json fail: " + e.getMessage());
    }
    return null;
}

...

User user = JacksonUtil.fromJson(json, User.class);


```

建立物件`ObjectMapper`後使用上述的函式就可以做到把物件轉換為json，或是把json轉換為指定class的功能

單純的Class的話可以直接使用`User user = JacksonUtil.fromJson(json, User.class);`；<br>
如果是泛型的話則要使用`TypeReference`，像這樣：
```java
TypeReference<List<User>> typeReference = new TypeReference<>(){};
List<User> userList = objectMapper.readValue(json, typeReference);
```

也可以使用一些annotation定義物件與json轉換的規則：

* `@JsonIgnore`: 指定某欄位在雙向轉換時都無視
* `@JsonProperty`: 指定物件欄位與json欄位名稱的對應，可以取得/轉換為與java命名規則不符的欄位
* `@JsonFormat`: 可以定義json與物件之間的轉換方式，像是日期文字轉換為LocalDate之類
* `@JsonDeserialize`、`@JsonSerialize`: 指定欄位的序列與反序列化方式(自定義)

## Gson-Google的輕量化json轉換套件

gson是google出的專門用於轉換json的套件，有異於jackson的另一套annotation，兩者互相不可混用(不認得對方的功能)

使用範例如下
```java
public static void main(String[] args) {
    Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
    String jsonStr = """
            [  {"key": "value"}  ]
            """;
    List<Map<String, Object>> list = fromJson(jsonStr, type);
    System.out.println(list);// [{key=value}]
    System.out.println(toJson(list));// [{"key":"value"}]
}

private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

public static String toJson(Object obj) {
    return GSON.toJson(obj);
}

public static <T> T fromJson(String json, Type type) {
    return GSON.fromJson(json, type);
}

public static <T> T fromJson(String json, Class<T> clazz) {
    return GSON.fromJson(json, clazz);
}
```
和jackson一樣，對class或是泛型的時候會有不同的使用方法。

## 如何選擇

### 套件空間大小：gson較省

gson的套件大小約250KB，jackson完整功能套件總共約2.8MB，所以在使用情境相對簡單，對空間要求較大的客戶端可能會為了空間大小而選擇gson

### 設定json與物件欄位名的對應：兩者皆有

jackson使用`@JsonProperty("attr_name")`，gson使用`@SerializedName("attr_name")`

### 日期欄位格式化：jackson有內建方法

jackson可使用`@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")`，也可以自訂`JsonSerializer`和`JsonDeserializer`定義序列和反序列化方式；

gson沒有內建方法，可以自訂`TypeAdapter`設定序列和反序列化方法。

### 排除某欄位不收/不輸出的方法

jackson使用`@JsonIgnore`指定排除的欄位，gson沒有對應方法，只能使用`@Expose`反向運作，定義要使用的欄位，所以沒標記的就是要排除。

個人覺得這是很不好用的點，曾經嘗試搭配`ExclusionStrategy`自定義`@Exclude`排除欄位，不可否認這部分gson真的比較麻煩。

### 泛型物件的轉換：兩者皆有

如上述例子，不額外說明

-----

除此之外jackson還可以支援yaml、xml等其他格式的處理，使用範圍比gson更大。

gson和jackson做為主流物件轉換套件，不太可能碰到效能問題(至少我碰過)，根據查到的資料顯示jackson略快於gson，但是自己平時使用沒感覺到差異。

**個人認為不用為了效能而選擇使用，以可讀性和使用情境考慮即可。**

如果確定使用情境比較單純另外有空間壓力，而且確認只會使用到json的話可以選擇gson

但是jackson作為spring boot預設的轉換套件，個人認為習慣使用比較能在不同專案之間轉換的時候適應，此外也可能碰到上述沒提到的，只有jackson能處理的情境，建議第一優先還是jackson。

專案裡要並用兩種套件也可以(我目前是這樣用的)，但是要確保互相不要使用到對方的功能，首先對方的annotation一定是無效的，此外jackson解析gson的JsonObject，或是gson解析jackson的JsonNode一定會出錯或是解析不如預期，這點特別要注意別誤用。

<!--Finish-->