package org.batfish.vendor.fastpath.representation;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.VendorConfiguration;

/** Vendor-specific datamodel for a FastPath (Broadcom FASTPATH / ICOS) device configuration. */
public final class FastpathConfiguration extends VendorConfiguration {

  private @Nullable String _hostname;
  private @Nullable String _rawHostname;
  private final Dns _dns;
  private final Sntp _sntp;
  private final Logging _logging;

  public FastpathConfiguration() {
    _dns = new Dns();
    _sntp = new Sntp();
    _logging = new Logging();
  }

  @Override
  public @Nullable String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "hostname cannot be null");
    _hostname = hostname.toLowerCase();
    _rawHostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {}

  /** This device's DNS-client configuration. */
  public @Nonnull Dns getDns() {
    return _dns;
  }

  public @Nonnull Sntp getSntp() {
    return _sntp;
  }

  /** This device's {@code logging} configuration. */
  public @Nonnull Logging getLogging() {
    return _logging;
  }

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    Configuration c = new Configuration(_hostname, ConfigurationFormat.FASTPATH);
    c.setHumanName(_rawHostname);
    c.setDeviceModel(DeviceModel.FASTPATH_UNSPECIFIED);
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDefaultInboundAction(LineAction.PERMIT);

    // Generated default VRF
    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    c.setVrfs(ImmutableMap.of(DEFAULT_VRF_NAME, vrf));

    if (_dns.getDomainName() != null) {
      c.setDomainName(_dns.getDomainName());
    }
    c.setDnsServers(ImmutableSet.copyOf(_dns.getServers()));
    if (_dns.getSourceInterface() != null) {
      c.setDnsSourceInterface(_dns.getSourceInterface());
    }
    c.setNtpServers(ImmutableSet.copyOf(_sntp.getServers()));
    if (_sntp.getSourceInterface() != null) {
      c.setNtpSourceInterface(_sntp.getSourceInterface());
    }
    c.setLoggingServers(ImmutableSet.copyOf(_logging.getServers().keySet()));
    if (_logging.getSourceInterface() != null) {
      c.setLoggingSourceInterface(_logging.getSourceInterface());
    }

    return c;
  }

  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }
}
