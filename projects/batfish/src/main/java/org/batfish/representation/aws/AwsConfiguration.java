package org.batfish.representation.aws;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GenericConfigObject;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class AwsConfiguration implements Serializable, GenericConfigObject {

  private static final long INITIAL_GENERATED_IP = Ip.FIRST_CLASS_E_EXPERIMENTAL_IP.asLong();

  private static final long serialVersionUID = 1L;

  private Map<String, Configuration> _configurationNodes = new HashMap<>();

  private AtomicLong _currentGeneratedIpAsLong;

  private Map<String, Region> _regions = new HashMap<>();

  private Settings _settings;

  private transient Map<String, Warnings> _warningsByHost;

  public AwsConfiguration() {
    _currentGeneratedIpAsLong = new AtomicLong(INITIAL_GENERATED_IP);
  }

  public void addConfigElement(
      String region,
      JSONObject jsonObj,
      String sourceFileName,
      ParseVendorConfigurationAnswerElement pvcae)
      throws JSONException {
    _regions
        .computeIfAbsent(region, r -> new Region(region))
        .addConfigElement(jsonObj, sourceFileName, pvcae);
  }

  public Map<String, Configuration> getConfigurationNodes() {
    return _configurationNodes;
  }

  public Prefix getNextGeneratedLinkSubnet() {
    long base = _currentGeneratedIpAsLong.getAndAdd(2L);
    assert base % 2 == 0;
    return Prefix.create(Ip.create(base), Prefix.MAX_PREFIX_LENGTH - 1);
  }

  public Settings getSettings() {
    return _settings;
  }

  public Map<String, Warnings> getWarningsByHost() {
    return _warningsByHost;
  }

  public Map<String, Configuration> toConfigurations(
      Settings settings, Map<String, Warnings> warningsByHost) {
    _warningsByHost = warningsByHost;
    _settings = settings;

    for (Region region : _regions.values()) {
      region.toConfigurationNodes(this, _configurationNodes);
    }

    return _configurationNodes;
  }
}
