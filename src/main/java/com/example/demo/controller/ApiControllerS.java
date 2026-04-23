package com.example.demo.controller;

import com.example.demo.model.CourseS;
import com.example.demo.repository.CourseRepositoryS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiControllerS {

    @Autowired
    private CourseRepositoryS courseRepositoryS;
    
    // สำหรับค้นหาวิชา
    @GetMapping("/coursesS")
    public List<CourseS> searchCourses(@RequestParam(required = false) String query) {
        if (query == null || query.isBlank()) {
            return courseRepositoryS.findAll();
        }
        return courseRepositoryS.findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(query, query);
    }
    
    // สำหรับดูรายละเอียดวิชาเดียว
    @GetMapping("/coursesS/{id}")
    public ResponseEntity<CourseS> getCourseById(@PathVariable String id) {
        return courseRepositoryS.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    
}