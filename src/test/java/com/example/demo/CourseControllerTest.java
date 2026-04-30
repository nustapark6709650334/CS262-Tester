package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

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
@AutoConfigureMockMvc(addFilters = false) // ปิด Security เพื่อแก้ 403 Forbidden
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseRepositoryS courseRepositoryS; // ตัวที่คุณใช้ทดสอบ

    @MockBean
    private CourseRepositoryN courseRepositoryN; // คงไว้เพื่อให้ Application Context โหลดผ่าน (จากฝั่ง integation)

    //-------------------------------------------------------------------------------------
    //1. TC-ROUTE-01: Program Routing ยืนยันว่าระบบแยกแยะผู้ใช้ภาคปกติ (N) และภาคพิเศษ (S) ได้ถูกต้อง
    //-------------------------------------------------------------------------------------
      @Test
      public void testProgramRouting() throws Exception {
          // ทดสอบฝั่ง ภาคปกติ (N)
          mockMvc.perform(get("/api/coursesN").param("query", "CS101"))
                 .andExpect(status().isOk());
          verify(courseRepositoryN).findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(anyString(), anyString());

          // ทดสอบฝั่ง ภาคพิเศษ (S)
          mockMvc.perform(get("/api/coursesS").param("query", "CS101"))
                 .andExpect(status().isOk());
          verify(courseRepositoryS).findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(anyString(), anyString());
      }
      //-------------------------------------------------------------------------------------
      //2. TC-SEARCH-01: Course Search ค้นหารหัสวิชาแล้วต้องได้ข้อมูล JSON ที่มีรายละเอียดตรงตามจริง
      //-------------------------------------------------------------------------------------
    //ภาคพิเศษ
      @Test
    public void testSearchCourseSByCodeSuccess() throws Exception {
        // 1. กำหนดข้อมูลสมมติ (Mock Data)
        CourseS mockCourse = new CourseS("CS101", "Introduction to CS", null, null, null, null, 0);
        
        // 2. Mock ให้คืนค่าเป็น List
        Mockito.when(courseRepositoryS.findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(anyString(), anyString()))
               .thenReturn(List.of(mockCourse));

        // 3. จำลองการเรียก API และตรวจสอบผลลัพธ์ (ดึงความละเอียดมาจากฝั่ง main)
        mockMvc.perform(get("/api/coursesS")
                .param("query", "CS101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"))
                .andExpect(jsonPath("$[0].courseName").value("Introduction to CS"));
    }
    //ภาคปกติ
    @Test
    public void testSearchCourseNByCodeSuccess() throws Exception {
        // 1. กำหนดข้อมูลสมมติ (Mock Data)
        CourseN mockCourse = new CourseN("CS101", "Introduction to CS", null, null, null, null, 0);
        
        // 2. Mock ให้คืนค่าเป็น List
        Mockito.when(courseRepositoryN.findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(anyString(), anyString()))
               .thenReturn(List.of(mockCourse));

        // 3. จำลองการเรียก API และตรวจสอบผลลัพธ์ (ดึงความละเอียดมาจากฝั่ง main)
        mockMvc.perform(get("/api/coursesN")
                .param("query", "CS101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"))
                .andExpect(jsonPath("$[0].courseName").value("Introduction to CS"));
    }

    //-------------------------------------------------------------------------------------
    //3. TC-DETAIL-01: Course Detail แสดงข้อมูลวิชาบังคับก่อน (Prerequisite) และวิชาถัดไป (Next Courses) ครบถ้วน
    //-------------------------------------------------------------------------------------
    @Test
    public void testCourseDetailSuccess() throws Exception {
        // 1. เตรียมข้อมูล Mock โดยใช้ฟิลด์ตาม Model จริง
        // courseCode, courseName, courseDetail, courseGroup, coursePermission, courseNext, credit
        CourseS mockDetail = new CourseS(
            "CS201", 
            "Algorithm", 
            "Detail...", 
            "Group A", 
            "CS101", // นี่คือวิชาบังคับก่อน (coursePermission)
            "CS301", // นี่คือวิชาถัดไป (courseNext)
            3
        );

        Mockito.when(courseRepositoryS.findById("CS201")).thenReturn(Optional.of(mockDetail));

        // 2. เรียก API และตรวจสอบฟิลด์ให้ตรงตาม Model
        mockMvc.perform(get("/api/coursesS/CS201")) // Path ตาม @GetMapping("/coursesS/{id}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseCode").value("CS201"))
                // แก้ไข jsonPath ให้ตรงกับชื่อใน Class CourseS[cite: 1]
                .andExpect(jsonPath("$.coursePermission").value("CS101")) 
                .andExpect(jsonPath("$.courseNext").value("CS301"));
    }

}