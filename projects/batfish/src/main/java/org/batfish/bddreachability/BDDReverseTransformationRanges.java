package org.batfish.bddreachability;

import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;

interface BDDReverseTransformationRanges {
  @Nonnull
  BDD reverseIncomingTransformationRange(String node, String iface);

  @Nonnull
  BDD reverseOutgoingTransformationRange(String node, String iface);
}
