package batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class RoutingInstance implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _hostname;

   private final Map<String, Interface> _interfaces;

   private String _name;

   private final Map<String, RoutingInformationBase> _ribs;

   private final JuniperSystem _system;

   public RoutingInstance(String name) {
      _name = name;
      _interfaces = new TreeMap<String, Interface>();
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

   public String getHostname() {
      return _hostname;
   }

   public Map<String, Interface> getInterfaces() {
      return _interfaces;
   }

   public String getName() {
      return _name;
   }

   public Map<String, RoutingInformationBase> getRibs() {
      return _ribs;
   }

   public JuniperSystem getSystem() {
      return _system;
   }

   public void setHostname(String hostname) {
      _hostname = hostname;
   }

}
