package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "courses_S")
public class CourseS {

    @Id
    @Column(name = "course_code", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String courseCode;

    @Column(name = "course_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String courseName;

    @Lob
    @Column(name = "course_detail", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String courseDetail;

    @Column(name = "course_group", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String courseGroup;

    @Column(name = "course_permission", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String coursePermission;
    
    @Column(name = "course_next", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String courseNext;
    
    @Column(name = "credit", nullable = false)
    private int credit;

    public CourseS() {}

    public CourseS(String courseCode, String courseName, String courseDetail, String courseGroup, String coursePermission, String courseNext, int credit) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.courseDetail = courseDetail;
        this.courseGroup = courseGroup;
        this.coursePermission = coursePermission;
        this.courseNext = courseNext;
        this.credit = credit;
    }

    // --- Getters and Setters ---
    
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCourseDetail() { return courseDetail; }
    public void setCourseDetail(String courseDetail) { this.courseDetail = courseDetail; }

    public String getCourseGroup() { return courseGroup; }
    public void setCourseGroup(String courseGroup) { this.courseGroup = courseGroup; }

    public String getCoursePermission() { return coursePermission; }
    public void setCoursePermission(String coursePermission) { this.coursePermission = coursePermission; }

    public String getCourseNext() { return courseNext; }
    public void setCourseNext(String courseNext) { this.courseNext = courseNext; }

    public int getCredit() { return credit; }
    public void setCredit(int credit) { this.credit = credit; }
}