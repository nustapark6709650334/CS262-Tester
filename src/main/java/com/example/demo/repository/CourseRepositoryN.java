package com.example.demo.repository;

import com.example.demo.model.CourseN;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepositoryN extends JpaRepository<CourseN, String> {
    // ค้นหาจากชื่อวิชาหรือรหัสวิชา
    List<CourseN> findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(String courseName, String courseCode);
}