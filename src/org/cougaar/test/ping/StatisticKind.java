package org.cougaar.test.ping;

public enum StatisticKind {
	ANOVA {
		public Anova makeStatistic(String name) {
			return new Anova(name);
		}
	},
	
	TRACE {
		public Trace makeStatistic(String name) {
			return new Trace(name);
		}
	};
	
	public abstract Statistic<?> makeStatistic(String name);

}
