package org.batfish.symbolic.bdd;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.CommunityVar.Type;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.OspfType;

/*
 * Class to manage the various BDD indexes and operations. This is useful
 * because, which routeVariables are used or needed depends on a number of
 * factors such as what we want to model, if we are doing abstraction etc.
 *
 * It is also made more complicated by the fact that the number of BDD bits
 * needed depends on the particular topology. For example, we may use one
 * bit for every community variable, or a number of bits to encode a router.
 *
 */
public class BDDNetFactory {

  private BDDFactory _factory;

  private List<RoutingProtocol> _allProtos;

  private BDDPairing _pairing;

  private BDDNetConfig _config;

  private List<OspfType> _allMetricTypes;

  private List<CommunityVar> _allCommunities;

  private List<String> _allRouters;

  private List<Integer> _allLocalPrefs;

  private List<Long> _allMeds;

  private List<Long> _allAds;

  private List<Ip> _allIps;

  private Long _maxCost;

  private BDDRoute _routeVariables;

  private BDDPacket _packetVariables;

  private int _numBitsIpProto;

  private int _numBitsDstIp;

  private int _numBitsSrcIp;

  private int _numBitsDstPort;

  private int _numBitsSrcPort;

  private int _numBitsIcmpCode;

  private int _numBitsIcmpType;

  private int _numBitsTcpFlags;

  private int _numBitsPrefixLen;

  private int _numBitsAdminDist;

  private int _numBitsCommunities;

  private int _numBitsRoutingProtocol;

  private int _numBitsMed;

  private int _numBitsMetric;

  private int _numBitsLocalPref;

  private int _numBitsOspfMetric;

  private int _numBitsRouters;

  private int _numBitsRRClient;

  private int _numBitsNextHopIp;

  private int _indexIpProto;

  private int _indexDstIp;

  private int _indexSrcIp;

  private int _indexDstPort;

  private int _indexSrcPort;

  private int _indexIcmpCode;

  private int _indexIcmpType;

  private int _indexTcpFlags;

  private int _indexPrefixLen;

  private int _indexAdminDist;

  private int _indexAdminDistTemp;

  private int _indexCommunities;

  private int _indexCommunitiesTemp;

  private int _indexRoutingProtocol;

  private int _indexRoutingProtocolTemp;

  private int _indexMed;

  private int _indexMedTemp;

  private int _indexMetric;

  private int _indexMetricTemp;

  private int _indexLocalPref;

  private int _indexLocalPrefTemp;

  private int _indexOspfMetric;

  private int _indexOspfMetricTemp;

  private int _indexSrcRouter;

  private int _indexDstRouter;

  private int _indexRouterTemp;

  private int _indexRRClient;

  private int _indexRRClientTemp;

  private int _indexNextHopIp;

  private int _indexNextHopIpTemp;

  public BDDNetFactory(Graph g, BDDNetConfig config) {
    this(
        new ArrayList<>(g.getRouters()),
        (config.getKeepCommunities() ? new ArrayList<>(g.getAllCommunities()) : new ArrayList<>()),
        (config.getKeepLp() ? new ArrayList<>(BDDUtils.findAllLocalPrefs(g)) : new ArrayList<>()),
        (config.getKeepMed() ? new ArrayList<>(BDDUtils.findAllMeds(g)) : new ArrayList<>()),
        (config.getKeepAd()
            ? new ArrayList<>(BDDUtils.findAllAdminDistances(g))
            : new ArrayList<>()),
        (config.getKeepNextHopIp()
            ? new ArrayList<>(BDDUtils.findAllNextHopIps(g))
            : new ArrayList<>()),
        (config.getKeepMetric() ? conservativeMaxCost(g) : 0),
        config);
  }

