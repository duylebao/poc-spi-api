package com.walmart.platform.exp.smartcomm.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    private List<IExpoAssignmentEngine> expoAssignmentEngines;
    private List<IPostAssignment> postAssignmentProviders;
    private IPropertiesProvider propertiesProvider;
    
    private ExpoAEClient() {
        // load up the implementation
        this.expoAssignmentEngines = loadProviderList(IExpoAssignmentEngine.class);

        this.propertiesProvider = Optional.ofNullable(loadProvider(IPropertiesProvider.class))
                .orElse( null );    
        this.postAssignmentProviders = loadProviderList(IPostAssignment.class);
    }
    
    public static ExpoAEClient getInstance(){
        return InstanceHolder.INSTANCE;
    }
    
    public AssignmentTreatment getAssignmentTreatment(String cid) {
        return getAssignmentTreatment(cid, new Context(){});
    }
    
    public AssignmentTreatment getAssignmentTreatment(String cid, Context ctx) {       
        AssignmentTreatment assignmentTreatment = new AssignmentTreatment();
        assignmentTreatment.setAssignments("");
        assignmentTreatment.setTreatments(new HashMap<>());
        try {
            for (IExpoAssignmentEngine ae : expoAssignmentEngines) {
                AssignmentTreatment at = ae.getAssignmentTreatment(new ExpoAssignmentMetadata(), cid).orElse(new AssignmentTreatment());
                if (assignmentTreatment.getAssignments().isEmpty() &&
                        at.getAssignments() != null && !at.getAssignments().isEmpty()) {
                    assignmentTreatment.setAssignments(at.getAssignments());
                }
                if (assignmentTreatment.getTreatments().isEmpty() &&
                        at.getTreatments() != null && !at.getTreatments().isEmpty()) {
                    assignmentTreatment.setTreatments(at.getTreatments());
                }
            }

            // process post assignment
            postAssignmentProviders.forEach( action -> {
                action.execute(assignmentTreatment, ctx);
            });
            
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
    
    private <T> List<T> loadProviderList(Class<T> clazz) {
        Iterator<T> iterator = loadProviders(clazz);
        List<T> l = new ArrayList<>();
        while (iterator.hasNext()) {
            T t = iterator.next();
            l.add( t );
            System.out.println("loading providers: " + t.getClass().getName());
        }
        return l;
    }
}
