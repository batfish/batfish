package batfish.grammar.juniper.policy_options;

public class MetricThenTPSStanza extends ThenTPSStanza {
	private int _metric;

	public MetricThenTPSStanza(int m) {
		_metric = m;
	}

	public int getMetric() {
		return _metric;
	}

	@Override
	public ThenType getType() {
		return ThenType.METRIC;
	}
}
