package org.batfish.symbolic.interpreter;

import java.util.ArrayList;
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
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDRouteFactory;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.smt.EdgeType;

public class ReachabilityAbstractDomainAP implements IAbstractDomain<BitSet> {

  private static BDDFactory factory = BDDRouteFactory.factory;

  private AtomicPredicates _atomicPredicates;

  private Map<String, Set<Prefix>> _origins;

  ReachabilityAbstractDomainAP(ReachabilityAbstractDomainBDD domain, BDDNetwork network) {
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
    List<Transformer> transforms = new ArrayList<>();
    for (EdgeTransformer et : trans) {
      Function<BDD, BDD> f = (bdd) -> domain.transform(bdd, et);
      Transformer x = new Transformer(et.getBgpTransfer(), f);
      transforms.add(x);
    }

    _origins = new HashMap<>();
    _atomicPredicates = AtomOps.computeAtomicPredicates(filters, transforms);

    /* System.out.println("Disjoint: ");
    for (BDD bdd : ap.getDisjoint()) {
      System.out.println(domain.getVariables().dot(bdd));
    }

    System.out.println("Mapping:");
    ap.getAtoms()
        .forEach(
            (bdd, is) -> {
              System.out.println(domain.getVariables().dot(bdd));
              System.out.println("goes to: " + is);
            }); */
  }

  @Override
  public BitSet init(String router, Set<Prefix> prefixes) {
    _origins.put(router, prefixes);
    int n = _atomicPredicates.getDisjoint().size();
    BitSet b = new BitSet(n);
    b.set(0, n - 1, true);
    return b;
  }

  @Override
  public BitSet transform(BitSet input, EdgeTransformer t) {
    BDDTransferFunction f = t.getBgpTransfer();
    BDD filter = f.getFilter();
    BitSet atoms = _atomicPredicates.getAtoms().get(filter);
    int size = _atomicPredicates.getDisjoint().size();

    // TODO: change _atomicPredicates to directly return BitSets
    BitSet x = (BitSet) input.clone();
    x.and(atoms);

    BitSet output = new BitSet(size);
    Map<Integer, Set<Integer>> map = _atomicPredicates.getTransformers().get(f);
    for (int i = x.nextSetBit(0); i != -1; i = x.nextSetBit(i + 1)) {
      Set<Integer> modified = map.get(i);
      for (Integer j : modified) {
        output.set(j);
      }
    }

    return output;
  }

  @Override
  public BitSet join(BitSet x, BitSet y) {
    BitSet b = (BitSet) x.clone();
    b.or(y);
    return b;
  }

  // TODO: need to track where it came from
  @Override
  public BDD finalize(BitSet value) {
    BDD acc = factory.zero();
    for (int i = value.nextSetBit(0); i != -1; i = value.nextSetBit(i + 1)) {
      BDD b = _atomicPredicates.getDisjoint().get(i);
      acc = acc.or(b);
    }
    return acc;
  }
}
