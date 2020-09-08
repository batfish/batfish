package org.batfish.minesweeper.bdd;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.minesweeper.TransferParam;

/**
 * The state that is tracked during symbolic BDD-based route analysis: a pair of a {@link
 * org.batfish.minesweeper.TransferParam} and a {@link TransferReturn}.
 */
@ParametersAreNonnullByDefault
public class TransferBDDState {

  @Nonnull private final TransferParam<BDDRoute> _param;
  @Nonnull private final TransferResult _result;

  public TransferBDDState(TransferParam<BDDRoute> param, TransferResult result) {
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
