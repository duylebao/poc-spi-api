package com.walmart.platform.exp.smartcomm.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import com.walmart.platform.exp.client.ae.context.Context;
import com.walmart.platform.exp.client.ae.context.IExpoAssignmentEngine;
import com.walmart.platform.exp.client.ae.context.IPostAssignment;
import com.walmart.platform.exp.client.ae.context.IPropertiesProvider;
import com.walmart.platform.exp.client.ae.context.model.AssignmentTreatment;
import com.walmart.platform.exp.client.ae.metadata.model.ExpoAssignmentMetadata;

public class ExpoAEClient {
    private IExpoAssignmentEngine expoAssignmentEngine;
    private Iterator<IPostAssignment> postAssignmentProviders;
    private IPropertiesProvider propertiesProvider;
    
    private ExpoAEClient() {
        // load up the implementation
        this.expoAssignmentEngine = Optional.ofNullable(loadProvider(IExpoAssignmentEngine.class))
            .orElse( new IExpoAssignmentEngine(){} );

        this.propertiesProvider = Optional.ofNullable(loadProvider(IPropertiesProvider.class))
                .orElse( null );    
        this.postAssignmentProviders = loadProviders(IPostAssignment.class);
    }
    
    public static ExpoAEClient getInstance(){
        return InstanceHolder.INSTANCE;
    }
    
    public AssignmentTreatment getAssignmentTreatment(String cid) {
        return getAssignmentTreatment(cid, new Context(){});
    }
    
    public AssignmentTreatment getAssignmentTreatment(String cid, Context ctx) {
        AssignmentTreatment assignmentTreatment = new AssignmentTreatment();
        try {
            assignmentTreatment = expoAssignmentEngine
                                                .getAssignmentTreatment(new ExpoAssignmentMetadata(), cid)
                                                .orElse( new AssignmentTreatment() );
            

            // process post assignment
            while (postAssignmentProviders.hasNext()) {
                IPostAssignment action = postAssignmentProviders.next();
                action.execute(assignmentTreatment, ctx);
            }
            
            // retain only matched keys and override them
            if (propertiesProvider != null) {
                Map<String, String> props = new HashMap<>(propertiesProvider.getDefaultProperties());
                assignmentTreatment.getTreatments().keySet().retainAll( props.keySet() );
                // override
                props.putAll( assignmentTreatment.getTreatments() );
                assignmentTreatment.setTreatments(props);        
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assignmentTreatment;
    }
    
    private static class InstanceHolder {
        private static final ExpoAEClient INSTANCE = new ExpoAEClient();
    }
    
    private <T> T loadProvider(Class<T> clazz) {
        Iterator<T> iterator = loadProviders(clazz);
        if (iterator.hasNext() ) {
            return iterator.next();
        }
        return null;
    }
    
    private <T> Iterator<T> loadProviders(Class<T> clazz) {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        return loader.iterator();
    }
}
