package org.batfish.symbolic.interpreter;

import java.util.function.Function;
import net.sf.javabdd.BDD;
import org.batfish.symbolic.bdd.TransferReturn;

public class Transformer {

  private TransferReturn _transfer;

  private Function<BDD, BDD> _function;

  public Transformer(TransferReturn transfer, Function<BDD, BDD> function) {
    this._transfer = transfer;
    this._function = function;
  }

  public TransferReturn getTransfer() {
    return _transfer;
  }

  public Function<BDD, BDD> getFunction() {
    return _function;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Transformer that = (Transformer) o;

    return _transfer != null ? _transfer.equals(that._transfer) : that._transfer == null;
  }

  @Override
  public int hashCode() {
    return _transfer != null ? _transfer.hashCode() : 0;
  }
}
