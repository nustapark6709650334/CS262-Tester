package com.example.demo.repository;

import com.example.demo.model.CourseS;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepositoryS extends JpaRepository<CourseS, String> {
    // ค้นหาจากชื่อวิชาหรือรหัสวิชา
    List<CourseS> findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(String courseName, String courseCode);
}