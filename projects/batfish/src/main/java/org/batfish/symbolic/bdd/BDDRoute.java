package org.batfish.symbolic.bdd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.CommunityVar.Type;
import org.batfish.symbolic.IDeepCopy;
import org.batfish.symbolic.OspfType;

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
 * in classic symbolic model checking. This gives rise to more efficient
 * ways to compute, e.g., strongest postcondition by doing a single
 * BDD substitution operation for all temporary variables at the end.
 *
 */
public class BDDRoute implements IDeepCopy<BDDRoute> {

  private BDDNetFactory _factory;

  private Map<Integer, String> _bitNames;

  private Map<CommunityVar, Integer> _communityIndexOffset;

  private final BDDFiniteDomain<RoutingProtocol> _protocolHistory;

  private final BDDFiniteDomain<RoutingProtocol> _protocolHistoryTemp;

  private BDDInteger _metric;

  private BDDInteger _metricTemp;

  private BDDFiniteDomain<Long> _med;

  private BDDFiniteDomain<Long> _medTemp;

  private BDDFiniteDomain<Long> _adminDist;

  private BDDFiniteDomain<Long> _adminDistTemp;

  private BDDFiniteDomain<Integer> _localPref;

  private BDDFiniteDomain<Integer> _localPrefTemp;

  private SortedMap<CommunityVar, BDD> _communities;

  private SortedMap<CommunityVar, BDD> _communitiesTemp;

  private BDDFiniteDomain<OspfType> _ospfMetric;

  private BDDFiniteDomain<OspfType> _ospfMetricTemp;

  private final BDDInteger _prefixLength;

  private final BDDInteger _prefix;

  private BDDFiniteDomain<String> _dstRouter;

  private BDDFiniteDomain<String> _srcRouter;

  private BDDFiniteDomain<String> _routerTemp;

  private BDD _fromRRClient;

  private BDD _fromRRClientTemp;

  private BDDFiniteDomain<Ip> _nextHopIp;

  private BDDFiniteDomain<Ip> _nextHopIpTemp;

