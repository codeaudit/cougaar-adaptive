/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

/**
 * An enumeration of possible {@link SelectionPolicy} factories, ie,
 * objects whose job is to create policy objects.
 * 
 */
public enum SelectionPolicyFactory implements SelectionPolicyFactoryService {
    RANDOM {
        public SelectionPolicy makePolicy() {
            return new RandomPolicy();
        }
    },
    ROUND_ROBIN {
        public SelectionPolicy makePolicy() {
            return new RoundRobinPolicy();
        }
    },
    FIRST_UP {
        public SelectionPolicy makePolicy() {
            return new FirstUpPolicy();
        }
    },
    CLOSEST {
        public SelectionPolicy makePolicy() {
            return new ClosestPolicy();
        }
    }, 
    
    // for deterministic testing
    FIRST {
        public SelectionPolicy makePolicy() {
            return new FirstPolicy();
        }
    }
}