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
import com.example.demo.model.CourseN;
import com.example.demo.repository.CourseRepositoryN;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // ปิด Security เพื่อแก้ 403 Forbidden [cite: 78]
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseRepositoryS courseRepositoryS; // ตัวที่คุณใช้ทดสอบ

    @MockBean
    private CourseRepositoryN courseRepositoryN; // เพิ่มตัวนี้เพื่อให้ Application Context โหลดผ่าน

    @Test
    public void testSearchCourseSByCodeSuccess() throws Exception {
        CourseS mockCourse = new CourseS("CS101", "Introduction to CS", null, null, null, null, 0);
        
        // Mock ให้คืนค่าเป็น List ตามที่ Repository นิยามไว้ [cite: 66]
        Mockito.when(courseRepositoryS.findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(anyString(), anyString()))
               .thenReturn(List.of(mockCourse));

        mockMvc.perform(get("/api/coursesS") // เช็ค Path ให้ตรงกับที่ระบุใน Controller [cite: 74]
                .param("query", "CS101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }
}