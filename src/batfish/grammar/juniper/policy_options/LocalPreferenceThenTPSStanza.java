package batfish.grammar.juniper.policy_options;

public class LocalPreferenceThenTPSStanza extends ThenTPSStanza {
	private int _localPref;

	public LocalPreferenceThenTPSStanza(int x) {
		_localPref = x;
	}

	public int getLocalPref() {
		return _localPref;
	}

	@Override
	public ThenType getType() {
		return ThenType.LOCAL_PREF;
	}

}
