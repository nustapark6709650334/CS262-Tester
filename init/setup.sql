/*
  FILE: setup.sql
*/

-- 1. สลับไปใช้ Database 'myDB1'
USE myDB1;

-- 2. สร้างตาราง 'courses_N' (ต้องมี END; ปิด)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='courses_N' and xtype='U')
BEGIN
    CREATE TABLE dbo.courses_N (
        course_code NVARCHAR(50) PRIMARY KEY,
        course_name NVARCHAR(255) NOT NULL,
        course_detail NVARCHAR(MAX),
        course_group NVARCHAR(255),
        course_permission NVARCHAR(255),
        course_next NVARCHAR(255),
        credit INT NOT NULL
    );
END; -- <-- ⭐️ คุณลืมตัวนี้ครับ

-- 3. สร้างตาราง 'courses_S' (แยกเป็นอีกบล็อกหนึ่ง)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='courses_S' and xtype='U')
BEGIN
    CREATE TABLE dbo.courses_S (
        course_code NVARCHAR(50) PRIMARY KEY,
        course_name NVARCHAR(255) NOT NULL,
        course_detail NVARCHAR(MAX),
        course_group NVARCHAR(255),
        course_permission NVARCHAR(255),
        course_next NVARCHAR(255),
        credit INT NOT NULL
    );
END;

-- (ไม่ต้องมี GO ถูกต้องแล้วครับ)