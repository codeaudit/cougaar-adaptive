/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

/**
 * For now we use an enum to create the policy instance, ie, by treating each
 * enum constant as a factory. Not clear this buys anything vis-a-vis using
 * classnames directly, or creating a service in proper SOA fashion, or using
 * string keys.
 * 
 * I would be more inclined to load a component that advertises
 * {@link SelectionPolicyFactoryService}.
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
            // FIXME: We need a real implementation of this
            return new FirstPolicy();
        }
    }, 
    
    // for deterministic testing
    FIRST {
        public SelectionPolicy makePolicy() {
            return new FirstPolicy();
        }
    }
}