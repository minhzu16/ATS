package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

class InterviewScheduleValidator {

    public void validateRescheduleTime(LocalDateTime newTime) {
        if (newTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot reschedule interview to a past time", HttpStatus.BAD_REQUEST);
        }
    }
}
