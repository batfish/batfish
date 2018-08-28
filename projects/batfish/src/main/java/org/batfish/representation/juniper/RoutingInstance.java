package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SnmpServer;

public class RoutingInstance implements Serializable {

  private static final double DEFAULT_OSPF_REFERENCE_BANDWIDTH = 1E9;

  private static final String MASTER_INTERFACE_NAME = "MASTER_INTERFACE";

  /** */
  private static final long serialVersionUID = 1L;

  @Nullable private Long _as;

  private final SortedMap<String, DhcpRelayGroup> _dhcpRelayGroups;

  private final SortedMap<String, DhcpRelayServerGroup> _dhcpRelayServerGroups;

  private String _domainName;

  private boolean _exportLocalRoutesLan;

  private boolean _exportLocalRoutesPointToPoint;

  private String _forwardingTableExportPolicy;

  private final Interface _globalMasterInterface;

  private String _hostname;

  private final Map<String, Interface> _interfaces;

  private Map<Prefix, IpBgpGroup> _ipBgpGroups;

  private final IsisSettings _isisSettings;

  @Nullable private Integer _loops;

  private BgpGroup _masterBgpGroup;

  private String _name;

  private Map<String, NamedBgpGroup> _namedBgpGroups;

  private final Map<String, NodeDevice> _nodeDevices;

  private Map<Long, OspfArea> _ospfAreas;

  private List<String> _ospfExportPolicies;

  private double _ospfReferenceBandwidth;

  private final Map<String, RoutingInformationBase> _ribs;

  private Ip _routerId;

  private SnmpServer _snmpServer;

  private final JuniperSystem _system;

  public RoutingInstance(String name) {
    _dhcpRelayGroups = new TreeMap<>();
    _dhcpRelayServerGroups = new TreeMap<>();
    _isisSettings = new IsisSettings();
    _interfaces = new TreeMap<>();
    _ipBgpGroups = new TreeMap<>();
    _masterBgpGroup = new BgpGroup();
    _masterBgpGroup.setMultipath(false);
    _masterBgpGroup.setMultipathMultipleAs(false);
    _globalMasterInterface = new Interface(MASTER_INTERFACE_NAME);
    _globalMasterInterface.setRoutingInstance(name);
    _name = name;
    _namedBgpGroups = new TreeMap<>();
    _nodeDevices = new TreeMap<>();
    _ospfAreas = new TreeMap<>();
    _ospfExportPolicies = new LinkedList<>();
    _ospfReferenceBandwidth = DEFAULT_OSPF_REFERENCE_BANDWIDTH;
    _ribs = new TreeMap<>();
    _ribs.put(
        RoutingInformationBase.RIB_IPV4_UNICAST,
        new RoutingInformationBase(RoutingInformationBase.RIB_IPV4_UNICAST));
    _ribs.put(
        RoutingInformationBase.RIB_IPV4_MULTICAST,
        new RoutingInformationBase(RoutingInformationBase.RIB_IPV4_MULTICAST));
    _ribs.put(
        RoutingInformationBase.RIB_IPV4_MPLS,
        new RoutingInformationBase(RoutingInformationBase.RIB_IPV4_MPLS));
    _ribs.put(
        RoutingInformationBase.RIB_IPV6_UNICAST,
        new RoutingInformationBase(RoutingInformationBase.RIB_IPV6_UNICAST));
    _ribs.put(
        RoutingInformationBase.RIB_MPLS,
        new RoutingInformationBase(RoutingInformationBase.RIB_MPLS));
    _ribs.put(
        RoutingInformationBase.RIB_ISIS,
        new RoutingInformationBase(RoutingInformationBase.RIB_ISIS));
    _system = new JuniperSystem();
  }

  @Nullable
  public Long getAs() {
    return _as;
  }

  public SortedMap<String, DhcpRelayGroup> getDhcpRelayGroups() {
    return _dhcpRelayGroups;
  }

  public SortedMap<String, DhcpRelayServerGroup> getDhcpRelayServerGroups() {
    return _dhcpRelayServerGroups;
  }

  public String getDomainName() {
    return _domainName;
  }

  public boolean getExportLocalRoutesLan() {
    return _exportLocalRoutesLan;
  }

  public boolean getExportLocalRoutesPointToPoint() {
    return _exportLocalRoutesPointToPoint;
  }

  public String getForwardingTableExportPolicy() {
    return _forwardingTableExportPolicy;
  }

  public Interface getGlobalMasterInterface() {
    return _globalMasterInterface;
  }

  public String getHostname() {
    return _hostname;
  }

  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public Map<Prefix, IpBgpGroup> getIpBgpGroups() {
    return _ipBgpGroups;
  }

  public IsisSettings getIsisSettings() {
    return _isisSettings;
  }

  @Nullable
  public Integer getLoops() {
    return _loops;
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

  public Map<String, NodeDevice> getNodeDevices() {
    return _nodeDevices;
  }

  public Map<Long, OspfArea> getOspfAreas() {
    return _ospfAreas;
  }

  public List<String> getOspfExportPolicies() {
    return _ospfExportPolicies;
  }

  public double getOspfReferenceBandwidth() {
    return _ospfReferenceBandwidth;
  }

  public Map<String, RoutingInformationBase> getRibs() {
    return _ribs;
  }

  public Ip getRouterId() {
    return _routerId;
  }

  public SnmpServer getSnmpServer() {
    return _snmpServer;
  }

  public JuniperSystem getSystem() {
    return _system;
  }

  public void setAs(@Nullable Long as) {
    _as = as;
  }

  public void setDomainName(String domainName) {
    _domainName = domainName;
  }

  public void setExportLocalRoutesLan(boolean exportLocalRoutesLan) {
    _exportLocalRoutesLan = exportLocalRoutesLan;
  }

  public void setExportLocalRoutesPointToPoint(boolean exportLocalRoutesPointToPoint) {
    _exportLocalRoutesPointToPoint = exportLocalRoutesPointToPoint;
  }

  public void setForwardingTableExportPolicy(String forwardingTableExportPolicy) {
    _forwardingTableExportPolicy = forwardingTableExportPolicy;
  }

  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
  }

  public void setOspfReferenceBandwidth(double ospfReferenceBandwidth) {
    _ospfReferenceBandwidth = ospfReferenceBandwidth;
  }

  public void setLoops(@Nullable Integer loops) {
    _loops = loops;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  public void setSnmpServer(SnmpServer snmpServer) {
    _snmpServer = snmpServer;
  }
}
