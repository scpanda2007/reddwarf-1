/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html 
 *
 * Copyright (c) 2007-2010, Oracle and/or its affiliates.
 *
 * All rights reserved.
 */

package com.sun.sgs.management;

import com.sun.sgs.service.DataService;
/**
 * The management interface for the data service.
 * <p>
 * An instance implementing this MBean can be obtained from the from the 
 * {@link java.lang.management.ManagementFactory.html#getPlatformMBeanServer() 
 * getPlatformMBeanServer} method.
 * <p>
 * The {@code ObjectName} for uniquely identifying this MBean is
 * {@value #MXBEAN_NAME}.
 * 
 */
public interface DataServiceMXBean {
    /** The name for uniquely identifying this MBean. */
    String MXBEAN_NAME = "com.sun.sgs.service:type=DataService";
    
    /**
     * Returns the number of times 
     * {@link DataService#createReference createReference} 
     * has been called.
     * @return the number of times {@code createReference} has been called
     */
    long getCreateReferenceCalls();

    /**
     * Returns the number of times 
     * {@link DataService#getBinding getBinding} 
     * has been called.
     * @return the number of times {@code getBinding} has been called
     */
    long getGetBindingCalls();
        
    /**
     * Returns the number of times 
     * {@link DataService#getBindingForUpdate getBindingForUpdate} 
     * has been called.
     * @return the number of times {@code getBindingForUpdate} has been called
     */
    long getGetBindingForUpdateCalls();
        
    /**
     * Returns the number of times 
     * {@link DataService#getObjectId getObjectId} 
     * has been called.
     * @return the number of times {@code getObjectId} has been called
     */
    long getGetObjectIdCalls();
        
    /**
     * Returns the number of times 
     * {@link DataService#markForUpdate markForUpdate} 
     * has been called.
     * @return the number of times {@code markForUpdate} has been called
     */
    long getMarkForUpdateCalls();
            
    /**
     * Returns the number of times 
     * {@link DataService#nextBoundName nextBoundName} 
     * has been called.
     * @return the number of times {@code nextBoundName} has been called
     */
    long getNextBoundNameCalls();
    
    /**
     * Returns the number of times 
     * {@link DataService#removeBinding removeBinding} 
     * has been called.
     * @return the number of times {@code removeBinding} has been called
     */
    long getRemoveBindingCalls();
        
    /**
     * Returns the number of times 
     * {@link DataService#removeObject removeObject} 
     * has been called.
     * @return the number of times {@code removeObject} has been called
     */
    long getRemoveObjectCalls();
       
    /**
     * Returns the number of times 
     * {@link DataService#setBinding setBinding} 
     * has been called.
     * @return the number of times {@code setBinding} has been called
     */
    long getSetBindingCalls();
  
    /**
     * Returns the number of times {@link DataService#getLocalNodeId
     * getLocalNodeId} has been called.
     * 
     * @return the number of times {@code getLocalNodeId} has been called
     */
    long getGetLocalNodeIdCalls();

    /**
     * Returns the number of times 
     * {@link DataService#createReferenceForId createReferenceForId} 
     * has been called.
     * @return the number of times {@code createReferenceForId} has been called
     */
    long getCreateReferenceForIdCalls();
        
    /**
     * Returns the number of times 
     * {@link DataService#getServiceBinding getServiceBinding} 
     * has been called.
     * @return the number of times {@code getServiceBinding} has been called
     */
    long getGetServiceBindingCalls();
            
    /**
     * Returns the number of times {@link
     * DataService#getServiceBindingForUpdate getServiceBindingForUpdate} has
     * been called.
     * @return the number of times {@code getServiceBindingForUpdate} has been
     * called
     */
    long getGetServiceBindingForUpdateCalls();
            
    /**
     * Returns the number of times 
     * {@link DataService#nextObjectId nextObjectId} 
     * has been called.
     * @return the number of times {@code nextObjectId} has been called
     */
    long getNextObjectIdCalls();
                
    /**
     * Returns the number of times 
     * {@link DataService#nextServiceBoundName nextServiceBoundName} 
     * has been called.
     * @return the number of times {@code nextServiceBoundName} has been called
     */
    long getNextServiceBoundNameCalls();
                    
    /**
     * Returns the number of times 
     * {@link DataService#removeServiceBinding removeServiceBinding} 
     * has been called.
     * @return the number of times {@code removeServiceBinding} has been called
     */
    long getRemoveServiceBindingCalls();
                        
    /**
     * Returns the number of times 
     * {@link DataService#setServiceBinding setServiceBinding} 
     * has been called.
     * @return the number of times {@code setServiceBinding} has been called
     */
    long getSetServiceBindingCalls();
}
