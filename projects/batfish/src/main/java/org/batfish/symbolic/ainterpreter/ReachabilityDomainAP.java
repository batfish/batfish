package org.batfish.symbolic.ainterpreter;

/* import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.smt.EdgeType; */

public class ReachabilityDomainAP {

  /*

  private static BDDFactory factory = BDDNetFactory.factory;

  private ReachabilityDomain _domain;

  private AtomicPredicates _atomicPredicates;

  private FiniteIndexMap<String> _routerIndexMap;

  private Map<String, BDD> _origins;

  ReachabilityDomainAP(ReachabilityDomain domain, Set<String> routers, BDDNetwork network) {
    Set<BDD> allFilters = new HashSet<>();
    Map<EdgeTransformer, BDDTransferFunction> allTransforms = new HashMap<>();

    for (Entry<GraphEdge, BDDTransferFunction> e : network.getExportBgpPolicies().entrySet()) {
      GraphEdge ge = e.getKey();
      BDDTransferFunction f = e.getValue();
      EdgeTransformer et = new EdgeTransformer(ge, EdgeType.EXPORT, f);
      allFilters.add(f.getFilter());
      allTransforms.put(et, f);
    }

    for (Entry<GraphEdge, BDDTransferFunction> e : network.getImportBgpPolicies().entrySet()) {
      GraphEdge ge = e.getKey();
      BDDTransferFunction f = e.getValue();
      EdgeTransformer et = new EdgeTransformer(ge, EdgeType.IMPORT, f);
      allFilters.add(f.getFilter());
      allTransforms.put(et, f);
    }

    List<BDD> filters = new ArrayList<>(allFilters);
    List<EdgeTransformer> trans = new ArrayList<>(allTransforms.keySet());
    List<AtomTransformer> transforms = new ArrayList<>();
    for (EdgeTransformer et : trans) {
      Function<BDD, BDD> f = (bdd) -> domain.transform(bdd, et);
      AtomTransformer x = new AtomTransformer(et.getBgpTransfer(), f);
      transforms.add(x);
    }

    _domain = domain;
    _origins = new HashMap<>();
    _atomicPredicates = AtomOps.computeAtomicPredicates(filters, transforms);
    _routerIndexMap = new FiniteIndexMap<>(routers);

    System.out.println("Disjoint: ");
    for (BDD bdd : _atomicPredicates.getDisjoint()) {
      System.out.println(domain.getVariables().dot(bdd));
    }

    System.out.println("Mapping:");
    _atomicPredicates.getAtoms()
        .forEach(
            (bdd, is) -> {
              System.out.println(domain.getVariables().dot(bdd));
              System.out.println("goes to: " + is);
            });
  }

  @Override
  public LocatedAP init() {
    return new LocatedAP(new BitSet(), new BitSet[_atomicPredicates.getDisjoint().size()]);
  }

  @Override
  public LocatedAP init(String router, Protocol proto, Set<Prefix> prefixes) {
    _origins.put(router, _domain.init(router, proto, prefixes));
    int n = _atomicPredicates.getDisjoint().size();
    int routerIndex = _routerIndexMap.index(router);
    BitSet b = new BitSet(n);
    b.set(0, n, true);
    BitSet[] locs = new BitSet[n];
    for (int i = 0; i < locs.length; i++) {
      BitSet l = new BitSet(_routerIndexMap.size());
      l.set(routerIndex);
      locs[i] = l;
    }
    return new LocatedAP(b, locs);
  }

  @Override
  public LocatedAP transform(LocatedAP input, EdgeTransformer t) {
    BDDTransferFunction f = t.getBgpTransfer();
    BDD filter = f.getFilter();
    BitSet atoms = _atomicPredicates.getAtoms().get(filter);

    BitSet x = (BitSet) input.getPredicates().clone();
    x.and(atoms);

    BitSet output = new BitSet(_atomicPredicates.getDisjoint().size());
    Map<Integer, Set<Integer>> map = _atomicPredicates.getTransformers().get(f);
    for (int i = x.nextSetBit(0); i != -1; i = x.nextSetBit(i + 1)) {
      Set<Integer> modified = map.get(i);
      for (Integer j : modified) {
        output.set(j);
      }
    }

    return new LocatedAP(output, input.getDestinationLocations());
  }

  @Override
  public LocatedAP merge(LocatedAP x, LocatedAP y) {
    return x.or(y);
  }

  // TODO: need to track where it came from
  @Override
  public BDD finalize(LocatedAP value) {
    BDD acc = factory.zero();
    BitSet atoms = value.getPredicates();
    for (int i = atoms.nextSetBit(0); i != -1; i = atoms.nextSetBit(i + 1)) {
      BDD b = _atomicPredicates.getDisjoint().get(i);
      // collect original prefixes
      BDD origin = factory.zero();
      BitSet locs = value.getDestinationLocations()[i];
      for (int j = locs.nextSetBit(0); j != -1; j = locs.nextSetBit(j + 1)) {
        String router = _routerIndexMap.value(j);
        BDD bdd = _origins.get(router);
        origin = origin.or(bdd);
      }
      acc = acc.or(b.and(origin));
    }
    return acc;
  }

  */
}
