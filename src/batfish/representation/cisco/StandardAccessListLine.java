package batfish.representation.cisco;

import batfish.representation.LineAction;


public class StandardAccessListLine {
	   private String _ip;
	   private String _wildcard;
	   private LineAction _action;
	   
	   public StandardAccessListLine(LineAction action, String ip, String wildcard) {
	      _action = action;
	      _ip = ip;
	      _wildcard = wildcard;
	   }

	   public LineAction getAction() {
		   return _action;
	   }
	   
	   public String getIP() {
	      return _ip;
	   }
	   
	   public String getWildcard() {
	      return _wildcard;
	   }
	   
	   public ExtendedAccessListLine toExtendedAccessListLine() {
	      return new ExtendedAccessListLine(_action, 0, _ip, _wildcard, "0.0.0.0", "255.255.255.255", null, null);
	   }
	   
}
