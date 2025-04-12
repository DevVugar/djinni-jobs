package com.example.demo.service;

import com.example.demo.mapper.JobMapper;
import com.example.demo.model.dto.JobDTO;
import com.example.demo.model.dto.JobFilterRequest;
import com.example.demo.model.dto.PaginatedResponse;
import com.example.demo.model.entity.Job;
import com.example.demo.repository.JobRepository;
import com.example.demo.scraper.DjinniJobScraper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface JobService {



     PaginatedResponse<JobDTO> getAllJobs(int page, int size, String sortBy, String sortDirection);



     PaginatedResponse<JobDTO> filterJobs(JobFilterRequest filterRequest);


     JobDTO getJobById(Long id) ;


     List<JobDTO> scrapeAndSaveJobs();
    }
