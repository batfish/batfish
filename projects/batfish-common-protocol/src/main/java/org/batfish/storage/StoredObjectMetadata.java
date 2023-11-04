package org.batfish.storage;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class StoredObjectMetadata {
  private static final String PROP_KEY = "key";
  private static final String PROP_SIZE = "size";

  private final @Nonnull String _key;
  private final long _size;

  @JsonCreator
  private static @Nonnull StoredObjectMetadata create(
      @JsonProperty(PROP_KEY) @Nullable String key, @JsonProperty(PROP_SIZE) @Nullable Long size) {
    checkArgument(size != null, "Object size must not be null!");
    return new StoredObjectMetadata(firstNonNull(key, ""), size);
  }

  public StoredObjectMetadata(String key, long size) {
    _key = key;
    _size = size;
  }

  @JsonProperty(PROP_KEY)
  public @Nonnull String getKey() {
    return _key;
  }

  @JsonProperty(PROP_SIZE)
  public long getSize() {
    return _size;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StoredObjectMetadata)) {
      return false;
    }
    StoredObjectMetadata other = (StoredObjectMetadata) o;
    return _key.equals(other._key) && _size == other._size;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_key, _size);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add(PROP_KEY, _key).add(PROP_SIZE, _size).toString();
  }
}
