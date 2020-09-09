package org.batfish.minesweeper;

import com.microsoft.z3.Expr;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.minesweeper.collections.PList;
import org.batfish.minesweeper.utils.MsPair;

/**
 * This class is used to keep track of the state of a symbolic control-plane analysis. It is not
 * currently being maintained. The BDD-based route analysis in {@link
 * org.batfish.minesweeper.bdd.TransferBDD} uses its own version of this class in the bdd package,
 * {@link org.batfish.minesweeper.bdd.TransferResult}.
 *
 * @param <U> the type of the data that the analysis produces
 * @param <T> the type of the predicate used to track the internal state of the analysis
 */
public class TransferResult<U, T> {

  private PList<MsPair<String, Expr>> _changedVariables; // should be a map

  // holds the current data that the analysis has produced so far
  private U _returnValue;

  // a predicate indicating whether the analysis has hit a fall-through condition in the
  // configuration structure being analyzed, which is used to ensure that the analysis accurately
  // identifies all and only feasible execution paths through the structure
  private T _fallthroughValue;

  // a predicate indicating whether the analysis has hit a return or exit condition in the
  // configuration structure being analyzed, which is used to ensure that the analysis accurately
  // identifies all and only feasible execution paths through the structure
  private T _returnAssignedValue;

  public TransferResult() {
    _changedVariables = PList.empty();
    _returnValue = null;
    _fallthroughValue = null;
    _returnAssignedValue = null;
  }

  private TransferResult(TransferResult<U, T> other) {
    _changedVariables = other._changedVariables;
    _returnValue = other._returnValue;
    _fallthroughValue = other._fallthroughValue;
    _returnAssignedValue = other._returnAssignedValue;
  }

  @Nullable
  private Expr find(PList<MsPair<String, Expr>> vals, String s) {
    for (MsPair<String, Expr> pair : vals) {
      if (pair.getFirst().equals(s)) {
        return pair.getSecond();
      }
    }
    return null;
  }

  // TODO: this really needs to use persistent set data types
  public PList<MsPair<String, MsPair<Expr, Expr>>> mergeChangedVariables(
      TransferResult<U, T> other) {
    Set<String> seen = new HashSet<>();
    PList<MsPair<String, MsPair<Expr, Expr>>> vars = PList.empty();

    for (MsPair<String, Expr> cv1 : _changedVariables) {
      String s = cv1.getFirst();
      Expr x = cv1.getSecond();
      if (!seen.contains(s)) {
        seen.add(s);
        Expr e = find(other._changedVariables, s);
        MsPair<Expr, Expr> pair = new MsPair<>(x, e);
        vars = vars.plus(new MsPair<>(s, pair));
      }
    }

    for (MsPair<String, Expr> cv1 : other._changedVariables) {
      String s = cv1.getFirst();
      Expr x = cv1.getSecond();
      if (!seen.contains(s)) {
        seen.add(s);
        Expr e = find(_changedVariables, s);
        MsPair<Expr, Expr> pair = new MsPair<>(e, x); // preserve order
        vars = vars.plus(new MsPair<>(s, pair));
      }
    }

    return vars;
  }

  public PList<MsPair<String, Expr>> getChangedVariables() {
    return _changedVariables;
  }

  public U getReturnValue() {
    return _returnValue;
  }

  public T getFallthroughValue() {
    return _fallthroughValue;
  }

  public T getReturnAssignedValue() {
    return _returnAssignedValue;
  }

  public TransferResult<U, T> addChangedVariable(String s, Expr x) {
    TransferResult<U, T> ret = new TransferResult<>(this);
    ret._changedVariables = ret._changedVariables.plus(new MsPair<>(s, x));
    return ret;
  }

  public TransferResult<U, T> addChangedVariables(TransferResult<U, T> other) {
    TransferResult<U, T> ret = new TransferResult<>(this);
    ret._changedVariables = ret._changedVariables.plusAll(other._changedVariables);
    return ret;
  }

  public boolean isChanged(String s) {
    for (MsPair<String, Expr> pair : _changedVariables) {
      if (pair.getFirst().equals(s)) {
        return true;
      }
    }
    return false;
  }

  public TransferResult<U, T> clearChanged() {
    TransferResult<U, T> ret = new TransferResult<>(this);
    ret._changedVariables = PList.empty();
    return ret;
  }

  public TransferResult<U, T> setReturnValue(U x) {
    TransferResult<U, T> ret = new TransferResult<>(this);
    ret._returnValue = x;
    return ret;
  }

  public TransferResult<U, T> setFallthroughValue(T x) {
    TransferResult<U, T> ret = new TransferResult<>(this);
    ret._fallthroughValue = x;
    return ret;
  }

  public TransferResult<U, T> setReturnAssignedValue(T x) {
    TransferResult<U, T> ret = new TransferResult<>(this);
    ret._returnAssignedValue = x;
    return ret;
  }
}
