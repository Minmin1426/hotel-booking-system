@echo off
chcp 65001 > nul
echo =======================================================
echo          HOTEL BOOKING SYSTEM - SETUP TOOL
echo =======================================================
echo.

echo [1/3] Cai dat thu vien Frontend (ReactJS + Stripe)...
cd frontend
call npm install
cd ..
echo OK! Da cai dat xong thu vien Frontend.
echo.

echo [2/3] Kiem tra thu vien Backend (Java Maven)...
echo Luu y: Ban co the bo qua buoc nay vi IntelliJ/Eclipse se tu dong tai thu vien (Auto-Reload).
call mvn dependency:resolve
echo.

echo [3/3] Cau hinh file moi truong (application-dev.properties)...
if not exist "src\main\resources\application-dev.properties" (
    copy "src\main\resources\application-dev.properties.example" "src\main\resources\application-dev.properties"
    echo DA TAO FILE application-dev.properties.
    echo VUI LONG MO FILE NAY TRONG IDE VA DIEN MAT KHAU DB KEM STRIPE KEY!
) else (
    echo File application-dev.properties da ton tai tren may ban.
    echo Vui long kiem tra xem da them dong "stripe.api.key=sk_test_..." vao chua nhe.
)
echo.

echo =======================================================
echo HOAN TAT! BAN CO THE CHAY DU AN NGAY BAY GIO!
echo =======================================================
pause
