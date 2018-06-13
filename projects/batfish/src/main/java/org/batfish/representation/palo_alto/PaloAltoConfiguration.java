package org.batfish.representation.palo_alto;

import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.VendorConfiguration;

public final class PaloAltoConfiguration extends VendorConfiguration {

  private static final long serialVersionUID = 1L;

  private Configuration _c;

  private String _dnsServerPrimary;

  private String _dnsServerSecondary;

  private String _hostname;

  private String _ntpServerPrimary;

  private String _ntpServerSecondary;

  /**
   * Map of group name to server group, where each server group is a map of server name to server
   * configuration
   */
  private SortedMap<String, SortedMap<String, SyslogServer>> _syslogServerGroups;

  private transient Set<String> _unimplementedFeatures;

  private ConfigurationFormat _vendor;

  public PaloAltoConfiguration(Set<String> unimplementedFeatures) {
    _syslogServerGroups = new TreeMap<>();
    _unimplementedFeatures = unimplementedFeatures;
  }

  private NavigableSet<String> getDnsServers() {
    NavigableSet<String> servers = new TreeSet<>();
    if (_dnsServerPrimary != null) {
      servers.add(_dnsServerPrimary);
    }
    if (_dnsServerSecondary != null) {
      servers.add(_dnsServerSecondary);
    }
    return servers;
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  private NavigableSet<String> getNtpServers() {
    NavigableSet<String> servers = new TreeSet<>();
    if (_ntpServerPrimary != null) {
      servers.add(_ntpServerPrimary);
    }
    if (_ntpServerSecondary != null) {
      servers.add(_ntpServerSecondary);
    }
    return servers;
  }

  public SortedMap<String, SortedMap<String, SyslogServer>> getSyslogServerGroups() {
    return _syslogServerGroups;
  }

  @Override
  public Set<String> getUnimplementedFeatures() {
    return _unimplementedFeatures;
  }

  public void setDnsServerPrimary(String dnsServerPrimary) {
    _dnsServerPrimary = dnsServerPrimary;
  }

  public void setDnsServerSecondary(String dnsServerSecondary) {
    _dnsServerSecondary = dnsServerSecondary;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  public void setNtpServerPrimary(String ntpServerPrimary) {
    _ntpServerPrimary = ntpServerPrimary;
  }

  public void setNtpServerSecondary(String ntpServerSecondary) {
    _ntpServerSecondary = ntpServerSecondary;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @Override
  public Configuration toVendorIndependentConfiguration() throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setDefaultCrossZoneAction(LineAction.REJECT);
    _c.setDefaultInboundAction(LineAction.ACCEPT);
    _c.setDnsServers(getDnsServers());
    _c.setNtpServers(getNtpServers());

    NavigableSet<String> loggingServers = new TreeSet<>();
    _syslogServerGroups
        .values()
        .forEach(g -> g.values().forEach(s -> loggingServers.add(s.getAddress())));
    _c.setLoggingServers(loggingServers);
    return _c;
  }
}
