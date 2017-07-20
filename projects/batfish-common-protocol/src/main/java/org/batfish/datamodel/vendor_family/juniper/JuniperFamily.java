package org.batfish.datamodel.vendor_family.juniper;

import java.util.SortedMap;

public class JuniperFamily {
   
   private SortedMap<String, TacplusServer> _tacplusServers;

   private String _rootAuthenticationEncryptedPassword;
   
   public String getRootAuthenticationEncryptedPassword() {
      return _rootAuthenticationEncryptedPassword;
   }

   public void setRootAuthenticationEncryptedPassword(
         String rootAuthenticationEncryptedPassword) {
      _rootAuthenticationEncryptedPassword = rootAuthenticationEncryptedPassword;
   }

   public SortedMap<String, TacplusServer> getTacplusServers() {
      return _tacplusServers;
   }

   public void setTacplusServers(SortedMap<String, TacplusServer> tacplusServers) {
      _tacplusServers = tacplusServers;
   }

}
