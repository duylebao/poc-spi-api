package com.walmart.platform.exp.client.ae.context;

import com.walmart.platform.exp.client.ae.context.model.AssignmentTreatment;

public interface IPostAssignment {
    public void execute(AssignmentTreatment treatment, Context ctx);
}
