package org.batfish.bddreachability;

import javax.annotation.Nonnull;
import org.batfish.bddreachability.transition.Transition;

interface BDDReverseFlowTransformationFactory {
  @Nonnull
  Transition reverseFlowIncomingTransformation(String hostname, String iface);

  @Nonnull
  Transition reverseFlowOutgoingTransformation(String hostname, String iface);
}
