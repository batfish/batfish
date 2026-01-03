package org.batfish.common.bdd;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;

/**
 * Class for picking a representative flow from a BDD according to the given preference as a list of
 * BDDs
 */
@ParametersAreNonnullByDefault
public final class BDDRepresentativePicker {

  /**
   * Picks a representative assignment using the input {@link
   * BDDFlowConstraintGenerator.BddRefiner}.
   */
  public static @Nonnull BDD pickRepresentative(
      BDD bdd, BDDFlowConstraintGenerator.BddRefiner preference) {
    if (bdd.isZero()) {
      return bdd;
    }
    BDD refinedBdd = preference.refine(bdd);
    BDD curBdd = refinedBdd.isZero() ? bdd : refinedBdd;
    return curBdd.satOne();
  }

  private BDDRepresentativePicker() {}
}
