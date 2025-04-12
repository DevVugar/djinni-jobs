package com.example.demo.service;

import com.example.demo.mapper.JobMapper;
import com.example.demo.model.dto.JobDTO;
import com.example.demo.model.dto.JobFilterRequest;
import com.example.demo.model.dto.PaginatedResponse;
import com.example.demo.model.entity.Job;
import com.example.demo.repository.JobRepository;
import com.example.demo.scraper.DjinniJobScraper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private DjinniJobScraper djinniJobScraper;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    private Job job1;
    private Job job2;
    private JobDTO jobDTO1;
    private JobDTO jobDTO2;
    private List<Job> jobList;
    private List<JobDTO> jobDTOList;
    private Page<Job> jobPage;
    private PaginatedResponse<JobDTO> paginatedResponse;

    @BeforeEach
    void setUp() {
        // Initialize test data
        job1 = new Job();
        job1.setId(1L);
        job1.setJobTitle("Java Developer");
        job1.setSalaryMin(3000);
        job1.setSalaryMax(5000);

        job2 = new Job();
        job2.setId(2L);
        job2.setJobTitle("DevOps Engineer");
        job2.setSalaryMin(4000);
        job2.setSalaryMax(6000);

        jobDTO1 = new JobDTO();
        jobDTO1.setId(1L);
        jobDTO1.setJobTitle("Java Developer");
        jobDTO1.setSalaryMin(3000);
        jobDTO1.setSalaryMax(5000);

        jobDTO2 = new JobDTO();
        jobDTO2.setId(2L);
        jobDTO2.setJobTitle("DevOps Engineer");
        jobDTO2.setSalaryMin(4000);
        jobDTO2.setSalaryMax(6000);

        jobList = Arrays.asList(job1, job2);
        jobDTOList = Arrays.asList(jobDTO1, jobDTO2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        jobPage = new PageImpl<>(jobList, pageable, jobList.size());

        paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setContent(jobDTOList);
        paginatedResponse.setTotalElements(jobList.size());
        paginatedResponse.setTotalPages(1);
        paginatedResponse.setPageNumber(0);
        paginatedResponse.setPageSize(10);
    }

    @Test
    void getAllJobs_ShouldReturnPaginatedResponse() {
        // Arrange
        when(jobRepository.findAll(any(Pageable.class))).thenReturn(jobPage);
        when(jobMapper.toPaginatedResponse(jobPage)).thenReturn(paginatedResponse);

        // Act
        PaginatedResponse<JobDTO> result = jobService.getAllJobs(0, 10, "createdAt", "desc");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(jobDTOList, result.getContent());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());
        assertEquals(Sort.Direction.DESC, capturedPageable.getSort().getOrderFor("createdAt").getDirection());
    }

    @Test
    void filterJobs_WithRegularSorting_ShouldReturnFilteredJobs() {
        // Arrange
        JobFilterRequest filterRequest = new JobFilterRequest();
        filterRequest.setPage(0);
        filterRequest.setSize(10);
        filterRequest.setSortBy("createdAt");
        filterRequest.setSortDirection("desc");
        filterRequest.setLocation("Remote");
        filterRequest.setJobType("Full-time");

        when(jobRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(jobPage);
        when(jobMapper.toPaginatedResponse(jobPage)).thenReturn(paginatedResponse);

        // Act
        PaginatedResponse<JobDTO> result = jobService.filterJobs(filterRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(jobDTOList, result.getContent());

        verify(jobRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(jobMapper).toPaginatedResponse(jobPage);
    }

    @Test
    void filterJobs_WithSalaryRangeSort_ShouldSortAndReturnFilteredJobs() {
        // Arrange
        JobFilterRequest filterRequest = new JobFilterRequest();
        filterRequest.setPage(0);
        filterRequest.setSize(10);
        filterRequest.setSortBy("salaryRange");
        filterRequest.setSortDirection("desc");

        when(jobRepository.findAll(any(Specification.class))).thenReturn(jobList);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Job> manualSortedPage = new PageImpl<>(jobList, pageable, jobList.size());

        when(jobMapper.toPaginatedResponse(any(Page.class))).thenReturn(paginatedResponse);

        // Act
        PaginatedResponse<JobDTO> result = jobService.filterJobs(filterRequest);

        // Assert
        assertNotNull(result);
        assertEquals(paginatedResponse, result);

        verify(jobRepository).findAll(any(Specification.class));
        verify(jobMapper).toPaginatedResponse(any(Page.class));
        verifyNoMoreInteractions(jobRepository);
    }

    @Test
    void getJobById_WhenJobExists_ShouldReturnJobDTO() {
        // Arrange
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(jobMapper.toDto(job1)).thenReturn(jobDTO1);

        // Act
        JobDTO result = jobService.getJobById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Java Developer", result.getJobTitle());

        verify(jobRepository).findById(1L);
        verify(jobMapper).toDto(job1);
    }

    @Test
    void getJobById_WhenJobDoesNotExist_ShouldThrowException() {
        // Arrange
        when(jobRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jobService.getJobById(999L));

        assertTrue(exception.getMessage().contains("Job not found with id: 999"));
        verify(jobRepository).findById(999L);
        verifyNoInteractions(jobMapper);
    }

    @Test
    void scrapeAndSaveJobs_WhenJobsScraped_ShouldSaveAndReturnJobs() {
        // Arrange
        when(djinniJobScraper.main()).thenReturn(jobList);
        when(jobMapper.toDtoList(jobList)).thenReturn(jobDTOList);

        // Act
        List<JobDTO> result = jobService.scrapeAndSaveJobs();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(jobDTOList, result);

        verify(djinniJobScraper).main();
        verify(jobRepository).saveAll(jobList);
        verify(jobMapper).toDtoList(jobList);
    }

    @Test
    void scrapeAndSaveJobs_WhenNoJobsScraped_ShouldReturnEmptyList() {
        // Arrange
        when(djinniJobScraper.main()).thenReturn(Collections.emptyList());
        when(jobMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<JobDTO> result = jobService.scrapeAndSaveJobs();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(djinniJobScraper).main();
        verify(jobRepository, never()).saveAll(anyList());
        verify(jobMapper).toDtoList(Collections.emptyList());
    }
}