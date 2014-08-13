package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;

public class StaticOptions_Communities extends StaticOptions {
   
   private List<String> _communities;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_Communities (List<String> c) {
      _communities = c;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void AddCommunity(String c) {
      _communities.add(c);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public StaticOptionsType getType() {
		return StaticOptionsType.COMMUNITY;
	}

}
