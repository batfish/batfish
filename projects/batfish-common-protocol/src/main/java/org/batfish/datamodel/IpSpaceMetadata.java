package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.vendor.VendorStructureId;

public final class IpSpaceMetadata implements Serializable {
  private static final String PROP_SOURCE_NAME = "sourceName";
  private static final String PROP_SOURCE_TYPE = "sourceType";
  private static final String PROP_VENDOR_STRUCTURE_ID = "vendorStructureId";

  @JsonCreator
  private static IpSpaceMetadata create(
      @JsonProperty(PROP_SOURCE_NAME) @Nullable String sourceName,
      @JsonProperty(PROP_SOURCE_TYPE) @Nullable String sourceType,
      @JsonProperty(PROP_VENDOR_STRUCTURE_ID) @Nullable VendorStructureId vendorStructureId) {
    return new IpSpaceMetadata(
        requireNonNull(sourceName), requireNonNull(sourceType), vendorStructureId);
  }

  /**
   * The human-readable name of the IpSpace being matched. For example, "Public_Ips". Ideally, from
   * the configuration without Batfish tracking modifications like ~vsys.
   */
  private final @Nonnull String _sourceName;

  /**
   * The human-readable name of the type of object being matched. For example, "network object-group
   * ip". Ideally, expressed in vendor terminology appearing in the configuration.
   */
  private final @Nonnull String _sourceType;

  /** A reference to the definition text of the object. */
  private final @Nullable VendorStructureId _vendorStructureId;

  public IpSpaceMetadata(
      @Nonnull String sourceName,
      @Nonnull String sourceType,
      @Nullable VendorStructureId vendorStructureId) {
    _sourceName = sourceName;
    _sourceType = sourceType;
    _vendorStructureId = vendorStructureId;
  }

  @JsonProperty(PROP_SOURCE_NAME)
  public String getSourceName() {
    return _sourceName;
  }

  @JsonProperty(PROP_SOURCE_TYPE)
  public String getSourceType() {
    return _sourceType;
  }

  @JsonProperty(PROP_VENDOR_STRUCTURE_ID)
  public @Nullable VendorStructureId getVendorStructureId() {
    return _vendorStructureId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof IpSpaceMetadata)) {
      return false;
    }
    IpSpaceMetadata metadata = (IpSpaceMetadata) o;
    return _sourceName.equals(metadata._sourceName)
        && _sourceType.equals(metadata._sourceType)
        && Objects.equals(_vendorStructureId, metadata._vendorStructureId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_sourceName, _sourceType, _vendorStructureId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("sourceName", _sourceName)
        .add("sourceType", _sourceType)
        .add("vendorStructureId", _vendorStructureId)
        .toString();
  }
}
