package batfish.grammar.juniper.policy_options;

public class PrefixListFromTPSStanza extends FromTPSStanza {
	private String _listName;

	public PrefixListFromTPSStanza(String l) {
		_listName = l;
	}

	public String getListName() {
		return _listName;
	}

	@Override
	public FromType getType() {
		return FromType.PREFIX_LIST;
	}

}