  public BDDNetFactory(
      List<String> routers,
      List<CommunityVar> comms,
      List<Integer> localPrefs,
      List<Long> meds,
      List<Long> adminDistances,
      List<Ip> ips,
      long maxCost,
      BDDNetConfig config) {

    _allRouters = routers;
    _maxCost = maxCost;

    _allIps = ips;
    Ip ip = new Ip(0);
    if (!_allIps.contains(ip)) {
      _allIps.add(ip);
    }

    _allLocalPrefs = localPrefs;
    if (!_allLocalPrefs.contains(100)) {
      _allLocalPrefs.add(100);
    }

    _allMeds = meds;
    if (!_allMeds.contains(80L)) {
      _allMeds.add(80L);
    }

    _allCommunities = new ArrayList<>();
    for (CommunityVar cvar : comms) {
      if (cvar.getType() != Type.REGEX) {
        _allCommunities.add(cvar);
      }
    }

    _allAds = adminDistances;
    if (_allAds.isEmpty()) {
      _allAds.add(0L);
    }

    _allMetricTypes = new ArrayList<>();
    _allMetricTypes.add(OspfType.O);
    _allMetricTypes.add(OspfType.OIA);
    _allMetricTypes.add(OspfType.E1);
    _allMetricTypes.add(OspfType.E2);

    _allProtos = new ArrayList<>();
    _allProtos.add(RoutingProtocol.CONNECTED);
    _allProtos.add(RoutingProtocol.STATIC);
    _allProtos.add(RoutingProtocol.BGP);
    _allProtos.add(RoutingProtocol.OSPF);
    _allProtos.add(RoutingProtocol.IBGP);
    _allProtos.add(RoutingProtocol.AGGREGATE);
    _allProtos.add(RoutingProtocol.LOCAL);

    Collections.sort(_allAds);
    Collections.sort(_allMeds);
    Collections.sort(_allLocalPrefs);
    Collections.sort(_allRouters);
    Collections.sort(_allIps);
    // invert local preference order so lower is better
    Collections.reverse(_allLocalPrefs);

    int numNodes;
    int cacheSize;
    int numRouters = routers.size();
    if (numRouters < 100) {
      numNodes = 10000;
      cacheSize = 1000;
    } else if (numRouters < 200) {
      numNodes = 100000;
      cacheSize = 10000;
    } else if (numRouters < 400) {
      numNodes = 500000;
      cacheSize = 20000;
    } else {
      numNodes = 1000000;
      cacheSize = 30000;
    }

    _factory = JFactory.init(numNodes, cacheSize);
    _factory.enableReorder();
    _factory.setCacheRatio(32);
    // _factory.setIncreaseFactor(4);

    try {
      // Disables printing
      CallbackHandler handler = new CallbackHandler();
      Method m = handler.getClass().getDeclaredMethod("handle", (Class<?>[]) null);
      _factory.registerGCCallback(handler, m);
      _factory.registerResizeCallback(handler, m);
      _factory.registerReorderCallback(handler, m);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }

    _pairing = _factory.makePair();
    _config = config;

    // BDD Packet routeVariables
    _numBitsIpProto = 8;
    _numBitsDstIp = 32;
    _numBitsSrcIp = 32;
    _numBitsDstPort = 16;
    _numBitsSrcPort = 16;
    _numBitsIcmpCode = 8;
    _numBitsIcmpType = 8;
    _numBitsTcpFlags = 8;

    // BDD Route routeVariables
    _numBitsPrefixLen = 6;
    _numBitsAdminDist = (config.getKeepAd() ? BDDUtils.numBits(_allAds.size()) : 0);
    _numBitsCommunities = (config.getKeepCommunities() ? _allCommunities.size() : 0);
    _numBitsLocalPref = (config.getKeepLp() ? BDDUtils.numBits(_allLocalPrefs.size()) : 0);
    _numBitsMed = (config.getKeepMed() ? BDDUtils.numBits(_allMeds.size()) : 0);
    _numBitsMetric = (config.getKeepMetric() ? BDDUtils.numBits(_maxCost) : 0);
    _numBitsOspfMetric =
        (config.getKeepOspfMetric() ? BDDUtils.numBits(_allMetricTypes.size()) : 0);
    _numBitsRoutingProtocol = (config.getKeepProtocol() ? BDDUtils.numBits(_allProtos.size()) : 0);
    _numBitsRouters = (config.getKeepRouters() ? BDDUtils.numBits(_allRouters.size()) : 0);
    _numBitsRRClient = 1;
    _numBitsNextHopIp = (config.getKeepNextHopIp() ? BDDUtils.numBits(_allIps.size()) : 0);

    int numNeeded =
        _numBitsIpProto
            + _numBitsDstIp
            + _numBitsSrcIp
            + _numBitsDstPort
            + _numBitsSrcPort
            + _numBitsIcmpCode
            + _numBitsIcmpType
            + _numBitsTcpFlags
            + _numBitsPrefixLen
            + 2 * _numBitsAdminDist
            + 2 * _numBitsCommunities
            + 2 * _numBitsLocalPref
            + 2 * _numBitsMed
            + 2 * _numBitsMetric
            + 2 * _numBitsOspfMetric
            + 2 * _numBitsRoutingProtocol
            + 3 * _numBitsRouters
            + 2 * _numBitsRRClient
            + 2 * _numBitsNextHopIp;

    _factory.setVarNum(numNeeded);

    _indexIpProto = 0;
    _indexRoutingProtocol = _indexIpProto + _numBitsIpProto;
    _indexRoutingProtocolTemp = _indexRoutingProtocol + _numBitsRoutingProtocol;
    _indexPrefixLen = _indexRoutingProtocolTemp + _numBitsRoutingProtocol;
    _indexDstIp = _indexPrefixLen + _numBitsPrefixLen;
    _indexSrcIp = _indexDstIp + _numBitsDstIp;
    _indexDstPort = _indexSrcIp + _numBitsSrcIp;
    _indexSrcPort = _indexDstPort + _numBitsDstPort;
    _indexIcmpCode = _indexSrcPort + _numBitsSrcPort;
    _indexIcmpType = _indexIcmpCode + _numBitsIcmpCode;
    _indexTcpFlags = _indexIcmpType + _numBitsIcmpType;

    _indexNextHopIp = _indexTcpFlags + _numBitsTcpFlags;
    _indexNextHopIpTemp = _indexNextHopIp + _numBitsNextHopIp;

    // order decision variables by highest priority
    _indexAdminDist = _indexNextHopIpTemp + _numBitsNextHopIp;
    _indexAdminDistTemp = _indexAdminDist + _numBitsAdminDist;
    _indexLocalPref = _indexAdminDistTemp + _numBitsAdminDist;
    _indexLocalPrefTemp = _indexLocalPref + _numBitsLocalPref;
    _indexMetric = _indexLocalPrefTemp + _numBitsLocalPref;
    _indexMetricTemp = _indexMetric + _numBitsMetric;
    _indexOspfMetric = _indexMetricTemp + _numBitsMetric;
    _indexOspfMetricTemp = _indexOspfMetric + _numBitsOspfMetric;
    _indexMed = _indexOspfMetricTemp + _numBitsOspfMetric;
    _indexMedTemp = _indexMed + _numBitsMed;

    _indexCommunities = _indexMedTemp + _numBitsMed;
    _indexCommunitiesTemp = _indexCommunities + _numBitsCommunities;
    _indexDstRouter = _indexCommunitiesTemp + _numBitsCommunities;
    _indexSrcRouter = _indexDstRouter + _numBitsRouters;
    _indexRouterTemp = _indexSrcRouter + _numBitsRouters;
    _indexRRClient = _indexRouterTemp + _numBitsRouters;
    _indexRRClientTemp = _indexRRClient + _numBitsRRClient;

    _routeVariables = createRoute();
    _packetVariables = createPacket();
  }

