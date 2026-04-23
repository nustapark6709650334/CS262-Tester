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

                    // C. ตรวจสอบรหัส 2 ตัวกลาง (08 หรือ 09)
                    if (userCode && userCode.length >= 4) {
                        // ดึงอักขระตัวที่ 3 และ 4 (index ที่ 2 และ 3)
                        const programCode = userCode.substring(4, 6); 

                        if (programCode === '65') {
                            // ไปหน้าภาคพิเศษ (อ้างอิงจากไฟล์ในรูปแรกของคุณ)
                            alert('เข้าสู่ระบบสำเร็จ (ภาคพิเศษ)');
                            window.location.href = 'main.html'; 
                        
                        } else if (programCode === '61') {
                            // ไปหน้าภาคปกติ (อ้างอิงจากไฟล์ในรูปแรกของคุณ)
                            alert('เข้าสู่ระบบสำเร็จ (ภาคปกติ)');
                            window.location.href = 'mainNormal.html';
                        
                        } else {
                            // กรณีอื่นๆ (เช่น รหัสไม่ตรงเงื่อนไข หรือเป็น admin/staff)
                            alert('เข้าสู่ระบบสำเร็จ');
                            window.location.href = 'index.html'; // ไปหน้าหลัก
                        }
                    
                    } else {
                        // กรณีได้ username กลับมา แต่รูปแบบแปลกๆ
                        console.error('Invalid username format received:', userCode);
                        alert('เข้าสู่ระบบสำเร็จ (ไม่ทราบประเภท)');
                        window.location.href = 'index.html'; // ไปหน้าหลัก
                    }

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