package org.batfish.minesweeper.bdd;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.minesweeper.TransferParam;

/**
 * The state that is tracked during symbolic BDD-based route analysis: a pair of a {@link
 * org.batfish.minesweeper.TransferParam} and a {@link TransferResult}.
 */
@ParametersAreNonnullByDefault
public class TransferBDDState {

  @Nonnull private final TransferParam<BDDRoute> _param;
  @Nonnull private final TransferResult _result;

  public TransferBDDState(TransferParam<BDDRoute> param, TransferResult result) {
    // eventually we may want to refactor things so that the BDDRoute appears only once
    Preconditions.checkArgument(
        param.getData() == result.getReturnValue().getFirst(),
        "TransferParam and TransferReturn should contain the same BDDRoute object");
    _param = param;
    _result = result;
  }

  public TransferParam<BDDRoute> getTransferParam() {
    return _param;
  }

  public TransferResult getTransferResult() {
    return _result;
  }
}
