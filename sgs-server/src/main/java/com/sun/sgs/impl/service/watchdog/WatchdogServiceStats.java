/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html 
 *
 * Copyright (c) 2007-2010, Oracle and/or its affiliates.
 *
 * All rights reserved.
 */

package com.sun.sgs.impl.service.watchdog;

import com.sun.sgs.impl.profile.ProfileCollectorImpl;
import com.sun.sgs.management.NodeInfo;
import com.sun.sgs.management.WatchdogServiceMXBean;
import com.sun.sgs.profile.AggregateProfileOperation;
import com.sun.sgs.profile.ProfileCollector;
import com.sun.sgs.profile.ProfileCollector.ProfileLevel;
import com.sun.sgs.profile.ProfileConsumer;
import com.sun.sgs.profile.ProfileConsumer.ProfileDataType;
import com.sun.sgs.profile.ProfileOperation;
import com.sun.sgs.service.Node.Health;

/**
 *
 * The Statistics MBean object for the watchdog service.
 */
class WatchdogServiceStats implements WatchdogServiceMXBean {
    // the backing watchdog service
    final WatchdogServiceImpl watchdog;
    
    // the profiled operations
    final ProfileOperation addNodeListenerOp;
    final ProfileOperation addRecoveryListenerOp;
    final ProfileOperation getBackupOp;
    final ProfileOperation getNodeOp;
    final ProfileOperation getNodesOp;
    final ProfileOperation getLocalNodeHealthOp;
    final ProfileOperation getLocalNodeHealthNonTransOp;
    final ProfileOperation isLocalNodeAliveOp;
    final ProfileOperation isLocalNodeAliveNonTransOp;
    
    WatchdogServiceStats(ProfileCollector collector, WatchdogServiceImpl wdog) {
        watchdog = wdog;
        
        ProfileConsumer consumer = 
            collector.getConsumer(ProfileCollectorImpl.CORE_CONSUMER_PREFIX + 
                                  "WatchdogService");
        ProfileLevel level = ProfileLevel.MAX;
        ProfileDataType type = ProfileDataType.TASK_AND_AGGREGATE;
        
        addNodeListenerOp =
            consumer.createOperation("addNodeListener", type, level);
        addRecoveryListenerOp =
            consumer.createOperation("addRecoveryListener", type, level);
        getBackupOp = 
            consumer.createOperation("getBackup", type, level);
        getNodeOp =
            consumer.createOperation("getNode", type, level);
        getNodesOp =
            consumer.createOperation("getNodes", type, level);
        getLocalNodeHealthOp =
            consumer.createOperation("getLocalNodeHealth", type, level);
        getLocalNodeHealthNonTransOp =
            consumer.createOperation("getLocalNodeHealthNonTransactional",
                                     type, level);
        isLocalNodeAliveOp =
            consumer.createOperation("isLocalNodeAlive", type, level);
        isLocalNodeAliveNonTransOp =
            consumer.createOperation("isLocalNodeAliveNonTransactional", 
                                     type, level);
    }
    
    /** {@inheritDoc} */
    public Health getNodeHealth() {
        return watchdog.getLocalNodeHealthNonTransactional();
    }

    /** {@inheritDoc} */
    public void setNodeHealth(Health health) {
        watchdog.reportHealth(watchdog.localNodeId, health,
                              WatchdogServiceStats.class.getName());
    }

    /** {@inheritDoc} */
    public long getAddNodeListenerCalls() {
        return ((AggregateProfileOperation) addNodeListenerOp).getCount();
    }
        
    /** {@inheritDoc} */
    public long getAddRecoveryListenerCalls() {
        return ((AggregateProfileOperation) addRecoveryListenerOp).getCount();
    }
        
    /** {@inheritDoc} */
    public long getGetBackupCalls() {
        return ((AggregateProfileOperation) getBackupOp).getCount();
    }
        
    /** {@inheritDoc} */
    public long getGetNodeCalls() {
        return ((AggregateProfileOperation) getNodeOp).getCount();
    }
        
    /** {@inheritDoc} */
    public long getGetNodesCalls() {
        return ((AggregateProfileOperation) getNodesOp).getCount();
    }

    /** {@inheritDoc} */
    public long getGetLocalNodeHealthCalls() {
        return ((AggregateProfileOperation) getLocalNodeHealthOp).getCount();
    }

    /** {@inheritDoc} */
    public long getGetLocalNodeHealthNonTransactionalCalls() {
        return ((AggregateProfileOperation) getLocalNodeHealthNonTransOp).
                getCount();
    }

    /** {@inheritDoc} */
    public long getIsLocalNodeAliveCalls() {
        return ((AggregateProfileOperation) isLocalNodeAliveOp).getCount();
    }
        
    /** {@inheritDoc} */
    public long getIsLocalNodeAliveNonTransactionalCalls() {
        return ((AggregateProfileOperation) isLocalNodeAliveNonTransOp).
                getCount();
    }
    
    /** {@inheritDoc} */
    public NodeInfo getStatusInfo() {
        return watchdog.getNodeStatusInfo();
    }
}
