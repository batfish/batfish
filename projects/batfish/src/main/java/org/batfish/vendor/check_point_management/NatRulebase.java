package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Data model for an entry of the list response to the {@code show-nat-rulebase} command. */
public final class NatRulebase extends ManagementObject {

  public @Nonnull Map<Uid, TypedManagementObject> getObjectsDictionary() {
    return _objectsDictionary;
  }

  public @Nonnull List<NatRuleOrSection> getRulebase() {
    return _rulebase;
  }

  @JsonCreator
  private static @Nonnull NatRulebase create(
      @JsonProperty(PROP_OBJECTS_DICTIONARY) @Nullable
          List<TypedManagementObject> objectsDictionary,
      @JsonProperty(PROP_RULEBASE) @Nullable List<NatRuleOrSection> rulebase,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(objectsDictionary != null, "Missing %s", PROP_OBJECTS_DICTIONARY);
    checkArgument(rulebase != null, "Missing %s", PROP_RULEBASE);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new NatRulebase(
        objectsDictionary.stream()
            .collect(ImmutableMap.toImmutableMap(ManagementObject::getUid, Function.identity())),
        rulebase,
        uid);
  }

  @VisibleForTesting
  NatRulebase(
      Map<Uid, TypedManagementObject> objectsDictionary, List<NatRuleOrSection> rulebase, Uid uid) {
    super(uid);
    _objectsDictionary = objectsDictionary;
    _rulebase = rulebase;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    NatRulebase that = (NatRulebase) o;
    return _objectsDictionary.equals(that._objectsDictionary) && _rulebase.equals(that._rulebase);
  }

  @Override
  public String toString() {
    return baseToStringHelper()
        .add(PROP_OBJECTS_DICTIONARY, _objectsDictionary)
        .add(PROP_RULEBASE, _rulebase)
        .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _objectsDictionary, _rulebase);
  }

  private static final String PROP_OBJECTS_DICTIONARY = "objects-dictionary";
  private static final String PROP_RULEBASE = "rulebase";

  private final @Nonnull Map<Uid, TypedManagementObject> _objectsDictionary;
  private final @Nonnull List<NatRuleOrSection> _rulebase;
}
