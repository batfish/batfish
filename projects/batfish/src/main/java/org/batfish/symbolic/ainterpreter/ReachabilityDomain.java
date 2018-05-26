package org.batfish.symbolic.ainterpreter;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDPairing;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.bdd.BDDAcl;
import org.batfish.symbolic.bdd.BDDFiniteDomain;
import org.batfish.symbolic.bdd.BDDInteger;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.bdd.BDDRoute;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.bdd.BDDUtils;
import org.batfish.symbolic.bdd.SatAssignment;
import org.batfish.symbolic.utils.Tuple;

public class ReachabilityDomain implements IAbstractDomain<Tuple<BDD, BDD>> {

  private enum Transformation {
    UNDER_APPROXIMATION,
    OVER_APPROXIMATION
  }

  // Reference to the BDD network factory
  private BDDNetFactory _netFactory;

  // A substitution pairing that we will reuse
  private BDDPairing _pairing;

  private BDDRoute _variables;

  // A cache of BDDs representing a prefix length exact match
  private Map<Integer, BDD> _lengthCache;

  // A cache of sets of BDDs for a given prefix length to quantify away
  private Map<Integer, BDD> _dstBitsCache;

  // The prefix length bits that we will quantify away
  private BDD _lenBits;

  // The destination router bits that we will quantify away
  private BDD _dstRouterBits;

  // The set of community and protocol BDD routeVariables that we will quantify away
  private BDD _communityAndProtocolBits;

  // The source router bits that we will quantify away
  private BDD _tempRouterBits;

  // Substitution of dst router bits for src router bits used to compute transitive closure
  private BDDPairing _dstToTempRouterSubstitution;

  // Substitution of dst router bits for src router bits used to compute transitive closure
  private BDDPairing _srcToTempRouterSubstitution;

  ReachabilityDomain(BDDNetFactory netFactory) {
    _netFactory = netFactory;
    _variables = _netFactory.routeVariables();
    _pairing = _netFactory.makePair();
    _lengthCache = new HashMap<>();
    _dstBitsCache = new HashMap<>();
    _lenBits = _netFactory.one();

    BDD[] srcRouterBits = _variables.getSrcRouter().getInteger().getBitvec();
    BDD[] tempRouterBits = _variables.getRouterTemp().getInteger().getBitvec();
    BDD[] dstRouterBits = _variables.getDstRouter().getInteger().getBitvec();

    _dstToTempRouterSubstitution = _netFactory.makePair();
    _srcToTempRouterSubstitution = _netFactory.makePair();

    _tempRouterBits = _netFactory.one();
    for (int i = 0; i < srcRouterBits.length; i++) {
      _tempRouterBits = _tempRouterBits.and(tempRouterBits[i]);
      _dstToTempRouterSubstitution.set(dstRouterBits[i].var(), tempRouterBits[i].var());
      _srcToTempRouterSubstitution.set(srcRouterBits[i].var(), tempRouterBits[i].var());
    }

    _dstRouterBits = _netFactory.one();
    for (BDD x : dstRouterBits) {
      _dstRouterBits = _dstRouterBits.and(x);
    }

    _lenBits = _netFactory.one();
    BDD[] pfxLen = _variables.getPrefixLength().getBitvec();
    for (BDD x : pfxLen) {
      _lenBits = _lenBits.and(x);
    }

    _communityAndProtocolBits = _netFactory.one();
    BDD[] protoHistory = _variables.getProtocolHistory().getInteger().getBitvec();
    for (BDD x : protoHistory) {
      _communityAndProtocolBits = _communityAndProtocolBits.and(x);
    }
    for (Entry<CommunityVar, BDD> e : _variables.getCommunities().entrySet()) {
      BDD c = e.getValue();
      _communityAndProtocolBits = _communityAndProtocolBits.and(c);
    }
  }

  @Override
  public Tuple<BDD, BDD> bot() {
    return new Tuple<>(_netFactory.zero(), _netFactory.zero());
  }

