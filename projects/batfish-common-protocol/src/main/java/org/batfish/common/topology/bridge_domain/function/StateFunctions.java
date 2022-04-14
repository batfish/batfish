package org.batfish.common.topology.bridge_domain.function;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.SubRange;

/**
 * A utility class for generating {@link StateFunction}s.
 *
 * <p>See the documentation for the return type of each function for the function's semantics.
 */
public final class StateFunctions {

  public static @Nonnull Identity identity() {
    return Identity.instance();
  }

  public static @Nonnull AssignVlanFromOuterTag assignVlanFromOuterTag(
      @Nullable Integer nativeVlan) {
    return AssignVlanFromOuterTag.of(nativeVlan);
  }

  public static @Nonnull ClearVlanId clearVlanId() {
    return ClearVlanId.instance();
  }

  public static @Nonnull FilterByOuterTag filterByOuterTag(
      IntegerSpace allowedOuterTags, boolean allowUntagged) {
    return FilterByOuterTag.of(allowedOuterTags, allowUntagged);
  }

  public static @Nonnull FilterByVlanId filterByVlanId(IntegerSpace allowedVlanIds) {
    return FilterByVlanId.of(allowedVlanIds);
  }

  public static @Nonnull PopTag popTag(int count) {
    return PopTag.of(count);
  }

  public static @Nonnull PushTag pushTag(int tagToPush) {
    return PushTag.of(tagToPush);
  }

  public static @Nonnull PushVlanId pushVlanId(@Nullable Integer exceptVlan) {
    return PushVlanId.of(exceptVlan);
  }

  public static @Nonnull SetVlanId setVlanId(int vlanId) {
    return SetVlanId.of(vlanId);
  }

  public static @Nonnull TranslateVlan translateVlan(Map<Integer, Integer> translations) {
    return TranslateVlan.of(translations);
  }

  static final IntegerSpace ALL_VLAN_IDS = IntegerSpace.of(new SubRange(1, 4094));

  private StateFunctions() {}
}
