package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.assignVlanFromOuterTag;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.filterByOuterTag;
import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.function.ComposeBaseImpl;
import org.batfish.common.topology.bridge_domain.function.StateFunction;
import org.batfish.datamodel.IntegerSpace;

/**
 * An edge from a {@link org.batfish.common.topology.bridge_domain.node.PhysicalInterface} to an
 * {@link org.batfish.common.topology.bridge_domain.node.L2Interface}.
 */
public final class PhysicalToL2 extends Edge {
  public interface Function extends StateFunction {}

  public static @Nonnull PhysicalToL2 physicalToAccess() {
    return of(filterByOuterTag(IntegerSpace.EMPTY, true));
  }

  /**
   * Helper for creating an edge from a physical interface to an IOS-XR-style l2transport interface,
   * i.e. a general purpose L2 interface. Note there is no concept of VLANs for this type of
   * interface.
   *
   * <p>The API may evolve as new features are added.
   */
  public static @Nonnull PhysicalToL2 physicalToL2Transport(
      IntegerSpace allowedOuterTags, boolean allowUntagged) {
    return of(filterByOuterTag(allowedOuterTags, allowUntagged));
  }

  /**
   * Helper for creating an edge from a physical interface to a traditional trunk-mode switchport
   * interface.
   */
  public static @Nonnull PhysicalToL2 physicalToTrunk(
      IntegerSpace allowedOuterTags, @Nullable Integer nativeVlan) {
    boolean allowUntagged = nativeVlan != null && allowedOuterTags.contains(nativeVlan);
    return of(
        compose(
            filterByOuterTag(allowedOuterTags, allowUntagged),
            assignVlanFromOuterTag(allowUntagged ? nativeVlan : null)));
  }

  @VisibleForTesting
  public static @Nonnull Function compose(Function func1, Function func2) {
    return func1.equals(identity())
        ? func2
        : func2.equals(identity()) ? func1 : new Compose(func1, func2);
  }

  @VisibleForTesting
  public static @Nonnull PhysicalToL2 of(Function stateFunction) {
    return new PhysicalToL2(stateFunction);
  }

  private static final class Compose extends ComposeBaseImpl<Function> implements Function {

    private Compose(Function func1, Function func2) {
      super(func1, func2);
    }
  }

  private PhysicalToL2(Function stateFunction) {
    super(stateFunction);
  }
}
