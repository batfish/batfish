package org.batfish.symbolic.bdd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.CommunityVar.Type;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.IDeepCopy;
import org.batfish.symbolic.OspfType;
import org.batfish.symbolic.Protocol;

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

  private static int id = 0;

  private int _globalId;

  private BDDFactory _factory;

  private List<Protocol> _allProtos;

  private List<OspfType> _allMetricTypes;

  private BDDPairing _pairing;

  private BDDNetConfig _config;

  private Set<CommunityVar> _allCommunities;

  private List<String> _allRouters;

  private List<Integer> _allLocalPrefs;

  private BDDRoute _routeVariables;

  private BDDPacket _packetVariables;

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

  private int _hcode = 0;

  public BDDNetFactory(Graph g, BDDNetConfig config) {
    _globalId = id++;

    _allMetricTypes = new ArrayList<>();
    _allMetricTypes.add(OspfType.O);
    _allMetricTypes.add(OspfType.OIA);
    _allMetricTypes.add(OspfType.E1);
    _allMetricTypes.add(OspfType.E2);

    _allProtos = new ArrayList<>();
    _allProtos.add(Protocol.CONNECTED);
    _allProtos.add(Protocol.STATIC);
    _allProtos.add(Protocol.OSPF);
    _allProtos.add(Protocol.BGP);

    int numNodes;
    int cacheSize;
    int numRouters = g.getRouters().size();
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
    _factory.disableReorder();
    _factory.setCacheRatio(32);
    // _factory.setIncreaseFactor(4);
    /*
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
    */
    _pairing = _factory.makePair();
    _config = config;
    _allCommunities = g.getAllCommunities();
    _allRouters = new ArrayList<>(g.getRouters());
    _allLocalPrefs = new ArrayList<>(BDDUtils.findAllLocalPrefs(g));

    // BDD Packet routeVariables
    int numIpProto = 8;
    int numDstIp = 32;
    int numSrcIp = 32;
    int numDstPort = 16;
    int numSrcPort = 16;
    int numIcmpCode = 8;
    int numIcmpType = 8;
    int numTcpFlags = 8;

    // BDD Route routeVariables
    int numPrefixLen = 6;
    int numAd = (config.getKeepAd() ? 2 * 32 : 0);
    int numCommunities = (config.getKeepCommunities() ? 2 * _allCommunities.size() : 0);
    int numLocalPref = (config.getKeepLp() ? 2 * BDDUtils.numBits(_allLocalPrefs.size()) : 0);
    int numMed = (config.getKeepMed() ? 2 * 32 : 0);
    int numMetric = (config.getKeepMetric() ? 2 * 32 : 0);
    int numOspfMetric = (config.getKeepOspfMetric() ? 2 * 32 : 0);
    int numProtocol = (config.getKeepProtocol() ? 2 * BDDUtils.numBits(_allProtos.size()) : 0);
    int numRouter = (config.getKeepRouters() ? 3 * BDDUtils.numBits(_allRouters.size()) : 0);
    int numNeeded =
        numIpProto
            + numDstIp
            + numSrcIp
            + numDstPort
            + numSrcPort
            + numIcmpCode
            + numIcmpType
            + numTcpFlags
            + numPrefixLen
            + numAd
            + numCommunities
            + numLocalPref
            + numMed
            + numMetric
            + numOspfMetric
            + numProtocol
            + numRouter;

    _factory.setVarNum(numNeeded);

    _indexIpProto = 0;
    _indexRoutingProtocol = _indexIpProto + numIpProto;
    _indexRoutingProtocolTemp = _indexRoutingProtocol + (numProtocol / 2);
    _indexPrefixLen = _indexRoutingProtocolTemp + (numProtocol / 2);
    _indexDstIp = _indexPrefixLen + numPrefixLen;
    _indexSrcIp = _indexDstIp + numDstIp;
    _indexDstPort = _indexSrcIp + numSrcIp;
    _indexSrcPort = _indexDstPort + numDstPort;
    _indexIcmpCode = _indexSrcPort + numSrcPort;
    _indexIcmpType = _indexIcmpCode + numIcmpCode;
    _indexTcpFlags = _indexIcmpType + numIcmpType;
    _indexMetric = _indexTcpFlags + numTcpFlags;
    _indexMetricTemp = _indexMetric + (numMetric / 2);
    _indexOspfMetric = _indexMetricTemp + (numMetric / 2);
    _indexOspfMetricTemp = _indexOspfMetric + (numOspfMetric / 2);
    _indexMed = _indexOspfMetricTemp + (numOspfMetric / 2);
    _indexMedTemp = _indexMed + (numMed / 2);
    _indexAdminDist = _indexMedTemp + (numMed / 2);
    _indexAdminDistTemp = _indexAdminDist + (numAd / 2);
    _indexLocalPref = _indexAdminDistTemp + (numAd / 2);
    _indexLocalPrefTemp = _indexLocalPref + (numLocalPref / 2);
    _indexCommunities = _indexLocalPrefTemp + (numLocalPref / 2);
    _indexCommunitiesTemp = _indexCommunities + (numCommunities / 2);
    _indexDstRouter = _indexCommunitiesTemp + (numCommunities / 2);
    _indexSrcRouter = _indexDstRouter + (numRouter / 3);
    _indexRouterTemp = _indexSrcRouter + (numRouter / 3);

    _routeVariables = createRoute();
    _packetVariables = createPacket();
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
    return new BDDRoute();
  }

  public BDDRoute createRoute(BDDRoute other) {
    return new BDDRoute(other);
  }

  public BDDPacket createPacket() {
    return new BDDPacket();
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

  public List<Protocol> getAllProtos() {
    return _allProtos;
  }

  public BDDFactory getFactory() {
    return _factory;
  }

  /*
   * BDD encoding of a route message. This includes things like
   * - local preference
   * - administrative distance
   * - path metric, etc.
   *
   * We also include the source and destination router since sometimes
   * this is useful to track where messages came from etc. What features
   * are modeled and which are not is controlled by the BDDNetConfig object
   *
   * There are also several temporary routeVariables named with *Temp. These
   * are here to allow for certain operations such as next state computation
   * in classic symbolic model checking where they represent the next state.
   *
   */
  public class BDDRoute implements IDeepCopy<BDDRoute> {

    private Map<Integer, String> _bitNames;

    private BDDInteger _adminDist;

    private BDDInteger _adminDistTemp;

    private SortedMap<CommunityVar, BDD> _communities;

    private SortedMap<CommunityVar, BDD> _communitiesTemp;

    private BDDFiniteDomain<Integer> _localPref;

    private BDDFiniteDomain<Integer> _localPrefTemp;

    private BDDInteger _med;

    private BDDInteger _medTemp;

    private BDDInteger _metric;

    private BDDInteger _metricTemp;

    private BDDFiniteDomain<OspfType> _ospfMetric;

    private BDDFiniteDomain<OspfType> _ospfMetricTemp;

    private final BDDFiniteDomain<Protocol> _protocolHistory;

    private final BDDFiniteDomain<Protocol> _protocolHistoryTemp;

    private BDDFiniteDomain<String> _dstRouter;

    private BDDFiniteDomain<String> _srcRouter;

    private BDDFiniteDomain<String> _routerTemp;

    private final BDDInteger _prefix;

    private final BDDInteger _prefixLength;

    /*
     * Creates a collection of BDD routeVariables representing the
     * various attributes of a control plane advertisement.
     */
    private BDDRoute() {
      _bitNames = new HashMap<>();

      if (_config.getKeepProtocol()) {
        _protocolHistory = new BDDFiniteDomain<>(_factory, _allProtos, _indexRoutingProtocol);
        _protocolHistoryTemp =
            new BDDFiniteDomain<>(_factory, _allProtos, _indexRoutingProtocolTemp);
        addBitNames("proto", _protocolHistory.numBits(), _indexRoutingProtocol, false);
        addBitNames("proto'", _protocolHistoryTemp.numBits(), _indexRoutingProtocolTemp, false);
      } else {
        _protocolHistory = null;
        _protocolHistoryTemp = null;
      }

      if (_config.getKeepMetric()) {
        _metric = BDDInteger.makeFromIndex(_factory, 32, _indexMetric, false);
        _metricTemp = BDDInteger.makeFromIndex(_factory, 32, _indexMetricTemp, false);
        addBitNames("metric", 32, _indexMetric, false);
        addBitNames("metric'", 32, _indexMetricTemp, false);
      }

      if (_config.getKeepMed()) {
        _med = BDDInteger.makeFromIndex(_factory, 32, _indexMed, false);
        _medTemp = BDDInteger.makeFromIndex(_factory, 32, _indexMedTemp, false);
        addBitNames("med", 32, _indexMed, false);
        addBitNames("med'", 32, _indexMedTemp, false);
      }

      if (_config.getKeepAd()) {
        _adminDist = BDDInteger.makeFromIndex(_factory, 32, _indexAdminDist, false);
        _adminDistTemp = BDDInteger.makeFromIndex(_factory, 32, _indexAdminDistTemp, false);
        addBitNames("ad", 32, _indexAdminDist, false);
        addBitNames("ad'", 32, _indexAdminDistTemp, false);
      }

      if (_config.getKeepLp()) {
        _localPref = new BDDFiniteDomain<>(_factory, _allLocalPrefs, _indexLocalPref);
        _localPrefTemp = new BDDFiniteDomain<>(_factory, _allLocalPrefs, _indexLocalPrefTemp);
        addBitNames("lp", _localPref.numBits(), _indexLocalPref, false);
        addBitNames("lp'", _localPrefTemp.numBits(), _indexLocalPrefTemp, false);
      }

      _prefixLength = BDDInteger.makeFromIndex(_factory, 6, _indexPrefixLen, false);
      addBitNames("pfxLen", 6, _indexPrefixLen, false);
      _prefix = BDDInteger.makeFromIndex(_factory, 32, _indexDstIp, false);
      addBitNames("pfx", 32, _indexDstIp, false);

      if (_config.getKeepCommunities()) {
        _communities = new TreeMap<>();
        int i = 0;
        for (CommunityVar comm : _allCommunities) {
          if (comm.getType() != Type.REGEX) {
            _communities.put(comm, _factory.ithVar(_indexCommunities + i));
            _bitNames.put(_indexCommunities + i, comm.getValue());
            i++;
          }
        }
        _communitiesTemp = new TreeMap<>();
        i = 0;
        for (CommunityVar comm : _allCommunities) {
          if (comm.getType() != Type.REGEX) {
            _communitiesTemp.put(comm, _factory.ithVar(_indexCommunitiesTemp + i));
            _bitNames.put(_indexCommunitiesTemp + i, comm.getValue());
            i++;
          }
        }
      }

      if (_config.getKeepOspfMetric()) {
        _ospfMetric = new BDDFiniteDomain<>(_factory, _allMetricTypes, _indexOspfMetric);
        _ospfMetricTemp = new BDDFiniteDomain<>(_factory, _allMetricTypes, _indexOspfMetricTemp);
        addBitNames("ospfMetric", _ospfMetric.numBits(), _indexOspfMetric, false);
        addBitNames("ospfMetric'", _ospfMetricTemp.numBits(), _indexOspfMetricTemp, false);
      }

      if (_config.getKeepRouters()) {
        _dstRouter = new BDDFiniteDomain<>(_factory, _allRouters, _indexDstRouter);
        _srcRouter = new BDDFiniteDomain<>(_factory, _allRouters, _indexSrcRouter);
        _routerTemp = new BDDFiniteDomain<>(_factory, _allRouters, _indexRouterTemp);
        addBitNames("dstRouter", _dstRouter.numBits(), _indexDstRouter, false);
        addBitNames("srcRouter", _srcRouter.numBits(), _indexSrcRouter, false);
        addBitNames("tempRouter", _routerTemp.numBits(), _indexRouterTemp, false);
      }
    }

    /*
     * Create a BDDRecord from another. Because BDDs are immutable,
     * there is no need for a deep copy.
     */
    private BDDRoute(BDDRoute other) {
      _prefixLength = new BDDInteger(other._prefixLength);
      _prefix = new BDDInteger(other._prefix);
      if (_config.getKeepCommunities()) {
        _communities = new TreeMap<>(other._communities);
        _communitiesTemp = new TreeMap<>(other._communitiesTemp);
      }
      if (_config.getKeepMetric()) {
        _metric = new BDDInteger(other._metric);
        _metricTemp = new BDDInteger(other._metricTemp);
      }
      if (_config.getKeepAd()) {
        _adminDist = new BDDInteger(other._adminDist);
        _adminDistTemp = new BDDInteger(other._adminDistTemp);
      }
      if (_config.getKeepMed()) {
        _med = new BDDInteger(other._med);
        _medTemp = new BDDInteger(other._medTemp);
      }
      if (_config.getKeepLp()) {
        _localPref = new BDDFiniteDomain<>(other._localPref);
        _localPrefTemp = new BDDFiniteDomain<>(other._localPrefTemp);
      }
      if (_config.getKeepProtocol()) {
        _protocolHistory = new BDDFiniteDomain<>(other._protocolHistory);
        _protocolHistoryTemp = new BDDFiniteDomain<>(other._protocolHistoryTemp);
      } else {
        _protocolHistory = null;
        _protocolHistoryTemp = null;
      }
      if (_config.getKeepOspfMetric()) {
        _ospfMetric = new BDDFiniteDomain<>(other._ospfMetric);
        _ospfMetricTemp = new BDDFiniteDomain<>(other._ospfMetricTemp);
      }
      if (_config.getKeepRouters()) {
        _dstRouter = new BDDFiniteDomain<>(other._dstRouter);
        _srcRouter = new BDDFiniteDomain<>(other._srcRouter);
        _routerTemp = new BDDFiniteDomain<>(other._routerTemp);
      }
      _bitNames = other._bitNames;
    }

    private void addBitNames(String s, int length, int index, boolean reverse) {
      for (int i = index; i < index + length; i++) {
        if (reverse) {
          _bitNames.put(i, s + (length - 1 - (i - index)));
        } else {
          _bitNames.put(i, s + (i - index + 1));
        }
      }
    }

    @Override
    public BDDRoute deepCopy() {
      return new BDDRoute(this);
    }

    public String dot(BDD bdd) {
      StringBuilder sb = new StringBuilder();
      sb.append("digraph G {\n");
      if (!bdd.isOne()) {
        sb.append("0 [shape=box, label=\"0\", style=filled, shape=box, height=0.3, width=0.3];\n");
      }
      if (!bdd.isZero()) {
        sb.append("1 [shape=box, label=\"1\", style=filled, shape=box, height=0.3, width=0.3];\n");
      }
      dotRec(sb, bdd, new HashSet<>());
      sb.append("}");
      return sb.toString();
    }

    private Integer dotId(BDD bdd) {
      if (bdd.isZero()) {
        return 0;
      }
      if (bdd.isOne()) {
        return 1;
      }
      return bdd.hashCode() + 2;
    }

    private void dotRec(StringBuilder sb, BDD bdd, Set<BDD> visited) {
      if (bdd.isOne() || bdd.isZero() || visited.contains(bdd)) {
        return;
      }
      int val = dotId(bdd);
      int valLow = dotId(bdd.low());
      int valHigh = dotId(bdd.high());
      String name = _bitNames.get(bdd.var());
      sb.append(val).append(" [label=\"").append(name).append("\"]\n");
      sb.append(val).append(" -> ").append(valLow).append("[style=dotted]\n");
      sb.append(val).append(" -> ").append(valHigh).append("[style=filled]\n");
      visited.add(bdd);
      dotRec(sb, bdd.low(), visited);
      dotRec(sb, bdd.high(), visited);
    }

    public String name(int i) {
      return _bitNames.get(i);
    }

    public BDDNetConfig getConfig() {
      return _config;
    }

    public BDDInteger getAdminDist() {
      return _adminDist;
    }

    public void setAdminDist(BDDInteger adminDist) {
      this._adminDist = adminDist;
    }

    public Map<CommunityVar, BDD> getCommunities() {
      return _communities;
    }

    public void setCommunities(SortedMap<CommunityVar, BDD> communities) {
      this._communities = communities;
    }

    public BDDFiniteDomain<Integer> getLocalPref() {
      return _localPref;
    }

    public void setLocalPref(BDDFiniteDomain<Integer> localPref) {
      this._localPref = localPref;
    }

    public BDDInteger getMed() {
      return _med;
    }

    public void setMed(BDDInteger med) {
      this._med = med;
    }

    public BDDInteger getMetric() {
      return _metric;
    }

    public void setMetric(BDDInteger metric) {
      this._metric = metric;
    }

    public BDDFiniteDomain<OspfType> getOspfMetric() {
      return _ospfMetric;
    }

    public void setOspfMetric(BDDFiniteDomain<OspfType> ospfMetric) {
      this._ospfMetric = ospfMetric;
    }

    public BDDInteger getPrefix() {
      return _prefix;
    }

    public BDDInteger getPrefixLength() {
      return _prefixLength;
    }

    public BDDFiniteDomain<Protocol> getProtocolHistory() {
      return _protocolHistory;
    }

    public BDDFiniteDomain<String> getDstRouter() {
      return _dstRouter;
    }

    public BDDFiniteDomain<String> getSrcRouter() {
      return _srcRouter;
    }

    public BDDFiniteDomain<String> getRouterTemp() {
      return _routerTemp;
    }

    public void setDstRouter(BDDFiniteDomain<String> dstRouter) {
      this._dstRouter = dstRouter;
    }

    public BDDInteger getAdminDistTemp() {
      return _adminDistTemp;
    }

    public SortedMap<CommunityVar, BDD> getCommunitiesTemp() {
      return _communitiesTemp;
    }

    public BDDFiniteDomain<Integer> getLocalPrefTemp() {
      return _localPrefTemp;
    }

    public BDDInteger getMedTemp() {
      return _medTemp;
    }

    public BDDInteger getMetricTemp() {
      return _metricTemp;
    }

    public BDDFiniteDomain<OspfType> getOspfMetricTemp() {
      return _ospfMetricTemp;
    }

    public BDDFiniteDomain<Protocol> getProtocolHistoryTemp() {
      return _protocolHistoryTemp;
    }

    @Override
    public int hashCode() {
      if (_hcode == 0) {
        int result = _adminDist != null ? _adminDist.hashCode() : 0;
        result = 31 * result + (_metric != null ? _metric.hashCode() : 0);
        result = 31 * result + (_ospfMetric != null ? _ospfMetric.hashCode() : 0);
        result = 31 * result + (_med != null ? _med.hashCode() : 0);
        result = 31 * result + (_localPref != null ? _localPref.hashCode() : 0);
        result = 31 * result + (_communities != null ? _communities.hashCode() : 0);
        _hcode = result;
      }
      return _hcode;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof BDDRoute)) {
        return false;
      }
      BDDRoute other = (BDDRoute) o;
      return Objects.equals(_metric, other._metric)
          && Objects.equals(_ospfMetric, other._ospfMetric)
          && Objects.equals(_localPref, other._localPref)
          && Objects.equals(_communities, other._communities)
          && Objects.equals(_med, other._med)
          && Objects.equals(_adminDist, other._adminDist);
    }

    public void orWith(BDDRoute other) {

      if (_config.getKeepMetric()) {
        BDD[] metric = getMetric().getBitvec();
        BDD[] metric2 = other.getMetric().getBitvec();
        for (int i = 0; i < 32; i++) {
          metric[i].orWith(metric2[i]);
        }
      }

      if (_config.getKeepAd()) {
        BDD[] adminDist = getAdminDist().getBitvec();
        BDD[] adminDist2 = other.getAdminDist().getBitvec();
        for (int i = 0; i < 32; i++) {
          adminDist[i].orWith(adminDist2[i]);
        }
      }

      if (_config.getKeepMed()) {
        BDD[] med = getMed().getBitvec();
        BDD[] med2 = other.getMed().getBitvec();
        for (int i = 0; i < 32; i++) {
          med[i].orWith(med2[i]);
        }
      }

      if (_config.getKeepLp()) {
        BDD[] localPref = getLocalPref().getInteger().getBitvec();
        BDD[] localPref2 = other.getLocalPref().getInteger().getBitvec();
        for (int i = 0; i < 32; i++) {
          localPref[i].orWith(localPref2[i]);
        }
      }

      if (_config.getKeepOspfMetric()) {
        BDD[] ospfMet = getOspfMetric().getInteger().getBitvec();
        BDD[] ospfMet2 = other.getOspfMetric().getInteger().getBitvec();
        for (int i = 0; i < ospfMet.length; i++) {
          ospfMet[i].orWith(ospfMet2[i]);
        }
      }

      if (_config.getKeepCommunities()) {
        getCommunities()
            .forEach(
                (cvar, bdd1) -> {
                  BDD bdd2 = other.getCommunities().get(cvar);
                  bdd1.orWith(bdd2);
                });
      }
    }

    public BDDRoute restrict(Prefix pfx) {
      int len = pfx.getPrefixLength();
      long bits = pfx.getStartIp().asLong();
      int[] vars = new int[len];
      BDD[] vals = new BDD[len];
      // NOTE: do not create a new _pairing each time
      // JavaBDD will start to memory leak
      _pairing.reset();
      for (int i = 0; i < len; i++) {
        int var = _prefix.getBitvec()[i].var(); // prefixIndex + i;
        BDD subst = Ip.getBitAtPosition(bits, i) ? _factory.one() : _factory.zero();
        vars[i] = var;
        vals[i] = subst;
      }
      _pairing.set(vars, vals);

      BDDRoute rec = new BDDRoute(this);

      if (_config.getKeepMetric()) {
        BDD[] metric = rec.getMetric().getBitvec();
        for (int i = 0; i < 32; i++) {
          metric[i] = metric[i].veccompose(_pairing);
        }
      }

      if (_config.getKeepAd()) {
        BDD[] adminDist = rec.getAdminDist().getBitvec();
        for (int i = 0; i < 32; i++) {
          adminDist[i] = adminDist[i].veccompose(_pairing);
        }
      }

      if (_config.getKeepMed()) {
        BDD[] med = rec.getMed().getBitvec();
        for (int i = 0; i < 32; i++) {
          med[i] = med[i].veccompose(_pairing);
        }
      }

      if (_config.getKeepLp()) {
        BDD[] localPref = rec.getLocalPref().getInteger().getBitvec();
        for (int i = 0; i < 32; i++) {
          localPref[i] = localPref[i].veccompose(_pairing);
        }
      }

      if (_config.getKeepOspfMetric()) {
        BDD[] ospfMet = rec.getOspfMetric().getInteger().getBitvec();
        for (int i = 0; i < ospfMet.length; i++) {
          ospfMet[i] = ospfMet[i].veccompose(_pairing);
        }
      }

      if (_config.getKeepCommunities()) {
        rec.getCommunities().replaceAll((k, v) -> v.veccompose(_pairing));
      }

      return rec;
    }

    public BDDRoute restrict(List<Prefix> prefixes) {
      if (prefixes.isEmpty()) {
        throw new BatfishException("Empty prefix list in BDDRecord restrict");
      }
      BDDRoute r = restrict(prefixes.get(0));
      for (int i = 1; i < prefixes.size(); i++) {
        Prefix p = prefixes.get(i);
        BDDRoute x = restrict(p);
        r.orWith(x);
      }
      return r;
    }
  }

  /*
   * Symbolic encoding of a packet using BDDs. Includes packet header
   * fields such as dstIp, srcIp, dstPort, etc.
   *
   * The dstIp is shared with the 'prefix' bits from the BDDRoute class
   * so that we can apply ACLs freely to headers generated from a prefix.
   *
   */
  public class BDDPacket {

    private Map<Integer, String> _bitNames;

    private BDDInteger _dstIp;

    private BDDInteger _dstPort;

    private BDDInteger _icmpCode;

    private BDDInteger _icmpType;

    private BDDInteger _ipProtocol;

    private BDDInteger _srcIp;

    private BDDInteger _srcPort;

    private BDD _tcpAck;

    private BDD _tcpCwr;

    private BDD _tcpEce;

    private BDD _tcpFin;

    private BDD _tcpPsh;

    private BDD _tcpRst;

    private BDD _tcpSyn;

    private BDD _tcpUrg;

    /*
     * Creates a collection of BDD routeVariables representing the
     * various attributes of a control plane advertisement.
     */
    private BDDPacket() {
      _bitNames = new HashMap<>();

      // Initialize integer values
      _ipProtocol = BDDInteger.makeFromIndex(_factory, 8, _indexIpProto, false);
      _dstIp = BDDInteger.makeFromIndex(_factory, 32, _indexDstIp, true);
      _srcIp = BDDInteger.makeFromIndex(_factory, 32, _indexSrcIp, true);
      _dstPort = BDDInteger.makeFromIndex(_factory, 16, _indexDstPort, false);
      _srcPort = BDDInteger.makeFromIndex(_factory, 16, _indexSrcPort, false);
      _icmpCode = BDDInteger.makeFromIndex(_factory, 8, _indexIcmpCode, false);
      _icmpType = BDDInteger.makeFromIndex(_factory, 8, _indexIcmpType, false);
      _tcpAck = _factory.ithVar(_indexTcpFlags);
      _tcpCwr = _factory.ithVar(_indexTcpFlags + 1);
      _tcpEce = _factory.ithVar(_indexTcpFlags + 2);
      _tcpFin = _factory.ithVar(_indexTcpFlags + 3);
      _tcpPsh = _factory.ithVar(_indexTcpFlags + 4);
      _tcpRst = _factory.ithVar(_indexTcpFlags + 5);
      _tcpSyn = _factory.ithVar(_indexTcpFlags + 6);
      _tcpUrg = _factory.ithVar(_indexTcpFlags + 7);
      addBitNames("ipProtocol", 8, _indexIpProto, false);
      addBitNames("dstIp", 32, _indexDstIp, false);
      addBitNames("srcIp", 32, _indexSrcIp, false);
      addBitNames("dstPort", 16, _indexDstPort, false);
      addBitNames("srcPort", 16, _indexSrcPort, false);
      addBitNames("icmpCode", 8, _indexIcmpCode, false);
      addBitNames("icmpType", 8, _indexIcmpType, false);
      _bitNames.put(_indexTcpFlags, "tcpAck");
      _bitNames.put(_indexTcpFlags + 1, "tcpCwr");
      _bitNames.put(_indexTcpFlags + 2, "tcpEce");
      _bitNames.put(_indexTcpFlags + 3, "tcpFin");
      _bitNames.put(_indexTcpFlags + 4, "tcpPsh");
      _bitNames.put(_indexTcpFlags + 5, "tcpRst");
      _bitNames.put(_indexTcpFlags + 6, "tcpSyn");
      _bitNames.put(_indexTcpFlags + 7, "tcpUrg");
    }

    /*
     * Create a BDDRecord from another. Because BDDs are immutable,
     * there is no need for a deep copy.
     */
    private BDDPacket(BDDPacket other) {
      _srcIp = new BDDInteger(other._srcIp);
      _dstIp = new BDDInteger(other._dstIp);
      _srcPort = new BDDInteger(other._srcPort);
      _dstPort = new BDDInteger(other._dstPort);
      _icmpCode = new BDDInteger(other._icmpCode);
      _icmpType = new BDDInteger(other._icmpType);
      _ipProtocol = new BDDInteger(other._ipProtocol);
      _tcpAck = other._tcpAck;
      _tcpCwr = other._tcpCwr;
      _tcpEce = other._tcpEce;
      _tcpFin = other._tcpFin;
      _tcpPsh = other._tcpPsh;
      _tcpRst = other._tcpRst;
      _tcpSyn = other._tcpSyn;
      _tcpUrg = other._tcpUrg;
    }

    /*
     * Helper function that builds a map from BDD variable index
     * to some more meaningful name. Helpful for debugging.
     */
    private void addBitNames(String s, int length, int index, boolean reverse) {
      for (int i = index; i < index + length; i++) {
        if (reverse) {
          _bitNames.put(i, s + (length - 1 - (i - index)));
        } else {
          _bitNames.put(i, s + (i - index + 1));
        }
      }
    }

    /*
     * Convenience method for the copy constructor
     */
    public BDDPacket copy() {
      return new BDDPacket(this);
    }

    /*
     * Converts a BDD to the graphviz DOT format for debugging.
     */
    public String dot(BDD bdd) {
      StringBuilder sb = new StringBuilder();
      sb.append("digraph G {\n");
      sb.append("0 [shape=box, label=\"0\", style=filled, shape=box, height=0.3, width=0.3];\n");
      sb.append("1 [shape=box, label=\"1\", style=filled, shape=box, height=0.3, width=0.3];\n");
      dotRec(sb, bdd, new HashSet<>());
      sb.append("}");
      return sb.toString();
    }

    /*
     * Creates a unique id for a bdd node when generating
     * a DOT file for graphviz
     */
    private Integer dotId(BDD bdd) {
      if (bdd.isZero()) {
        return 0;
      }
      if (bdd.isOne()) {
        return 1;
      }
      return bdd.hashCode() + 2;
    }

    /*
     * Recursively builds each of the intermediate BDD nodes in the
     * graphviz DOT format.
     */
    private void dotRec(StringBuilder sb, BDD bdd, Set<BDD> visited) {
      if (bdd.isOne() || bdd.isZero() || visited.contains(bdd)) {
        return;
      }
      int val = dotId(bdd);
      int valLow = dotId(bdd.low());
      int valHigh = dotId(bdd.high());
      String name = _bitNames.get(bdd.var());
      sb.append(val).append(" [label=\"").append(name).append("\"]\n");
      sb.append(val).append(" -> ").append(valLow).append("[style=dotted]\n");
      sb.append(val).append(" -> ").append(valHigh).append("[style=filled]\n");
      visited.add(bdd);
      dotRec(sb, bdd.low(), visited);
      dotRec(sb, bdd.high(), visited);
    }

    public BDDInteger getDstIp() {
      return _dstIp;
    }

    public void setDstIp(BDDInteger x) {
      this._dstIp = x;
    }

    public BDDInteger getDstPort() {
      return _dstPort;
    }

    public void setDstPort(BDDInteger x) {
      this._dstPort = x;
    }

    public BDDInteger getIcmpCode() {
      return _icmpCode;
    }

    public void setIcmpCode(BDDInteger x) {
      this._icmpCode = x;
    }

    public BDDInteger getIcmpType() {
      return _icmpType;
    }

    public void setIcmpType(BDDInteger x) {
      this._icmpType = x;
    }

    public BDDInteger getIpProtocol() {
      return _ipProtocol;
    }

    public void setIpProtocol(BDDInteger x) {
      this._ipProtocol = x;
    }

    public BDDInteger getSrcIp() {
      return _srcIp;
    }

    public void setSrcIp(BDDInteger x) {
      this._srcIp = x;
    }

    public BDDInteger getSrcPort() {
      return _srcPort;
    }

    public void setSrcPort(BDDInteger x) {
      this._srcPort = x;
    }

    public BDD getTcpAck() {
      return _tcpAck;
    }

    public void setTcpAck(BDD tcpAck) {
      this._tcpAck = tcpAck;
    }

    public BDD getTcpCwr() {
      return _tcpCwr;
    }

    public void setTcpCwr(BDD tcpCwr) {
      this._tcpCwr = tcpCwr;
    }

    public BDD getTcpEce() {
      return _tcpEce;
    }

    public void setTcpEce(BDD tcpEce) {
      this._tcpEce = tcpEce;
    }

    public BDD getTcpFin() {
      return _tcpFin;
    }

    public void setTcpFin(BDD tcpFin) {
      this._tcpFin = tcpFin;
    }

    public BDD getTcpPsh() {
      return _tcpPsh;
    }

    public void setTcpPsh(BDD tcpPsh) {
      this._tcpPsh = tcpPsh;
    }

    public BDD getTcpRst() {
      return _tcpRst;
    }

    public void setTcpRst(BDD tcpRst) {
      this._tcpRst = tcpRst;
    }

    public BDD getTcpSyn() {
      return _tcpSyn;
    }

    public void setTcpSyn(BDD tcpSyn) {
      this._tcpSyn = tcpSyn;
    }

    public BDD getTcpUrg() {
      return _tcpUrg;
    }

    public void setTcpUrg(BDD tcpUrg) {
      this._tcpUrg = tcpUrg;
    }

    @Override
    public int hashCode() {
      int result = _dstIp != null ? _dstIp.hashCode() : 0;
      result = 31 * result + (_srcIp != null ? _srcIp.hashCode() : 0);
      result = 31 * result + (_dstPort != null ? _dstPort.hashCode() : 0);
      result = 31 * result + (_srcPort != null ? _srcPort.hashCode() : 0);
      result = 31 * result + (_icmpCode != null ? _icmpCode.hashCode() : 0);
      result = 31 * result + (_icmpType != null ? _icmpType.hashCode() : 0);
      result = 31 * result + (_ipProtocol != null ? _ipProtocol.hashCode() : 0);
      result = 31 * result + (_tcpAck != null ? _tcpAck.hashCode() : 0);
      result = 31 * result + (_tcpCwr != null ? _tcpCwr.hashCode() : 0);
      result = 31 * result + (_tcpEce != null ? _tcpEce.hashCode() : 0);
      result = 31 * result + (_tcpFin != null ? _tcpFin.hashCode() : 0);
      result = 31 * result + (_tcpPsh != null ? _tcpPsh.hashCode() : 0);
      result = 31 * result + (_tcpRst != null ? _tcpRst.hashCode() : 0);
      result = 31 * result + (_tcpSyn != null ? _tcpSyn.hashCode() : 0);
      result = 31 * result + (_tcpUrg != null ? _tcpUrg.hashCode() : 0);
      return result;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof BDDPacket)) {
        return false;
      }
      BDDPacket other = (BDDPacket) o;

      return Objects.equals(_srcPort, other._srcPort)
          && Objects.equals(_icmpType, other._icmpType)
          && Objects.equals(_icmpCode, other._icmpCode)
          && Objects.equals(_ipProtocol, other._ipProtocol)
          && Objects.equals(_dstPort, other._dstPort)
          && Objects.equals(_dstIp, other._dstIp)
          && Objects.equals(_srcIp, other._srcIp)
          && Objects.equals(_tcpAck, other._tcpAck)
          && Objects.equals(_tcpCwr, other._tcpCwr)
          && Objects.equals(_tcpEce, other._tcpEce)
          && Objects.equals(_tcpFin, other._tcpFin)
          && Objects.equals(_tcpPsh, other._tcpPsh)
          && Objects.equals(_tcpRst, other._tcpRst)
          && Objects.equals(_tcpSyn, other._tcpSyn)
          && Objects.equals(_tcpUrg, other._tcpUrg);
    }

    public BDD restrict(BDD bdd, Prefix pfx) {
      int len = pfx.getPrefixLength();
      long bits = pfx.getStartIp().asLong();
      int[] vars = new int[len];
      BDD[] vals = new BDD[len];
      _pairing.reset();
      for (int i = 0; i < len; i++) {
        int var = _dstIp.getBitvec()[i].var(); // dstIpIndex + i;
        BDD subst = Ip.getBitAtPosition(bits, i) ? _factory.one() : _factory.zero();
        vars[i] = var;
        vals[i] = subst;
      }
      _pairing.set(vars, vals);
      return bdd.veccompose(_pairing);
    }

    public BDD restrict(BDD bdd, List<Prefix> prefixes) {
      if (prefixes.isEmpty()) {
        throw new BatfishException("Empty prefix list in BDDRecord restrict");
      }
      BDD r = restrict(bdd, prefixes.get(0));
      for (int i = 1; i < prefixes.size(); i++) {
        Prefix p = prefixes.get(i);
        BDD x = restrict(bdd, p);
        r = r.or(x);
      }
      return r;
    }
  }
}