  private static long conservativeMaxCost(Graph g) {
    Set<Long> metrics = BDDUtils.findAllSetMetrics(g);
    long maxCost = 0;
    for (Long metric : metrics) {
      if (metric > maxCost) {
        maxCost = metric;
      }
    }
    for (Configuration conf : g.getConfigurations().values()) {
      for (Interface iface : conf.getInterfaces().values()) {
        int cost = 1;
        if (iface.getOspfCost() != null) {
          cost = Math.max(cost, iface.getOspfCost());
        }
        maxCost += cost;
      }
    }
    return maxCost;
  }

  public BDD one() {
    return _factory.one();
  }

  public BDD zero() {
    return _factory.zero();
  }

  public BDDPairing makePair() {
    return _factory.makePair();
  }

  public BDDRoute createRoute() {
    return new BDDRoute(this);
  }

  public BDDRoute createRoute(BDDRoute other) {
    return new BDDRoute(other);
  }

  public BDDPacket createPacket() {
    return new BDDPacket(this);
  }

  public BDDPacket createPacket(BDDPacket pkt) {
    return new BDDPacket(pkt);
  }

  public BDDRoute routeVariables() {
    return _routeVariables;
  }

  public BDDPacket packetVariables() {
    return _packetVariables;
  }

  public BDDNetConfig getConfig() {
    return _config;
  }

  public String getRouter(int i) {
    return _allRouters.get(i);
  }

  public List<RoutingProtocol> getAllProtos() {
    return _allProtos;
  }

  public BDDFactory getFactory() {
    return _factory;
  }

  public int getNumBitsIpProto() {
    return _numBitsIpProto;
  }

  public int getNumBitsDstIp() {
    return _numBitsDstIp;
  }

  public int getNumBitsSrcIp() {
    return _numBitsSrcIp;
  }

