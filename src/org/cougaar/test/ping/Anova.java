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
 * $Revision: 1.4 $
 * $Date: 2008-03-31 10:29:41 $
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
    private double max = Long.MIN_VALUE;
    private double min = Long.MAX_VALUE;
    private double timeStamp = System.currentTimeMillis();
    private double deltaT;

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
        timeStamp = System.currentTimeMillis();
        deltaT =0.0;

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
        timeStamp = System.currentTimeMillis();
    }
    
    public Anova delta(Anova other) {
        Anova delta = new Anova();
        delta.name=name;
        delta.valueCount = valueCount - other.valueCount;
        delta.sum = sum - other.sum;
        delta.sumSq = sumSq - other.sumSq;
        delta.max = Math.max(max, other.max);
        delta.min = Math.min(min, other.min);
        delta.deltaT = timeStamp - other.timeStamp;
        delta.timeStamp = timeStamp;
        return delta;
    }
    
    public void accumulate(Anova other) {
        valueCount += other.valueCount;
        sum += other.sum;
        sumSq += other.sumSq;
        max = Math.max(max, other.max);
        min = Math.min(min, other.min);
        timeStamp= Math.max(timeStamp, other.timeStamp);
    }
    
    public String getName() {
        return name;
    }
         
    public Anova clone() throws CloneNotSupportedException {
        return (Anova) super.clone();
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
        if (deltaT != 0) {
            return 1000.0 * valueCount/deltaT;
        } else {
            return -1f;
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