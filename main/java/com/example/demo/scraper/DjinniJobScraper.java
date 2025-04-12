package com.example.demo.scraper;

import com.example.demo.model.entity.Job;
import com.example.demo.repository.JobRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@AllArgsConstructor
public class DjinniJobScraper {
    private final JobRepository jobRepository;

//    static class Job {
//        private int id;
//        private String jobTitle;
//        private String companyName;
//        private String location;
//        private String jobType;
//        private Integer salaryMin;
//        private Integer salaryMax;
//        private String jobDescription;
//        private String requirements;
//        private String experienceLevel;
//        private String educationLevel;
//        private String industry;
//        private String postedDate;
//        private String applicationDeadline;
//        private String howToApply;
//        private String companyLogo;
//        private String benefits;
//        private String tags;
//        private String source;
//    }


    public  List<Job> main() {
        List<Job> jobs = new ArrayList<>();
        long jobCounter = 1;

        try {
            // Get the main job listing page
            Document doc = Jsoup.connect("https://djinni.co/jobs/")
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements jobElements = doc.select("li[id^=job-item-]");

            for (Element jobItem : jobElements) {
                Job job = new Job();
                // Remove this line to let Hibernate handle IDs
                // job.setId(jobCounter++);
                job.setSource("Djinni");

                // Job title and URL for details page
                Element titleElement = jobItem.selectFirst(".job-item__title-link");
                if (titleElement == null) continue;
                job.setJobTitle(titleElement.text().trim());
                String detailUrl = "https://djinni.co" + titleElement.attr("href");
                job.setHowToApply(detailUrl); // Set the detail URL as the application link

                // Company name
                Element companyElement = jobItem.selectFirst(".js-analytics-event");
                if (companyElement != null) {
                    job.setCompanyName(companyElement.text().trim());
                }

                // Company logo URL
                Element logoElement = jobItem.selectFirst(".userpic-image");
                if (logoElement != null) {
                    job.setCompanyLogo(logoElement.attr("src"));
                }

                // Short description from job list
                Element descElement = jobItem.selectFirst(".js-truncated-text");
                if (descElement != null) {
                    job.setJobDescription(descElement.text().trim());
                }

                // Fetch and parse the detail page for additional information
                try {
                    Document detailDoc = Jsoup.connect(detailUrl)
                            .userAgent("Mozilla/5.0")
                            .timeout(10000)
                            .get();

                    // Get company logo from detail page if not found on listing page
                    if (job.getCompanyLogo() == null) {
                        Element detailLogoElement = detailDoc.selectFirst(".userpic-image_img");
                        if (detailLogoElement != null) {
                            job.setCompanyLogo(detailLogoElement.attr("src"));
                        }
                    }

                    // Get company name from detail page if not found on listing page
                    if (job.getCompanyName() == null) {
                        Element companyElementDetail = detailDoc.selectFirst("a.text-reset");
                        if (companyElementDetail != null) {
                            job.setCompanyName(companyElementDetail.text().trim());
                        }
                    }

                    // Get job type from JSON-LD script and sidebar
                    Element jsonLdScript = detailDoc.selectFirst("script[type='application/ld+json']");
                    String employmentType = "Full-time"; // Default
                    boolean isRemote = false;
                    if (jsonLdScript != null) {
                        String jsonText = jsonLdScript.html();
                        Pattern pattern = Pattern.compile("\"employmentType\":\\s*\"([^\"]+)\"");
                        Matcher matcher = pattern.matcher(jsonText);
                        if (matcher.find()) {
                            employmentType = matcher.group(1).equals("FULL_TIME") ? "Full-time" : matcher.group(1);
                        }
                    }
                    Element remoteElement = detailDoc.selectFirst(".list-unstyled:contains(Full Remote)");
                    if (remoteElement != null) {
                        isRemote = true;
                    }
                    job.setJobType(employmentType + (isRemote ? ", Remote" : ""));

                    // Get salary information
                    Element salaryElement = detailDoc.selectFirst("#salary-suggestion");
                    if (salaryElement != null) {
                        String salaryText = salaryElement.text().trim();
                        Pattern pattern = Pattern.compile("\\$(\\d+)-(\\d+)");
                        Matcher matcher = pattern.matcher(salaryText);
                        if (matcher.find()) {
                            try {
                                job.setSalaryMin(Integer.parseInt(matcher.group(1)));
                                job.setSalaryMax(Integer.parseInt(matcher.group(2)));
                            } catch (NumberFormatException e) {
                                System.err.println("Error parsing salary: " + salaryText);
                            }
                        }
                    }

                    // Get full job description from detail page
                    Elements jobDescSections = detailDoc.select(".job-post__description");
                    if (!jobDescSections.isEmpty()) {
                        StringBuilder fullDesc = new StringBuilder();
                        for (Element section : jobDescSections.first().children()) {
                            if (section.text().contains("Responsibilities:")) {
                                break;
                            }
                            fullDesc.append(section.text().trim()).append(" ");
                        }
                        if (fullDesc.length() > 0) {
                            job.setJobDescription(fullDesc.toString().trim());
                        }
                    }

                    // Get requirements section (Responsibilities, Required technical skills, Required soft skills)
                    Element reqHeader = detailDoc.getElementsContainingText("Responsibilities:").first();
                    if (reqHeader != null) {
                        StringBuilder reqBuilder = new StringBuilder();
                        Element currentElement = reqHeader;
                        while (currentElement != null) {
                            String text = currentElement.text().trim();
                            if (text.contains("What we offer:")) {
                                break;
                            }
                            if (!text.isEmpty()) {
                                // If the element is a <ul>, iterate through its <li> children
                                if (currentElement.tagName().equals("ul")) {
                                    for (Element li : currentElement.select("li")) {
                                        reqBuilder.append("- ").append(li.text().trim()).append("\n");
                                    }
                                } else {
                                    reqBuilder.append(text).append("\n");
                                }
                            }
                            currentElement = currentElement.nextElementSibling();
                        }
                        job.setRequirements(reqBuilder.toString().trim());
                    } else {
                        System.out.println("Responsibilities header not found for job: " + job.getJobTitle());
                    }

                    // Get benefits
                    Element benefitsHeader = detailDoc.getElementsContainingText("What we offer:").first();
                    if (benefitsHeader != null) {
                        StringBuilder benefitsBuilder = new StringBuilder();
                        Element currentElement = benefitsHeader.nextElementSibling();
                        while (currentElement != null) {
                            String text = currentElement.text().trim();
                            if (text.isEmpty() || currentElement.tagName().equals("p") && text.equals(" ")) {
                                break;
                            }
                            if (currentElement.tagName().equals("ul")) {
                                for (Element li : currentElement.select("li")) {
                                    benefitsBuilder.append("- ").append(li.text().trim()).append("\n");
                                }
                            } else {
                                benefitsBuilder.append(text).append("\n");
                            }
                            currentElement = currentElement.nextElementSibling();
                        }
                        job.setBenefits(benefitsBuilder.toString().trim());
                    } else {
                        System.out.println("Benefits header not found for job: " + job.getJobTitle());
                    }

                    // Posted date
                    Element publishedElement = detailDoc.selectFirst("#rate-all-time-tab");
                    if (publishedElement != null) {
                        String publishText = publishedElement.text().trim();
                        job.setPostedDate(publishText.replace("Published", "").trim());
                    }

                    // Application deadline
                    if (jsonLdScript != null) {
                        String jsonText = jsonLdScript.html();
                        Pattern pattern = Pattern.compile("\"validThrough\":\\s*\"([^\"]+)\"");
                        Matcher matcher = pattern.matcher(jsonText);
                        if (matcher.find()) {
                            String validThrough = matcher.group(1);
                            job.setApplicationDeadline(validThrough.split("T")[0]);
                        }
                    }

                    // Industry/domain
                    Element domainElement = detailDoc.selectFirst(".row.gx-2:contains(Domain:)");
                    if (domainElement != null) {
                        job.setIndustry(domainElement.text().replace("Domain:", "").trim());
                    }

                    // Location (from detail page)
                    Element locationElement = detailDoc.selectFirst(".location-text");
                    if (locationElement != null) {
                        job.setLocation(locationElement.text().trim());
                    }

                    // Experience level (from detail page)
                    Element expElement = detailDoc.selectFirst(".list-unstyled:contains(years of experience)");
                    if (expElement != null) {
                        String expText = expElement.text().trim();
                        if (expText.contains("Intermediate")) {
                            job.setExperienceLevel("Intermediate, " + expText);
                        } else if (expText.contains("Senior")) {
                            job.setExperienceLevel("Senior, " + expText);
                        } else {
                            job.setExperienceLevel(expText);
                        }
                    }

                    // Education level (from detail page)
                    Element eduElement = detailDoc.selectFirst(".list-unstyled:contains(Intermediate)");
                    if (eduElement != null && (eduElement.text().contains("Intermediate") || eduElement.text().contains("Advanced") ||
                            eduElement.text().contains("Beginner") || eduElement.text().contains("Fluent"))) {
                        job.setEducationLevel("Intermediate"); // Since the job specifies Intermediate English level
                    }

                    // Tags/Keywords (from detail page)
                    Elements skillElements = detailDoc.select(".job-additional-info a");
                    if (skillElements.isEmpty()) {
                        // Fallback to detail page if not found on listing page
                        skillElements = detailDoc.select("a[href*='/jobs/keyword-']");
                    }
                    if (!skillElements.isEmpty()) {
                        StringBuilder skills = new StringBuilder();
                        for (Element skill : skillElements) {
                            String skillText = skill.text().trim();
                            if (!skillText.isEmpty() && !skillText.equals("QA") && !skillText.equals("QA Automation")) { // Avoid duplicates with job title
                                if (skills.length() > 0) skills.append(", ");
                                skills.append(skillText);
                            }
                        }
                        job.setTags(skills.toString());
                    }

                    System.out.println("Processed job: " + job.getJobTitle());

                } catch (IOException e) {
                    System.err.println("Error scraping detail page: " + detailUrl);
                    e.printStackTrace();
                }

                jobs.add(job);
            }

            // Convert to JSON and save
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(jobs);

            try (FileWriter writer = new FileWriter("djinni_jobs.json")) {
                writer.write(json);
                System.out.println("Successfully saved " + jobs.size() + " jobs to djinni_jobs.json");
            } catch (IOException e) {
                System.err.println("Error writing to file");
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.err.println("Error scraping main page");
            e.printStackTrace();
        }

       List<Job> result=jobRepository.saveAll(jobs);
        return result;

    }
}