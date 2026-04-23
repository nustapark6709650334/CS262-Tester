package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; // ต้องมีอันนี้
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.model.CourseS;
import com.example.demo.repository.CourseRepositoryS;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseRepositoryS courseRepositoryS; // เปลี่ยนชื่อให้ตรงกับหน้าที่ [cite: 20]

    @Test
    public void testSearchCourseSByCodeSuccess() throws Exception {
        // 1. กำหนดข้อมูลสมมติ (Mock Data)
        CourseS mockCourse = new CourseS("CS101", "Introduction to CS", null, null, null, null, 0);
        
        // แก้ไข: ใช้ List.of และ anyString() เพื่อให้ Match กับสิ่งที่ Controller เรียก 
        Mockito.when(courseRepositoryS.findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(anyString(), anyString()))
               .thenReturn(List.of(mockCourse));

        // 2. จำลองการเรียก API
        mockMvc.perform(get("/api/coursesS")
                .param("query", "CS101"))
                .andExpect(status().isOk()) // ตรวจสอบ Status 200 [cite: 81]
                .andExpect(jsonPath("$[0].courseCode").value("CS101")) // ถ้าคืนเป็น List ต้องมี [0] 
                .andExpect(jsonPath("$[0].courseName").value("Introduction to CS"));
    }
}