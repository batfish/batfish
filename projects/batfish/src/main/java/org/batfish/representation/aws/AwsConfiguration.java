package org.batfish.representation.aws;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GenericConfigObject;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class AwsConfiguration implements Serializable, GenericConfigObject {

  private static final long INITIAL_GENERATED_IP = Ip.FIRST_CLASS_E_EXPERIMENTAL_IP.asLong();

  private static final long serialVersionUID = 1L;

  private Map<String, Configuration> _configurationNodes = new HashMap<>();

  private long _currentGeneratedIpAsLong;

  private Map<String, Region> _regions = new HashMap<>();

  private transient Warnings _warnings;

  public AwsConfiguration() {
    _currentGeneratedIpAsLong = INITIAL_GENERATED_IP;
  }

  public void addConfigElement(String region, JSONObject jsonObj, BatfishLogger logger)
      throws JSONException {

    if (!_regions.containsKey(region)) {
      _regions.put(region, new Region(region));
    }

    _regions.get(region).addConfigElement(jsonObj, logger);
  }

  public Map<String, Configuration> getConfigurationNodes() {
    return _configurationNodes;
  }

  public synchronized Prefix getNextGeneratedLinkSubnet() {
    Ip prefixBase = new Ip(_currentGeneratedIpAsLong);
    Prefix val = new Prefix(prefixBase, 31);
    _currentGeneratedIpAsLong += 2L;
    return val;
  }

  public Warnings getWarnings() {
    return _warnings;
  }

  public Map<String, Configuration> toConfigurations(Warnings warnings) {
    _warnings = warnings;

    for (Region region : _regions.values()) {
      region.toConfigurationNodes(this, _configurationNodes);
    }

    return _configurationNodes;
  }
}
