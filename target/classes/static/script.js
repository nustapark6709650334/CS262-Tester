// =======================================================
// === 1. "ยาม FRONTEND" (Security Check) ===
// =======================================================
const authToken = localStorage.getItem('authToken'); 

if (!authToken) {
    alert('คุณยังไม่ได้เข้าสู่ระบบ กรุณา Login ก่อนครับ');
    window.location.href = 'index.html'; 
    throw new Error('Authentication required. Redirecting to login.');
}

// =======================================================
// === 2. Helper Functions (Utilities) ===
// =======================================================
function getAuthToken() {
    return localStorage.getItem('authToken');
}

function getAuthHeaders() {
    const token = getAuthToken();
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json' 
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
}

// =======================================================
// === 3. Logic หลัก (Main Execution) ===
// =======================================================
document.addEventListener('DOMContentLoaded', () => {

    // --- 1. ระบบเลือก API อัตโนมัติ (S หรือ N) ---
    const path = window.location.pathname;
    let endpoint = '/coursesS'; // ค่าเริ่มต้น

    if (path.includes('mainNormal.html') || path.includes('curriculumNormal.html')) {
        endpoint = '/coursesN';
        sessionStorage.setItem('currentApiMode', '/coursesN');
    } 
    else if (path.includes('main.html') || path.includes('curriculum.html')) {
        endpoint = '/coursesS';
        sessionStorage.setItem('currentApiMode', '/coursesS');
    } 
    else if (path.includes('courses-detail.html')) {
        const savedMode = sessionStorage.getItem('currentApiMode');
        if (savedMode) {
            endpoint = savedMode;
        }
    }

    const FULL_API_URL = `/api${endpoint}`; 
    // ---------------------------------------------------

    // === ส่วนที่ 1: Universal Code (ใช้ได้ทุกหน้า) ===
    const backButton = document.querySelector('.btn-back');
    if (backButton) {
        backButton.addEventListener('click', () => {
            // เช็คว่าจะย้อนกลับไปหน้าหลักอันไหน (S หรือ N)
            if (endpoint === '/coursesN') window.location.href = 'mainNormal.html'; 
            else window.location.href = 'main.html'; 
        });
    }

    // ปุ่มออกจากระบบ (มีในทุกหน้า)
    const btnLogout = document.getElementById('btnLogout');
    if (btnLogout) {
        btnLogout.addEventListener('click', () => {
            localStorage.removeItem('authToken');
            sessionStorage.removeItem('currentApiMode');
            alert('ออกจากระบบสำเร็จ');
            window.location.href = 'index.html';
        });
    }

    // คลิกที่รหัสวิชา (Link ไปหน้า detail)
    document.querySelectorAll('.subject').forEach(span => {
        span.addEventListener('click', () => {
            let courseId = span.id;
            if (!courseId) {
                 courseId = span.textContent.trim().replace('คพ.', 'CS').replace('มธ.', 'TU').replace('ส.', 'ST').replace('ค.', 'MA').replace('ศศ.', 'SS').replace('สษ.', 'EL');
            }
            if (courseId) {
                window.location.href = `courses-detail.html?course=${courseId}`;
            }
        });
    });

    // === ส่วนที่ 2: หน้าค้นหา (main.html / mainNormal.html) ===
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        const searchResultsDiv = document.getElementById('search-results');
	function normalizeSearchQuery(raw) {
		    if (!raw) return '';

		    let q = raw.trim();

		    // รวมช่องว่างซ้ำ ๆ
		    q = q.replace(/\s+/g, ' ');
		    // แก้ "คพ. 101" → "คพ.101"
		    q = q.replace(/(\.)\s+/g, '$1');

		    // ถ้าขึ้นต้นด้วยคพ. ให้เปลี่ยนเป็น CS
		    if (/^คพ\./i.test(q)) {
		        q = q.replace(/^คพ\./i, 'CS');
		    }

		    // ถ้าขึ้นต้นด้วยคพ (ไม่มีจุด) ก็รองรับ เช่น "คพ101" → "CS101"
		    else if (/^คพ/i.test(q)) {
		        q = q.replace(/^คพ/i, 'CS');
		    }

		    return q;
		}
		
        const searchCourses = async (query) => {
            if (!query || query.trim().length < 2) {
                searchResultsDiv.innerHTML = '';
                searchResultsDiv.style.display = 'none';
                return;
            }
		const apiQuery = normalizeSearchQuery(query);
            try {
                const response = await fetch(
                    `${FULL_API_URL}?query=${encodeURIComponent(apiQuery)}`, 
                    { headers: getAuthHeaders() }
                );

                if (response.status === 401) {
                    alert('Session หมดอายุ กรุณา Login ใหม่');
                    localStorage.removeItem('authToken');
                    window.location.href = 'index.html';
                    return;
                }
                
                const courses = await response.json();
                
                searchResultsDiv.innerHTML = '';
                searchResultsDiv.style.display = 'block';

                if (courses.length === 0) {
                    searchResultsDiv.innerHTML = '<div class="search-item">ไม่พบรายวิชา</div>';
                    return;
                }
                
                courses.forEach(course => {
                    const courseEl = document.createElement('div');
                    courseEl.className = 'search-item'; 
                    
                    // [แก้ไขตรงนี้] : ดักจับชื่อตัวแปรทุกรูปแบบที่เป็นไปได้
                    // ถ้า Backend ส่งมาเป็น courseCode (Java Standard) หรือ course_code (DB Style) ก็จะรับได้หมด
                    const code = course.courseCode || course.course_code || course.id || "ไม่พบรหัส";
                    const name = course.courseName || course.course_name || course.name || "ไม่พบชื่อวิชา";

                    courseEl.textContent = `${code} - ${name}`;
                    
                    courseEl.addEventListener('click', () => {
                        // ส่งรหัสที่ถูกต้องไป
                        window.location.href = `courses-detail.html?course=${code}`;
                    });
                    searchResultsDiv.appendChild(courseEl);
                });
            } catch (error) {
                console.error("Search Error:", error); // เพิ่ม log เพื่อดู error ใน console
                searchResultsDiv.innerHTML = '<div class="search-item error">เกิดข้อผิดพลาด</div>';
            }
        };

        const curriculumBtn = document.querySelector('.title-card .btn');
        if (curriculumBtn) {
            curriculumBtn.addEventListener('click', () => {
                const currentMode = sessionStorage.getItem('currentApiMode'); 

                if (currentMode === '/coursesN') {
                    window.location.href = 'curriculumNormal.html'; 
                } else {
                    window.location.href = 'curriculum.html'; 
                }
            });
        }
        
        let debounceTimer;
        searchInput.addEventListener('input', () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                searchCourses(searchInput.value);
            }, 400);
        });
    }

    // === ส่วนที่ 3: หน้ารายละเอียด (courses-detail.html) ===
    const courseHeader = document.getElementById('course-header');
    if (courseHeader) {
        const courseCredit = document.getElementById('course-credit');
        const courseDescription = document.getElementById('course-description');
        const prerequisiteCoursesDiv = document.getElementById('prerequisite-courses');
        const nextCoursesDiv = document.getElementById('next-courses');

        const fetchCourseDetails = async (id) => {
            try {
                // เรียก API ตาม endpoint ที่เลือกไว้
                const response = await fetch(
                    `${FULL_API_URL}/${id}`, 
                    { headers: getAuthHeaders() }
                );

                if (response.status === 401) {
                    alert('Session หมดอายุ');
                    localStorage.removeItem('authToken');
                    window.location.href = 'index.html';
                    return;
                }
                if (!response.ok) throw new Error('Load failed');
                
                const course = await response.json();

                // --- แปลงข้อมูล (Data Mapping) ---
                // แปลง String จาก Java ให้เป็น Array ใน JS
                if (course.coursePermission && course.coursePermission !== '-') {
                    course.prerequisites = course.coursePermission.split(',').map(s => s.trim());
                } else {
                    course.prerequisites = [];
                }

                if (course.courseNext && course.courseNext !== '-') {
                    course.nextCourses = course.courseNext.split(',').map(s => s.trim());
                } else {
                    course.nextCourses = [];
                }
                // -------------------------------

                // --- แสดงผล (Rendering) ---
                document.title = `${course.courseCode}`;
                courseHeader.textContent = `${course.courseCode} — ${course.courseName}`;
                courseCredit.innerHTML = `${course.courseGroup || 'วิชา'} จำนวน <b>${course.credit} หน่วยกิต</b>`;
                courseDescription.textContent = course.courseDetail || 'ไม่มีคำอธิบาย';

                // แสดง Prerequisite
                prerequisiteCoursesDiv.innerHTML = '';
                if (course.prerequisites && course.prerequisites.length > 0) {
                    course.prerequisites.forEach(preReqId => {
                        const link = document.createElement('a');
                        link.href = `courses-detail.html?course=${preReqId}`;
                        link.className = 'subject';
                        link.textContent = preReqId;
                        prerequisiteCoursesDiv.appendChild(link);
                        prerequisiteCoursesDiv.append(' '); // เว้นวรรค
                    });
                } else {
                    prerequisiteCoursesDiv.textContent = 'ไม่มี';
                }

                // แสดง Next Courses
                nextCoursesDiv.innerHTML = '';
                if (course.nextCourses && course.nextCourses.length > 0) {
                     course.nextCourses.forEach(nextId => {
                        const link = document.createElement('a');
                        link.href = `courses-detail.html?course=${nextId}`;
                        link.className = 'subject';
                        link.textContent = nextId;
                        nextCoursesDiv.appendChild(link);
                        nextCoursesDiv.append(' '); // เว้นวรรค
                    });
                } else {
                    nextCoursesDiv.textContent = 'ไม่มี';
                }

            } catch (error) {
                courseHeader.textContent = `เกิดข้อผิดพลาด`;
                courseDescription.textContent = `ไม่สามารถโหลดข้อมูลสำหรับวิชา ${id} ได้`;
                console.error(error);
            }
        };

        const params = new URLSearchParams(window.location.search);
        const courseId = params.get('course');
        if (courseId) {
            fetchCourseDetails(courseId);
        } else {
            courseHeader.textContent = 'ไม่พบรายวิชา';
        }
    }
});