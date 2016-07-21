package org.batfish.representation.host;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.batfish.common.VendorConversionException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.iptables.IptablesVendorConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HostConfiguration implements VendorConfiguration {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final String HOSTNAME_VAR = "hostname";
   
   private static final String HOST_INTERFACES_VAR = "hostInterfaces";
   
   private static final String IPTABLES_FILE_VAR = "iptablesFile";
   
   private Configuration _c;

   private String _hostname;
   
   protected final Map<String, HostInterface> _hostInterfaces = new HashMap<String, HostInterface>();
   
   private String _iptablesFile;
   
   private IptablesVendorConfiguration _iptablesVendorConfig;
    
   protected final RoleSet _roles = new RoleSet();

   private transient Set<String> _unimplementedFeatures;
   
   private transient Warnings _warnings;
   
//   @JsonCreator
//   public HostConfiguration(@JsonProperty(HOSTNAME_VAR) String name) {
//      _hostname = name;      
//      _interfaces = new HashMap<String, Interface>();
//      _roles = new RoleSet();
//   }
   
   public static HostConfiguration fromJson(String text, Warnings warnings) throws JsonParseException, JsonMappingException, IOException {      
      ObjectMapper mapper = new BatfishObjectMapper();
      HostConfiguration hostConfiguration = mapper.readValue(text, HostConfiguration.class);
      hostConfiguration._warnings = warnings;
      return hostConfiguration;
   }

   @JsonProperty(HOSTNAME_VAR)
   @Override
   public String getHostname() {
      return _hostname;
   }
   
   @JsonProperty(HOST_INTERFACES_VAR)
   public Map<String, HostInterface> getHostInterfaces() {
      return _hostInterfaces;
   }

   public Map<String, Interface> getInterfaces() {
      throw new UnsupportedOperationException("no implementation for generated method");
   }
   
   @JsonProperty(IPTABLES_FILE_VAR)
   public String getIptablesFile() {
      return _iptablesFile;
   }


   @JsonIgnore
   @Override
   public RoleSet getRoles() {
      return _roles;
   }

   @JsonIgnore
   @Override
   public Set<String> getUnimplementedFeatures() {
      return _unimplementedFeatures;
   }

   @JsonIgnore
   @Override
   public Warnings getWarnings() {
      return _warnings;
   }

   @Override
   public void setHostname(String hostname) {
      _hostname = hostname;
   }

   public void setIptablesFile(String file) {
      _iptablesFile = file;
   }

   public void setIptablesConfig(IptablesVendorConfiguration config) {
      _iptablesVendorConfig = config;
   }   

   @Override
   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
   }

   @JsonIgnore
   @Override
   public void setVendor(ConfigurationFormat format) {
      throw new UnsupportedOperationException("Cannot set vendor for host configuration");
   }

   @Override
   public Configuration toVendorIndependentConfiguration(Warnings warnings)
         throws VendorConversionException {
      _warnings = warnings;
      String hostname = getHostname();
      _c = new Configuration(hostname);
      _c.setConfigurationFormat(ConfigurationFormat.HOST);
      _c.setRoles(_roles);
      
      //add interfaces
      for (HostInterface hostInterface : _hostInterfaces.values()) {
         _c.getInterfaces().put(hostInterface.getName(), hostInterface.toInterface(_c));         
      }
      
      //add iptables
      if (_iptablesVendorConfig != null) {
         _iptablesVendorConfig.addAsIpAccessLists(_c);
      }
      
      return _c;
   }

}
