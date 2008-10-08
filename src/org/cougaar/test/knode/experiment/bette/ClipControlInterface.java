package org.cougaar.test.knode.experiment.bette;

public interface ClipControlInterface extends Quitable{
	public void startCapture();
	public void stopCapture();
	public void send();
	public void clear();
}
