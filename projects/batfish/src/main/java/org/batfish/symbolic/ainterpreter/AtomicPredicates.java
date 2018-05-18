package org.batfish.symbolic.ainterpreter;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.collections.Table2;

public class AtomicPredicates {

  private Map<BDD, BitSet> _atoms;

  private Table2<BDDTransferFunction, Integer, Set<Integer>> _transformers;

  private List<BDD> _disjoint;

  public AtomicPredicates(
      Map<BDD, BitSet> atoms,
      Table2<BDDTransferFunction, Integer, Set<Integer>> transformers,
      List<BDD> disjoint) {
    this._atoms = atoms;
    this._transformers = transformers;
    this._disjoint = disjoint;
  }

  public Map<BDD, BitSet> getAtoms() {
    return _atoms;
  }

  public Table2<BDDTransferFunction, Integer, Set<Integer>> getTransformers() {
    return _transformers;
  }

  public List<BDD> getDisjoint() {
    return _disjoint;
  }
}
