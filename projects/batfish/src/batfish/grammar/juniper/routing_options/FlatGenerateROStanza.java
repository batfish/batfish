package batfish.grammar.juniper.routing_options;

public class FlatGenerateROStanza extends ROStanza {
   private RGType _type1;
   private String _policy;
   private String _prefix;
   private int _prefixLength;   

   public FlatGenerateROStanza(String wholePrefix) {
      String[] tmp = wholePrefix.split("/");
      _prefix = tmp[0];
      _prefixLength = Integer.parseInt(tmp[1]);
      
   }
   
   public void processStanza(RGStanza rgs) {
      _type1 = rgs.getType();
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
   
   public RGType getType1(){
      return _type1;
   }

   @Override
   public ROType getType() {
      return ROType.GENERATE;
   }

}
