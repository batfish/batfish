package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.vendor.VendorStructureId;

public class IpSpaceMetadata implements Serializable {
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
}
