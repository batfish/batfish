package batfish.grammar.juniper.interfaces;

public class FilterFamilyUStanza extends FamilyUStanza {
   private String _incoming;
   private String _outgoing;
   
   public FilterFamilyUStanza(){
   }
   
   public void addFilter(String name, boolean in){
      if(in){
         _incoming = name;
      }else{
         _outgoing = name;
      }
   }
   
   public String getIncomingFilterName(){
      return _incoming;
   }
   
   public String getOutgoingFilterName(){
      return _outgoing;
   }
   

   @Override
   public FamilyUType getType() {
      return FamilyUType.FILTER;
   }

}