  private BDD valueAux(String router, Protocol proto, Set<Prefix> prefixes) {
    BDD acc = _netFactory.zero();
    if (prefixes != null) {
      for (Prefix prefix : prefixes) {
        BDD pfx = BDDUtils.prefixToBdd(_netFactory.getFactory(), _variables, prefix);
        acc.orWith(pfx);
      }
    }
    BDD prot = _variables.getProtocolHistory().value(proto);
    BDD dst = _variables.getDstRouter().value(router);
    acc = acc.andWith(dst);
    acc = acc.andWith(prot);
    return acc;
  }

  @Override
  public Tuple<BDD, BDD> value(String router, Protocol proto, Set<Prefix> prefixes) {
    BDD ret = valueAux(router, proto, prefixes);
    return new Tuple<>(ret, ret);
  }

  @Override
  public Tuple<BDD, BDD> transform(Tuple<BDD, BDD> input, EdgeTransformer t) {
    BDD under = transformAux(input.getFirst(), t, Transformation.UNDER_APPROXIMATION);
    BDD over = transformAux(input.getSecond(), t, Transformation.OVER_APPROXIMATION);
    return new Tuple<>(under, over);
  }

  private BDD transformAux(BDD input, EdgeTransformer t, Transformation type) {
    BDDTransferFunction f = t.getBddTransfer();
    // Filter routes that can not pass through the transformer
    BDD allow = f.getFilter();

    if (type == Transformation.OVER_APPROXIMATION) {
      input = input.and(allow);
    } else {
      BDD block = allow.not();
      BDD blockedInputs = input.and(block);
      BDD blockedPrefixes = blockedInputs.exist(_communityAndProtocolBits);
      BDD notBlockedPrefixes = blockedPrefixes.not();
      // Not sure why, but andWith does not work here (JavaBDD bug?)
      input = input.and(notBlockedPrefixes);
    }

    // Modify the result
    BDDRoute mods = f.getRoute();
    _pairing.reset();
    if (mods.getConfig().getKeepCommunities()) {
      for (Entry<CommunityVar, BDD> e : _variables.getCommunities().entrySet()) {
        CommunityVar cvar = e.getKey();
        BDD x = e.getValue();
        BDD temp = _variables.getCommunitiesTemp().get(cvar);
        BDD expr = mods.getCommunities().get(cvar);
        BDD equal = temp.biimp(expr);
        input = input.andWith(equal);
        _pairing.set(temp.var(), x.var());
      }
    }

    input = modifyProtocol(input, mods, t.getProtocol());
    input = input.exist(_communityAndProtocolBits);
    input = input.replaceWith(_pairing);
    return input;
  }

  /*
   * Substitutes the old protocol for the new one by updating the input to
   * have temporary variables, and modify the BDDPairing to have the
   * appropriate substitution. This has a side-effect, which is used
   * to be able to batch modifications and only apply them all once.
   */
  private BDD modifyProtocol(BDD input, BDDRoute mods, Protocol proto) {
    if (mods.getConfig().getKeepProtocol()) {
      BDDFiniteDomain<Protocol> var = _variables.getProtocolHistory();
      BDDFiniteDomain<Protocol> prot = new BDDFiniteDomain<>(var);
      prot.setValue(proto);
      BDD[] vec = _variables.getProtocolHistory().getInteger().getBitvec();
      for (int i = 0; i < vec.length; i++) {
        BDD x = vec[i];
        BDD temp = _variables.getProtocolHistoryTemp().getInteger().getBitvec()[i];
        BDD expr = prot.getInteger().getBitvec()[i];
        BDD equal = temp.biimp(expr);
        input.andWith(equal);
        _pairing.set(temp.var(), x.var());
      }
    }
    return input;
  }

  @Override
  public Tuple<BDD, BDD> merge(Tuple<BDD, BDD> x, Tuple<BDD, BDD> y) {
    BDD newFirst = x.getFirst().or(y.getFirst());
    BDD newSecond = x.getSecond().or(y.getSecond());
    return new Tuple<>(newFirst, newSecond);
  }

  @Override
  public Tuple<BDD, BDD> selectBest(Tuple<BDD, BDD> x) {
    return x;
  }

