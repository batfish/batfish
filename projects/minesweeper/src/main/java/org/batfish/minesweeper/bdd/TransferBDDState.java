package org.batfish.minesweeper.bdd;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The state that is tracked during symbolic BDD-based route analysis: a pair of a {@link
 * TransferParam} and a {@link TransferResult}.
 */
@ParametersAreNonnullByDefault
public class TransferBDDState {

  private final @Nonnull TransferParam _param;
  private final @Nonnull TransferResult _result;

  public TransferBDDState(TransferParam param, TransferResult result) {
    _param = param;
    _result = result;
  }

  public TransferParam getTransferParam() {
    return _param;
  }

  public TransferResult getTransferResult() {
    return _result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof TransferBDDState)) {
      return false;
    }
    TransferBDDState that = (TransferBDDState) o;
    return _param.equals(that._param) && _result.equals(that._result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_param, _result);
  }
}
