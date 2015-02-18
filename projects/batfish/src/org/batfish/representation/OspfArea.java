package org.batfish.representation;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public class OspfArea implements Serializable {

   private static final long serialVersionUID = 1L;

   private Set<Interface> _interfaces;
   private long _number;

   public OspfArea(long areaNum) {
      _interfaces = new LinkedHashSet<Interface>();
      _number = areaNum;
   }

   public Set<Interface> getInterfaces() {
      return _interfaces;
   }

   public long getNumber() {
      return _number;
   }

   public boolean sameParseTree(OspfArea area, String prefix) {
      boolean res = (_number == area._number);
      boolean finalRes = res;

      if (_interfaces.size() != area._interfaces.size()) {
         System.out.println("OspfArea:Interfaces:Size " + prefix);
         return false;
      }
      else {
         for (Interface lhs : _interfaces) {
            boolean found = false;
            for (Interface rhs : area._interfaces) {
               if (lhs.getIP().equals(rhs.getIP())) {
                  found = true;
                  break;
               }
            }
            if (found == false) {
               System.out.println("OspfArea:Interfaces " + prefix);
               finalRes = res;
            }
         }
      }
      return finalRes;
   }

}
