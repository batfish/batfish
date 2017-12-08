package org.batfish.main;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;

public class TestrigText {

  public static class Builder {

    private SortedMap<String, String> _awsText;

    private SortedMap<String, String> _bgpTablesText;

    private SortedMap<String, String> _configurationText;

    private SortedMap<String, String> _hostsText;

    private SortedMap<String, String> _iptablesFilesText;

    private SortedMap<String, String> _routingTablesText;

    public TestrigText build() {
      TestrigText testrigText = new TestrigText();
      testrigText.setAwsText(_awsText);
      testrigText.setBgpTablesText(_bgpTablesText);
      testrigText.setConfigurationText(_configurationText);
      testrigText.setHostsText(_hostsText);
      testrigText.setIptablesFilesText(_iptablesFilesText);
      testrigText.setRoutingTablesText(_routingTablesText);
      return testrigText;
    }

    private static SortedMap<String, String> readTestrigResources(
        String testrigResourcePrefix, String subfolder, SortedSet<String> filenames) {
      SortedMap<String, String> content = new TreeMap<>();
      if (filenames != null) {
        for (String filename : filenames) {
          String path = String.format("%s/%s/%s", testrigResourcePrefix, subfolder, filename);
          String text = CommonUtil.readResource(path);
          content.put(filename, text);
        }
      }
      return content;
    }

    public Builder setAwsText(String testrigResourcePrefix, SortedSet<String> filenames) {
      _awsText =
          readTestrigResources(
              testrigResourcePrefix, BfConsts.RELPATH_AWS_VPC_CONFIGS_DIR, filenames);
      return this;
    }

    public Builder setAwsText(SortedMap<String, String> awsText) {
      _awsText = awsText;
      return this;
    }

    public Builder setBgpTablesText(String testrigResourcePrefix, SortedSet<String> filenames) {
      _bgpTablesText =
          readTestrigResources(
              testrigResourcePrefix, BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES, filenames);
      return this;
    }

    public Builder setBgpTablesText(SortedMap<String, String> bgpTablesText) {
      _bgpTablesText = bgpTablesText;
      return this;
    }

    public Builder setConfigurationText(String testrigResourcePrefix, SortedSet<String> filenames) {
      _configurationText =
          readTestrigResources(
              testrigResourcePrefix, BfConsts.RELPATH_CONFIGURATIONS_DIR, filenames);
      return this;
    }

    public Builder setConfigurationText(SortedMap<String, String> configurationText) {
      _configurationText = configurationText;
      return this;
    }

    public Builder setHostsText(String testrigResourcePrefix, SortedSet<String> filenames) {
      _hostsText =
          readTestrigResources(testrigResourcePrefix, BfConsts.RELPATH_HOST_CONFIGS_DIR, filenames);
      return this;
    }

    public Builder setHostsText(SortedMap<String, String> hostsText) {
      _hostsText = hostsText;
      return this;
    }

    public Builder setIptablesFilesText(String testrigResourcePrefix, SortedSet<String> filenames) {
      _iptablesFilesText = readTestrigResources(testrigResourcePrefix, "iptables", filenames);
      return this;
    }

    public Builder setIptablesFilesText(SortedMap<String, String> iptablesFilesText) {
      _iptablesFilesText = iptablesFilesText;
      return this;
    }

    public Builder setRoutingTablesText(String testrigResourcePrefix, SortedSet<String> filenames) {
      _routingTablesText =
          readTestrigResources(
              testrigResourcePrefix, BfConsts.RELPATH_ENVIRONMENT_ROUTING_TABLES, filenames);
      return this;
    }

    public Builder setRoutingTablesText(SortedMap<String, String> routingTablesText) {
      _routingTablesText = routingTablesText;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private SortedMap<String, String> _awsText;

  private SortedMap<String, String> _bgpTablesText;

  private SortedMap<String, String> _configurationText;

  private SortedMap<String, String> _hostsText;

  private SortedMap<String, String> _iptablesFilesText;

  private SortedMap<String, String> _routingTablesText;

  public SortedMap<String, String> getAwsText() {
    return _awsText;
  }

  public SortedMap<String, String> getBgpTablesText() {
    return _bgpTablesText;
  }

  public SortedMap<String, String> getConfigurationText() {
    return _configurationText;
  }

  public SortedMap<String, String> getHostsText() {
    return _hostsText;
  }

  public SortedMap<String, String> getIptablesFilesText() {
    return _iptablesFilesText;
  }

  public SortedMap<String, String> getRoutingTablesText() {
    return _routingTablesText;
  }

  public void setAwsText(SortedMap<String, String> awsText) {
    _awsText = awsText;
  }

  public void setBgpTablesText(SortedMap<String, String> bgpTablesText) {
    _bgpTablesText = bgpTablesText;
  }

  public void setConfigurationText(SortedMap<String, String> configurationText) {
    _configurationText = configurationText;
  }

  public void setHostsText(SortedMap<String, String> hostsText) {
    _hostsText = hostsText;
  }

  public void setIptablesFilesText(SortedMap<String, String> iptablesFilesText) {
    _iptablesFilesText = iptablesFilesText;
  }

  public void setRoutingTablesText(SortedMap<String, String> routingTablesText) {
    _routingTablesText = routingTablesText;
  }
}
