package batfish.grammar.cisco.ospf;

import batfish.representation.cisco.OspfProcess;

public class AreaNssaROStanza implements ROStanza {

//   private int _num;

   public AreaNssaROStanza(int num) {
//      _num = num;
   }

   @Override
   public void process(OspfProcess p) {
//      p.getNssas().add(_num);
   }

}
