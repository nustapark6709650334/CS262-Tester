package com.example.demo.controller;

import com.example.demo.model.CourseN;
import com.example.demo.repository.CourseRepositoryN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiControllerN {

    @Autowired
    private CourseRepositoryN courseRepositoryN;
    
    // สำหรับค้นหาวิชา
    @GetMapping("/coursesN")
    public List<CourseN> searchCourses(@RequestParam(required = false) String query) {
        if (query == null || query.isBlank()) {
            return courseRepositoryN.findAll();
        }
        return courseRepositoryN.findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(query, query);
    }
    
    // สำหรับดูรายละเอียดวิชาเดียว
    @GetMapping("/coursesN/{id}")
    public ResponseEntity<CourseN> getCourseById(@PathVariable String id) {
        return courseRepositoryN.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    
}