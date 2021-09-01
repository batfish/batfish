package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.toStringHelper;
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

/** Data model for an entry of the list response to the {@code show-access-rulebase} command. */
public final class AccessLayer extends NamedManagementObject {

  public @Nonnull Map<Uid, TypedManagementObject> getObjectsDictionary() {
    return _objectsDictionary;
  }

  public @Nonnull List<AccessRuleOrSection> getRulebase() {
    return _rulebase;
  }

  @JsonCreator
  private static @Nonnull AccessLayer create(
      @JsonProperty(PROP_OBJECTS_DICTIONARY) @Nullable
          List<TypedManagementObject> objectsDictionary,
      @JsonProperty(PROP_RULEBASE) @Nullable List<AccessRuleOrSection> rulebase,
      @JsonProperty(PROP_UID) @Nullable Uid uid,
      @JsonProperty(PROP_NAME) @Nullable String name) {
    checkArgument(objectsDictionary != null, "Missing %s", PROP_OBJECTS_DICTIONARY);
    checkArgument(rulebase != null, "Missing %s", PROP_RULEBASE);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    checkArgument(name != null, "Missing %s", PROP_NAME);
    return new AccessLayer(
        objectsDictionary.stream()
            .collect(ImmutableMap.toImmutableMap(ManagementObject::getUid, Function.identity())),
        rulebase,
        uid,
        name);
  }

  @VisibleForTesting
  public AccessLayer(
      Map<Uid, TypedManagementObject> objectsDictionary,
      List<AccessRuleOrSection> rulebase,
      Uid uid,
      String name) {
    super(name, uid);
    _objectsDictionary = objectsDictionary;
    _rulebase = rulebase;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    AccessLayer that = (AccessLayer) o;
    return _objectsDictionary.equals(that._objectsDictionary) && _rulebase.equals(that._rulebase);
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .add(PROP_OBJECTS_DICTIONARY, _objectsDictionary)
        .add(PROP_RULEBASE, _rulebase)
        .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _objectsDictionary, _rulebase);
  }

  private static final String PROP_NAME = "name";
  private static final String PROP_OBJECTS_DICTIONARY = "objects-dictionary";
  private static final String PROP_RULEBASE = "rulebase";

  private final @Nonnull Map<Uid, TypedManagementObject> _objectsDictionary;
  private final @Nonnull List<AccessRuleOrSection> _rulebase;
}
