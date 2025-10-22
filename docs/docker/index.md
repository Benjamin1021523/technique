---
title: Docker簡介
---
<!--關鍵字: docker、容器化、虛擬機-->

# docker是什麼？

docker是一個包裝軟體的功能，可以打包之後在其他電腦上用docker執行

可以用虛擬機的概念理解，只是他虛擬的不一定是完整的作業系統，有時只是個例如python或java的開發環境，有時只是個網頁服務應用程式。

想要使用docker需要先在電腦上安裝docker應用程式，最容易上手的做法是下載 [Docker Desktop](https://www.docker.com/products/docker-desktop/)

# docker重要名詞

## 映像(image)

以虛擬機比喻就是安裝作業系統用的iso檔或光碟，裡面裝了這個服務為了啟動所需的一切內容，例如：
* 基底的作業系統
* 其中運作的應用程式(java, python, spring boot服務...)
* 運作時需要的環境變數(JAVA_HOME、設定檔等等)

不同的映像檔(比方說python或openjdk)，有可能擁有同樣的作業系統基底，也就是他們運作在同一種作業系統上，就像是用同樣的光碟安裝了兩台電腦一樣，只是差在一個安裝了python一個安裝了java，而且分別為了安裝的程式設定了對應的參數。

openjdk以某個作業系統為基底，你可以以openjdk為基底加入自己的java程式，做出新的映像檔。

## 容器(container)

同樣以虛擬機比喻，就是用光碟或iso檔建立出的虛擬機，你可以用同樣的映像建立多個容器。

容器之間有以下的特性：
* 各自獨立運作、互不影響
* 可以分別啟動停止與刪除
* 容器的操作(下載程式、修改檔案)會被保留，除非容器刪除
  * 一旦容器刪除，容器內的修改都會消失
  * 如果希望保留或讓電腦上可以取得容器內建立的檔案，需要與電腦的路徑做連結(後續說明)

# docker基本指令

* docker -v
  * 確認版本
* docker ps
  * 列出運作中的服務
* docker pull [映像檔名稱:tag]
  * 從docker hub下載指定映像檔
* docker run [映像檔名稱:tag]
  * 執行指定映像檔，但是通常還會加上一堆參數
  * `-d`: 背景執行，測試啟動的過程中不要使用
  * ``-p 8081:8080``: 指定對接port，主機的8081對應到容器中的8080
  * ``--name``: 指定容器名稱，後續可使用`docker stop [容器名稱]`指定容器停止，如果沒指定會由docker隨機取名
  * 其他參數後續再講
* docker build -t myapp:1.0 .
  * 根據`.`路徑下的`Dockerfile`內容建立映像檔
  * 映像檔的名稱為`myapp`，版本為`1.0`
* `docker rm [容器名稱]`: 移除指定容器
  * 如果是`rm -f`就可以同時停下容器並刪除
* `docker images`: 查出電腦上的映像檔(包含版本)
  * 就像`ls`指令一樣，也可以用``docker images myapp``指定列出myapp的所有版本
* ``docker rmi [映像檔名稱:版本/映像檔ID]``: 移除指定映像檔
  * docker rmi myapp:1.0
  * docker rmi 2be2a446eea1

# Dockerfile

Dockerfile是用來定義一個映像檔產生的設定用的，下方是個基本的Dockerfile例子
```dockerfile
# 指定基底映像
FROM openjdk:17

# 設定容器內的工作目錄，相當於使用指令CD，因此這行執行後操作路徑為容器中的/app
WORKDIR /app

# 將 jar 檔案複製到容器中，延續前面工做目錄的設定，因此app.jar實際位置為/app/app.jar
COPY myapp-1.0.jar app.jar

# 容器對外開放的port，當然前提是上面的jar啟動後會在這個port提供服務
EXPOSE 8080

# 指定容器啟動時的指令
ENTRYPOINT ["java", "-jar", "app.jar"]
```

如此建立完Dockerfile，綜合一下執行`docker build -t myapp:1.0 .`讀取內容建立映像檔的邏輯：

* 先找到`openjdk:17`映像作為基底，如果本機找不到的話從docker hub上面找
* 把jar複製到映像檔中的/app路徑下改命名為app.jar
* 指定這個容器對外開放的port有8080

之後使用docker run執行映像檔啟動容器的時候：

* 容器中切換到/app目錄作業
* 執行`java -jar app.jar`指令
* 啟動服務完成

整套邏輯大概就是這樣

# 實際的啟動容器-講點現實的

上面只講到如何啟動容器，但和怎麼用容器提供服務被使用是兩回事

## 本機哪個port可以連到服務?

容器內運作的程式使用8080 port，Dockerfile也設定開放8080 port，所以可以使用8080 port連到容器內的服務嗎? 答案是不行

需要使用-p定義本機和容器的port對應才能使用，就像接水管一樣

使用`-p 8080:8080`就能在本機用8080 port連上容器，如果改成`-p 80:8080`則會以本機的80 port對應到容器的8080 port

## 如何讓容器讀取本機的檔案? 如何讓容器寫入資料到本機的檔案?

使用-v或--volume將本機的目錄掛載到容器的特定路徑，建立對應，概念上就像把遠端主機的某個目錄掛載到自己電腦上變成K槽一樣的做法

比方說我在C槽建立了myapp資料夾，裡面有conf/和logs/兩個目錄分別用來存設定檔和紀錄檔

當我使用`-v C:\myapp:/app/myapp`的時候，容器內/app/myapp下就會有conf/和logs/兩個目錄，裡面的檔案與本機的一致，容器可以從這裡讀取設定，也可以寫紀錄檔到這

## 設定環境變數

像是路徑這種東西並不會直接全寫死在程式裡面，光是作業系統不同路徑名稱規則就不同了，即使作業系統相同，不同電腦也可能把檔案放在不同位置。

所以通常會設定系統變數進行控制，要寫把這樣的程式包裝成容器執行也就需要設定容器內使用的環境變數。

延續前面的例子，如果我在windows運作時設定了`MYAPP_HOME=C:\myapp`，紀錄檔輸出路徑透過設定`filelog.path=${MYAPP_HOME}/logs/`指定輸出的目標，到了容器裡面就需要設定容器用的環境變數`MYAPP_HOME=/app/myapp`

使用`-e MYAPP_HOME=/app/myapp`就可以做到這件事

-----

綜合上述，要執行docker run啟動一個包裝好的映像，加上各項參數設定之後的指令如下：(使用windows的語法)
```shell
docker run ^
  -p 8080:8080 ^
  -e "MYAPP_HOME=/app/myapp" ^
  -v C:\myapp:/app/myapp ^
  myapp:1.0
```

# docker compose

雖然組合好指令之後就能正常啟動容器，但是這種寫法有點不利於控管，也不好閱讀

因此我們要使用docker compose管理這些參數的設定

``docker-compose.yml``的內容如下：
```yaml
services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    image: myapp:1.0
    ports:
      - "8080:8080"
    environment:
      - MYAPP_HOME=/app/myapp
    volumes:
      - C:\myapp:/app/myapp
```

docker compose不只是管理如何啟動，還有如何打包映像檔，至於是哪部分設定的從上面例子找關鍵字就能發現了。

下面是一些常用指令
```
:: 建立映像檔
docker compose build

:: 啟動容器(背景執行)
docker compose up -d

:: 看console log
docker compose logs -f

:: 停止容器
docker compose stop

:: 停止、刪除容器，刪除映像檔：
docker compose down --rmi all
```

# 常見Q&A

* 為什麼docker desktop已經安裝好了，使用docker指令卻沒辦法執行？
  * 啟動docker desktop應用程式，這種情況可能是電腦上的docker engine沒有啟動。docker和git不一樣，安裝後需要程式有在背景運作才能使用。
* 怎麼查到映像檔的基底作業系統？
  * `docker inspect openjdk:17`: 查詢映像`openjdk:17`的資訊
  * 可以查到作業系統的類型，還有其他啟動容器的資訊
  * 如果要查具體哪一版linux的話就回歸linux的指令，查詢`/etc/os-release`檔案內容了
* 啟動容器是成功了，但是顯示時間卻是UTC時間，要怎麼指定時區呢？
  * 透過環境變數`-e TZ=Asia/Taipei`，TZ(time zone)指定時區為本地時間

<!--Finish-->