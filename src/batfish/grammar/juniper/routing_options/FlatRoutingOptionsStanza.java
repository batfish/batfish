package batfish.grammar.juniper.routing_options;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;
import batfish.representation.juniper.GenerateRoute;
import batfish.representation.juniper.StaticRoute;

public class FlatRoutingOptionsStanza extends JStanza {
   private ROType _type1;
   private RGType _type2;
   private GenerateRoute _generateRoutes;
	private List<StaticRoute> _staticRoutes;
	private int _asNum;
	private String _routerID;

	public FlatRoutingOptionsStanza() {
		_staticRoutes = new ArrayList<StaticRoute>();
	}

	public void processStanza(ROStanza ros) {
		_type1 = ros.getType();
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
         FlatGenerateROStanza gros = (FlatGenerateROStanza) ros;
         _type2 = gros.getType1();
         _generateRoutes = new GenerateRoute(gros.getPrefix(), gros.getPrefixLength(), gros.getPolicy(), 130);         
         break;

		default:
			System.out.println("bad ro stanza type");
			break;
		}
	}

	public List<StaticRoute> getStaticRoutes() {
		return _staticRoutes;
	}
	
	public GenerateRoute getGenerateRoutes() {
      return _generateRoutes;
   }

	public int getASNum() {
		return _asNum;
	}

	public String getRouterID() {
		return _routerID;
	}
	
	public ROType getType1(){
	   return _type1;
	}
	
	public RGType getType2(){
	   return _type2;
	}

	@Override
	public JStanzaType getType() {
		return JStanzaType.ROUTING_OPTIONS;
	}

}
