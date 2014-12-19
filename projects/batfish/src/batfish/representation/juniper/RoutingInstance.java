package batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import batfish.representation.Ip;

public class RoutingInstance implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Integer _as;

   private String _hostname;

   private final Map<String, Interface> _interfaces;

   private Map<Ip, IpBgpGroup> _ipBgpGroups;

   private BgpGroup _masterBgpGroup;

   private String _name;

   private Map<String, NamedBgpGroup> _namedBgpGroups;

   private Map<Ip, OspfArea> _ospfAreas;

   private List<String> _ospfExportPolicies;

   private final Map<String, RoutingInformationBase> _ribs;

   private Ip _routerId;

   private final JuniperSystem _system;

   public RoutingInstance(String name) {
      _interfaces = new TreeMap<String, Interface>();
      _ipBgpGroups = new TreeMap<Ip, IpBgpGroup>();
      _masterBgpGroup = new BgpGroup();
      _name = name;
      _namedBgpGroups = new TreeMap<String, NamedBgpGroup>();
      _ospfAreas = new TreeMap<Ip, OspfArea>();
      _ospfExportPolicies = new ArrayList<String>();
      _ribs = new TreeMap<String, RoutingInformationBase>();
      _ribs.put(RoutingInformationBase.RIB_IPV4_UNICAST,
            new RoutingInformationBase(RoutingInformationBase.RIB_IPV4_UNICAST));
      _ribs.put(RoutingInformationBase.RIB_IPV4_MULTICAST,
            new RoutingInformationBase(
                  RoutingInformationBase.RIB_IPV4_MULTICAST));
      _ribs.put(RoutingInformationBase.RIB_IPV4_MPLS,
            new RoutingInformationBase(RoutingInformationBase.RIB_IPV4_MPLS));
      _ribs.put(RoutingInformationBase.RIB_IPV6_UNICAST,
            new RoutingInformationBase(RoutingInformationBase.RIB_IPV6_UNICAST));
      _ribs.put(RoutingInformationBase.RIB_MPLS, new RoutingInformationBase(
            RoutingInformationBase.RIB_MPLS));
      _ribs.put(RoutingInformationBase.RIB_ISIS, new RoutingInformationBase(
            RoutingInformationBase.RIB_ISIS));
      _system = new JuniperSystem();
   }

   public Integer getAs() {
      return _as;
   }

   public String getHostname() {
      return _hostname;
   }

   public Map<String, Interface> getInterfaces() {
      return _interfaces;
   }

   public Map<Ip, IpBgpGroup> getIpBgpGroups() {
      return _ipBgpGroups;
   }

   public BgpGroup getMasterBgpGroup() {
      return _masterBgpGroup;
   }

   public String getName() {
      return _name;
   }

   public Map<String, NamedBgpGroup> getNamedBgpGroups() {
      return _namedBgpGroups;
   }

   public Map<Ip, OspfArea> getOspfAreas() {
      return _ospfAreas;
   }

   public List<String> getOspfExportPolicies() {
      return _ospfExportPolicies;
   }

   public Map<String, RoutingInformationBase> getRibs() {
      return _ribs;
   }

   public Ip getRouterId() {
      return _routerId;
   }

   public JuniperSystem getSystem() {
      return _system;
   }

   public void setAs(int as) {
      _as = as;
   }

   public void setHostname(String hostname) {
      _hostname = hostname;
   }

   public void setRouterId(Ip routerId) {
      _routerId = routerId;
   }

}
