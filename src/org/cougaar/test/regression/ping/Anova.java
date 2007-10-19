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
 * $Revision: 1.1 $
 * $Date: 2007-10-19 15:01:52 $
 * $Author: rshapiro $
 *
 * =============================================================================
 */

package org.cougaar.test.regression.ping;

public class Anova implements Statistic<Anova> {
    private String name;
    private int valueCount;
    private long sum;
    private long sumSq;
    private long max = Long.MIN_VALUE;
    private long min = Long.MAX_VALUE;
    private long timeStamp = System.currentTimeMillis();
    private long deltaT;

    public Anova() {
        reset();
    }
    
    public Anova(String name) {
        reset();
        this.name = name;
    }

    public void newValue(long value) {
        ++valueCount;
        sum += value;
        sumSq += value ^ 2;
        max = Math.max(max, value);
        min = Math.min(min, value);
        timeStamp = System.currentTimeMillis();
    }

    public int getValueCount() {
        return valueCount;
    }

    public void setValueCount(int valueCount) {
        this.valueCount = valueCount;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public long getSum() {
        return sum;
    }

    
    public float itemPerSec() {
        if (deltaT != 0) {
            return 1000.0f * valueCount/deltaT;
        } else {
            return -1f;
        }
    }
    
    public float average(){
        if (valueCount != 0) {
            return (float) sum / (float) valueCount;
        } else {
            return -1;
        }
    }
    
    public float max() {
        return max;
    }
    
    public float min() {
        return min;
    }
    
    public float stdDev() {
        if (valueCount !=0) {
            double avgSquared = Math.pow(average(), 2);
            return (float) Math.sqrt(avgSquared - sumSq );
        }
        return 0;
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
    
    public void reset() {
        valueCount = 0;
        sum = 0l;
        sumSq = 0l;
        max = Long.MIN_VALUE;
        min = Long.MAX_VALUE;
        timeStamp = System.currentTimeMillis();
        deltaT =0;

    }

    @Override
    public Anova clone() throws CloneNotSupportedException {
        return (Anova) super.clone();
    }


}