package org.batfish.minesweeper.bdd;

import java.util.Objects;
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
   * Intermediate BGP attributes are used by the vendor-independent model to properly encode the
   * semantics of attribute reads and writes of various vendors. See {@link
   * org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement} for the statements
   * that pertain to intermediate attributes.
   */
  private final @Nonnull BDDRoute _intermediateBgpAttributes;

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
   * condition and FALSE as the initial value for having hit a return/exit/fallthrough statement.
   */
  public TransferResult(BDDRoute bddRoute) {
    this(bddRoute, bddRoute.getFactory().one());
  }

  /**
   * Construct a TransferResult from a BDDRoute with given input route constraints. This uses FALSE
   * as the initial value for having hit a return/exit/fallthrough statement.
   */
  public TransferResult(BDDRoute bddRoute, BDD inputRouteConstraints) {
    this(
        new TransferReturn(bddRoute, inputRouteConstraints, false),
        bddRoute.deepCopy(),
        false,
        false,
        false,
        false);
  }

  public TransferResult(
      TransferReturn retVal,
      BDDRoute intermediateBgpAttributes,
      boolean suppressedValue,
      boolean exitAssignedValue,
      boolean fallThroughValue,
      boolean returnAssignedValue) {
    _suppressedValue = suppressedValue;
    _returnValue = retVal;
    _intermediateBgpAttributes = intermediateBgpAttributes;
    _exitAssignedValue = exitAssignedValue;
    _fallthroughValue = fallThroughValue;
    _returnAssignedValue = returnAssignedValue;
  }

  public @Nonnull TransferReturn getReturnValue() {
    return _returnValue;
  }

  public @Nonnull BDDRoute getIntermediateBgpAttributes() {
    return _intermediateBgpAttributes;
  }

  public boolean getSuppressedValue() {
    return _suppressedValue;
  }

  public boolean getFallthroughValue() {
    return _fallthroughValue;
  }

  public boolean getExitAssignedValue() {
    return _exitAssignedValue;
  }

  public boolean getReturnAssignedValue() {
    return _returnAssignedValue;
  }

  public @Nonnull TransferResult setReturnValue(TransferReturn newReturn) {
    return new TransferResult(
        newReturn,
        _intermediateBgpAttributes,
        _suppressedValue,
        _exitAssignedValue,
        _fallthroughValue,
        _returnAssignedValue);
  }

  public @Nonnull TransferResult setIntermediateAttributes(BDDRoute newIntermediateAttributes) {
    return new TransferResult(
        _returnValue,
        newIntermediateAttributes,
        _suppressedValue,
        _exitAssignedValue,
        _fallthroughValue,
        _returnAssignedValue);
  }

  public @Nonnull TransferResult setReturnValueAccepted(boolean newAccepted) {
    return setReturnValue(_returnValue.setAccepted(newAccepted));
  }

  public @Nonnull TransferResult setReturnValueBDD(BDD newBDD) {
    return setReturnValue(
        new TransferReturn(_returnValue.getOutputRoute(), newBDD, _returnValue.getAccepted()));
  }

  public @Nonnull TransferResult setReturnValueOutputRoute(BDDRoute newOutputRoute) {
    return setReturnValue(
        new TransferReturn(
            newOutputRoute, _returnValue.getInputConstraints(), _returnValue.getAccepted()));
  }

  public @Nonnull TransferResult setSuppressedValue(boolean suppressedValue) {
    return new TransferResult(
        _returnValue,
        _intermediateBgpAttributes,
        suppressedValue,
        _exitAssignedValue,
        _fallthroughValue,
        _returnAssignedValue);
  }

  public @Nonnull TransferResult setExitAssignedValue(boolean exitAssignedValue) {
    return new TransferResult(
        _returnValue,
        _intermediateBgpAttributes,
        _suppressedValue,
        exitAssignedValue,
        _fallthroughValue,
        _returnAssignedValue);
  }

  public @Nonnull TransferResult setFallthroughValue(boolean fallthroughValue) {
    return new TransferResult(
        _returnValue,
        _intermediateBgpAttributes,
        _suppressedValue,
        _exitAssignedValue,
        fallthroughValue,
        _returnAssignedValue);
  }

  public @Nonnull TransferResult setReturnAssignedValue(boolean returnAssignedValue) {
    return new TransferResult(
        _returnValue,
        _intermediateBgpAttributes,
        _suppressedValue,
        _exitAssignedValue,
        _fallthroughValue,
        returnAssignedValue);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof TransferResult)) {
      return false;
    }
    TransferResult that = (TransferResult) o;
    return _suppressedValue == that._suppressedValue
        && _fallthroughValue == that._fallthroughValue
        && _exitAssignedValue == that._exitAssignedValue
        && _returnAssignedValue == that._returnAssignedValue
        && Objects.equals(_returnValue, that._returnValue)
        && Objects.equals(_intermediateBgpAttributes, that._intermediateBgpAttributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _returnValue,
        _intermediateBgpAttributes,
        _suppressedValue,
        _fallthroughValue,
        _exitAssignedValue,
        _returnAssignedValue);
  }
}
