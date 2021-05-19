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

  /**
   * the symbolic route information that is ultimately returned as the result of the analysis: a
   * pair of a BDDRoute, which represents a route policy's output announcement as a function of the
   * input announcement; and a BDD, which represents the conditions under which a route announcement
   * is accepted by the route policy.
   */
  @Nonnull private final TransferReturn _returnValue;

  /**
   * The set of route announcements that should be suppressed (i.e., not announced). Route
   * suppression happens due to aggregation, and this is represented by the {@link
   * org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement} Suppress, but routes
   * can also be explicitly unsuppressed with Unsuppress.
   */
  @Nonnull private final BDD _suppressedValue;

  /**
   * The following three fields are used to ensure that the analysis accurately identifies all and
   * only feasible execution paths through a route policy.
   */

  /* predicate indicating when the analysis has hit a fall-through condition in the policy
  being analyzed, meaning that control flow should continue to the next policy */
  @Nonnull private final BDD _fallthroughValue;

  /* predicate indicating when the analysis has hit an exit condition in the policy
  being analyzed, which represents the termination of the execution */
  @Nonnull private final BDD _exitAssignedValue;

  /* predicate indicating when the analysis has hit a return condition in the policy
  being analyzed, which represents the termination of a nested call to a routing policy */
  @Nonnull private final BDD _returnAssignedValue;

  /**
   * Some examples of how to use the information in this object:
   *
   * <p>_returnValue.getSecond().and(_exitAssignedValue) represents all scenarios in which the
   * policy exits and permits the route
   *
   * <p>_returnValue.getSecond().not().and(_exitAssignedValue) represents all scenarios in which the
   * * policy exits and denies the route
   *
   * <p>replacing _exitAssignedValue with _returnAssignedValue in the two examples above
   * respectively represents all scenarios in which the policy returns true/false
   */

  // Construct a TransferResult from a BDDRoute, using the zero BDD for all BDDs
  public TransferResult(BDDRoute bddRoute) {
    this(new TransferReturn(bddRoute, bddRoute.getFactory().zero()), bddRoute.getFactory().zero());
  }

  public TransferResult(TransferReturn ret, BDD defaultBDD) {
    this(ret, defaultBDD, defaultBDD, defaultBDD, defaultBDD);
  }

  public TransferResult(
      TransferReturn retVal,
      BDD suppressedValue,
      BDD exitAssignedValue,
      BDD fallThroughValue,
      BDD returnAssignedValue) {
    assert !exitAssignedValue.andSat(returnAssignedValue)
        : "the predicates for exiting and returning should be disjoint";
    assert !fallThroughValue.diffSat(returnAssignedValue)
        : "the predicate for signaling a fall-through should imply the predicate for returning";
    _suppressedValue = suppressedValue;
    _returnValue = retVal;
    _exitAssignedValue = exitAssignedValue;
    _fallthroughValue = fallThroughValue;
    _returnAssignedValue = returnAssignedValue;
  }

  @Nonnull
  public TransferReturn getReturnValue() {
    return _returnValue;
  }

  @Nonnull
  public BDD getSuppressedValue() {
    return _suppressedValue;
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
  public TransferResult setReturnValue(TransferReturn newReturn) {
    return new TransferResult(
        newReturn, _suppressedValue, _exitAssignedValue, _fallthroughValue, _returnAssignedValue);
  }

  @Nonnull
  public TransferResult setReturnValueBDD(BDD newBDD) {
    return setReturnValue(new TransferReturn(_returnValue.getFirst(), newBDD));
  }

  @Nonnull
  public TransferResult setReturnValueBDDRoute(BDDRoute newBDDRoute) {
    return setReturnValue(new TransferReturn(newBDDRoute, _returnValue.getSecond()));
  }

  @Nonnull
  public TransferResult setSuppressedValue(BDD suppressedValue) {
    return new TransferResult(
        _returnValue, suppressedValue, _exitAssignedValue, _fallthroughValue, _returnAssignedValue);
  }

  @Nonnull
  public TransferResult setExitAssignedValue(BDD exitAssignedValue) {
    return new TransferResult(
        _returnValue, _suppressedValue, exitAssignedValue, _fallthroughValue, _returnAssignedValue);
  }

  @Nonnull
  public TransferResult setFallthroughValue(BDD fallthroughValue) {
    return new TransferResult(
        _returnValue, _suppressedValue, _exitAssignedValue, fallthroughValue, _returnAssignedValue);
  }

  @Nonnull
  public TransferResult setReturnAssignedValue(BDD returnAssignedValue) {
    return new TransferResult(
        _returnValue, _suppressedValue, _exitAssignedValue, _fallthroughValue, returnAssignedValue);
  }
}
