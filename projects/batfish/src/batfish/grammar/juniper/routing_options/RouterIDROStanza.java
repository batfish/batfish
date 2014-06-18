package batfish.grammar.juniper.routing_options;

public class RouterIDROStanza extends ROStanza {
	private String _routerID;

	public RouterIDROStanza(String id) {
		_routerID = id;
	}

	public String getRouterID() {
		return _routerID;
	}

	@Override
	public ROType getType() {
		return ROType.ROUTER_ID;
	}
}
