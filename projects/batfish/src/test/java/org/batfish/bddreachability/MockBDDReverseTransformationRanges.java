package org.batfish.bddreachability;

import java.util.Map;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.collections.NodeInterfacePair;

final class MockBDDReverseTransformationRanges implements BDDReverseTransformationRanges {
  private final Map<NodeInterfacePair, BDD> _incomingTransformationRanges;
  private final Map<NodeInterfacePair, BDD> _outgoingTransformationRanges;
  private final BDD _zero;

  MockBDDReverseTransformationRanges(
      BDD zero,
      Map<NodeInterfacePair, BDD> incomingTransformationRanges,
      Map<NodeInterfacePair, BDD> outgoingTransformationRanges) {
    _zero = zero;
    _incomingTransformationRanges = incomingTransformationRanges;
    _outgoingTransformationRanges = outgoingTransformationRanges;
  }

  @Override
  public @Nonnull BDD reverseIncomingTransformationRange(String node, String iface) {
    return _incomingTransformationRanges.getOrDefault(new NodeInterfacePair(node, iface), _zero);
  }

  @Override
  public @Nonnull BDD reverseOutgoingTransformationRange(String node, String iface) {
    return _outgoingTransformationRanges.getOrDefault(new NodeInterfacePair(node, iface), _zero);
  }
}
