package org.batfish.datamodel.questions;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a difference in values for attributes that are lists or sets of strings */
@ParametersAreNonnullByDefault
public final class StringListDiff extends AttributeDiff {
  private static final String PROP_ADDED = "added";
  private static final String PROP_DELETED = "deleted";

  @Nonnull private final List<String> _added;
  @Nonnull private final List<String> _deleted;

  @JsonCreator
  private static StringListDiff jsonCreator(
      @Nullable @JsonProperty(PROP_FIELD_NAME) String fieldName,
      @Nullable @JsonProperty(PROP_ADDED) List<String> added,
      @Nullable @JsonProperty(PROP_DELETED) List<String> deleted) {
    checkArgument(fieldName != null, "Field name cannot be null for StringListDiff");
    return new StringListDiff(
        fieldName,
        firstNonNull(added, ImmutableList.of()),
        firstNonNull(deleted, ImmutableList.of()));
  }

  public static <T> StringListDiff create(
      String fieldName, Set<T> referenceValues, Set<T> snapshotValues) {
    return new StringListDiff(
        fieldName,
        Sets.difference(snapshotValues, referenceValues).stream()
            .map(Objects::toString)
            .collect(ImmutableList.toImmutableList()),
        Sets.difference(referenceValues, snapshotValues).stream()
            .map(Object::toString)
            .collect(ImmutableList.toImmutableList()));
  }

  public StringListDiff(String fieldName, List<String> added, List<String> deleted) {
    super(fieldName);
    _added = added;
    _deleted = deleted;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return _added.isEmpty() && _deleted.isEmpty();
  }

  @Nonnull
  @JsonProperty(PROP_ADDED)
  public List<String> getAdded() {
    return _added;
  }

  @Nonnull
  @JsonProperty(PROP_DELETED)
  public List<String> getDeleted() {
    return _deleted;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StringListDiff)) {
      return false;
    }
    StringListDiff listDiff = (StringListDiff) o;
    return _fieldName.equals(listDiff._fieldName)
        && _added.equals(listDiff._added)
        && _deleted.equals(listDiff._deleted);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_fieldName, _added, _deleted);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("fieldName", _fieldName)
        .add("added", _added)
        .add("deleted", _deleted)
        .toString();
  }
}
