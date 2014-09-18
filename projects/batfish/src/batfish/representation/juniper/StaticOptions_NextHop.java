package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;

public class StaticOptions_NextHop extends StaticOptions {
   
   private String _ip;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_NextHop (String s) {
      _ip = s;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public StaticOptionsType getType() {
		return StaticOptionsType.NEXT_HOP;
	}

}
