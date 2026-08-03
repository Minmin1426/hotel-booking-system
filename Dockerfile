# --- Stage 1: Build Stage ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Giới hạn bộ nhớ cho Maven trong quá trình build để tránh OOM exit 137 trên Render
ENV MAVEN_OPTS="-Xmx384m -XX:+UseSerialGC"

# Sao chép file pom.xml để tải dependencies trước (tận dụng Docker cache)
COPY pom.xml .
RUN mvn dependency:resolve -B

# Sao chép mã nguồn và thực hiện build jar file (bỏ qua chạy test để build nhanh hơn)
COPY src ./src
RUN mvn package -DskipTests -B

# --- Stage 2: Run Stage ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Sao chép file jar đã build thành công từ build stage
COPY --from=build /app/target/*.jar app.jar

# Khai báo port ứng dụng chạy trong container
EXPOSE 8080

# Chạy ứng dụng Spring Boot với cấu hình giới hạn RAM tối ưu (giữ dưới 300MB RAM)
ENTRYPOINT ["java", "-Xmx280m", "-Xms128m", "-XX:+UseSerialGC", "-jar", "app.jar"]
