/* =============================================================================
*
*                  COPYRIGHT 2007 BBN Technologies Corp.
*                  10 Moulton St
*                  Cambridge MA 02138
*                  (617) 873-8000
*
*       This program is the subject of intellectual property rights
*       licensed from BBN Technologies
*
*       This legend must continue to appear in the source code
*       despite modifications or enhancements by any party.
*
*
* =============================================================================
*
* Created : Aug 14, 2007
* Workfile: Statistic.java
* $Revision: 1.3 $
* $Date: 2008-04-01 09:19:53 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.ping;

import java.io.Serializable;

/*
 * Statistic gathers a summary of a series of statistical values.
 * Each statistic has its own interface for the actual data summary that it is collecting
 * Only the mechanisms for processing statistics in general are exposed in this interface:
 * Several types of operations are performed on statistic be different roles
 * 1) Collector initialize the statistic (reset) and tags the statistic with the collection point name
 * 2) Collector puts  values into statistics (new value)
 * 3) Report Generator snap shots statistics (clone) at two points puts in time
 *   and sends the (delta)  between two snapshots
 * 4) Controller (accumulate) statistics from multiple collectors
 * 5) Controller (store) statistic for later post processing
 */

public interface Statistic<T extends Statistic<?>> extends Cloneable, Serializable {
	
	/*
	 * Add a new value to the statistic
	 */
	public void newValue(long value);

	/*
	 * Return Statistic to initial state
	 */
    public void reset();
    
    /*
     * Take two samples of the statistic in time and return a new statistic representing 
     * only the newValue events that happened between the two samples.
     */
    public T delta(T earlierStatistic);
    
    /* 
     * Accumulate additional statistics into overview statistic
     */
    public void accumulate(T additionalStatistic);
 
    /*
     * Copy the content of the statistics, leaving endTime alone;
     */
    public T clone() throws CloneNotSupportedException;
    
    /*
     * Snapshot the content of the statistics, setting endTime to the current time;
     */
    public T snapshot();
 
    
	/*
	 * Get name of the collection point where the statistic was gathered.
	 * The name should represent a unique collection point over the whole society.
	 * The name is used to bind statistic collected at the same collection point, 
	 * but at different times
	 */
	public String getName();
	
	/*
	 * Short one-line summary string for human consumption
	 */
    public String getSummaryString();
    
    /*
     * Return enum for use in switch statements
     */
    public StatisticKind getKind();
    
    /*
     * Time statistics started (not the time of the first newValue)
     */    
	public long getStartTime();
	
	/* 
	 * Time of the last newValue or time the snapshot was taken
	 */
	public long getEndTime(); 
}
