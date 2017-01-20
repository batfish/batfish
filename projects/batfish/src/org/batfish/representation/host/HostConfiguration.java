package org.batfish.representation.host;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.batfish.common.VendorConversionException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.iptables.IptablesVendorConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HostConfiguration extends VendorConfiguration {

   private static final String FILTER_FORWARD = "filter::FORWARD";

   private static final Object FILTER_INPUT = "filter::INPUT";

   private static final Object FILTER_OUTPUT = "filter::OUTPUT";

   private static final String HOST_INTERFACES_VAR = "hostInterfaces";

   private static final String HOSTNAME_VAR = "hostname";

   private static final String IPTABLES_FILE_VAR = "iptablesFile";

   private static final String MANGLE_FORWARD = "mangle::FORWARD";

   private static final String MANGLE_INPUT = "mangle::INPUT";

   private static final String MANGLE_OUTPUT = "mangle::OUTPUT";

   private static final String MANGLE_POSTROUTING = "mangle::POSTROUTING";

   private static final String MANGLE_PREROUTING = "mangle::PREROUTING";

   private static final String NAT_OUTPUT = "nat::OUTPUT";

   private static final String NAT_PREROUTING = "nat::PREROUTING";

   private static final String RAW_OUTPUT = "raw::OUTPUT";

   private static final String RAW_PREROUTING = "raw::PREROUTING";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public static HostConfiguration fromJson(String text, Warnings warnings)
         throws JsonParseException, JsonMappingException, IOException {
      ObjectMapper mapper = new BatfishObjectMapper();
      HostConfiguration hostConfiguration = mapper.readValue(text,
            HostConfiguration.class);
      hostConfiguration._w = warnings;
      return hostConfiguration;
   }

   private Configuration _c;

   protected final Map<String, HostInterface> _hostInterfaces;

   private String _hostname;

   private String _iptablesFile;

   private IptablesVendorConfiguration _iptablesVendorConfig;

   protected final RoleSet _roles = new RoleSet();

   private final Set<HostStaticRoute> _staticRoutes;

   // @JsonCreator
   // public HostConfiguration(@JsonProperty(HOSTNAME_VAR) String name) {
   // _hostname = name;
   // _interfaces = new HashMap<String, Interface>();
   // _roles = new RoleSet();
   // }

   private transient Set<String> _unimplementedFeatures;

   public HostConfiguration() {
      _hostInterfaces = new TreeMap<>();
      _staticRoutes = new TreeSet<>();
   }

   @JsonProperty(HOST_INTERFACES_VAR)
   public Map<String, HostInterface> getHostInterfaces() {
      return _hostInterfaces;
   }

   @JsonProperty(HOSTNAME_VAR)
   @Override
   public String getHostname() {
      return _hostname;
   }

   public Map<String, Interface> getInterfaces() {
      throw new UnsupportedOperationException(
            "no implementation for generated method");
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

   @Override
   public void setHostname(String hostname) {
      _hostname = hostname;
   }

   public void setIptablesConfig(IptablesVendorConfiguration config) {
      _iptablesVendorConfig = config;
   }

   public void setIptablesFile(String file) {
      _iptablesFile = file;
   }

   @Override
   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
   }

   @JsonIgnore
   @Override
   public void setVendor(ConfigurationFormat format) {
      throw new UnsupportedOperationException(
            "Cannot set vendor for host configuration");
   }

   private boolean simple() {
      String[] aclsToCheck = new String[] { RAW_PREROUTING, MANGLE_PREROUTING,
            NAT_PREROUTING, MANGLE_INPUT, RAW_OUTPUT, MANGLE_OUTPUT, NAT_OUTPUT,
            MANGLE_FORWARD, FILTER_FORWARD, MANGLE_POSTROUTING };
      for (String aclName : aclsToCheck) {
         IpAccessList acl = _c.getIpAccessLists().get(aclName);
         if (acl != null) {
            for (IpAccessListLine line : acl.getLines()) {
               if (line.getAction() == LineAction.REJECT) {
                  return false;
               }
               if (!line.unrestricted()) {
                  return false;
               }
            }
         }
      }
      return true;
   }

   @Override
   public Configuration toVendorIndependentConfiguration()
         throws VendorConversionException {
      String hostname = getHostname();
      _c = new Configuration(hostname);
      _c.setConfigurationFormat(ConfigurationFormat.HOST);
      _c.setDefaultCrossZoneAction(LineAction.ACCEPT);
      _c.setDefaultInboundAction(LineAction.ACCEPT);
      _c.setRoles(_roles);
      _c.getVrfs().put(Configuration.DEFAULT_VRF_NAME,
            new Vrf(Configuration.DEFAULT_VRF_NAME));

      // add interfaces
      _hostInterfaces.forEach((iname, hostInterface) -> {
         org.batfish.datamodel.Interface newIface = hostInterface
               .toInterface(_c, _w);
         _c.getInterfaces().put(iname, newIface);
         _c.getDefaultVrf().getInterfaces().put(iname, newIface);
      });

      // add iptables
      if (_iptablesVendorConfig != null) {
         _iptablesVendorConfig.addAsIpAccessLists(_c, _w);
      }

      // apply acls to interfaces
      if (simple()) {
         for (Interface iface : _c.getDefaultVrf().getInterfaces().values()) {
            iface.setIncomingFilter(_c.getIpAccessLists().get(FILTER_INPUT));
            iface.setOutgoingFilter(_c.getIpAccessLists().get(FILTER_OUTPUT));
         }
      }
      else {
         _w.unimplemented("Do not support complicated iptables rules yet");
      }

      _c.getDefaultVrf().getStaticRoutes().addAll(_staticRoutes.stream()
            .map(hsr -> hsr.toStaticRoute()).collect(Collectors.toSet()));
      Set<StaticRoute> staticRoutes = _c.getDefaultVrf().getStaticRoutes();
      for (HostInterface iface : _hostInterfaces.values()) {
         Ip gateway = iface.getGateway();
         if (gateway != null) {
            StaticRoute sr = new StaticRoute(Prefix.ZERO, gateway,
                  iface.getName(), AbstractRoute.NO_TAG);
            sr.setAdministrativeCost(
                  HostStaticRoute.DEFAULT_ADMINISTRATIVE_COST);
            staticRoutes.add(sr);
            break;
         }
      }
      if (_staticRoutes.isEmpty() && staticRoutes.isEmpty()
            && !_c.getInterfaces().isEmpty()) {
         String ifaceName = _c.getInterfaces().values().iterator().next()
               .getName();
         StaticRoute sr = new StaticRoute(Prefix.ZERO, null, ifaceName,
               AbstractRoute.NO_TAG);
         sr.setAdministrativeCost(HostStaticRoute.DEFAULT_ADMINISTRATIVE_COST);
         _c.getDefaultVrf().getStaticRoutes().add(sr);
      }
      return _c;
   }
}
