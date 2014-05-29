package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import batfish.grammar.cisco.ospf.OSPFWildcardNetwork;
import batfish.representation.OspfMetricType;
import batfish.util.Util;

public class OspfProcess {
   private static final int DEFAULT_DEFAULT_INFORMATION_METRIC = 1;
   private static final OspfMetricType DEFAULT_DEFAULT_INFORMATION_METRIC_TYPE = OspfMetricType.E2;
   private static final int DEFAULT_REDISTRIBUTE_CONNECTED_METRIC = 20;
   private static final int DEFAULT_REDISTRIBUTE_STATIC_METRIC = 20;
   private static final double DEFAULT_REFERENCE_BANDWIDTH = 1E9; // bits per
                                                                  // second

   private int _defaultInformationMetric;
   private OspfMetricType _defaultInformationMetricType;
   private boolean _defaultInformationOriginate;
   private boolean _defaultInformationOriginateAlways;
   private String _defaultInformationOriginateMap;
   private Set<String> _interfaceBlacklist;
   private Set<String> _interfaceWhitelist;
   private ArrayList<OspfNetwork> _networks;
   private Set<Integer> _nssas;
   private boolean _passiveInterfaceDefault;
   private int _pid;
   private boolean _redistributeConnected;
   private String _redistributeConnectedMap;
   private int _redistributeConnectedMetric;
   private boolean _redistributeConnectedSubnets;
   private boolean _redistributeStatic;
   private String _redistributeStaticMap;
   private int _redistributeStaticMetric;
   private boolean _redistributeStaticSubnets;
   private double _referenceBandwidth;
   private String _routerId;
   private List<OSPFWildcardNetwork> _wildcardNetworks;

   public OspfProcess(int procnum) {
      _pid = procnum;
      _referenceBandwidth = DEFAULT_REFERENCE_BANDWIDTH;
      _networks = new ArrayList<OspfNetwork>();
      _defaultInformationOriginate = false;
      _defaultInformationOriginateAlways = false;
      _defaultInformationOriginateMap = null;
      _defaultInformationMetric = DEFAULT_DEFAULT_INFORMATION_METRIC;
      _defaultInformationMetricType = DEFAULT_DEFAULT_INFORMATION_METRIC_TYPE;
      _passiveInterfaceDefault = false;
      _nssas = new HashSet<Integer>();
      _interfaceBlacklist = new HashSet<String>();
      _interfaceWhitelist = new HashSet<String>();
      _redistributeConnectedMetric = DEFAULT_REDISTRIBUTE_CONNECTED_METRIC;
      _redistributeStaticMetric = DEFAULT_REDISTRIBUTE_STATIC_METRIC;
      _wildcardNetworks = new ArrayList<OSPFWildcardNetwork>();
   }

   public void computeNetworks(List<Interface> interfaces) {
      for (Interface i : interfaces) {
         String iname = i.getName();
         if (_interfaceBlacklist.contains(iname)
               || (_passiveInterfaceDefault && !_interfaceWhitelist
                     .contains(iname))) {
            continue;
         }
         String intIpStr = i.getIP();
         if (intIpStr == null) {
            continue;
         }
         for (OSPFWildcardNetwork wn : _wildcardNetworks) {
            long intIp = Util.ipToLong(intIpStr);
            long intSubnet = Util.ipToLong(i.getSubnetMask());
            long wildcardNet = Util.ipToLong(wn.getNetworkAddress());
            long wildcard = Util.ipToLong(wn.getWildcard());
            long maskedIntIp = intIp & ~wildcard;
            long maskedWildcardNet = wildcardNet & ~wildcard;
            if (maskedIntIp == maskedWildcardNet) {
               long intNetwork = intIp & intSubnet;
               String intNetworkStr = Util.longToIp(intNetwork);
               _networks.add(new OspfNetwork(intNetworkStr, i.getSubnetMask(),
                     wn.getArea()));
               break;
            }
         }
      }
   }

   public int getDefaultInformationMetric() {
      return _defaultInformationMetric;
   }

   public OspfMetricType getDefaultInformationMetricType() {
      return _defaultInformationMetricType;
   }

   public boolean getDefaultInformationOriginate() {
      return _defaultInformationOriginate;
   }

   public boolean getDefaultInformationOriginateAlways() {
      return _defaultInformationOriginateAlways;
   }

   public String getDefaultInformationOriginateMap() {
      return _defaultInformationOriginateMap;
   }

   public Set<String> getInterfaceBlacklist() {
      return _interfaceBlacklist;
   }

   public Set<String> getInterfaceWhitelist() {
      return _interfaceWhitelist;
   }

   public List<OspfNetwork> getNetworks() {
      return _networks;
   }

   public Set<Integer> getNssas() {
      return _nssas;
   }

   public int getPid() {
      return _pid;
   }

   public boolean getRedistributeConnected() {
      return _redistributeConnected;
   }

   public String getRedistributeConnectedMap() {
      return _redistributeConnectedMap;
   }

   public int getRedistributeConnectedMetric() {
      return _redistributeConnectedMetric;
   }

   public boolean getRedistributeConnectedSubnets() {
      return _redistributeConnectedSubnets;
   }

   public boolean getRedistributeStatic() {
      return _redistributeStatic;
   }

   public String getRedistributeStaticMap() {
      return _redistributeStaticMap;
   }

   public int getRedistributeStaticMetric() {
      return _redistributeStaticMetric;
   }

   public boolean getRedistributeStaticSubnets() {
      return _redistributeStaticSubnets;
   }

   public double getReferenceBandwidth() {
      return _referenceBandwidth;
   }

   public String getRouterId() {
      return _routerId;
   }

   public List<OSPFWildcardNetwork> getWildcardNetworks() {
      return _wildcardNetworks;
   }

   public void setDefaultInformationMetric(int metric) {
      _defaultInformationMetric = metric;
   }

   public void setDefaultInformationMetricType(OspfMetricType metricType) {
      _defaultInformationMetricType = metricType;
   }

   public void setDefaultInformationOriginate(boolean b) {
      _defaultInformationOriginate = b;
   }

   public void setDefaultInformationOriginateAlways(boolean b) {
      _defaultInformationOriginateAlways = b;
   }

   public void setDefaultInformationOriginateMap(String name) {
      _defaultInformationOriginateMap = name;
   }

   public void setPassiveInterfaceDefault(boolean b) {
      _passiveInterfaceDefault = b;
   }

   public void setRedistributeConnected(boolean b) {
      _redistributeConnected = b;
   }

   public void setRedistributeConnectedMetric(int metric) {
      _redistributeConnectedMetric = metric;
   }

   public void setRedistributeConnectedSubnets(boolean b) {
      _redistributeConnectedSubnets = b;
   }

   public void setRedistributeStatic(boolean b) {
      _redistributeStatic = b;
   }

   public void setRedistributeStaticMap(String name) {
      _redistributeStaticMap = name;
   }

   public void setRedistributeStaticMetric(int cost) {
      _redistributeStaticMetric = cost;
   }

   public void setRedistributeStaticSubnets(boolean b) {
      _redistributeStaticSubnets = b;
   }

   public void setReferenceBandwidth(double referenceBandwidth) {
      _referenceBandwidth = referenceBandwidth;
   }

   public void setRouterId(String id) {
      _routerId = id;
   }

}
