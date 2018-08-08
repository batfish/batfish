package org.batfish.symbolic.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.symbolic.bdd.BDDUtils.isAssignment;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.math.LongMath;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import org.batfish.common.util.CommonUtil;

/**
 * Manages BDD variables to track a packet's source: which interface it entered the node through, if
 * any (in which case it originated from the device).
 */
public final class BDDSrcManager {
  private static final String VAR_NAME = "SrcInterface";

  private final BDD _originatingFromDeviceBDD;

  private final Map<String, BDD> _srcInterfaceBDDs;

  private final BDDInteger _var;

  public BDDSrcManager(BDDPacket pkt, List<String> srcInterfaces) {
    int bitsRequired = LongMath.log2(srcInterfaces.size() + 1, RoundingMode.CEILING);
    _var = pkt.allocateBDDInteger(VAR_NAME, bitsRequired, false);
    _originatingFromDeviceBDD = _var.value(0);
    _srcInterfaceBDDs = computeMatchSrcInterfaceBDDs(_var, srcInterfaces);
  }

  /**
   * A packet can enter a router from at most 1 interface, possibly none (if the packet originated
   * at the router). So for N srcInterfaces, we need N+1 distinct values: one for each interface,
   * and one for "none of them". We use 0 for "none of them".
   */
  private static Map<String, BDD> computeMatchSrcInterfaceBDDs(
      BDDInteger srcInterfaceVar, List<String> srcInterfaces) {
    ImmutableMap.Builder<String, BDD> matchSrcInterfaceBDDs = ImmutableMap.builder();
    CommonUtil.forEachWithIndex(
        srcInterfaces,
        (idx, iface) -> matchSrcInterfaceBDDs.put(iface, srcInterfaceVar.value(idx + 1)));
    return matchSrcInterfaceBDDs.build();
  }

  public BDD getOriginatingFromDeviceBDD() {
    return _originatingFromDeviceBDD;
  }

  public BDD getSrcInterfaceBDD(String iface) {
    checkArgument(_srcInterfaceBDDs.containsKey(iface), "Unknown source interface: " + iface);
    return _srcInterfaceBDDs.get(iface);
  }

  /**
   * @param bdd An assignment (i.e. only 1 possible value for each variable mentioned).
   * @return The interface of identified by the assigned value.
   */
  public Optional<String> getInterfaceFromAssignment(BDD bdd) {
    checkArgument(isAssignment(bdd));
    List<String> interfaces =
        _srcInterfaceBDDs
            .entrySet()
            .stream()
            .filter(entry -> bdd.imp(entry.getValue()).isOne())
            .map(Entry::getKey)
            .collect(Collectors.toList());
    if (interfaces.isEmpty()) {
      /*
       * Either "originating from device" or "don't care".
       *
       * Since our allocated may contain more values than we actually use, the assigned value may
       * be meaningless (i.e. not the identify for any interface, and not the dedicated value for
       * originating from device). This is only possible if the variable is unconstrained.
       */
      return Optional.empty();
    }
    return Optional.of(interfaces.get(0));
  }

  @VisibleForTesting
  BDDInteger getSrcInterfaceVar() {
    return _var;
  }
}
