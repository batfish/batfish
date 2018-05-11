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
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.symbolic.AstVisitor;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.CommunityVar.Type;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.IDeepCopy;
import org.batfish.symbolic.OspfType;
import org.batfish.symbolic.Protocol;

public class BDDRouteFactory {

  public static BDDFactory factory;

  private static List<Protocol> allProtos;

  private static List<OspfType> allMetricTypes;

  private static BDDPairing pairing;

  private int _hcode = 0;

  static {
    allMetricTypes = new ArrayList<>();
    allMetricTypes.add(OspfType.O);
    allMetricTypes.add(OspfType.OIA);
    allMetricTypes.add(OspfType.E1);
    allMetricTypes.add(OspfType.E2);

    allProtos = new ArrayList<>();
    allProtos.add(Protocol.CONNECTED);
    allProtos.add(Protocol.STATIC);
    allProtos.add(Protocol.OSPF);
    allProtos.add(Protocol.BGP);

    factory = JFactory.init(100000, 10000);
    // factory.disableReorder();
    factory.setCacheRatio(64);
    /*
    try {
      // Disables printing
      CallbackHandler handler = new CallbackHandler();
      Method m = handler.getClass().getDeclaredMethod("handle", (Class<?>[]) null);
      factory.registerGCCallback(handler, m);
      factory.registerResizeCallback(handler, m);
      factory.registerReorderCallback(handler, m);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    */
    pairing = factory.makePair();
  }

  private BDDRouteConfig _config;

  private Set<CommunityVar> _allCommunities;

  Set<String> _allRouters;

  private List<Integer> _allLocalPrefs;

  private BDDRoute _variables;

  private Set<Integer> findAllLocalPrefs(Graph g) {
    Set<Integer> prefs = new HashSet<>();
    AstVisitor v = new AstVisitor();
    v.visit(
        g.getConfigurations().values(),
        stmt -> {
          if (stmt instanceof SetLocalPreference) {
            SetLocalPreference slp = (SetLocalPreference) stmt;
            IntExpr ie = slp.getLocalPreference();
            if (ie instanceof LiteralInt) {
              LiteralInt l = (LiteralInt) ie;
              prefs.add(l.getValue());
            }
          }
        },
        expr -> {});
    return prefs;
  }

  public BDDRouteFactory(Graph g, BDDRouteConfig config) {
    _config = config;
    _allCommunities = g.getAllCommunities();
    _allRouters = g.getRouters();
    _allLocalPrefs = new ArrayList<>(findAllLocalPrefs(g));
    _variables = createRoute();
  }

  public BDDRoute createRoute() {
    return new BDDRoute();
  }

  public BDDRoute createRoute(BDDRoute other) {
    return new BDDRoute(other);
  }

  public BDDRoute variables() {
    return _variables;
  }

  public BDDRouteConfig getConfig() {
    return _config;
  }

  /**
   * A collection of attributes describing an advertisement, represented using BDDs
   *
   * @author Ryan Beckett
   */
  public class BDDRoute implements IDeepCopy<BDDRoute> {

    private int _numVars;

    private BDDInteger _adminDist;

    private Map<Integer, String> _bitNames;

    private SortedMap<CommunityVar, BDD> _communities;

    private BDDFiniteDomain<Integer> _localPref;

    private BDDInteger _med;

    private BDDInteger _metric;

    private BDDFiniteDomain<OspfType> _ospfMetric;

    private BDDFiniteDomain<String> _dstRouter;

    private final BDDInteger _prefix;

    private final BDDInteger _prefixLength;

    private final BDDFiniteDomain<Protocol> _protocolHistory;

    /*
     * Creates a collection of BDD variables representing the
     * various attributes of a control plane advertisement.
     */
    private BDDRoute() {
      int numVars = factory.varNum();
      int numNeeded =
          2
              * (32 * 4
                  + 6
                  + _allCommunities.size()
                  + _allLocalPrefs.size()
                  + _allRouters.size()
                  + 4);
      if (numVars < numNeeded) {
        factory.setVarNum(numNeeded); // allow for temporary variables
      }
      _numVars = factory.varNum() / 2;

      _bitNames = new HashMap<>();

      int idx = 0;

      if (_config.getKeepHistory()) {
        _protocolHistory = new BDDFiniteDomain<>(factory, allProtos, idx);
        addBitNames("proto", _protocolHistory.numBits(), idx, false);
        idx += _protocolHistory.numBits();
      } else {
        _protocolHistory = null;
      }
      if (_config.getKeepMetric()) {
        _metric = BDDInteger.makeFromIndex(factory, 32, idx, false);
        addBitNames("metric", 32, idx, false);
        idx += 32;
      }
      if (_config.getKeepMed()) {
        _med = BDDInteger.makeFromIndex(factory, 32, idx, false);
        addBitNames("med", 32, idx, false);
        idx += 32;
      }
      if (_config.getKeepAd()) {
        _adminDist = BDDInteger.makeFromIndex(factory, 32, idx, false);
        addBitNames("ad", 32, idx, false);
        idx += 32;
      }
      if (_config.getKeepLp()) {
        _localPref = new BDDFiniteDomain<>(factory, _allLocalPrefs, idx);
        // _localPref = BDDInteger.makeFromIndex(factory, 32, idx, false);
        addBitNames("lp", _localPref.numBits(), idx, false);
        idx += _localPref.numBits();
      }
      _prefixLength = BDDInteger.makeFromIndex(factory, 6, idx, true);
      addBitNames("pfxLen", 6, idx, true);
      idx += 6;
      _prefix = BDDInteger.makeFromIndex(factory, 32, idx, true);
      addBitNames("pfx", 32, idx, true);
      idx += 32;

      if (_config.getKeepCommunities()) {
        _communities = new TreeMap<>();
        for (CommunityVar comm : _allCommunities) {
          if (comm.getType() != Type.REGEX) {
            _communities.put(comm, factory.ithVar(idx));
            _bitNames.put(idx, comm.getValue());
            idx++;
          }
        }
      }
      if (_config.getKeepDstRouter()) {
        _dstRouter = new BDDFiniteDomain<>(factory, new ArrayList<>(_allRouters), idx);
        addBitNames("router", _dstRouter.numBits(), idx, false);
        idx += _dstRouter.numBits();
      }
      // Initialize OSPF type
      if (_config.getKeepOspfMetric()) {
        _ospfMetric = new BDDFiniteDomain<>(factory, allMetricTypes, idx);
        addBitNames("ospfMetric", _ospfMetric.numBits(), idx, false);
        // idx += _ospfMetric.numBits();
      }
    }

