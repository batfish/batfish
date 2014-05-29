package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpProcess;

public class NeighborUpdateSourceRBStanza implements RBStanza {
   private String _address;
   private String _source;
   
   public NeighborUpdateSourceRBStanza(String address, String source){
      _address = address;
      _source = source;
   }
   
   
   @Override
   public void process(BgpProcess p) {
      p.setPeerGroupUpdateSource(_address, _source);

   }

}
