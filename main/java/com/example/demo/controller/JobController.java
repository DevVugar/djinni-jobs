package com.example.demo.controller;


import com.example.demo.model.dto.JobDTO;
import com.example.demo.model.dto.JobFilterRequest;
import com.example.demo.model.dto.PaginatedResponse;
import com.example.demo.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;


    @GetMapping
    public ResponseEntity<PaginatedResponse<JobDTO>> getAllJobs(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "postedDate") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

        PaginatedResponse<JobDTO> jobs = jobService.getAllJobs(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDTO> getJobById(@PathVariable Long id) {
        JobDTO job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/filter")
    public ResponseEntity<PaginatedResponse<JobDTO>> filterJobs(@RequestBody JobFilterRequest filterRequest) {
        PaginatedResponse<JobDTO> jobs = jobService.filterJobs(filterRequest);
        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/scrape")
    public ResponseEntity<List<JobDTO>> scrapeJobs() {
        List<JobDTO> scrapedJobs = jobService.scrapeAndSaveJobs();
        return ResponseEntity.status(HttpStatus.CREATED).body(scrapedJobs);
    }
}