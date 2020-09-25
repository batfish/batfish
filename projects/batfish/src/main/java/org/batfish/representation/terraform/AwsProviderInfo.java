package org.batfish.representation.terraform;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
class AwsProviderInfo {

  @Nullable private final String _partition;
  @Nullable private final String _account;
  @Nullable private final String _region;

  AwsProviderInfo(@Nullable String partition, @Nullable String account, @Nullable String region) {
    _partition = partition;
    _account = account;
    _region = region;
  }

  @Nullable
  public String getPartition() {
    return _partition;
  }

  @Nullable
  public String getAccount() {
    return _account;
  }

  @Nullable
  public String getRegion() {
    return _region;
  }
}
