package org.batfish.vendor;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Identifies a vendor structure in a configuration file. */
@ParametersAreNonnullByDefault
public final class VendorStructure {
  private static final String PROP_FILENAME = "filename";
  private static final String PROP_STRUCTURE_TYPE = "structureType";
  private static final String PROP_STRUCTURE_NAME = "structureName";

  private final String _filename;
  private final String _structureType;
  private final String _structureName;

  public VendorStructure(String filename, String structureType, String structureName) {
    _filename = filename;
    _structureType = structureType;
    _structureName = structureName;
  }

  @JsonCreator
  private static VendorStructure jsonCreator(
      @Nullable @JsonProperty(PROP_FILENAME) String filename,
      @Nullable @JsonProperty(PROP_STRUCTURE_TYPE) String structureType,
      @Nullable @JsonProperty(PROP_STRUCTURE_NAME) String structureName) {
    checkNotNull(filename, "%s cannot be null", PROP_FILENAME);
    checkNotNull(structureType, "%s cannot be null", PROP_STRUCTURE_TYPE);
    checkNotNull(structureName, "%s cannot be null", PROP_STRUCTURE_NAME);
    return new VendorStructure(filename, structureType, structureName);
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
    if (!(obj instanceof VendorStructure)) {
      return false;
    }
    VendorStructure other = (VendorStructure) obj;
    return _filename.equals(other._filename)
        && _structureType.equals(other._structureType)
        && _structureName.equals(other._structureName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_filename, _structureType, _structureName);
  }
}
