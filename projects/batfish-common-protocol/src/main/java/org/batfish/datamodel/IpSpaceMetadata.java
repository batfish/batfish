package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;

public class IpSpaceMetadata implements Serializable {
  private static final String PROP_SOURCE_NAME = "sourceName";
  private static final String PROP_SOURCE_TYPE = "sourceType";

  @JsonCreator
  private static IpSpaceMetadata create(
      @JsonProperty(PROP_SOURCE_NAME) String sourceName,
      @JsonProperty(PROP_SOURCE_TYPE) String sourceType) {
    return new IpSpaceMetadata(requireNonNull(sourceName), requireNonNull(sourceType));
  }

  private final String _sourceName;

  private final String _sourceType;

  public IpSpaceMetadata(@Nonnull String sourceName, @Nonnull String sourceType) {
    _sourceName = sourceName;
    _sourceType = sourceType;
  }

  @JsonProperty(PROP_SOURCE_NAME)
  public String getSourceName() {
    return _sourceName;
  }

  @JsonProperty(PROP_SOURCE_TYPE)
  public String getSourceType() {
    return _sourceType;
  }
}
