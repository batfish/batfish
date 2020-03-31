package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A representation of difference in the value of a field from the reference snapshot to the current
 * snapshot
 */
@ParametersAreNonnullByDefault
public final class StringDiff extends AttributeDiff {
  private static final String PROP_REFERENCE_VALUE = "referenceValue";
  private static final String PROP_SNAPSHOT_VALUE = "snapshotValue";

  @Nullable private final String _referenceValue;
  @Nullable private final String _snapshotValue;

  public StringDiff(
      String fieldName, @Nullable String referenceValue, @Nullable String snapshotValue) {
    super(fieldName);
    _referenceValue = referenceValue;
    _snapshotValue = snapshotValue;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StringDiff)) {
      return false;
    }
    StringDiff diff = (StringDiff) o;
    return Objects.equals(_fieldName, diff._fieldName)
        && Objects.equals(_referenceValue, diff._referenceValue)
        && Objects.equals(_snapshotValue, diff._snapshotValue);
  }

  @JsonCreator
  private static StringDiff create(
      @Nullable @JsonProperty(PROP_FIELD_NAME) String fieldName,
      @Nullable @JsonProperty(PROP_REFERENCE_VALUE) String referenceValue,
      @Nullable @JsonProperty(PROP_SNAPSHOT_VALUE) String snapshotValue) {
    checkNotNull(fieldName);
    return new StringDiff(fieldName, referenceValue, snapshotValue);
  }

  private static void checkArgument(boolean equals, String s, String type, String simpleName) {}

  @Nullable
  @JsonProperty(PROP_REFERENCE_VALUE)
  public String getReferenceValue() {
    return _referenceValue;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_fieldName, _referenceValue, _snapshotValue);
  }

  @Nullable
  @JsonProperty(PROP_SNAPSHOT_VALUE)
  public String getSnapshotValue() {
    return _snapshotValue;
  }
}
