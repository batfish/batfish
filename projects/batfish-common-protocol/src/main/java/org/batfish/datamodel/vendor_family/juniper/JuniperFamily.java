package org.batfish.datamodel.vendor_family.juniper;

import java.util.SortedMap;

public class JuniperFamily {
   
   private SortedMap<String, TacplusServer> _tacplusServers;

   public SortedMap<String, TacplusServer> getTacplusServers() {
      return _tacplusServers;
   }

   public void setTacplusServers(SortedMap<String, TacplusServer> tacplusServers) {
      _tacplusServers = tacplusServers;
   }

}
