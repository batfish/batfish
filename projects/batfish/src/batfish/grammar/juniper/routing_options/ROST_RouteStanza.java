package batfish.grammar.juniper.routing_options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import batfish.representation.juniper.StaticOptions;

public class ROST_RouteStanza extends RO_STStanza {
   
	private String _ip;
   private List<StaticOptions> _staticOptions;	
   
   /* ------------------------------ Constructor ----------------------------*/
   public ROST_RouteStanza() {
      _staticOptions = new ArrayList<StaticOptions> ();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void AddStaticOption (StaticOptions s) {
      _staticOptions.add(s);
   }
   public void set_ip (String i) {
      _ip = i;
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_ip () {
      return _ip;
   }  
   public void set_ip (String i) {
      _ip  =i;
   }
   public List<StaticOptions> get_staticOptions () {
      return _staticOptions;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/ 

	@Override
	public RO_STType getType() {
		return RO_STType.ROUTE;
	}
 
}