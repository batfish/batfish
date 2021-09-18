package org.batfish.vendor.a10.representation;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.VendorConfiguration;

/** Datamodel class representing an A10 device configuration. */
public final class A10Configuration extends VendorConfiguration {

  public A10Configuration() {
    _interfacesEthernet = new HashMap<>();
    _interfacesLoopback = new HashMap<>();
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public Map<Integer, Interface> getInterfacesEthernet() {
    return _interfacesEthernet;
  }

  public Map<Integer, Interface> getInterfacesLoopback() {
    return _interfacesLoopback;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setHumanName(hostname);
    _c.setDeviceModel(DeviceModel.A10);
    _c.setDefaultCrossZoneAction(LineAction.DENY);
    _c.setDefaultInboundAction(LineAction.PERMIT);
    return ImmutableList.of(_c);
  }

  private Configuration _c;
  private String _hostname;
  private final Map<Integer, Interface> _interfacesEthernet;
  private final Map<Integer, Interface> _interfacesLoopback;
  private ConfigurationFormat _vendor;
}
