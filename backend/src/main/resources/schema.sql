-- 创建数据库
CREATE DATABASE IF NOT EXISTS ielts_helper DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ielts_helper;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    phone VARCHAR(11) NOT NULL UNIQUE COMMENT '手机号',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    username VARCHAR(50) COMMENT '用户名',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    gender TINYINT COMMENT '性别：1-男，2-女',
    age INT COMMENT '年龄',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 教师表
CREATE TABLE IF NOT EXISTS teachers (
    teacher_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '教师ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    qualification TEXT NOT NULL COMMENT '资历',
    teaching_style TEXT COMMENT '教学风格',
    bio TEXT COMMENT '个人简介',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
    course_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
    title VARCHAR(100) NOT NULL COMMENT '课程标题',
    type TINYINT NOT NULL COMMENT '课程类型：1-Listening, 2-Speaking, 3-Reading, 4-Writing',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    price DECIMAL(10, 2) NOT NULL COMMENT '价格（元）',
    description TEXT COMMENT '课程描述',
    is_open BOOLEAN DEFAULT TRUE COMMENT '是否开放预约',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 预约/选课表
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-pending_payment, 2-confirmed, 3-in_progress, 4-completed, 5-cancelled',
    order_id VARCHAR(100) COMMENT '订单号',
    paid_amount DECIMAL(10, 2) COMMENT '支付金额',
    paid_at DATETIME COMMENT '支付时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约/选课表';

-- 验证码表
CREATE TABLE IF NOT EXISTS verification_codes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    phone VARCHAR(11) NOT NULL COMMENT '手机号',
    code VARCHAR(6) NOT NULL COMMENT '验证码',
    type VARCHAR(20) NOT NULL COMMENT '类型：register, reset',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    used BOOLEAN DEFAULT FALSE COMMENT '是否已使用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码表';

-- 对话表
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '对话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(20) NOT NULL COMMENT '对话标题',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话表';

-- 消息表
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    conversation_id BIGINT NOT NULL COMMENT '对话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色：user/assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    FOREIGN KEY (conversation_id) REFERENCES conversations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 插入初始数据

-- 插入教师数据
INSERT INTO teachers (name, qualification, teaching_style, bio) VALUES
('张老师', '剑桥认证雅思培训师', '互动式教学，注重实战', '毕业于牛津大学，拥有10年雅思教学经验，帮助众多学生获得7分以上成绩。'),
('李老师', '雅思官方认证讲师', '逻辑清晰，方法独特', '前雅思考官，熟悉考试评分标准，擅长写作和阅读教学。'),
('王老师', '英语专业八级', NULL, '拥有8年教学经验，专注于口语和听力训练。'),
('刘老师', '剑桥大学语言学硕士', '学术严谨，注重基础', NULL),
('陈老师', '澳洲莫纳什大学教育学硕士', '因材施教，耐心细致', '专注于雅思阅读教学多年，独创阅读高分技巧体系。'),
('赵老师', '雅思前考官', '点评精准，直击要害', '曾任雅思官方口语考官，深谙考试评分标准。'),
('孙老师', '英国帝国理工学院硕士', '严谨认真，循循善诱', '擅长雅思写作教学，帮助学生快速突破写作瓶颈。'),
('周老师', '美国哥伦比亚大学TESOL专业', '生动有趣，互动性强', '专注雅思口语教学，发音标准，帮助众多学生克服口语恐惧。');

-- 插入课程数据
INSERT INTO courses (title, type, teacher_id, start_time, end_time, price, description, is_open) VALUES
('雅思写作高分技巧', 4, 2, '2026-05-10 14:00:00', '2026-05-10 16:00:00', 299.00, '适合目标7分以上学员', TRUE),
('雅思口语冲刺班', 2, 1, '2026-05-12 09:00:00', '2026-05-12 11:00:00', 399.00, '针对口语薄弱学员，快速提升口语能力', TRUE),
('雅思阅读技巧提升', 3, 3, '2026-05-15 16:00:00', '2026-05-15 18:00:00', 249.00, '掌握阅读技巧，提高阅读速度', TRUE),
('雅思听力强化训练', 1, 4, '2026-05-18 10:00:00', '2026-05-18 12:00:00', 279.00, '强化听力训练，提高听力得分', TRUE),
('雅思口语实战演练', 2, 6, '2026-05-20 14:00:00', '2026-05-20 17:00:00', 459.00, '模拟真实考试场景，快速提升口语表达能力', TRUE),
('雅思写作精批精改', 4, 7, '2026-05-22 09:00:00', '2026-05-22 12:00:00', 359.00, '名师一对一作文批改，直击写作痛点', TRUE),
('雅思阅读真题精讲', 3, 5, '2026-05-25 15:00:00', '2026-05-25 18:00:00', 329.00, '深入分析真题规律，掌握高效解题方法', TRUE),
('雅思听力满分冲刺', 1, 4, '2026-05-28 10:00:00', '2026-05-28 13:00:00', 299.00, '冲刺听力满分，突破重点难点', TRUE),
('雅思全科强化班', 4, 1, '2026-06-01 09:00:00', '2026-06-01 17:00:00', 899.00, '全科系统学习，一站式备考冲刺', TRUE),
('雅思VIP一对一', 2, 6, '2026-06-05 10:00:00', '2026-06-05 12:00:00', 699.00, '名师一对一专属辅导，个性化教学', TRUE);
