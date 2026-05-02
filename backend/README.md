# IELTS Helper Backend

雅思培训助手后端服务，基于Spring Boot 3.2和MyBatis-Plus开发。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0+
- JWT (jjwt 0.12.3)

## 项目结构

```
src/main/java/com/ielts/helper/
├── common/          # 通用工具类
│   ├── JwtUtil.java       # JWT工具类
│   └── Result.java        # 统一响应结果
├── config/          # 配置类
│   ├── MyBatisPlusConfig.java  # MyBatis-Plus配置
│   └── WebConfig.java          # Web配置（CORS）
├── controller/      # 控制器层
│   ├── AuthController.java      # 认证控制器
│   ├── UserController.java      # 用户控制器
│   ├── TeacherController.java   # 教师控制器
│   ├── CourseController.java    # 课程控制器
│   └── InstitutionController.java # 机构控制器
├── dto/             # 数据传输对象
├── entity/          # 实体类
├── mapper/          # Mapper接口
├── service/         # 服务接口
│   └── impl/        # 服务实现类
└── IeltsHelperApplication.java  # 主类
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

1. 创建数据库并执行SQL脚本：
```sql
CREATE DATABASE ielts_helper DEFAULT CHARACTER SET utf8mb4;
```

2. 执行 `src/main/resources/schema.sql` 初始化数据库表结构和初始数据

3. 修改 `application.yml` 中的数据库配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ielts_helper?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 3. 运行项目

```bash
# 编译项目
mvn clean install

# 运行项目
mvn spring-boot:run
```

项目将在 `http://localhost:8080` 启动

## API接口

### 认证模块 `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /register | 用户注册 |
| POST | /login | 用户登录 |
| POST | /send-code | 发送验证码 |
| POST | /reset-password | 重置密码 |

### 用户模块 `/api/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /profile | 获取个人信息 |
| PUT | /profile | 更新个人信息 |
| GET | /courses | 获取用户课程列表 |
| POST | /courses/{enrollmentId}/cancel | 取消课程 |

### 教师模块 `/api`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /teachers | 获取所有教师列表 |

### 课程模块 `/api`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /courses | 获取可预约课程列表 |
| POST | /courses/{courseId}/reserve | 预约课程 |
| GET | /payments/orders/{orderId}/status | 查询支付状态 |

### 机构模块 `/api`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /institution | 获取机构信息 |

## 配置说明

### JWT配置

```yaml
jwt:
  secret: ielts-helper-jwt-secret-key-2026-very-long-secret-key
  expiration: 86400000  # 24小时
```

### 跨域配置

项目已配置CORS，允许所有来源访问，生产环境请根据需要修改。

## 注意事项

1. 验证码功能目前仅打印到控制台，生产环境需要对接短信服务
2. 支付功能为模拟实现，生产环境需要对接微信支付API
3. 密码使用MD5加密，生产环境建议使用更安全的加密方式
