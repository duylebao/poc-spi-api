package com.walmart.platform.exp.client.ae.context.model;

import java.io.Serializable;
import java.util.Map;

public class AssignmentTreatment implements Serializable {
    private static final long serialVersionUID = -172814436224086889L;
    private String assignments;
    
    private Map<String, String> treatments;

    public String getAssignments() {
        return assignments;
    }

    public void setAssignments(String assignments) {
        this.assignments = assignments;
    }

    public Map<String, String> getTreatments() {
        return treatments;
    }

    public void setTreatments(Map<String, String> treatments) {
        this.treatments = treatments;
    }
    
    
}
