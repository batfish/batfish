package org.batfish.minesweeper.bdd;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;

/**
 * This class is used to keep track of the state of the BDD-based symbolic control-plane analysis in
 * {@link TransferBDD}. It's effectively the symbolic version of {@link
 * org.batfish.datamodel.routing_policy.Result}.
 */
@ParametersAreNonnullByDefault
public class TransferResult {

  // the current symbolic route information
  @Nonnull private TransferReturn _returnValue;

  /**
   * The following three fields are used to ensure that the analysis accurately identifies all and
   * only feasible execution paths through a route policy.
   */

  /* predicate indicating when the analysis has hit a fall-through condition in the policy
  being analyzed, meaning that control flow should continue to the next policy */
  @Nonnull private BDD _fallthroughValue;

  /* predicate indicating when the anlaysis has hit an exit condition in the policy
  being analyzed, which represents the termination of the execution */
  @Nonnull private BDD _exitAssignedValue;

  /* predicate indicating when the analysis has hit a return condition in the policy
  being analyzed, which represents the termination of a nested call to a routing policy */
  @Nonnull private BDD _returnAssignedValue;

  // Construct a TransferResult from a BDDRoute, using the zero BDD for all BDDs
  public TransferResult(BDDRoute bddRoute) {
    this(new TransferReturn(bddRoute, bddRoute.getFactory().zero()), bddRoute.getFactory().zero());
  }

  public TransferResult(TransferReturn ret, BDD defaultBDD) {
    this(ret, defaultBDD, defaultBDD, defaultBDD);
  }

  public TransferResult(
      TransferReturn retVal,
      BDD exitAssignedValue,
      BDD fallThroughValue,
      BDD returnAssignedAValue) {
    _returnValue = retVal;
    _exitAssignedValue = exitAssignedValue;
    _fallthroughValue = fallThroughValue;
    _returnAssignedValue = returnAssignedAValue;
  }

  private TransferResult(TransferResult other) {
    _returnValue = other._returnValue;
    _fallthroughValue = other._fallthroughValue;
    _exitAssignedValue = other._exitAssignedValue;
    _returnAssignedValue = other._returnAssignedValue;
  }

  @Nonnull
  public TransferReturn getReturnValue() {
    return _returnValue;
  }

  @Nonnull
  public BDD getFallthroughValue() {
    return _fallthroughValue;
  }

  @Nonnull
  public BDD getExitAssignedValue() {
    return _exitAssignedValue;
  }

  @Nonnull
  public BDD getReturnAssignedValue() {
    return _returnAssignedValue;
  }

  @Nonnull
  public TransferResult setReturnValue(TransferReturn x) {
    TransferResult ret = new TransferResult(this);
    ret._returnValue = x;
    return ret;
  }

  @Nonnull
  public TransferResult setFallthroughValue(BDD x) {
    TransferResult ret = new TransferResult(this);
    ret._fallthroughValue = x;
    return ret;
  }

  @Nonnull
  public TransferResult setReturnAssignedValue(BDD x) {
    TransferResult ret = new TransferResult(this);
    ret._returnAssignedValue = x;
    return ret;
  }

  @Nonnull
  public TransferResult setExitAssignedValue(BDD x) {
    TransferResult ret = new TransferResult(this);
    ret._exitAssignedValue = x;
    return ret;
  }
}
