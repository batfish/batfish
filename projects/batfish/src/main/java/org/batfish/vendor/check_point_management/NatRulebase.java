package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Data model for an entry of the list response to the {@code show-nat-rulebase} command. */
public final class NatRulebase extends ManagementObject {

  public @Nonnull Map<Uid, AddressSpace> getAddressSpaces() {
    return _addressSpaces;
  }

  @JsonCreator
  private static @Nonnull NatRulebase create(
      @JsonProperty(PROP_OBJECTS_DICTIONARY) @Nullable
          List<TypedManagementObject> objectsDictionary,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(objectsDictionary != null, "Missing %s", PROP_OBJECTS_DICTIONARY);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    Map<Uid, AddressSpace> addressSpaces =
        objectsDictionary.stream()
            .filter(AddressSpace.class::isInstance)
            .collect(
                ImmutableMap.toImmutableMap(
                    TypedManagementObject::getUid, obj -> (AddressSpace) obj));
    return new NatRulebase(addressSpaces, uid);
  }

  @VisibleForTesting
  NatRulebase(Map<Uid, AddressSpace> addressSpaces, Uid uid) {
    super(uid);
    _addressSpaces = addressSpaces;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    NatRulebase that = (NatRulebase) o;
    return _addressSpaces.equals(that._addressSpaces);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _addressSpaces);
  }

  private static final String PROP_OBJECTS_DICTIONARY = "objects-dictionary";

  private final @Nonnull Map<Uid, AddressSpace> _addressSpaces;
}
