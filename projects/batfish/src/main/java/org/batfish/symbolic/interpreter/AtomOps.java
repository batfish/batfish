package org.batfish.symbolic.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.symbolic.bdd.TransferReturn;
import org.batfish.symbolic.collections.Table2;

public class AtomOps {

  /*
   * Given a collection of predicates, returns the set of atomic
   * predicates that are mutually disjoint, and are minimal.
   */
  @Nonnull
  private static Set<BDD> computeAtomicPredicatesAux(List<BDD> atoms) {
    Set<BDD> allPredicates = new HashSet<>();
    if (!atoms.isEmpty()) {
      BDD initial = atoms.get(0);
      BDD initialNot = initial.not();
      allPredicates.add(initial);
      allPredicates.add(initialNot);
      for (int i = 1; i < atoms.size(); i++) {
        BDD current = atoms.get(i);
        Set<BDD> newBdds = new HashSet<>();
        for (BDD pred : allPredicates) {
          BDD x = pred.and(current);
          if (!x.isZero()) {
            newBdds.add(x);
          }
        }
        if (!newBdds.isEmpty()) {
          for (BDD pred : allPredicates) {
            BDD x = pred.and(current.not());
            if (!x.isZero()) {
              newBdds.add(x);
            }
          }
          allPredicates = newBdds;
        }
      }
    }
    return allPredicates;
  }

  /*
   * Given a collection of predicates and transformers over those
   * predicates, return a set of atomic predicates that are disjoint
   * and minimal, and treated the same by every transformer.
   */
  private static Set<BDD> computeAtomicPredicatesAux(
      List<BDD> atoms, List<Transformer> transformers) {

    Set<BDD> allPredicates = new HashSet<>(atoms);
    Set<BDD> disjoint = computeAtomicPredicatesAux(atoms);

    while (true) {
      for (Transformer transformer : transformers) {
        for (BDD b : disjoint) {
          BDD tb = transformer.getFunction().apply(b);
          allPredicates.add(tb);
        }
      }
      Set<BDD> newDisjoint = computeAtomicPredicatesAux(new ArrayList<>(allPredicates));
      if (newDisjoint.equals(disjoint)) {
        return newDisjoint;
      }
      disjoint = newDisjoint;
    }
  }

  private static Map<BDD, Set<Integer>> computeMapping(List<BDD> atoms, List<BDD> disjoint) {
    Map<BDD, Set<Integer>> map = new HashMap<>();
    for (BDD x : atoms) {
      for (int i = 0; i < disjoint.size(); i++) {
        BDD y = disjoint.get(i);
        BDD both = x.and(y);
        if (!both.isZero()) {
          Set<Integer> is = map.computeIfAbsent(x, k -> new HashSet<>());
          is.add(i);
        }
      }
    }
    return map;
  }

  public static AtomicPredicates computeAtomicPredicates(List<BDD> atoms) {
    List<BDD> disjoint = new ArrayList<>(computeAtomicPredicatesAux(atoms));
    Map<BDD, Set<Integer>> map = computeMapping(atoms, disjoint);
    return new AtomicPredicates(map, new Table2<>(), disjoint);
  }

  public static AtomicPredicates computeAtomicPredicates(
      List<BDD> atoms, List<Transformer> transformers) {
    List<BDD> disjoint = new ArrayList<>(computeAtomicPredicatesAux(atoms, transformers));

    Map<BDD, Set<Integer>> map = computeMapping(atoms, disjoint);

    Table2<TransferReturn, BDD, Integer> intMap = new Table2<>();
    Set<BDD> allTransformed = new HashSet<>();
    for (Transformer t : transformers) {
      for (int j = 0; j < disjoint.size(); j++) {
        BDD d = disjoint.get(j);
        BDD result = t.getFunction().apply(d);
        intMap.put(t.getTransfer(), result, j);
        allTransformed.add(result);
      }
    }

    Map<BDD, Set<Integer>> tmap = computeMapping(new ArrayList<>(allTransformed), disjoint);
    Table2<TransferReturn, Integer, Set<Integer>> transformMap = new Table2<>();
    intMap.forEach(
        (tr, bdd, idx) -> {
          transformMap.put(tr, idx, tmap.get(bdd));
        });

    return new AtomicPredicates(map, transformMap, disjoint);
  }
}
