package com.example.demo.model.dto;


import lombok.Data;

import java.util.List;

@Data
public class JobFilterRequest {
    private String location;
    private String jobType;
    private String experienceLevel;
    private String industry;
    private List<String> tags;
    private Integer minSalary;
    private Integer maxSalary;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "postedDate";
    private String sortDirection = "desc";
}