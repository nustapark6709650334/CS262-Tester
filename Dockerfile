# Stage 1: ใช้ Maven เพื่อ build โปรเจกต์ให้เป็นไฟล์ .jar
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: สร้าง Image สุดท้าย
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy ไฟล์ .jar จาก Stage 1 มา (ใช้ Wildcard เพื่อความยืดหยุ่น)
COPY --from=builder /app/target/*.jar app.jar

# บอกว่าแอปของคุณทำงานที่ Port 8081
EXPOSE 8081

# คำสั่งสำหรับเปิดแอป
ENTRYPOINT ["java", "-jar", "app.jar"]