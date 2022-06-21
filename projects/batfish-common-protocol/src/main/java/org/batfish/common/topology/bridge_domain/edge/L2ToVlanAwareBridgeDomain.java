package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.assignVlanFromOuterTag;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.setVlanId;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.translateVlan;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.function.ComposeBaseImpl;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain;
import org.batfish.datamodel.IntegerSpace;

/**
 * An edge from an {@link org.batfish.common.topology.bridge_domain.node.L2Interface} to a {@link
 * NonVlanAwareBridgeDomain}.
 */
public final class L2ToVlanAwareBridgeDomain extends L2ToBridgeDomain {
  public interface Function extends StateFunction {}

  /**
   * Helper for creating and edge from a traditional access-mode switchport to a device's vlan-aware
   * bridge domain.
   */
  public static @Nonnull L2ToVlanAwareBridgeDomain accessToBridgeDomain(int vlanId) {
    return of(setVlanId(vlanId));
  }

  /**
   * Helper for creating an edge from a traditional trunk-mode switchport to a device's vlan-aware
   * bridge domain.
   */
  public static @Nonnull L2ToVlanAwareBridgeDomain trunkToBridgeDomain(
      IntegerSpace allowedOuterTags,
      @Nullable Integer nativeVlan,
      Map<Integer, Integer> translations) {
    boolean allowUntagged = nativeVlan != null && allowedOuterTags.contains(nativeVlan);
    return of(
        compose(
            assignVlanFromOuterTag(allowUntagged ? nativeVlan : null),
            translateVlan(translations)));
  }

  @VisibleForTesting
  public static @Nonnull Function compose(Function func1, Function func2) {
    return func1.equals(identity())
        ? func2
        : func2.equals(identity()) ? func1 : new Compose(func1, func2);
  }

  @VisibleForTesting
  public static @Nonnull L2ToVlanAwareBridgeDomain of(Function stateFunction) {
    return new L2ToVlanAwareBridgeDomain(stateFunction);
  }

  private static final class Compose extends ComposeBaseImpl<Function> implements Function {

    private Compose(Function func1, Function func2) {
      super(func1, func2);
    }
  }

  private L2ToVlanAwareBridgeDomain(Function stateFunction) {
    super(stateFunction);
  }
}
