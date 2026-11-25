package fptu.sba301.ats.service;

import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.entity.Interview;
import fptu.sba301.ats.entity.InterviewParticipant;
import fptu.sba301.ats.entity.InterviewScore;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.entity.Offer;
import fptu.sba301.ats.entity.OnboardingChecklist;
import fptu.sba301.ats.entity.OnboardingTask;
import fptu.sba301.ats.entity.ScorecardCriterion;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApplicationStatus;
import fptu.sba301.ats.enums.InterviewStatus;
import fptu.sba301.ats.enums.InterviewType;
import fptu.sba301.ats.enums.JobStatus;
import fptu.sba301.ats.enums.OfferStatus;
import fptu.sba301.ats.enums.OnboardingStatus;
import fptu.sba301.ats.enums.ParticipantRole;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.CandidateRepository;
import fptu.sba301.ats.repository.InterviewParticipantRepository;
import fptu.sba301.ats.repository.InterviewRepository;
import fptu.sba301.ats.repository.InterviewScoreRepository;
import fptu.sba301.ats.repository.JobRepository;
import fptu.sba301.ats.repository.OfferRepository;
import fptu.sba301.ats.repository.OnboardingChecklistRepository;
import fptu.sba301.ats.repository.OnboardingTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoPipelineSeedHelper {

    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final InterviewScoreRepository interviewScoreRepository;
    private final OfferRepository offerRepository;
    private final OnboardingChecklistRepository onboardingChecklistRepository;
    private final OnboardingTaskRepository onboardingTaskRepository;

    public void seedRecruitmentPipeline(
            User hr, Department itDept, List<ScorecardCriterion> frontendCriteria) {
        Job frontendJob = saveJob("Senior Frontend Developer",
                "Looking for an experienced React developer to join our team.", itDept, hr, 2);
        saveJob("Java Backend Developer", "Seeking a Java Spring Boot expert.", itDept, hr, 1);

        Candidate c1 = saveCandidate("Nguyen Van A", "nguyenvana@example.com", "+84987654321",
                "Tech Corp", "LinkedIn", "Hanoi, Vietnam", 5, "Experienced Frontend Developer.", hr);
        Application app1 = saveApplication(c1, frontendJob, ApplicationStage.INTERVIEW, hr);

        Candidate c2 = saveCandidate("Tran Thi B", "tranthib@example.com", "+84912345678",
                "Startup XYZ", "Referral", "Ho Chi Minh City, Vietnam", 3, "Junior Frontend Developer.", hr);
        Application app2 = saveApplication(c2, frontendJob, ApplicationStage.OFFER, hr);

        Candidate c3 = saveCandidate("Le Van C", "levanc@example.com", "+84911223344",
                "Freelancer", "Job Fair", "Da Nang, Vietnam", 1, "Entry-level Developer.", hr);
        Application app3 = saveApplication(c3, frontendJob, ApplicationStage.HIRED, hr);

        seedCompletedInterview(app1, hr, frontendCriteria, 2, "Google Meet",
                "https://meet.google.com/abc-defg-hij", 60, List.of(9, 8, 9));
        seedCompletedInterview(app2, hr, frontendCriteria, 1, "Zoom",
                "https://zoom.us/j/123456789", 45, List.of(7, 6, 7));

        saveOffer(app1, "3500.00", "Senior Frontend Developer", 30,
                "Health insurance, 13th month salary, remote work options",
                "Strong candidate with excellent React skills", OfferStatus.DRAFT, hr);
        saveOffer(app2, "2800.00", "Frontend Developer", 25,
                "Health insurance, team building", "Promising candidate", OfferStatus.PENDING_APPROVAL, hr);
        saveOffer(app3, "4000.00", "Senior Frontend Developer", 14,
                "Full benefits package", null, OfferStatus.APPROVED, hr);

        seedOnboarding(app1, app2, hr);
    }

    private Job saveJob(String title, String description, Department dept, User hr, int headcount) {
        Job job = new Job();
        job.setTitle(title);
        job.setDescription(description);
        job.setDepartment(dept);
        job.setHiringManager(hr);
        job.setStatus(JobStatus.APPROVED);
        job.setHeadcount(headcount);
        job.setCreatedBy(hr.getId());
        return jobRepository.save(job);
    }

    private Candidate saveCandidate(
            String name, String email, String phone, String company,
            String source, String location, int years, String summary, User hr) {
        Candidate candidate = new Candidate();
        candidate.setFullName(name);
        candidate.setEmail(email);
        candidate.setPhone(phone);
        candidate.setCurrentCompany(company);
        candidate.setSource(source);
        candidate.setLocation(location);
        candidate.setExperienceYears(years);
        candidate.setSummary(summary);
        candidate.setCreatedBy(hr.getId());
        return candidateRepository.save(candidate);
    }

    private Application saveApplication(
            Candidate candidate, Job job, ApplicationStage stage, User hr) {
        Application app = new Application();
        app.setCandidate(candidate);
        app.setJob(job);
        app.setStage(stage);
        app.setStatus(ApplicationStatus.ACTIVE);
        app.setCreatedBy(hr.getId());
        return applicationRepository.save(app);
    }

    private void seedCompletedInterview(
            Application app, User hr, List<ScorecardCriterion> criteria,
            int daysAgo, String location, String meetingLink, int duration, List<Integer> scores) {
        Interview interview = new Interview();
        interview.setApplication(app);
        interview.setScheduledAt(LocalDateTime.now().minus(daysAgo, ChronoUnit.DAYS));
        interview.setLocation(location);
        interview.setMeetingLink(meetingLink);
        interview.setDurationMinutes(duration);
        interview.setType(InterviewType.ONLINE);
        interview.setStatus(InterviewStatus.COMPLETED);
        interview.setCreatedBy(hr.getId());
        interview = interviewRepository.save(interview);

        InterviewParticipant participant = new InterviewParticipant();
        participant.setId(new InterviewParticipant.InterviewParticipantId(interview.getId(), hr.getId()));
        participant.setInterview(interview);
        participant.setUser(hr);
        participant.setRole(ParticipantRole.INTERVIEWER);
        interviewParticipantRepository.save(participant);

        for (int i = 0; i < Math.min(criteria.size(), scores.size()); i++) {
            InterviewScore score = new InterviewScore();
            score.setInterview(interview);
            score.setInterviewer(hr);
            score.setCriterion(criteria.get(i));
            score.setScore(scores.get(i));
            score.setCreatedBy(hr.getId());
            interviewScoreRepository.save(score);
        }
    }

    private void saveOffer(
            Application app, String salary, String positionTitle, int startDays,
            String benefits, String notes, OfferStatus status, User hr) {
        Offer offer = new Offer();
        offer.setApplication(app);
        offer.setSalary(new BigDecimal(salary));
        offer.setPositionTitle(positionTitle);
        offer.setStartDate(LocalDate.now().plusDays(startDays));
        offer.setBenefits(benefits);
        offer.setNotes(notes);
        offer.setStatus(status);
        offer.setCreatedBy(hr.getId());
        offerRepository.save(offer);
    }

    private void seedOnboarding(Application app1, Application app2, User hr) {
        OnboardingChecklist onboarding1 = new OnboardingChecklist();
        onboarding1.setApplication(app1);
        onboarding1.setTitle("Frontend Developer Onboarding");
        onboarding1.setStatus(OnboardingStatus.IN_PROGRESS);
        onboarding1.setCreatedBy(hr.getId());
        onboardingChecklistRepository.save(onboarding1);

        OnboardingChecklist onboarding2 = new OnboardingChecklist();
        onboarding2.setApplication(app2);
        onboarding2.setTitle("Junior Frontend Onboarding");
        onboarding2.setStatus(OnboardingStatus.NOT_STARTED);
        onboarding2.setCreatedBy(hr.getId());
        onboarding2 = onboardingChecklistRepository.save(onboarding2);

        OnboardingTask t1 = buildTask(onboarding2, "Setup Workstation", hr, 3, 1);
        OnboardingTask t2 = buildTask(onboarding2, "Codebase Walkthrough", hr, 5, 2);
        onboardingTaskRepository.saveAll(List.of(t1, t2));
    }

    private OnboardingTask buildTask(
            OnboardingChecklist checklist, String title, User hr, int dueDays, int sortOrder) {
        OnboardingTask task = new OnboardingTask();
        task.setChecklist(checklist);
        task.setTitle(title);
        task.setAssignedTo(hr);
        task.setDueDate(LocalDate.now().plusDays(dueDays));
        task.setSortOrder(sortOrder);
        task.setCreatedBy(hr.getId());
        return task;
    }
}
