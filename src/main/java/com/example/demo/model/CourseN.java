package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "courses_N")
public class CourseN {

	@Id
	@Column(name = "course_code", nullable = false, unique = true, columnDefinition = "NVARCHAR(255)")
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


}