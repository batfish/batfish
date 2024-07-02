package org.batfish.minesweeper.bdd;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;

/**
 * The data produced by the symbolic route policy analysis performed in {@link TransferBDD}. It is a
 * triple representing the analysis results along a particular execution path through the route
 * policy:
 *
 * <p>1. A {@link BDDRoute} that represents a function from an input announcement to the
 * corresponding output announcement produced by the analyzed route policy on this particular path.
 * Note that even if the path ends up rejecting the given route, we still record all route updates
 * that occur. We also record whether the path encountered a statement that the {@link TransferBDD}
 * analysis does not currently support, which indicates that the analysis results may not be
 * accurate.
 *
 * <p>2. A {@link BDD} that represents the set of input announcements that take this particular
 * path.
 *
 * <p>3. A boolean indicating whether the path accepts or rejects the input announcement.
 */
public class TransferReturn {

  private final @Nonnull BDDRoute _route;
  private final @Nonnull BDD _bdd;
  private final boolean _accepted;

  TransferReturn(BDDRoute r, BDD b, boolean accepted) {
    _route = r;
    _bdd = b;
    _accepted = accepted;
  }

  public @Nonnull BDDRoute getOutputRoute() {
    return _route;
  }

  public @Nonnull BDD getInputBDD() {
    return _bdd;
  }

  public boolean getAccepted() {
    return _accepted;
  }

  public TransferReturn setAccepted(boolean accepted) {
    return new TransferReturn(_route, _bdd, accepted);
  }

  public String debug() {
    return getOutputRoute().dot(getInputBDD());
  }

  // TransferReturns are mutable (because the BDDRoutes that they contain are mutable), so in
  // general the default pointer equality is the right thing to use;
  // This method is used only to test the results of our symbolic route analysis.
  @VisibleForTesting
  boolean equalsForTesting(TransferReturn other) {
    return this.getOutputRoute().equalsForTesting(other.getOutputRoute())
        && this.getInputBDD().equals(other.getInputBDD())
        && this.getAccepted() == other.getAccepted();
  }

  @Override
  public String toString() {
    return "<" + getOutputRoute() + "," + getInputBDD() + "," + _accepted + ">";
  }
}
