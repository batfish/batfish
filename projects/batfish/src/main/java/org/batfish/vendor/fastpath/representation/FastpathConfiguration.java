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
  private final Tacacs _tacacs;
  private final Aaa _aaa;

  private transient Configuration _c;

  public FastpathConfiguration() {
    _dns = new Dns();
    _sntp = new Sntp();
    _logging = new Logging();
    _tacacs = new Tacacs();
    _aaa = new Aaa();
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

  public @Nonnull Tacacs getTacacs() {
    return _tacacs;
  }

  public @Nonnull Aaa getAaa() {
    return _aaa;
  }

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    _c = new Configuration(_hostname, ConfigurationFormat.FASTPATH);
    _c.setHumanName(_rawHostname);
    _c.setDeviceModel(DeviceModel.FASTPATH_UNSPECIFIED);
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    // Generated default VRF
    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    _c.setVrfs(ImmutableMap.of(DEFAULT_VRF_NAME, vrf));

    convertDomainName();
    convertDnsServers();
    convertDnsSourceInterface();
    convertNtpServers();
    convertNtpSourceInterface();
    convertLoggingServers();
    convertLoggingSourceInterface();
    convertTacacsServers();
    convertTacacsSourceInterface();

    return _c;
  }

  private void convertDomainName() {
    if (_dns.getDomainName() != null) {
      _c.setDomainName(_dns.getDomainName());
    }
  }

  private void convertDnsServers() {
    _c.setDnsServers(ImmutableSet.copyOf(_dns.getServers()));
  }

  private void convertDnsSourceInterface() {
    if (_dns.getSourceInterface() != null) {
      _c.setDnsSourceInterface(_dns.getSourceInterface());
    }
  }

  private void convertNtpServers() {
    _c.setNtpServers(ImmutableSet.copyOf(_sntp.getServers()));
  }

  private void convertNtpSourceInterface() {
    if (_sntp.getSourceInterface() != null) {
      _c.setNtpSourceInterface(_sntp.getSourceInterface());
    }
  }

  private void convertLoggingServers() {
    _c.setLoggingServers(ImmutableSet.copyOf(_logging.getServers().keySet()));
  }

  private void convertLoggingSourceInterface() {
    if (_logging.getSourceInterface() != null) {
      _c.setLoggingSourceInterface(_logging.getSourceInterface());
    }
  }

  private void convertTacacsServers() {
    _c.setTacacsServers(ImmutableSet.copyOf(_tacacs.getServers().keySet()));
  }

  private void convertTacacsSourceInterface() {
    if (_tacacs.getSourceInterface() != null) {
      _c.setTacacsSourceInterface(_tacacs.getSourceInterface());
    }
  }

  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }
}
