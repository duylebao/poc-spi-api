package com.walmart.platform.exp.client.ae.context;

import com.walmart.platform.exp.client.ae.context.model.AssignmentTreatment;
import com.walmart.platform.exp.client.ae.metadata.model.ExpoAssignmentMetadata;
import java.util.Optional;

public interface IExpoAssignmentEngine {
    // return empty
    default public Optional<AssignmentTreatment> getAssignmentTreatment(final ExpoAssignmentMetadata expoAssignmentMetadata, final String cid) throws Exception {
        return Optional.of( new AssignmentTreatment() );
    }

}