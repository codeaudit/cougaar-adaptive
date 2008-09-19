/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.test.knode.experiment.bette;

import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.plugin.TodoPlugin;
import org.cougaar.core.service.UIDService;
import org.cougaar.test.ping.StartRequest;
import org.cougaar.test.ping.StopRequest;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

public class VideoStreamCreaterPlugin extends TodoPlugin {
	@Cougaar.Arg(name = "frameRate", defaultValue = "15", description = "frames per second")
	public float frameRate;

	//TODO use Annotated Service lookup
	private TimedImageService imageService;
	private StartRequest startRequest;
	private StopRequest stopRequest;
	private long waitTime;
	private boolean failed = false;
	private Alarm sendNextAlarm;

	public void start() {
		super.start();
		imageService = getServiceBroker().getService(this,
				TimedImageService.class, null);
	}

	@Cougaar.Execute(on = Subscribe.ModType.ADD)
	public void executeStartRun(StartRequest request) {
		//Indicate that we have started
		startRequest = request;
		startRequest.inc();
		blackboard.publishChange(startRequest);
		// schedule the first frame
		waitTime = (long) (1000 / frameRate);
		sendNextAlarm = executeLater(waitTime, new sendNextImageRunnable(uids,System.currentTimeMillis()));
	}

	@Cougaar.Execute(on = Subscribe.ModType.ADD)
	public void executeStopRun(StopRequest request) {
		stopRequest = request;
	}

	private class sendNextImageRunnable implements Runnable {
		private long time;
		private UIDService uids;

		public sendNextImageRunnable(UIDService uids, long time) {
			this.time = time;
			this.uids = uids;
		}

		public void run() {
			// check if test should stop
			if (stopRequest != null) {
				stopRequest.inc();
				if (failed) {
					stopRequest.forceFailed();
				}
				sendNextAlarm.cancel();
				blackboard.publishChange(stopRequest);
				return;
			}
			// Publish frame for time
			byte[] image = imageService.getImage(time);
			ImageHolder imageHolder = new ImageHolder(uids, time, image);
			blackboard.publishAdd(imageHolder);

			// Schedule Query after Next
			sendNextAlarm = executeLater(waitTime, new sendNextImageRunnable(
					uids, time + waitTime));
		}

	}
}
