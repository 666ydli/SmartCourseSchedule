# SmartCourseSchedule

# 📝 Description

**SmartCourseSchedule** 是一个用于管理课程安排和作业提醒的 Android 应用。

应用支持：

- 多课表管理  
- 课程时间安排  
- 作业提醒  
- 今日课程桌面小组件  

用户可以快速查看每天课程信息，并高效进行学习管理。

该项目采用 **MVVM 架构设计**，结合 **Room 本地数据库** 与 **LiveData 数据观察机制**，实现课程信息的持久化管理和界面自动更新。

---

# ✨ Features

- 📅 **多课表管理**  
  支持创建、切换和删除不同课表

- 📚 **课程信息管理**  
  添加、编辑和删除课程

- ⏰ **灵活的课程时间设置**  
  支持设置星期、节次和周次

- ⚠️ **课程冲突检测**  
  自动检测课程时间冲突

- 📝 **作业管理与提醒**  
  支持作业记录与截止时间提醒

- 📱 **今日课程桌面小组件**  
  在桌面查看当天课程

- 📍 **课程地点导航**  
  支持跳转地图查看课程地点

- 📊 **课表视图展示**  
  周视图展示课程安排

---

# 🛠 Tech Stack

本项目主要使用以下技术实现：

| 技术 | 说明 |
|-----|-----|
| **Java** | Android 应用开发语言 |
| **MVVM** | 应用架构模式 |
| **Room** | 本地数据库 |
| **LiveData + ViewModel** | 状态管理 |
| **Hilt** | 依赖注入 |
| **ViewBinding** | 视图绑定 |
| **RecyclerView** | 列表 UI |
| **Glide** | 图片加载 |
| **AppWidget** | 桌面小组件 |
| **AlarmManager** | 作业提醒通知 |

---

# 📷 Screenshots

<p align="center">

### 课表主界面
<img src="screenshots/schedule.jpg" width="260"/>

### 添加课程
<img src="screenshots/add_course.jpg" width="260"/>

### 作业管理
<img src="screenshots/homework.jpg" width="260"/>

### 今日课程桌面小组件
<img src="screenshots/widget.png" width="220"/>

</p>

> 可以将这些截图替换为实际应用截图。

---

# 🚀 Main Functions

### 📚 课程管理

用户可以创建课程并设置：

- 课程名称
- 教师
- 上课地点
- 上课节次
- 上课周次

---

### 📅 课表视图

以 **周视图** 形式展示课程安排，帮助用户快速查看每天课程情况。

---

### 🔔 作业提醒

支持为课程添加作业并设置提醒时间，系统会通过通知提醒用户完成作业。

---

### 📱 桌面小组件

桌面小组件可显示 **当天课程**，无需打开应用即可查看今日安排。

---
## How to Run

1. 克隆项目到本地

```bash
git clone https://github.com/666ydli/SmartCourseSchedule.git
```
2. 使用 Android Studio 打开项目

3. 等待 Gradle 同步完成

4. 连接 Android 模拟器或真机

5. 点击 Run 运行应用

## License

This project is intended for learning and demonstration purposes.
