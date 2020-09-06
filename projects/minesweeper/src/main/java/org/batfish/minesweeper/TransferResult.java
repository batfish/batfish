package org.batfish.minesweeper;

import com.microsoft.z3.Expr;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.minesweeper.collections.PList;
import org.batfish.minesweeper.utils.MsPair;

/**
 * This class is used to keep track of the state of a symbolic control-plane analysis. It's
 * effectively the symbolic version of {@link org.batfish.datamodel.routing_policy.Result}.
 *
 * @param <U> the type of the data that the analysis produces
 * @param <T> the type of the predicate used to track the internal state of the analysis
 */
@ParametersAreNonnullByDefault
public class TransferResult<U, T> {

  /**
   * this field and the associated functions are currently not used at all by the symbolic route
   * analysis, {@link org.batfish.minesweeper.bdd.TransferBDD}. they are used only by the
   * TransferSSA analysis in minesweeper, which is not being maintained currently.
   */
  private PList<MsPair<String, Expr>> _changedVariables; // should be a map

  // the current state of the analysis
  private U _returnValue;

  /**
   * The following three fields are used to ensure that the analysis accurately identifies all and
   * only feasible execution paths through a route policy.
   */

  // predicate indicating when the analysis has hit a fall-through condition in the policy
  // being analyzed, meaning that control flow should continue to the next policy
  private T _fallthroughValue;

  // predicate indicating when the anlaysis has hit an exit condition in the policy
  // being analyzed, which represents the termination of the execution
  private T _exitAssignedValue;

  // predicate indicating when the analysis has hit a return condition in the policy
  // being analyzed, which represents the termination of a nested call to a routing policy
  private T _returnAssignedValue;

  public TransferResult() {
    this(null, null, null, null);
  }

  public TransferResult(
      @Nullable U retVal,
      @Nullable T exitAssignedValue,
      @Nullable T fallThroughValue,
      @Nullable T returnAssignedAValue) {
    _changedVariables = PList.empty();
    _returnValue = retVal;
    _exitAssignedValue = exitAssignedValue;
    _fallthroughValue = fallThroughValue;
    _returnAssignedValue = returnAssignedAValue;
  }

  private TransferResult(TransferResult<U, T> other) {
    _changedVariables = other._changedVariables;
    _returnValue = other._returnValue;
    _fallthroughValue = other._fallthroughValue;
    _exitAssignedValue = other._exitAssignedValue;
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

  public T getExitAssignedValue() {
    return _exitAssignedValue;
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

  public TransferResult<U, T> setExitAssignedValue(T x) {
    TransferResult<U, T> ret = new TransferResult<>(this);
    ret._exitAssignedValue = x;
    return ret;
  }
}
