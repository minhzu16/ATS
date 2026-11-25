package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.CriterionEvaluationResponse;
import fptu.sba301.ats.entity.Interview;
import fptu.sba301.ats.entity.InterviewParticipant;
import fptu.sba301.ats.entity.InterviewScore;
import fptu.sba301.ats.entity.ScorecardCriterion;
import fptu.sba301.ats.enums.ParticipantRole;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class InterviewScoreCalculator {

    public BigDecimal calculateWeightedInterviewerScore(List<InterviewScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalScoreWeight = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (InterviewScore s : scores) {
            BigDecimal weight = s.getCriterion().getWeight();
            if (weight == null) {
                weight = BigDecimal.ONE;
            }
            BigDecimal score = BigDecimal.valueOf(s.getScore());

            totalScoreWeight = totalScoreWeight.add(score.multiply(weight));
            totalWeight = totalWeight.add(weight);
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalScoreWeight.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateFinalScore(Interview interview) {
        List<InterviewParticipant> interviewers =
                interview.getParticipants().stream()
                        .filter(p -> p.getRole() == ParticipantRole.INTERVIEWER)
                        .filter(p -> p.getOverallScore() != null)
                        .toList();

        if (interviewers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = interviewers.stream()
                .map(InterviewParticipant::getOverallScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(
                BigDecimal.valueOf(interviewers.size()),
                2,
                RoundingMode.HALF_UP
        );
    }

    public List<CriterionEvaluationResponse> calculateCriterionEvaluations(List<InterviewScore> scoresToAggregate) {
        Map<ScorecardCriterion, List<InterviewScore>> scoresByCriterion =
                scoresToAggregate.stream()
                        .collect(Collectors.groupingBy(InterviewScore::getCriterion));

        return scoresByCriterion.entrySet().stream()
                .map(entry -> {
                    ScorecardCriterion criterion = entry.getKey();
                    List<InterviewScore> userScores = entry.getValue();

                    BigDecimal sum = userScores.stream()
                            .map(s -> BigDecimal.valueOf(s.getScore()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal avg = userScores.isEmpty() ? BigDecimal.ZERO : sum.divide(
                            BigDecimal.valueOf(userScores.size()),
                            2,
                            RoundingMode.HALF_UP
                    );

                    return CriterionEvaluationResponse.builder()
                            .criterionId(criterion.getId())
                            .criterionName(criterion.getName())
                            .weight(criterion.getWeight())
                            .averageScore(avg)
                            .build();
                })
                .toList();
    }

    public BigDecimal calculateApplicationOverallScore(BigDecimal totalScore, int completedCount) {
        return completedCount > 0
                ? totalScore.divide(BigDecimal.valueOf(completedCount), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    public String calculateRecommendation(int completedCount, BigDecimal overallScore) {
        if (completedCount > 0) {
            return overallScore.compareTo(new BigDecimal("3.5")) >= 0 ? "Hire" : "No Hire";
        }
        return "Pending";
    }
}
