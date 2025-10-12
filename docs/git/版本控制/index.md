---
title: git簡介&常用指令
---
<!--關鍵字: git、版本控制-->

# Git 版本控制

## 什麼是Git？

Git是一個分散式版本控制系統，用於追蹤檔案變更、協作開發和管理程式碼歷史。

分散式的意思是每個開發者的開發環境有一套檔案，不必依賴存在網路上的特定專案才能作業，也不限於單一專案，只要還有一台電腦上有資料就能復活整個專案

它讓開發者能夠：

- 記錄檔案的所有變更歷史
- 在不同版本間切換
- 多人協作開發而不會互相干擾
- 追蹤誰在什麼時候做了什麼修改
- 合併不同分支的程式碼

用比較宅的方式說明就是很方便建立不同的世界線

## 基本概念

* 本地的git專案其中檔案不只你看到的那些，專案目錄下有個隱藏資料夾`.git`裡面會儲存之前的版本
  * **進過git版控的檔案並不會真的消失，就算刪了也不會**
* 關於commit和分支的概念，這邊使用銀行帳戶餘額和明細來說明
  * 銀行不會只存著你的餘額多少，而是存著你的帳戶歷來所有交易紀錄，哪時轉入轉出多少，這些交易造就了今天的餘額，過程和結果不容有錯，這點git也是一樣的，只是git不只是金額變化和交易方式而已。
  * 就像銀行餘額「有這些交易才有今天的我」，Git上的每個commit都是「有過前面這些commit才有今天的我」，一個接一個。
* 除了一條線的發展之外，Git的分支功能讓你可以在同一個時間點進行不同的發展
  * 你可以建立新分支A嘗試做法，改壞了之後放棄A回到原本建立A的commit再次建立分支B，用不同的做法。
  * 也可以和其他人分別使用不同的分支(有人過程中刪掉/新增分支也不影響你)，最後再合併到原本的主分支

### Repository (儲存庫)

- **本地儲存庫 (Local Repository)**: 存在你電腦上的版本控制資料庫，你pull或clone下來的資料夾
- **遠端儲存庫 (Remote Repository)**: 存在伺服器上的版本控制資料庫，你pull或clone下來的來源

### 工作區域

- **工作目錄 (Working Directory)**: 你正在編輯的檔案
- **暫存區 (Staging Area)**: 準備commit的檔案
- **本地儲存庫 (Local Repository)**: 已commit的檔案

### 分支 (Branch)

- **主分支 (Main/Master)**: 預設的主要開發分支
- **功能分支 (Feature Branch)**: 開發新功能時創建的分支

## 常用指令

### 初始設定

```bash
# 設定使用者名稱和信箱
git config --global user.name "你的名字"
git config --global user.email "你的信箱@example.com"

# 查看設定
git config --list
```

### 建立儲存庫

```bash
# 初始化新的 Git 儲存庫
git init

# 複製現有的遠端儲存庫
git clone <儲存庫網址>
```

### 基本工作流程

```bash
# 查看檔案狀態
git status

# 將檔案加入暫存區
git add <檔案名稱>
git add .                    # .是指當前目錄，所以在專案根目錄執行等同加入整個專案的修改

# 提交變更
git commit -m "commit說明"

# 查看commit歷史
git log
```

### 分支操作

```bash
# 查看所有分支
git branch

# 創建新分支
git branch <分支名稱>

# 切換分支
git checkout <分支名稱>
git switch <分支名稱>        # 較新的指令

# 創建並切換到新分支
git checkout -b <分支名稱>
git switch -c <分支名稱>     # 較新的指令

# 合併分支
git merge <分支名稱>

# 刪除分支
git branch -d <分支名稱>     # 安全刪除
git branch -D <分支名稱>     # 強制刪除
```

### 遠端操作

```bash
# 查看遠端儲存庫
git remote -v

# 加入遠端儲存庫
git remote add origin <儲存庫網址>

# 推送到遠端
git push origin <分支名稱>
git push -u origin main     # 設定上游分支

# 從遠端拉取
git pull origin <分支名稱>
git fetch origin            # 只下載不合併
```

### 檔案操作

```bash
# 查看檔案差異
git diff                    # 工作目錄 vs 暫存區

# 取消暫存
git reset HEAD <檔案名稱>

# 恢復檔案到最後一個commit的狀態
git checkout <檔案名稱>

# 移動或重新命名檔案
git mv <舊檔名> <新檔名>

# 刪除檔案
git rm <檔案名稱>
```

### 歷史操作

```bash
# 查看commit歷史
git log

# 將檔案切換到特定commit，如果要從這開始開發需要建立新的分支
git checkout <commit ID>

# 修改最後一次commit，可以把其他改動加進去，也可以修改commit內容
git commit --amend

# 重置到特定commit
git reset <commit ID>
```

## 使用情境

### 日常開發流程

1. `git status` - 查看目前狀態
2. `git add .` - 加入所有變更
3. `git commit -m "描述變更"` - commit
4. `git push` - 推送到遠端

### 開發新功能

1. `git checkout -b feature/new-feature` - 創建功能分支
2. 開發並commit
3. `git push origin feature/new-feature` - 推送分支
4. 建立 Pull Request 或 Merge Request

### 解決衝突

1. `git pull` - 拉取最新變更
2. 手動解決衝突
3. `git add .` - 加入解決後的檔案
4. `git commit` - 完成解衝突的commit

## 建議使用版控的注意事項

1. **頻繁，而且用意明確的commit**: 小而頻繁的commit比低頻率且範圍較大的commit更好
   * 就和寫程式一樣，commit的時候要知道為什麼
   * 初學者普遍認為完成整個功能才commit
   * 問：你覺得馬力歐的存檔點只在拉下旗子後才有，遊戲會變得比較簡單嗎？
   * 完成定義API接口規格、完成某API功能、完成測試...，可以賦予一個主題，完成一件階段性任務的時候都是commit的好時機。
   * 不同的需求、不同主題的修改內容應該分成不同的commit，或者有合理的理由他們需要共用存檔點
2. **分支策略**: 為每個功能創建獨立分支
   * 分支如何命名，如何合併等等請遵照所屬專案的開發規範
3. **定期同步**: 經常從遠端拉取最新變更
   * 從太舊的版本切分支開發，就像從錯誤的分支開發一樣，給自己找麻煩
4. **檢查狀態**: 先使用 `git status` 和 `git diff` 檢查變更再commit，確認是否有遺漏
   * 近年開發工具整合git功能做的很方便，也可以在介面上確認

<!--Finish-->