  /*
   * Creates a collection of BDD routeVariables representing the
   * various attributes of a control plane advertisement.
   */
  public BDDRoute(BDDNetFactory netFactory) {
    _factory = netFactory;
    _bitNames = new HashMap<>();
    _communityIndexOffset = new HashMap<>();

    BDDFactory factory = _factory.getFactory();

    if (_factory.getConfig().getKeepProtocol()) {
      _protocolHistory =
          new BDDFiniteDomain<>(
              factory, _factory.getAllProtos(), _factory.getIndexRoutingProtocol());
      _protocolHistoryTemp =
          new BDDFiniteDomain<>(
              factory, _factory.getAllProtos(), _factory.getIndexRoutingProtocolTemp());
      addBitNames("proto", _protocolHistory.numBits(), _factory.getIndexRoutingProtocol(), false);
      addBitNames(
          "proto'", _protocolHistoryTemp.numBits(), _factory.getIndexRoutingProtocolTemp(), false);
    } else {
      _protocolHistory = null;
      _protocolHistoryTemp = null;
    }

    if (_factory.getConfig().getKeepMetric()) {
      _metric =
          BDDInteger.makeFromIndex(
              factory, _factory.getNumBitsMetric(), _factory.getIndexMetric(), false);
      _metricTemp =
          BDDInteger.makeFromIndex(
              factory, _factory.getNumBitsMetric(), _factory.getIndexMetricTemp(), false);
      addBitNames("metric", _factory.getNumBitsMetric(), _factory.getIndexMetric(), false);
      addBitNames("metric'", _factory.getNumBitsMetric(), _factory.getIndexMetricTemp(), false);
    }

    if (_factory.getConfig().getKeepMed()) {
      _med = new BDDFiniteDomain<>(factory, _factory.getAllMeds(), _factory.getIndexMed());
      _medTemp = new BDDFiniteDomain<>(factory, _factory.getAllMeds(), _factory.getIndexMedTemp());
      addBitNames("med", _factory.getNumBitsMed(), _factory.getIndexMed(), false);
      addBitNames("med'", _factory.getNumBitsMed(), _factory.getIndexMedTemp(), false);
    }

    if (_factory.getConfig().getKeepAd()) {
      _adminDist =
          new BDDFiniteDomain<>(
              factory, _factory.getAllAdminDistances(), _factory.getIndexAdminDist());
      _adminDistTemp =
          new BDDFiniteDomain<>(
              factory, _factory.getAllAdminDistances(), _factory.getIndexAdminDistTemp());
      addBitNames("ad", _factory.getNumBitsAdminDist(), _factory.getIndexAdminDist(), false);
      addBitNames("ad'", _factory.getNumBitsAdminDist(), _factory.getIndexAdminDistTemp(), false);
    }

    if (_factory.getConfig().getKeepLp()) {
      _localPref =
          new BDDFiniteDomain<>(factory, _factory.getAllLocalPrefs(), _factory.getIndexLocalPref());
      _localPrefTemp =
          new BDDFiniteDomain<>(
              factory, _factory.getAllLocalPrefs(), _factory.getIndexLocalPrefTemp());
      addBitNames("lp", _localPref.numBits(), _factory.getIndexLocalPref(), false);
      addBitNames("lp'", _localPrefTemp.numBits(), _factory.getIndexLocalPrefTemp(), false);
    }

    _prefixLength =
        BDDInteger.makeFromIndex(
            factory, _factory.getNumBitsPrefixLen(), _factory.getIndexPrefixLen(), false);
    addBitNames("pfxLen", _factory.getNumBitsPrefixLen(), _factory.getIndexPrefixLen(), false);

    _prefix =
        BDDInteger.makeFromIndex(
            factory, _factory.getNumBitsDstIp(), _factory.getIndexDstIp(), false);
    addBitNames("dstIp", _factory.getNumBitsDstIp(), _factory.getIndexDstIp(), false);

    if (_factory.getConfig().getKeepCommunities()) {
      _communities = new TreeMap<>();
      int i = 0;
      for (CommunityVar comm : _factory.getAllCommunities()) {
        _communityIndexOffset.put(comm, i);
        if (comm.getType() != Type.REGEX) {
          _communities.put(comm, factory.ithVar(_factory.getIndexCommunities() + i));
          _bitNames.put(_factory.getIndexCommunities() + i, comm.toString());
          i++;
        }
      }
      _communitiesTemp = new TreeMap<>();
      i = 0;
      for (CommunityVar comm : _factory.getAllCommunities()) {
        if (comm.getType() != Type.REGEX) {
          _communitiesTemp.put(comm, factory.ithVar(_factory.getIndexCommunitiesTemp() + i));
          _bitNames.put(_factory.getIndexCommunitiesTemp() + i, comm.toString());
          i++;
        }
      }
    }

    if (_factory.getConfig().getKeepOspfMetric()) {
      _ospfMetric =
          new BDDFiniteDomain<>(
              factory, _factory.getAllMetricTypes(), _factory.getIndexOspfMetric());
      _ospfMetricTemp =
          new BDDFiniteDomain<>(
              factory, _factory.getAllMetricTypes(), _factory.getIndexOspfMetricTemp());
      addBitNames("ospfMetric", _ospfMetric.numBits(), _factory.getIndexOspfMetric(), false);
      addBitNames(
          "ospfMetric'", _ospfMetricTemp.numBits(), _factory.getIndexOspfMetricTemp(), false);
    }

    if (_factory.getConfig().getKeepRouters()) {
      _dstRouter =
          new BDDFiniteDomain<>(factory, _factory.getAllRouters(), _factory.getIndexDstRouter());
      _srcRouter =
          new BDDFiniteDomain<>(factory, _factory.getAllRouters(), _factory.getIndexSrcRouter());
      _routerTemp =
          new BDDFiniteDomain<>(factory, _factory.getAllRouters(), _factory.getIndexRouterTemp());
      addBitNames("dstRouter", _dstRouter.numBits(), _factory.getIndexDstRouter(), false);
      addBitNames("srcRouter", _srcRouter.numBits(), _factory.getIndexSrcRouter(), false);
      addBitNames("tempRouter", _routerTemp.numBits(), _factory.getIndexRouterTemp(), false);
    }

    if (_factory.getConfig().getKeepRRClient()) {
      _fromRRClient = _factory.getFactory().ithVar(_factory.getIndexRRClient());
      _fromRRClientTemp = _factory.getFactory().ithVar(_factory.getIndexRRClientTemp());
      addBitNames(
          "fromRRClient", _factory.getNumBitsRRClient(), _factory.getIndexRRClient(), false);
      addBitNames(
          "fromRRClient'", _factory.getNumBitsRRClient(), _factory.getIndexRRClientTemp(), false);
    }

    if (_factory.getConfig().getKeepNextHopIp()) {
      _nextHopIp =
          new BDDFiniteDomain<>(factory, _factory.getAllIps(), _factory.getIndexNextHopIp());
      _nextHopIpTemp =
          new BDDFiniteDomain<>(factory, _factory.getAllIps(), _factory.getIndexNextHopIpTemp());
      addBitNames("nextHop", _factory.getNumBitsNextHopIp(), _factory.getIndexNextHopIp(), false);
      addBitNames(
          "nextHop'", _factory.getNumBitsNextHopIp(), _factory.getIndexNextHopIpTemp(), false);
    }
  }

