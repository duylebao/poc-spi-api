package com.walmart.platform.exp.smartcomm.client;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import com.walmart.platform.exp.client.ae.context.IExpoAssignmentEngine;
import com.walmart.platform.exp.client.ae.context.IPropertiesProvider;
import com.walmart.platform.exp.client.ae.context.model.AssignmentTreatment;
import com.walmart.platform.exp.client.ae.metadata.model.ExpoAssignmentMetadata;

public class ExpoAEClient {
    private IExpoAssignmentEngine expoAssignmentEngine;
    private IPropertiesProvider propertiesProvider;
    
    private ExpoAEClient() {
        // load up the implementation
        this.expoAssignmentEngine = Optional.ofNullable(loadProvider(IExpoAssignmentEngine.class))
            .orElse( new IExpoAssignmentEngine(){} );

        this.propertiesProvider = Optional.ofNullable(loadProvider(IPropertiesProvider.class))
                .orElse( null );        
    }
    
    public static ExpoAEClient getInstance(){
        return InstanceHolder.INSTANCE;
    }
    
    public AssignmentTreatment getAssignmentTreatment(String cid) {
        AssignmentTreatment assignmentTreatment = new AssignmentTreatment();
        try {
            AssignmentTreatment override = expoAssignmentEngine
                                                .getAssignmentTreatment(new ExpoAssignmentMetadata(), cid)
                                                .orElse( new AssignmentTreatment() );
            assignmentTreatment.setAssignments(override.getAssignments());
            Map<String,String> treatments = null;
            
            if (propertiesProvider != null) {
                // set all defaults
                treatments = propertiesProvider.getDefaultProperties();
                if (override.getTreatments() != null && treatments != null && !treatments.isEmpty()) {
                    for (Entry<String,String> entry : override.getTreatments().entrySet()) {
                        if (treatments.containsKey( entry.getKey() )) {
                            // overwrite it
                            treatments.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            } else {
                treatments = override.getTreatments();
            }
            assignmentTreatment.setTreatments( treatments );
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assignmentTreatment;
    }
    
    private static class InstanceHolder {
        private static final ExpoAEClient INSTANCE = new ExpoAEClient();
    }
    
    private <T> T loadProvider(Class<T> clazz) {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        Iterator<T> iterator = loader.iterator();
        if (iterator.hasNext() ) {
            return iterator.next();
        }
        return null;
    }
}
