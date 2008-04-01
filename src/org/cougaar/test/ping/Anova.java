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
 * Workfile: Anova.java
 * $Revision: 1.5 $
 * $Date: 2008-04-01 09:19:53 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.test.ping;

public class Anova implements Statistic<Anova> {
    private String name;
    private int valueCount;
    private double sum;
    private double sumSq;
    private double max; 
    private double min;
    private long startTime ;
    private long endTime ;

    public Anova() {
        reset();
    }
    
    public Anova(String name) {
        reset();
        this.name = name;
    }
    
    public void reset() {
        valueCount = 0;
        sum = 0.0;
        sumSq = 0.0;
        max = Double.MIN_VALUE;
        min = Double.MAX_VALUE;
        startTime = System.currentTimeMillis();
        endTime = startTime;

    }

    public void newValue(long value) {
    	newValue((double)value);
    }
    
    public void newValue(double value){
        ++valueCount;
        sum += value;
        sumSq += value * value;
        max = Math.max(max, value);
        min = Math.min(min, value);
        endTime = System.currentTimeMillis();
    }
    
    public Anova delta(Anova other) {
        Anova delta = new Anova();
        delta.name=name;
        delta.valueCount = valueCount - other.valueCount;
        delta.sum = sum - other.sum;
        delta.sumSq = sumSq - other.sumSq;
        delta.max = Math.max(max, other.max);
        delta.min = Math.min(min, other.min);
        delta.startTime= other.endTime;
        delta.endTime = endTime;
        return delta;
    }
    
    public void accumulate(Anova other) {
        valueCount += other.valueCount;
        sum += other.sum;
        sumSq += other.sumSq;
        max = Math.max(max, other.max);
        min = Math.min(min, other.min);
        startTime= Math.min(startTime, other.startTime);
        endTime= Math.max(endTime, other.endTime);
    }
    
    public String getName() {
        return name;
    }
    
    public String getSummaryString() {
		StringBuffer buf = new StringBuffer();
		buf.append(getName());
		buf.append(":");
		buf.append(min());
		buf.append("/");
		buf.append(average());
		buf.append("/");
		buf.append(max());
		return buf.toString();
	}
         
    public String toString() {
    	return "<Anova: " + getSummaryString() + ">";
    }
    
    public Anova clone() throws CloneNotSupportedException {
        return (Anova) super.clone();
    }   
    
    public Anova snapshot() {
    	Anova copy;
		try {
			copy = clone();
		} catch (CloneNotSupportedException e) {
			// This can't happen
			return null;
		}
    	copy.endTime = System.currentTimeMillis();
    	return copy;
    }
    
    public StatisticKind getKind () {
    	return StatisticKind.ANOVA;
    }
    
	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}
    
    /*
     * Anova specific statistical output
     */
    
    public int getValueCount() {
        return valueCount;
    }
    
    public double getSum() {
        return sum;
    }
    
    public double itemPerSec() {
    	long deltaT = endTime - startTime;
        if (deltaT != 0) {
            return (1000.0 * valueCount)/deltaT;
        } else {
            return -1d;
        }
    }
    
    public double average(){
        if (valueCount != 0) {
            return sum / valueCount;
        } else {
            return -1;
        }
    }
    
    public double max() {
        return max;
    }
    
    public double min() {
        return min;
    }
    
    public double stdDev() {
        if (valueCount !=0) {
            double avgSquared = Math.pow(average(), 2);
            return  Math.sqrt(avgSquared - sumSq );
        }
        return 0;
    }

 }