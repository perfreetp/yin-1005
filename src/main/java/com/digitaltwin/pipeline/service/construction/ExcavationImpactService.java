package com.digitaltwin.pipeline.service.construction;

import com.digitaltwin.pipeline.dto.construction.ExcavationImpactAssessmentDTO;
import com.digitaltwin.pipeline.entity.construction.ExcavationApplication;

public interface ExcavationImpactService {

    ExcavationImpactAssessmentDTO comprehensiveAssessment(Long applicationId);

    String generateImpactReport(Long applicationId);

    void submitForReview(Long applicationId);
}
