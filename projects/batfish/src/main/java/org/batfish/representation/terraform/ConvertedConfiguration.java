package org.batfish.representation.terraform;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspFilter;
import org.batfish.representation.aws.AwsConfiguration;

/** Represents configuration that has been converted from Terraform to native vendor configs */
@ParametersAreNonnullByDefault
public class ConvertedConfiguration {

  @Nullable private AwsConfiguration _awsConfiguration;

  public void setAwsConfiguration(@Nullable AwsConfiguration awsConfiguration) {
    _awsConfiguration = awsConfiguration;
  }

  @Nonnull
  public List<Configuration> toVendorIndependentConfiguration() {
    return _awsConfiguration != null
        ? _awsConfiguration.toVendorIndependentConfigurations()
        : ImmutableList.of();
  }

  @Nonnull
  public Set<Layer1Edge> getLayer1Edges() {
    return _awsConfiguration != null ? _awsConfiguration.getLayer1Edges() : ImmutableSet.of();
  }

  @Nonnull
  public IspConfiguration getIspConfiguration() {
    return _awsConfiguration != null
        ? _awsConfiguration.getIspConfiguration()
        : new IspConfiguration(ImmutableList.of(), IspFilter.ALLOW_ALL);
  }
}
