package batfish.representation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import batfish.util.Util;

public class BgpProcess implements Serializable {

   private static final long serialVersionUID = 1L;

   private Map<String, BgpNeighbor> _bgpNeighbors;
   private Set<GeneratedRoute> _generatedRoutes;

   public BgpProcess() {
      _bgpNeighbors = new HashMap<String, BgpNeighbor>();
      _generatedRoutes = new HashSet<GeneratedRoute>();
   }

   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   public String getIFString(int indentLevel) {
      String retString = Util.getIndentString(indentLevel) + "BgpProcess";

      // ARICHECK: ok to not print the key of this map?
      for (Map.Entry<String, BgpNeighbor> entry : _bgpNeighbors.entrySet()) {
         retString += "\n" + entry.getValue().getIFString(indentLevel + 1);
      }

      for (GeneratedRoute gr : _generatedRoutes) {
         retString += "\n" + gr.getIFString(indentLevel + 1);
      }

      return retString;
   }

   public Map<String, BgpNeighbor> getNeighbors() {
      return _bgpNeighbors;
   }

   public boolean sameParseTree(BgpProcess bgp, String prefix) {
      boolean res = true;
      boolean finalRes = res;

      if (_bgpNeighbors.size() != bgp._bgpNeighbors.size()) {
         System.out.println("BGP:BGPNeighbor:Size" + _bgpNeighbors.size() + ":"
               + bgp._bgpNeighbors.size() + " " + prefix);
         finalRes = res;
      }
      else {
         for (BgpNeighbor lhs : _bgpNeighbors.values()) {
            BgpNeighbor rhs = bgp._bgpNeighbors
                  .get(lhs.getAddress().toString());
            if (rhs == null) {
               System.out.println("BGP:BGPNeighbor:RhsNull " + prefix);
               finalRes = false;
            }
            else {
               res = lhs.sameParseTree(rhs, "BGP:BGPNeighbor:ParseTree "
                     + prefix);
               if (res == false) {
                  finalRes = res;
               }
            }
         }
      }

      if (_generatedRoutes.size() != bgp._generatedRoutes.size()) {
         System.out.println("BGP:GenRoute:Size " + prefix);
         return false;
      }
      else {
         for (GeneratedRoute lhs : _generatedRoutes) {
            boolean found = false;
            for (GeneratedRoute rhs : bgp._generatedRoutes) {
               if (lhs.equals(rhs)) {
                  res = lhs.sameParseTree(rhs, "BGP:GenRoute " + prefix);
                  if (res == false) {
                     finalRes = res;
                  }
                  found = true;
                  break;
               }
            }
            if (found == false) {
               System.out.println("BGP:GenRoute:NotFound " + prefix);
               finalRes = false;
            }
         }
      }

      return finalRes;
   }

}
