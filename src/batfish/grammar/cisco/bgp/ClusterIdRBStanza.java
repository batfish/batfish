package batfish.grammar.cisco.bgp;

import batfish.representation.cisco.BgpProcess;
import batfish.util.Util;

public class ClusterIdRBStanza implements RBStanza {
   private Long _id;
   
   public ClusterIdRBStanza(int id){
      _id = Long.valueOf(id);
   }
   
   public ClusterIdRBStanza(String id){
      _id = Util.ipToLong(id);
   }

   @Override
   public void process(BgpProcess p) {
      p.setClusterId(_id);

   }

}
