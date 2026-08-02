# --- Stage 1: Build Stage ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Sao chép file pom.xml để tải dependencies trước (tận dụng Docker cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Sao chép mã nguồn và thực hiện build jar file (bỏ qua chạy test để build nhanh hơn)
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Stage 2: Run Stage ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Sao chép file jar đã build thành công từ build stage
COPY --from=build /app/target/*.jar app.jar

# Khai báo port ứng dụng chạy trong container
EXPOSE 8080

# Chạy ứng dụng Spring Boot với cấu hình giới hạn RAM (tránh lỗi OOM exit 137 trên Render Free)
ENTRYPOINT ["java", "-Xmx320m", "-XX:+UseSerialGC", "-jar", "app.jar"]