  // TODO: same thing, take away the ones that over approximated
  @Override
  public List<RibEntry> toRoutes(AbstractRib<Tuple<BDD, BDD>> value) {
    List<RibEntry> ribEntries = new ArrayList<>();
    List<SatAssignment> entries = BDDUtils.allSat(_netFactory, value.getMainRib().getFirst());
    for (SatAssignment entry : entries) {
      Prefix p = new Prefix(entry.getDstIp(), entry.getPrefixLen());
      RibEntry r =
          new RibEntry(entry.getSrcRouter(), p, entry.getRoutingProtocol(), entry.getDstRouter());
      ribEntries.add(r);
    }
    return ribEntries;
  }

  // TODO: ensure unique reachability (i.e., no other destinations)
  @Override
  public BDD toFib(Map<String, AbstractRib<Tuple<BDD, BDD>>> ribs, FiniteIndexMap<BDDAcl> acls) {
    Map<String, AbstractFib<Tuple<BDD, BDD>>> ret = new HashMap<>();
    for (Entry<String, AbstractRib<Tuple<BDD, BDD>>> e : ribs.entrySet()) {
      String router = e.getKey();
      AbstractRib<Tuple<BDD, BDD>> rib = e.getValue();
      AbstractFib<Tuple<BDD, BDD>> fib = new AbstractFib<>(rib, toFibAux(rib, acls));
      ret.put(router, fib);
    }
    return transitiveClosure(ret);
  }

  /*
   * Convert a pair of an underapproximation of the RIB and an overapproximation
   * of the RIB represented as a BDDs to an actual headerspace of packets that can
   * reach a destination. Normally, the final destination router is preserved
   * in this operation.
   *
   * As an example, if we have 1.2.3.0/24 learned from router X in the RIB, and
   * there can not be any more specific routes (in the overapproximation of routes)
   * that can take traffic away from this prefix, then the resulting packets matched
   * are going to be 1.2.3.*
   *
   * In general, we want to remove destinations in the overapproximated set that
   * are not in the underapproximated set. For instance, if we have prefix
   * 1.2.3.4/32 --> A in the underapproximation, and prefix
   * 1.2.3.4/32 --> {A,B} in the overapproximation, then we want to remove
   * this prefix since we can't be guaranteed that it will forward to A.
   *
   * As another example, if we have the prefix
   * 1.2.3.0/24 --> A in the underapproximated set, and another prefix
   * 1.2.3.4/32 --> B in the overapproximated set, then we only want the
   * destinations matched by the /24 but not the /32 to go to A
   *
   */
  private BDD toFibAux(AbstractRib<Tuple<BDD, BDD>> value, FiniteIndexMap<BDDAcl> aclMap) {
    Tuple<BDD, BDD> tup = value.getMainRib();

    // Get the prefixes only by removing communities, protocol tag etc.
    BDD underPfxOnly = tup.getFirst().exist(_communityAndProtocolBits);
    BDD overPfxOnly = tup.getSecond().exist(_communityAndProtocolBits);

    // Early return if there is nothing in the rib
    if (underPfxOnly.isZero()) {
      return underPfxOnly;
    }

    // Prefixes that might take traffic away
    BDD allOverPrefixes = overPfxOnly.andWith(underPfxOnly.not()).exist(_dstRouterBits);

    // Accumulate the might hijack destination addresses by prefix length
    BDD mightHijack = _netFactory.zero();

    // Accumulate the reachable packets (prefix length is removed at the end)
    BDD reachablePackets = _netFactory.zero();

    // Walk starting from most specific prefix length
    for (int i = 32; i >= 0; i--) {
      BDD len = makeLength(i);

      // Add new prefixes that might hijack traffic
      BDD withLenOver = allOverPrefixes.and(len).exist(_lenBits);
      mightHijack = mightHijack.orWith(withLenOver);

      // Get the must reach prefixes for this length
      BDD withLen = underPfxOnly.and(len);
      if (withLen.isZero()) {
        continue;
      }

      // quantify out bits i+1 to 32
      BDD removeBits = removeBits(i);
      if (!removeBits.isOne()) {
        withLen = withLen.exist(removeBits);
      }

      // Remove the may hijack addresses
      withLen = withLen.andWith(mightHijack.not());

      // accumulate resulting headers
      reachablePackets = reachablePackets.orWith(withLen);
    }

    reachablePackets = reachablePackets.exist(_lenBits);
    reachablePackets = reachablePackets.andWith(notBlockedByAcl(aclMap, value.getAclIds()));
    return reachablePackets;
  }

