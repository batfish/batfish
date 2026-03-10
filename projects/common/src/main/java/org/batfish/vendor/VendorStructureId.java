package org.batfish.vendor;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Identifies a vendor structure in a configuration file. */
@ParametersAreNonnullByDefault
public final class VendorStructureId implements Serializable {
  private static final String PROP_FILENAME = "filename";
  private static final String PROP_STRUCTURE_TYPE = "structureType";
  private static final String PROP_STRUCTURE_NAME = "structureName";

  private final String _filename;
  private final String _structureType;
  private final String _structureName;

  public VendorStructureId(String filename, String structureType, String structureName) {
    _filename = filename;
    _structureType = structureType;
    _structureName = structureName;
  }

  @JsonCreator
  private static VendorStructureId jsonCreator(
      @JsonProperty(PROP_FILENAME) @Nullable String filename,
      @JsonProperty(PROP_STRUCTURE_TYPE) @Nullable String structureType,
      @JsonProperty(PROP_STRUCTURE_NAME) @Nullable String structureName) {
    checkNotNull(filename, "%s cannot be null", PROP_FILENAME);
    checkNotNull(structureType, "%s cannot be null", PROP_STRUCTURE_TYPE);
    checkNotNull(structureName, "%s cannot be null", PROP_STRUCTURE_NAME);
    return new VendorStructureId(filename, structureType, structureName);
  }

  @JsonProperty(PROP_FILENAME)
  public String getFilename() {
    return _filename;
  }

  @JsonProperty(PROP_STRUCTURE_TYPE)
  public String getStructureType() {
    return _structureType;
  }

  @JsonProperty(PROP_STRUCTURE_NAME)
  public String getStructureName() {
    return _structureName;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VendorStructureId)) {
      return false;
    }
    VendorStructureId other = (VendorStructureId) obj;
    return _filename.equals(other._filename)
        && _structureType.equals(other._structureType)
        && _structureName.equals(other._structureName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_filename, _structureType, _structureName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(VendorStructureId.class)
        .add("filename", _filename)
        .add("structureType", _structureType)
        .add("structureName", _structureName)
        .toString();
  }
}
