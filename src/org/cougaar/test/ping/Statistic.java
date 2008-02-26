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
* $Revision: 1.1 $
* $Date: 2008-02-26 15:31:56 $
* $Author: jzinky $
*
* =============================================================================
*/
 
package org.cougaar.test.ping;

import java.io.Serializable;

public interface Statistic<T extends Statistic<?>> extends Cloneable, Serializable {
    public void newValue(long value);
    public void reset();
    public T delta(T s);
    public T clone() throws CloneNotSupportedException;
}
