package org.batfish.vendor;

import java.io.Serializable;
import javax.annotation.Nullable;

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
}
