package batfish.grammar.juniper.routing_options;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.juniper.StaticOptions;

public class ROST_DefaultsStanza extends RO_STStanza {
   
   private List<StaticOptions> _staticOptions;
   
   /* ------------------------------ Constructor ----------------------------*/
   public ROST_DefaultsStanza () {
      _staticOptions = new ArrayList<StaticOptions> ();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void AddStaticOption (StaticOptions s) {
      _staticOptions.add(s);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<StaticOptions> get_staticOptions () {
      return _staticOptions;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  

	@Override
	public RO_STType getType() {
		return RO_STType.DEFAULTS;
	}
}
