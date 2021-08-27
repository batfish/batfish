package org.batfish.vendor;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Wrapper class for any context needed to convert {@link VendorConfiguration} that is not included
 * in the source files for that configuration. This class does not result in any additional VI
 * devices, it only supplements conversion.
 */
public final class ConversionContext implements Serializable {
  public static final ConversionContext EMPTY_CONVERSION_CONTEXT = new ConversionContext();

  public @Nullable VendorSupplementalInformation getCheckpointManagementConfiguration() {
    return _checkpointManagementConfiguration;
  }

  public void setCheckpointManagementConfiguration(
      @Nullable VendorSupplementalInformation checkpointManagementConfiguration) {
    _checkpointManagementConfiguration = checkpointManagementConfiguration;
  }

  private @Nullable VendorSupplementalInformation _checkpointManagementConfiguration;

  public boolean isEmpty() {
    return _checkpointManagementConfiguration == null;
  }
}
