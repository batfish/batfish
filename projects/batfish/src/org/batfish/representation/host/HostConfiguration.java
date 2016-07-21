package org.batfish.representation.host;

import java.util.HashMap;
import java.util.Map;

import org.batfish.representation.vyos.Interface;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HostConfiguration {

   private static final String HOSTNAME_VAR = "hostname";
   
   private static final String INTERFACES_VAR = "interfaces";
   
   private static final String IPTABLES_FILE_VAR = "iptablesFile";
   
   private String _hostname;
   
   protected final Map<String, Interface> _interfaces;
   
   private String _iptablesFile;
    
   @JsonCreator
   public HostConfiguration(@JsonProperty(HOSTNAME_VAR) String name) {
      _hostname = name;      
      _interfaces = new HashMap<String, Interface>();
   }
   
   @JsonProperty(HOSTNAME_VAR)
   public String getHostname() {
      return _hostname;
   }
   
   @JsonProperty(INTERFACES_VAR)
   public Map<String, Interface> getInterfaces() {
      return _interfaces;
   }
   
   @JsonProperty(IPTABLES_FILE_VAR)
   public String getIptablesFile() {
      return _iptablesFile;
   }   
}
