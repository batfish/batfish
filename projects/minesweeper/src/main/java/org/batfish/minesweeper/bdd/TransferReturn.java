package org.batfish.minesweeper.bdd;

import net.sf.javabdd.BDD;
import org.batfish.minesweeper.utils.Tuple;

/**
 * The data produced by the symbolic route policy analysis performed in {@link TransferBDD}. It is a
 * pair of two things:
 *
 * <p>1. A {@link BDDRoute} that represents a function from input announcements to the corresponding
 * output announcements produced by the analyzed route policy.
 *
 * <p>2. A {@link BDD} that represents the set of input announcements that are accepted by the
 * analyzed route policy.
 */
public class TransferReturn extends Tuple<BDDRoute, BDD> {

  private final boolean _accepted;
  TransferReturn(BDDRoute r, BDD b, boolean accepted) {
    super(r, b);
    _accepted = accepted;
  }

  TransferReturn(BDDRoute r, BDD b) {
    this(r, b, false);
  }

  public boolean getAccepted() { return _accepted; }

  public TransferReturn setAccepted(boolean accepted) {
    return new TransferReturn(getFirst(), getSecond(), accepted);
  }

  public String debug() {
    return getFirst().dot(getSecond());
  }
}
