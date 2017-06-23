package org.batfish.datamodel;

import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonSchemaDescription("A virtual routing and forwarding (VRF) instance on a node.")
public class Vrf extends ComparableStructure<String> {

   private static final String BGP_PROCESS_VAR = "bgpProcess";

   private static final String GENERATED_ROUTES_VAR = "aggregateRoutes";

   private static final String INTERFACES_VAR = "interfaces";

   private static final String ISIS_PROCESS_VAR = "isisProcess";

   private static final String OSPF_PROCESS_VAR = "ospfProcess";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String STATIC_ROUTES_VAR = "staticRoutes";

   private transient NavigableSet<BgpAdvertisement> _bgpAdvertisements;

   private BgpProcess _bgpProcess;

   private NavigableSet<GeneratedRoute6> _generatedIpv6Routes;

   private NavigableSet<GeneratedRoute> _generatedRoutes;

   private transient SortedSet<String> _interfaceNames;

   private NavigableMap<String, Interface> _interfaces;

   private IsisProcess _isisProcess;

   private transient NavigableSet<BgpAdvertisement> _originatedAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _originatedEbgpAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _originatedIbgpAdvertisements;

   private OspfProcess _ospfProcess;

   private transient NavigableSet<BgpAdvertisement> _receivedAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _receivedEbgpAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _receivedIbgpAdvertisements;

   private transient NavigableSet<Route> _routes;

   private transient NavigableSet<BgpAdvertisement> _sentAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _sentEbgpAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _sentIbgpAdvertisements;

   private SnmpServer _snmpServer;

   private NavigableSet<StaticRoute> _staticRoutes;

   @JsonCreator
   public Vrf(@JsonProperty(NAME_VAR) String name) {
      super(name);
      _generatedRoutes = new TreeSet<>();
      _generatedIpv6Routes = new TreeSet<>();
      _interfaces = new TreeMap<>();
      _staticRoutes = new TreeSet<>();
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getBgpAdvertisements() {
      return _bgpAdvertisements;
   }

   @JsonProperty(BGP_PROCESS_VAR)
   @JsonPropertyDescription("BGP routing process for this VRF")
   public BgpProcess getBgpProcess() {
      return _bgpProcess;
   }

   @JsonPropertyDescription("Generated IPV6 routes for this VRF")
   public NavigableSet<GeneratedRoute6> getGeneratedIpv6Routes() {
      return _generatedIpv6Routes;
   }

   @JsonProperty(GENERATED_ROUTES_VAR)
   @JsonPropertyDescription("Generated IPV4 routes for this VRF")
   public NavigableSet<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   @JsonProperty(INTERFACES_VAR)
   @JsonPropertyDescription("Interfaces assigned to this VRF")
   public SortedSet<String> getInterfaceNames() {
      if (_interfaces != null && !_interfaces.isEmpty()) {
         return new TreeSet<>(_interfaces.keySet());
      }
      else {
         return _interfaceNames;
      }
   }

   @JsonIgnore
   public NavigableMap<String, Interface> getInterfaces() {
      return _interfaces;
   }

   @JsonProperty(ISIS_PROCESS_VAR)
   @JsonPropertyDescription("IS-IS routing process for this VRF")
   public IsisProcess getIsisProcess() {
      return _isisProcess;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getOriginatedAdvertisements() {
      return _originatedAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getOriginatedEbgpAdvertisements() {
      return _originatedEbgpAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getOriginatedIbgpAdvertisements() {
      return _originatedIbgpAdvertisements;
   }

   @JsonPropertyDescription("OSPF routing process for this VRF")
   @JsonProperty(OSPF_PROCESS_VAR)
   public OspfProcess getOspfProcess() {
      return _ospfProcess;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getReceivedAdvertisements() {
      return _receivedAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getReceivedEbgpAdvertisements() {
      return _receivedEbgpAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getReceivedIbgpAdvertisements() {
      return _receivedIbgpAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<Route> getRoutes() {
      return _routes;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getSentAdvertisements() {
      return _sentAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getSentEbgpAdvertisements() {
      return _sentEbgpAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getSentIbgpAdvertisements() {
      return _sentIbgpAdvertisements;
   }

   public SnmpServer getSnmpServer() {
      return _snmpServer;
   }

   @JsonPropertyDescription("Static routes for this VRF")
   @JsonProperty(STATIC_ROUTES_VAR)
   public NavigableSet<StaticRoute> getStaticRoutes() {
      return _staticRoutes;
   }

   public void initBgpAdvertisements() {
      _bgpAdvertisements = new TreeSet<>();
      _originatedAdvertisements = new TreeSet<>();
      _originatedEbgpAdvertisements = new TreeSet<>();
      _originatedIbgpAdvertisements = new TreeSet<>();
      _receivedAdvertisements = new TreeSet<>();
      _receivedEbgpAdvertisements = new TreeSet<>();
      _receivedIbgpAdvertisements = new TreeSet<>();
      _sentAdvertisements = new TreeSet<>();
      _sentEbgpAdvertisements = new TreeSet<>();
      _sentIbgpAdvertisements = new TreeSet<>();
   }

   public void initRoutes() {
      _routes = new TreeSet<>();
   }

   public void resolveReferences(Configuration owner) {
      if (_interfaceNames != null) {
         for (String ifaceName : _interfaceNames) {
            _interfaces.put(ifaceName, owner.getInterfaces().get(ifaceName));
         }
      }
   }

   @JsonProperty(BGP_PROCESS_VAR)
   public void setBgpProcess(BgpProcess process) {
      _bgpProcess = process;
   }

   public void setGeneratedIpv6Routes(
         NavigableSet<GeneratedRoute6> generatedIpv6Routes) {
      _generatedIpv6Routes = generatedIpv6Routes;
   }

   @JsonProperty(GENERATED_ROUTES_VAR)
   public void setGeneratedRoutes(
         NavigableSet<GeneratedRoute> generatedRoutes) {
      _generatedRoutes = generatedRoutes;
   }

   @JsonProperty(INTERFACES_VAR)
   public void setInterfaceNames(SortedSet<String> interfaceNames) {
      _interfaceNames = interfaceNames;
   }

   @JsonIgnore
   public void setInterfaces(NavigableMap<String, Interface> interfaces) {
      _interfaces = interfaces;
   }

   @JsonProperty(ISIS_PROCESS_VAR)
   public void setIsisProcess(IsisProcess process) {
      _isisProcess = process;
   }

   @JsonProperty(OSPF_PROCESS_VAR)
   public void setOspfProcess(OspfProcess process) {
      _ospfProcess = process;
   }

   public void setSnmpServer(SnmpServer snmpServer) {
      _snmpServer = snmpServer;
   }

   @JsonProperty(STATIC_ROUTES_VAR)
   public void setStaticRoutes(NavigableSet<StaticRoute> staticRoutes) {
      _staticRoutes = staticRoutes;
   }

}
