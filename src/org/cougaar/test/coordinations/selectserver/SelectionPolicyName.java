/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.coordinations.selectserver;

public enum SelectionPolicyName {
    RANDOM,
    ROUND_ROBIN,
    FIRST_UP,
    CLOSEST, 
    FIRST; // for deterministic testing

    /**
     *  Convert Policy name to instance of policy
     */
    public static SelectionPolicy getPolicy(SelectionPolicyName selectionPolicyName) {
        switch (selectionPolicyName) {
            case RANDOM: return new RandomPolicy();
            case ROUND_ROBIN: return new RoundRobinPolicy();
            case FIRST_UP: return new FirstPolicy();
            case CLOSEST: return new FirstPolicy();
            case FIRST: return new FirstPolicy();
            default: return null;
        }
    }
}