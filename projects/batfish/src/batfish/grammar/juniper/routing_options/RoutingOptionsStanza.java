package batfish.grammar.juniper.routing_options;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;
import batfish.representation.juniper.GenerateRoute;
import batfish.representation.juniper.StaticRoute;

public class RoutingOptionsStanza extends JStanza {
   private List<GenerateRoute> _generateRoutes;
	private List<StaticRoute> _staticRoutes;
	private int _asNum;
	private String _routerID;

	public RoutingOptionsStanza() {
	   _generateRoutes = new ArrayList<GenerateRoute>();
		_staticRoutes = new ArrayList<StaticRoute>();
	}

	public void processStanza(ROStanza ros) {
		
		switch (ros.getType()) {
		case AS:
			AutonomousSystemROStanza asros = (AutonomousSystemROStanza) ros;
			_asNum = asros.getASNum();
			break;

		case NULL:
			break;

		case STATIC:
			StaticROStanza sros = (StaticROStanza) ros;
			_staticRoutes.addAll(sros.getStaticRoutes());
			break;

		case ROUTER_ID:
			RouterIDROStanza rros = (RouterIDROStanza) ros;
			_routerID = rros.getRouterID();
			break;
			
      case AGGREGATE:
         break;
         
      case GENERATE:
         GenerateROStanza gros = (GenerateROStanza) ros;
         _generateRoutes.addAll(gros.getRoutes());
         break;

		default:
			System.out.println("bad ro stanza type");
			break;
		}
	}

	public List<StaticRoute> getStaticRoutes() {
		return _staticRoutes;
	}
	
	public List<GenerateRoute> getGenerateRoutes() {
      return _generateRoutes;
   }

	public int getASNum() {
		return _asNum;
	}

	public String getRouterID() {
		return _routerID;
	}

	@Override
	public JStanzaType getType() {
		return JStanzaType.ROUTING_OPTIONS;
	}

}
