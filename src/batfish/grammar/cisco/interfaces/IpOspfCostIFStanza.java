package batfish.grammar.cisco.interfaces;

import batfish.representation.cisco.Interface;

public class IpOspfCostIFStanza implements IFStanza {

   private int _cost;

   public IpOspfCostIFStanza(int cost) {
      _cost = cost;
   }

   @Override
   public void process(Interface i) {
      i.setOspfCost(_cost);
   }

}
