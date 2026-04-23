document.addEventListener('DOMContentLoaded', () => {
    
    const loginForm = document.getElementById('login-form');
    const loginErrorDiv = document.getElementById('login-error');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault(); // หยุดการ submit ฟอร์มแบบดั้งเดิม

            // 1. ซ่อนข้อความ Error เก่า (ถ้ามี)
            loginErrorDiv.style.display = 'none';
            loginErrorDiv.textContent = '';

            // 2. ดึงข้อมูลจากฟอร์ม
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            // 3. สร้างข้อมูลที่จะส่งไป Backend
            const loginData = {
                username: username,
                password: password
            };

            // 4. ส่งข้อมูลไปที่ Backend
            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(loginData)
                });

                // ========================================================
                // === (5.1) นี่คือส่วนที่แก้ไข ตามโจทย์ของคุณ ===
                // ========================================================
                if (response.ok) {
                    // 5.1 ถ้าล็อกอินสำเร็จ
                    const result = await response.json(); 
                    // result จะมีหน้าตาแบบนี้:
                    // { "token": "...", "username": "57090001", "displayName": "..." }
                    
                    // A. เก็บ Token ไว้ใน localStorage
                    if (result.token) {
                        localStorage.setItem('authToken', result.token); 
                    }
                    
                    // B. ดึง username ที่ได้จาก Backend
                    const userCode = result.username; 

                    // C. ตัดสินใจปลายทางด้วย AuthUtils (ทดสอบแยกได้ใน Jest)
                    const route = window.AuthUtils.resolveProgramRoute(userCode);
                    if (route.program === 'special') {
                        alert('เข้าสู่ระบบสำเร็จ (ภาคพิเศษ)');
                    } else if (route.program === 'normal') {
                        alert('เข้าสู่ระบบสำเร็จ (ภาคปกติ)');
                    } else {
                        console.warn('Unrecognised username format for routing:', userCode);
                        alert('เข้าสู่ระบบสำเร็จ');
                    }
                    window.location.href = route.url;

                } else {
                    // 5.2 ถ้า Backend ตอบกลับมาว่า Error (เช่น รหัสผ่านผิด)
                    const errorData = await response.json();
                    
                    // (เราแก้ไข Constructor ของ LoginResponse ให้ส่ง message กลับมา แม้จะล้มเหลว)
                    showError(errorData.message || 'ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง');
                }
                // ========================================================
                // === จบส่วนที่แก้ไข ===
                // ========================================================

            } catch (error) {
                // 5.3 ถ้าเกิดข้อผิดพลาดในการเชื่อมต่อ (เช่น Backend ไม่ได้รัน)
                console.error('Login Error:', error);
                showError('ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้');
            }
        });
    }

    // ฟังก์ชันสำหรับแสดงข้อความ Error
    function showError(message) {
        loginErrorDiv.textContent = message;
        loginErrorDiv.style.display = 'block';
    }

});