  /*
   * Create a BDDRoute from another. Because BDDs are immutable,
   * there is no need for a deep copy.
   */
  public BDDRoute(BDDRoute other) {
    _factory = other._factory;
    _communityIndexOffset = other._communityIndexOffset;
    _prefixLength = new BDDInteger(other._prefixLength);
    _prefix = new BDDInteger(other._prefix);
    if (_factory.getConfig().getKeepCommunities()) {
      _communities = new TreeMap<>(other._communities);
      _communitiesTemp = new TreeMap<>(other._communitiesTemp);
    }
    if (_factory.getConfig().getKeepMetric()) {
      _metric = new BDDInteger(other._metric);
      _metricTemp = new BDDInteger(other._metricTemp);
    }
    if (_factory.getConfig().getKeepAd()) {
      _adminDist = new BDDFiniteDomain<>(other._adminDist);
      _adminDistTemp = new BDDFiniteDomain<>(other._adminDistTemp);
    }
    if (_factory.getConfig().getKeepMed()) {
      _med = new BDDFiniteDomain<>(other._med);
      _medTemp = new BDDFiniteDomain<>(other._medTemp);
    }
    if (_factory.getConfig().getKeepLp()) {
      _localPref = new BDDFiniteDomain<>(other._localPref);
      _localPrefTemp = new BDDFiniteDomain<>(other._localPrefTemp);
    }
    if (_factory.getConfig().getKeepProtocol()) {
      _protocolHistory = new BDDFiniteDomain<>(other._protocolHistory);
      _protocolHistoryTemp = new BDDFiniteDomain<>(other._protocolHistoryTemp);
    } else {
      _protocolHistory = null;
      _protocolHistoryTemp = null;
    }
    if (_factory.getConfig().getKeepOspfMetric()) {
      _ospfMetric = new BDDFiniteDomain<>(other._ospfMetric);
      _ospfMetricTemp = new BDDFiniteDomain<>(other._ospfMetricTemp);
    }
    if (_factory.getConfig().getKeepRouters()) {
      _dstRouter = new BDDFiniteDomain<>(other._dstRouter);
      _srcRouter = new BDDFiniteDomain<>(other._srcRouter);
      _routerTemp = new BDDFiniteDomain<>(other._routerTemp);
    }
    if (_factory.getConfig().getKeepRRClient()) {
      _fromRRClient = other._fromRRClient;
      _fromRRClientTemp = other._fromRRClientTemp;
    }
    if (_factory.getConfig().getKeepNextHopIp()) {
      _nextHopIp = other._nextHopIp;
      _nextHopIpTemp = other._nextHopIpTemp;
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

  public String name(int i) {
    return _bitNames.get(i);
  }

  public BDDNetConfig getConfig() {
    return _factory.getConfig();
  }

  public BDDFiniteDomain<Long> getAdminDist() {
    return _adminDist;
  }

  public void setAdminDist(BDDFiniteDomain<Long> adminDist) {
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

  public BDDFiniteDomain<Long> getMed() {
    return _med;
  }

  public void setMed(BDDFiniteDomain<Long> med) {
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

  public BDDFiniteDomain<RoutingProtocol> getProtocolHistory() {
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

  public BDDFiniteDomain<Long> getAdminDistTemp() {
    return _adminDistTemp;
  }

  public SortedMap<CommunityVar, BDD> getCommunitiesTemp() {
    return _communitiesTemp;
  }

  public BDDFiniteDomain<Integer> getLocalPrefTemp() {
    return _localPrefTemp;
  }

  public BDDFiniteDomain<Long> getMedTemp() {
    return _medTemp;
  }

  public BDDInteger getMetricTemp() {
    return _metricTemp;
  }

  public BDDFiniteDomain<OspfType> getOspfMetricTemp() {
    return _ospfMetricTemp;
  }

  public BDDFiniteDomain<RoutingProtocol> getProtocolHistoryTemp() {
    return _protocolHistoryTemp;
  }

  public BDD getFromRRClient() {
    return _fromRRClient;
  }

  public void setFromRRClient(BDD x) {
    this._fromRRClient = x;
  }

  public BDD getFromRRClientTemp() {
    return _fromRRClientTemp;
  }

  public BDDFiniteDomain<Ip> getNextHopIp() {
    return _nextHopIp;
  }

  public void setNextHopIp(BDDFiniteDomain<Ip> x) {
    _nextHopIp = x;
  }

  public BDDFiniteDomain<Ip> getNextHopIpTemp() {
    return _nextHopIpTemp;
  }

  public void setFromRRClientTemp(BDD x) {
    this._fromRRClientTemp = x;
  }

  public Map<Integer, String> getBitNames() {
    return _bitNames;
  }

  public Map<CommunityVar, Integer> getCommunityIndexOffset() {
    return _communityIndexOffset;
  }

  @Override
  public int hashCode() {
    int result = _adminDist != null ? _adminDist.hashCode() : 0;
    result = 31 * result + (_metric != null ? _metric.hashCode() : 0);
    result = 31 * result + (_ospfMetric != null ? _ospfMetric.hashCode() : 0);
    result = 31 * result + (_med != null ? _med.hashCode() : 0);
    result = 31 * result + (_localPref != null ? _localPref.hashCode() : 0);
    result = 31 * result + (_communities != null ? _communities.hashCode() : 0);
    result = 31 * result + (_prefix != null ? _prefix.hashCode() : 0);
    result = 31 * result + (_prefixLength != null ? _prefixLength.hashCode() : 0);
    result = 31 * result + (_dstRouter != null ? _dstRouter.hashCode() : 0);
    result = 31 * result + (_srcRouter != null ? _srcRouter.hashCode() : 0);
    result = 31 * result + (_protocolHistory != null ? _protocolHistory.hashCode() : 0);
    result = 31 * result + (_fromRRClient != null ? _fromRRClient.hashCode() : 0);
    result = 31 * result + (_nextHopIp != null ? _nextHopIp.hashCode() : 0);
    return result;
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
        && Objects.equals(_adminDist, other._adminDist)
        && Objects.equals(_prefix, other._prefix)
        && Objects.equals(_prefixLength, other._prefixLength)
        && Objects.equals(_dstRouter, other._dstRouter)
        && Objects.equals(_srcRouter, other._srcRouter)
        && Objects.equals(_protocolHistory, other._protocolHistory)
        && Objects.equals(_fromRRClient, other._fromRRClient)
        && Objects.equals(_nextHopIp, other._nextHopIp);
  }

  public void orWith(BDDRoute other) {

    if (_factory.getConfig().getKeepMetric()) {
      BDD[] metric = getMetric().getBitvec();
      BDD[] metric2 = other.getMetric().getBitvec();
      for (int i = 0; i < 32; i++) {
        metric[i].orWith(metric2[i]);
      }
    }

    if (_factory.getConfig().getKeepAd()) {
      BDD[] adminDist = getAdminDist().getInteger().getBitvec();
      BDD[] adminDist2 = other.getAdminDist().getInteger().getBitvec();
      for (int i = 0; i < 32; i++) {
        adminDist[i].orWith(adminDist2[i]);
      }
    }

    if (_factory.getConfig().getKeepMed()) {
      BDD[] med = getMed().getInteger().getBitvec();
      BDD[] med2 = other.getMed().getInteger().getBitvec();
      for (int i = 0; i < 32; i++) {
        med[i].orWith(med2[i]);
      }
    }

    if (_factory.getConfig().getKeepLp()) {
      BDD[] localPref = getLocalPref().getInteger().getBitvec();
      BDD[] localPref2 = other.getLocalPref().getInteger().getBitvec();
      for (int i = 0; i < 32; i++) {
        localPref[i].orWith(localPref2[i]);
      }
    }

    if (_factory.getConfig().getKeepOspfMetric()) {
      BDD[] ospfMet = getOspfMetric().getInteger().getBitvec();
      BDD[] ospfMet2 = other.getOspfMetric().getInteger().getBitvec();
      for (int i = 0; i < ospfMet.length; i++) {
        ospfMet[i].orWith(ospfMet2[i]);
      }
    }

    if (_factory.getConfig().getKeepRRClient()) {
      _fromRRClient.orWith(other._fromRRClient);
    }

    if (_factory.getConfig().getKeepNextHopIp()) {
      BDD[] nh = getNextHopIp().getInteger().getBitvec();
      BDD[] nh2 = other.getNextHopIp().getInteger().getBitvec();
      for (int i = 0; i < nh.length; i++) {
        nh[i].orWith(nh2[i]);
      }
    }

    if (_factory.getConfig().getKeepCommunities()) {
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
    BDDPairing pairing = _factory.getPairing();
    pairing.reset();
    for (int i = 0; i < len; i++) {
      int var = _prefix.getBitvec()[i].var(); // prefixIndex + i;
      BDD subst = Ip.getBitAtPosition(bits, i) ? _factory.one() : _factory.zero();
      vars[i] = var;
      vals[i] = subst;
    }
    pairing.set(vars, vals);

    BDDRoute rec = new BDDRoute(this);

    if (_factory.getConfig().getKeepMetric()) {
      BDD[] metric = rec.getMetric().getBitvec();
      for (int i = 0; i < 32; i++) {
        metric[i] = metric[i].veccompose(pairing);
      }
    }

    if (_factory.getConfig().getKeepAd()) {
      BDD[] adminDist = rec.getAdminDist().getInteger().getBitvec();
      for (int i = 0; i < 32; i++) {
        adminDist[i] = adminDist[i].veccompose(pairing);
      }
    }

    if (_factory.getConfig().getKeepMed()) {
      BDD[] med = rec.getMed().getInteger().getBitvec();
      for (int i = 0; i < 32; i++) {
        med[i] = med[i].veccompose(pairing);
      }
    }

    if (_factory.getConfig().getKeepLp()) {
      BDD[] localPref = rec.getLocalPref().getInteger().getBitvec();
      for (int i = 0; i < 32; i++) {
        localPref[i] = localPref[i].veccompose(pairing);
      }
    }

    if (_factory.getConfig().getKeepOspfMetric()) {
      BDD[] ospfMet = rec.getOspfMetric().getInteger().getBitvec();
      for (int i = 0; i < ospfMet.length; i++) {
        ospfMet[i] = ospfMet[i].veccompose(pairing);
      }
    }

    if (_factory.getConfig().getKeepRRClient()) {
      rec.setFromRRClient(rec.getFromRRClient().veccompose(pairing));
    }

    if (_factory.getConfig().getKeepCommunities()) {
      rec.getCommunities().replaceAll((k, v) -> v.veccompose(pairing));
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
