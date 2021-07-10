package org.batfish.vendor.check_point_gateway.representation;

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

public class CheckPointGatewayConfiguration extends VendorConfiguration {

  public CheckPointGatewayConfiguration() {
    _interfaces = new HashMap<>();
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public Map<String, Interface> getInterfaces() {
    return _interfaces;
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
    _c.setDeviceModel(DeviceModel.CHECK_POINT_GATEWAY);
    _c.setDefaultCrossZoneAction(LineAction.DENY);
    _c.setDefaultInboundAction(LineAction.PERMIT);
    return ImmutableList.of(_c);
  }

  private Configuration _c;
  private String _hostname;
  private Map<String, Interface> _interfaces;
  private ConfigurationFormat _vendor;
}