  /*
   * Make a BDD representing an exact prefix length match.
   * For efficiency, these results are cached for each prefix length.
   */
  private BDD makeLength(int i) {
    BDD len = _lengthCache.get(i);
    if (len == null) {
      BDDInteger pfxLen = _variables.getPrefixLength();
      BDDInteger newVal = new BDDInteger(pfxLen);
      newVal.setValue(i);
      len = _netFactory.one();
      for (int j = 0; j < pfxLen.getBitvec().length; j++) {
        BDD var = pfxLen.getBitvec()[j];
        BDD val = newVal.getBitvec()[j];
        if (val.isOne()) {
          len = len.and(var);
        } else {
          len = len.andWith(var.not());
        }
      }
      _lengthCache.put(i, len);
    }
    return len;
  }

  /*
   * Compute the set of BDD routeVariables to quantify away for a given prefix length.
   * For efficiency, these values are cached.
   */
  private BDD removeBits(int len) {
    BDD removeBits = _dstBitsCache.get(len);
    if (removeBits == null) {
      removeBits = _netFactory.one();
      for (int i = len; i < 32; i++) {
        BDD x = _variables.getPrefix().getBitvec()[i];
        removeBits = removeBits.and(x);
      }
      _dstBitsCache.put(len, removeBits);
    }
    return removeBits;
  }

  /*
   * BDD representing those packets not blocked by an ACL
   */
  private BDD notBlockedByAcl(FiniteIndexMap<BDDAcl> aclMap, BitSet aclIds) {
    BDD blocked = _netFactory.zero();
    for (int i = aclIds.nextSetBit(0); i != -1; i = aclIds.nextSetBit(i + 1)) {
      BDDAcl acl = aclMap.value(i);
      blocked = blocked.or(acl.getBdd());
    }
    return blocked.not();
  }

  /*
   * Computes the transitive closure of a fib
   */
  private BDD transitiveClosure(BDD fibs) {
    BDD fibsTemp = fibs.replace(_srcToTempRouterSubstitution);
    Set<BDD> seenFibs = new HashSet<>();
    BDD newFib = fibs;
    do {
      seenFibs.add(newFib);
      newFib = newFib.replace(_dstToTempRouterSubstitution);
      newFib = newFib.and(fibsTemp);
      newFib = newFib.exist(_tempRouterBits);
    } while (!seenFibs.contains(newFib));
    return newFib;
  }

  private <T> BDD transitiveClosure(Map<String, AbstractFib<T>> fibs) {
    BDD allFibs = _netFactory.zero();
    for (Entry<String, AbstractFib<T>> e : fibs.entrySet()) {
      String router = e.getKey();
      AbstractFib<T> fib = e.getValue();
      BDD routerBdd = _variables.getSrcRouter().value(router);
      BDD annotatedFib = fib.getHeaderspace().and(routerBdd);
      allFibs = allFibs.orWith(annotatedFib);
    }
    return transitiveClosure(allFibs);
  }

  // TODO: cache the flows
  @Override
  public boolean reachable(
      Map<String, AbstractRib<Tuple<BDD, BDD>>> ribs,
      FiniteIndexMap<BDDAcl> acls,
      String src,
      String dst,
      Flow flow) {
    BDD f = BDDUtils.flowToBdd(_netFactory, flow);
    String current = src;
    while (true) {
      AbstractRib<Tuple<BDD, BDD>> rib = ribs.get(current);
      BDD fib = toFibAux(rib, acls);
      BDD fibForFlow = fib.and(f);
      SatAssignment assignment = BDDUtils.satOne(_netFactory, fibForFlow);
      if (assignment == null) {
        return false;
      }
      current = assignment.getDstRouter();
      if (current.equals(dst)) {
        return true;
      }
    }
  }

  @Override
  public String debug(Tuple<BDD, BDD> value) {
    return BDDUtils.dot(_netFactory, value.getFirst());
  }
}