    /*
     * Create a BDDRecord from another. Because BDDs are immutable,
     * there is no need for a deep copy.
     */
    private BDDRoute(BDDRoute other) {
      _numVars = other._numVars;
      _prefixLength = new BDDInteger(other._prefixLength);
      _prefix = new BDDInteger(other._prefix);
      if (_config.getKeepCommunities()) {
        _communities = new TreeMap<>(other._communities);
      }
      if (_config.getKeepMetric()) {
        _metric = new BDDInteger(other._metric);
      }
      if (_config.getKeepAd()) {
        _adminDist = new BDDInteger(other._adminDist);
      }
      if (_config.getKeepMed()) {
        _med = new BDDInteger(other._med);
      }
      if (_config.getKeepLp()) {
        _localPref = new BDDFiniteDomain<>(other._localPref);
      }
      if (_config.getKeepHistory()) {
        _protocolHistory = new BDDFiniteDomain<>(other._protocolHistory);
      } else {
        _protocolHistory = null;
      }
      if (_config.getKeepOspfMetric()) {
        _ospfMetric = new BDDFiniteDomain<>(other._ospfMetric);
      }
      if (_config.getKeepDstRouter()) {
        _dstRouter = new BDDFiniteDomain<>(other._dstRouter);
      }
      _bitNames = other._bitNames;
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

    public BDD getTemporary(BDD var) {
      return factory.ithVar(var.var() + _numVars);
    }

    /*
     * Convenience method for the copy constructor
     */
    @Override
    public BDDRoute deepCopy() {
      return new BDDRoute(this);
    }

    /*
     * Converts a BDD to the graphviz DOT format for debugging.
     */
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

    public BDDRouteConfig getConfig() {
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

    public void setDstRouter(BDDFiniteDomain<String> dstRouter) {
      this._dstRouter = dstRouter;
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

    /*
     * Take the point-wise disjunction of two BDDRecords
     */
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
      // NOTE: do not create a new pairing each time
      // JavaBDD will start to memory leak
      pairing.reset();
      for (int i = 0; i < len; i++) {
        int var = _prefix.getBitvec()[i].var(); // prefixIndex + i;
        BDD subst = Ip.getBitAtPosition(bits, i) ? factory.one() : factory.zero();
        vars[i] = var;
        vals[i] = subst;
      }
      pairing.set(vars, vals);

      BDDRoute rec = new BDDRoute(this);

      if (_config.getKeepMetric()) {
        BDD[] metric = rec.getMetric().getBitvec();
        for (int i = 0; i < 32; i++) {
          metric[i] = metric[i].veccompose(pairing);
        }
      }

      if (_config.getKeepAd()) {
        BDD[] adminDist = rec.getAdminDist().getBitvec();
        for (int i = 0; i < 32; i++) {
          adminDist[i] = adminDist[i].veccompose(pairing);
        }
      }

      if (_config.getKeepMed()) {
        BDD[] med = rec.getMed().getBitvec();
        for (int i = 0; i < 32; i++) {
          med[i] = med[i].veccompose(pairing);
        }
      }

      if (_config.getKeepLp()) {
        BDD[] localPref = rec.getLocalPref().getInteger().getBitvec();
        for (int i = 0; i < 32; i++) {
          localPref[i] = localPref[i].veccompose(pairing);
        }
      }

      if (_config.getKeepOspfMetric()) {
        BDD[] ospfMet = rec.getOspfMetric().getInteger().getBitvec();
        for (int i = 0; i < ospfMet.length; i++) {
          ospfMet[i] = ospfMet[i].veccompose(pairing);
        }
      }

      if (_config.getKeepCommunities()) {
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
}
