package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.CriterionEvaluationResponse;
import fptu.sba301.ats.entity.Interview;
import fptu.sba301.ats.entity.InterviewParticipant;
import fptu.sba301.ats.entity.InterviewScore;
import fptu.sba301.ats.entity.ScorecardCriterion;
import fptu.sba301.ats.enums.ParticipantRole;
import fptu.sba301.ats.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterviewHelpersTest {

    private final InterviewScoreCalculator calculator = new InterviewScoreCalculator();
    private final InterviewScheduleValidator validator = new InterviewScheduleValidator();

    @Test
    @DisplayName("calculateWeightedInterviewerScore handles null or empty scores gracefully")
    void weightedScore_emptyOrNull() {
        assertThat(calculator.calculateWeightedInterviewerScore(null)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(calculator.calculateWeightedInterviewerScore(Collections.emptyList())).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateWeightedInterviewerScore accurately weights scores")
    void weightedScore_normalCalculation() {
        ScorecardCriterion c1 = ScorecardCriterion.builder().weight(new BigDecimal("2.0")).build();
        ScorecardCriterion c2 = ScorecardCriterion.builder().weight(new BigDecimal("3.0")).build();

        InterviewScore s1 = InterviewScore.builder().criterion(c1).score(4).build();
        InterviewScore s2 = InterviewScore.builder().criterion(c2).score(5).build();

        BigDecimal result = calculator.calculateWeightedInterviewerScore(List.of(s1, s2));
        assertThat(result).isEqualByComparingTo(new BigDecimal("4.60"));
    }

    @Test
    @DisplayName("calculateFinalScore averages interviewer scores ignoring nulls and non-interviewers")
    void finalScore_calculation() {
        InterviewParticipant interviewer1 = InterviewParticipant.builder()
                .role(ParticipantRole.INTERVIEWER)
                .overallScore(new BigDecimal("4.00"))
                .build();
        InterviewParticipant interviewer2 = InterviewParticipant.builder()
                .role(ParticipantRole.INTERVIEWER)
                .overallScore(new BigDecimal("5.00"))
                .build();
        InterviewParticipant observer = InterviewParticipant.builder()
                .role(ParticipantRole.OBSERVER)
                .overallScore(new BigDecimal("1.00"))
                .build();

        Interview interview = Interview.builder()
                .participants(List.of(interviewer1, interviewer2, observer))
                .build();

        BigDecimal finalScore = calculator.calculateFinalScore(interview);
        assertThat(finalScore).isEqualByComparingTo(new BigDecimal("4.50"));
    }

    @Test
    @DisplayName("calculateCriterionEvaluations aggregates average score by criterion")
    void criterionEvaluations_aggregation() {
        UUID criterionId = UUID.randomUUID();
        ScorecardCriterion c1 = ScorecardCriterion.builder()
                .id(criterionId)
                .name("Technical Skill")
                .weight(new BigDecimal("1.0"))
                .build();

        InterviewScore s1 = InterviewScore.builder().criterion(c1).score(4).build();
        InterviewScore s2 = InterviewScore.builder().criterion(c1).score(2).build();

        List<CriterionEvaluationResponse> evaluations = calculator.calculateCriterionEvaluations(List.of(s1, s2));
        assertThat(evaluations).hasSize(1);
        assertThat(evaluations.get(0).getCriterionId()).isEqualTo(criterionId);
        assertThat(evaluations.get(0).getCriterionName()).isEqualTo("Technical Skill");
        assertThat(evaluations.get(0).getAverageScore()).isEqualByComparingTo(new BigDecimal("3.00"));
    }

    @Test
    @DisplayName("calculateApplicationOverallScore divides by completed count")
    void applicationOverallScore() {
        BigDecimal overall = calculator.calculateApplicationOverallScore(new BigDecimal("15.0"), 3);
        assertThat(overall).isEqualByComparingTo(new BigDecimal("5.0"));

        BigDecimal zero = calculator.calculateApplicationOverallScore(new BigDecimal("15.0"), 0);
        assertThat(zero).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("validateRescheduleTime throws BusinessException for past time")
    void validateRescheduleTime_past() {
        LocalDateTime past = LocalDateTime.now().minusHours(1);
        assertThatThrownBy(() -> validator.validateRescheduleTime(past))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot reschedule interview to a past time");
    }

    @Test
    @DisplayName("validateRescheduleTime allows future time")
    void validateRescheduleTime_future() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        validator.validateRescheduleTime(future);
    }
}
