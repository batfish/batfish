package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.filterByOuterTag;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.common.topology.bridge_domain.node.L1Interface;
import org.batfish.datamodel.IntegerSpace;

/**
 * An edge from a {@link L1Interface} to an {@link
 * org.batfish.common.topology.bridge_domain.node.L2Interface}.
 */
public final class L1ToL2 extends Edge {
  public interface Function extends StateFunction {}

  /**
   * Helper for creating an edge from an l1 interface to a traditional access-mode switchport
   * interface.
   */
  public static @Nonnull L1ToL2 l1ToAccess() {
    return of(filterByOuterTag(IntegerSpace.EMPTY, true));
  }

  /**
   * Helper for creating an edge from an l1 interface to an IOS-XR-style l2transport interface, i.e.
   * a general purpose L2 interface. Note there is no concept of VLANs for this type of interface.
   *
   * <p>The API may evolve as new features are added.
   */
  public static @Nonnull L1ToL2 l1ToL2Transport(
      IntegerSpace allowedOuterTags, boolean allowUntagged) {
    return of(filterByOuterTag(allowedOuterTags, allowUntagged));
  }

  /**
   * Helper for creating an edge from an l1 interface to a traditional trunk-mode switchport
   * interface.
   */
  public static @Nonnull L1ToL2 l1ToTrunk(
      IntegerSpace allowedOuterTags, @Nullable Integer nativeVlan) {
    boolean allowUntagged = nativeVlan != null && allowedOuterTags.contains(nativeVlan);
    return of(filterByOuterTag(allowedOuterTags, allowUntagged));
  }

  @VisibleForTesting
  public static @Nonnull L1ToL2 of(Function stateFunction) {
    return new L1ToL2(stateFunction);
  }

  private L1ToL2(Function stateFunction) {
    super(stateFunction);
  }
}
