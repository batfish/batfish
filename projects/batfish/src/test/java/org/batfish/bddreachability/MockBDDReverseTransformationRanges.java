package org.batfish.bddreachability;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.BDDReverseTransformationRangesImpl.Key;
import org.batfish.datamodel.collections.NodeInterfacePair;

final class MockBDDReverseTransformationRanges implements BDDReverseTransformationRanges {
  private final Map<Key, BDD> _incomingTransformationRanges;
  private final Map<Key, BDD> _outgoingTransformationRanges;
  private final BDD _zero;

  MockBDDReverseTransformationRanges(
      BDD zero,
      Map<Key, BDD> incomingTransformationRanges,
      Map<Key, BDD> outgoingTransformationRanges) {
    _zero = zero;
    _incomingTransformationRanges = incomingTransformationRanges;
    _outgoingTransformationRanges = outgoingTransformationRanges;
  }

  @Override
  public @Nonnull BDD reverseIncomingTransformationRange(
      String node, String iface, @Nullable String inIface, @Nullable NodeInterfacePair lastHop) {
    return _incomingTransformationRanges.getOrDefault(
        new Key(node, iface, inIface, lastHop), _zero);
  }

  @Override
  public @Nonnull BDD reverseOutgoingTransformationRange(
      String node, String iface, @Nullable String inIface, @Nullable NodeInterfacePair lastHop) {
    return _outgoingTransformationRanges.getOrDefault(
        new Key(node, iface, inIface, lastHop), _zero);
  }
}
