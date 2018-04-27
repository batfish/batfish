package org.batfish.symbolic.interpreter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.symbolic.bdd.TransferReturn;
import org.batfish.symbolic.collections.Table2;

public class AtomicPredicates {

  private Map<BDD, Set<Integer>> _atoms;

  private Table2<TransferReturn, Integer, Set<Integer>> _transformers;

  private List<BDD> _disjoint;

  public AtomicPredicates(
      Map<BDD, Set<Integer>> atoms,
      Table2<TransferReturn, Integer, Set<Integer>> transformers,
      List<BDD> disjoint) {
    this._atoms = atoms;
    this._transformers = transformers;
    this._disjoint = disjoint;
  }

  public Map<BDD, Set<Integer>> getAtoms() {
    return _atoms;
  }

  public Table2<TransferReturn, Integer, Set<Integer>> getTransformers() {
    return _transformers;
  }

  public List<BDD> getDisjoint() {
    return _disjoint;
  }
}
