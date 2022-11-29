package org.batfish.minesweeper.bdd;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;

/**
 * This class is used to keep track of the state of the BDD-based symbolic control-plane analysis in
 * {@link TransferBDD}. It's effectively the symbolic version of {@link
 * org.batfish.datamodel.routing_policy.Result} but represents the result along one particular path
 * through the route policy.
 */
@ParametersAreNonnullByDefault
public class TransferResult {

  /**
   * the symbolic route information that is ultimately returned as the result of the analysis: a
   * triple of a BDDRoute, which represents this path's output announcement as a function of the
   * input announcement; a BDD, which represents the conditions under which this particular path is
   * taken; and a boolean indicating whether the path ultimately accepts or rejects the announcement
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
   * Construct a TransferResult from a BDDRoute. By default we use TRUE as the initial path
   * condition and FALSE as the initial value for having hit a return/exit/fallthrough statement. *
   */
  public TransferResult(BDDRoute bddRoute) {
    this(new TransferReturn(bddRoute, bddRoute.getFactory().one()), bddRoute.getFactory().zero());
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
  public TransferResult setReturnValueAccepted(boolean newAccepted) {
    return setReturnValue(_returnValue.setAccepted(newAccepted));
  }

  @Nonnull
  public TransferResult setReturnValueBDD(BDD newBDD) {
    return setReturnValue(
        new TransferReturn(_returnValue.getFirst(), newBDD, _returnValue.getAccepted()));
  }

  @Nonnull
  public TransferResult setReturnValueBDDRoute(BDDRoute newBDDRoute) {
    return setReturnValue(
        new TransferReturn(newBDDRoute, _returnValue.getSecond(), _returnValue.getAccepted()));
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
