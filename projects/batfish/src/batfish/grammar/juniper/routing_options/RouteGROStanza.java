package batfish.grammar.juniper.routing_options;

public class RouteGROStanza {
   private String _policy;
   private String _prefix;
   private int _prefixLength;   

   public RouteGROStanza(String wholePrefix) {
      String[] tmp = wholePrefix.split("/");
      _prefix = tmp[0];
      _prefixLength = Integer.parseInt(tmp[1]);
   }

   public void processStanza(RGStanza rgs) {
      switch (rgs.getType()) {
      case NULL:
         break;
         
      case POLICY:
         PolicyRGStanza prgs = (PolicyRGStanza) rgs;
         _policy = prgs.getListName();
         break;
         
      default:
         throw new Error("bad generate route stanza type");
      }
   }
   
   public String getPolicy(){
      return _policy;
   }
   
   public String getPrefix(){
      return _prefix;      
   }
   
   public int getPrefixLength(){
      return _prefixLength;
   }

}
