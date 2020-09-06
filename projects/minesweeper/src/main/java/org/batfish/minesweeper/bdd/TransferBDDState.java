package org.batfish.minesweeper.bdd;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.minesweeper.TransferParam;
import org.batfish.minesweeper.TransferResult;

/**
 * The state that is tracked during symbolic BDD-based route analysis: a pair of a {@link
 * org.batfish.minesweeper.TransferParam} and a {@link TransferReturn}.
 */
@ParametersAreNonnullByDefault
public class TransferBDDState {

  @Nonnull private final TransferParam<BDDRoute> _param;
  @Nonnull private final TransferResult<TransferReturn, BDD> _result;

  public TransferBDDState(
      TransferParam<BDDRoute> param, TransferResult<TransferReturn, BDD> result) {
    _param = param;
    _result = result;
  }

  public TransferParam<BDDRoute> getTransferParam() {
    return _param;
  }

  public TransferResult<TransferReturn, BDD> getTransferResult() {
    return _result;
  }
}