  public int getNumBitsDstPort() {
    return _numBitsDstPort;
  }

  public int getNumBitsSrcPort() {
    return _numBitsSrcPort;
  }

  public int getNumBitsIcmpCode() {
    return _numBitsIcmpCode;
  }

  public int getNumBitsIcmpType() {
    return _numBitsIcmpType;
  }

  public int getNumBitsTcpFlags() {
    return _numBitsTcpFlags;
  }

  public int getNumBitsPrefixLen() {
    return _numBitsPrefixLen;
  }

  public int getNumBitsAdminDist() {
    return _numBitsAdminDist;
  }

  public int getNumBitsCommunities() {
    return _numBitsCommunities;
  }

  public int getNumBitsRoutingProtocol() {
    return _numBitsRoutingProtocol;
  }

  public int getNumBitsMed() {
    return _numBitsMed;
  }

  public int getNumBitsMetric() {
    return _numBitsMetric;
  }

  public int getNumBitsLocalPref() {
    return _numBitsLocalPref;
  }

  public int getNumBitsOspfMetric() {
    return _numBitsOspfMetric;
  }

  public int getNumBitsRouters() {
    return _numBitsRouters;
  }

  public int getNumBitsRRClient() {
    return _numBitsRRClient;
  }

  public int getNumBitsNextHopIp() {
    return _numBitsNextHopIp;
  }

  public int getIndexIpProto() {
    return _indexIpProto;
  }

  public int getIndexDstIp() {
    return _indexDstIp;
  }

  public int getIndexSrcIp() {
    return _indexSrcIp;
  }

  public int getIndexDstPort() {
    return _indexDstPort;
  }

  public int getIndexSrcPort() {
    return _indexSrcPort;
  }

  public int getIndexIcmpCode() {
    return _indexIcmpCode;
  }

  public int getIndexIcmpType() {
    return _indexIcmpType;
  }

  public int getIndexTcpFlags() {
    return _indexTcpFlags;
  }

  public int getIndexPrefixLen() {
    return _indexPrefixLen;
  }

  public int getIndexAdminDist() {
    return _indexAdminDist;
  }

  public int getIndexAdminDistTemp() {
    return _indexAdminDistTemp;
  }

  public int getIndexCommunities() {
    return _indexCommunities;
  }

  public int getIndexCommunitiesTemp() {
    return _indexCommunitiesTemp;
  }

  public int getIndexRoutingProtocol() {
    return _indexRoutingProtocol;
  }

  public int getIndexRoutingProtocolTemp() {
    return _indexRoutingProtocolTemp;
  }

  public int getIndexMed() {
    return _indexMed;
  }

  public int getIndexMedTemp() {
    return _indexMedTemp;
  }

  public int getIndexMetric() {
    return _indexMetric;
  }

  public int getIndexMetricTemp() {
    return _indexMetricTemp;
  }

  public int getIndexLocalPref() {
    return _indexLocalPref;
  }

  public int getIndexLocalPrefTemp() {
    return _indexLocalPrefTemp;
  }

  public int getIndexOspfMetric() {
    return _indexOspfMetric;
  }

  public int getIndexOspfMetricTemp() {
    return _indexOspfMetricTemp;
  }

  public int getIndexSrcRouter() {
    return _indexSrcRouter;
  }

  public int getIndexDstRouter() {
    return _indexDstRouter;
  }

  public int getIndexRouterTemp() {
    return _indexRouterTemp;
  }

  public int getIndexRRClient() {
    return _indexRRClient;
  }

  public int getIndexRRClientTemp() {
    return _indexRRClientTemp;
  }

  public int getIndexNextHopIp() {
    return _indexNextHopIp;
  }

  public int getIndexNextHopIpTemp() {
    return _indexNextHopIpTemp;
  }

  public BDDPairing getPairing() {
    return _pairing;
  }

  public List<OspfType> getAllMetricTypes() {
    return _allMetricTypes;
  }

  public List<CommunityVar> getAllCommunities() {
    return _allCommunities;
  }

  public List<String> getAllRouters() {
    return _allRouters;
  }

  public List<Integer> getAllLocalPrefs() {
    return _allLocalPrefs;
  }

  public List<Long> getAllMeds() {
    return _allMeds;
  }

  public List<Long> getAllAdminDistances() {
    return _allAds;
  }

  public List<Ip> getAllIps() {
    return _allIps;
  }

  public Long getMaxCost() {
    return _maxCost;
  }
}
