package batfish.grammar.juniper.routing_options;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.juniper.Martian;

public class RO_MartiansStanza extends ROStanza {
   
	private List<Martian> _martians;

   /* ------------------------------ Constructor ----------------------------*/
   public RO_MartiansStanza() {
      _martians = new ArrayList<Martian> ();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void AddMartian (Martian m)
   {
      _martians.add(m);
   }

   /* ---------------------------- Getters/Setters --------------------------*/

   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public ROType getType() {
		return ROType.MARTIAN;
	}

}
