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
  private final @Nonnull TransferReturn _returnValue;

  /**
   * Whether the routes that go down this path should be suppressed (i.e., not announced). Route
   * suppression happens due to aggregation, and this is represented by the {@link
   * org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement} Suppress, but routes
   * can also be explicitly unsuppressed with Unsuppress.
   */
  private final boolean _suppressedValue;

  /**
   * The following three fields are used to ensure that the analysis accurately identifies all and
   * only feasible execution paths through a route policy.
   */

  /* boolean indicating that the analysis has hit a fall-through condition in the policy
  being analyzed, meaning that control flow should continue to the next policy */
  private final boolean _fallthroughValue;

  /* boolean indicating that the analysis has hit an exit condition in the policy
  being analyzed, which represents the termination of the execution */
  private final boolean _exitAssignedValue;

  /* boolean indicating that the analysis has hit a return condition in the policy
  being analyzed, which represents the termination of a nested call to a routing policy */
  private final boolean _returnAssignedValue;

  /**
   * Construct a TransferResult from a BDDRoute. By default we use TRUE as the initial path
   * condition and FALSE as the initial value for having hit a return/exit/fallthrough statement. *
   */
  public TransferResult(BDDRoute bddRoute) {
    this(new TransferReturn(bddRoute, bddRoute.getFactory().one()), false);
  }

  public TransferResult(TransferReturn ret, boolean defaultVal) {
    this(ret, defaultVal, defaultVal, defaultVal, defaultVal);
  }

  public TransferResult(
      TransferReturn retVal,
      boolean suppressedValue,
      boolean exitAssignedValue,
      boolean fallThroughValue,
      boolean returnAssignedValue) {
    _suppressedValue = suppressedValue;
    _returnValue = retVal;
    _exitAssignedValue = exitAssignedValue;
    _fallthroughValue = fallThroughValue;
    _returnAssignedValue = returnAssignedValue;
  }

  public @Nonnull TransferReturn getReturnValue() {
    return _returnValue;
  }

  public @Nonnull boolean getSuppressedValue() {
    return _suppressedValue;
  }

  public @Nonnull boolean getFallthroughValue() {
    return _fallthroughValue;
  }

  public @Nonnull boolean getExitAssignedValue() {
    return _exitAssignedValue;
  }

  public @Nonnull boolean getReturnAssignedValue() {
    return _returnAssignedValue;
  }

  public @Nonnull TransferResult setReturnValue(TransferReturn newReturn) {
    return new TransferResult(
        newReturn, _suppressedValue, _exitAssignedValue, _fallthroughValue, _returnAssignedValue);
  }

  public @Nonnull TransferResult setReturnValueAccepted(boolean newAccepted) {
    return setReturnValue(_returnValue.setAccepted(newAccepted));
  }

  public @Nonnull TransferResult setReturnValueBDD(BDD newBDD) {
    return setReturnValue(
        new TransferReturn(_returnValue.getFirst(), newBDD, _returnValue.getAccepted()));
  }

  public @Nonnull TransferResult setReturnValueBDDRoute(BDDRoute newBDDRoute) {
    return setReturnValue(
        new TransferReturn(newBDDRoute, _returnValue.getSecond(), _returnValue.getAccepted()));
  }

  public @Nonnull TransferResult setSuppressedValue(boolean suppressedValue) {
    return new TransferResult(
        _returnValue, suppressedValue, _exitAssignedValue, _fallthroughValue, _returnAssignedValue);
  }

  public @Nonnull TransferResult setExitAssignedValue(boolean exitAssignedValue) {
    return new TransferResult(
        _returnValue, _suppressedValue, exitAssignedValue, _fallthroughValue, _returnAssignedValue);
  }

  public @Nonnull TransferResult setFallthroughValue(boolean fallthroughValue) {
    return new TransferResult(
        _returnValue, _suppressedValue, _exitAssignedValue, fallthroughValue, _returnAssignedValue);
  }

  public @Nonnull TransferResult setReturnAssignedValue(boolean returnAssignedValue) {
    return new TransferResult(
        _returnValue, _suppressedValue, _exitAssignedValue, _fallthroughValue, returnAssignedValue);
  }
}
