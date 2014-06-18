package batfish.grammar.juniper.policy_options;

import batfish.util.SubRange;

public class RouteFilterFromTPSStanza extends FromTPSStanza {
   private String _prefix;
   private int _prefixLength;
   private SubRange _lengthRange;
   private String _secondPrefix;
   private int _secondPrefixLength;

   public RouteFilterFromTPSStanza(String i, int p) {
      _prefix = i;
      _prefixLength = p;
   }

   public void addRange(SubRange l) {
      _lengthRange = l;
   }

   public void addSecondIP(String i, int p) {
      _secondPrefix = i;
      _secondPrefixLength = p;
   }

   public String getPrefix() {
      return _prefix;
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

   public String getSecondPrefix() {
      return _secondPrefix;
   }
   
   public SubRange getLengthRange(){
      return _lengthRange;
   }
   
   public int getSecondPrefixLength(){
      return _secondPrefixLength;
   }

   @Override
   public FromType getType() {
      return FromType.ROUTE_FILTER;
   }

}
