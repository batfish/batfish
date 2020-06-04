package org.batfish.main;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResourceBytes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;

public class TestrigText {

  public static class Builder {

    private static Map<String, byte[]> readTestrigResources(
        String testrigResourcePrefix, @Nullable String subfolder, Iterable<String> filenames) {
      if (filenames != null) {
        List<String> filenameList = ImmutableList.copyOf(filenames);
        SortedSet<String> filenameSet = ImmutableSortedSet.copyOf(filenames);
        if (filenameList.size() != filenameSet.size()) {
          throw new BatfishException("Duplicate filenames provided in: " + filenameList);
        }
        String subfolderText = subfolder != null ? String.format("/%s", subfolder) : "";
        return filenameList.stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Function.identity(),
                    filename ->
                        readResourceBytes(
                            String.format(
                                "%s%s/%s", testrigResourcePrefix, subfolderText, filename))));
      } else {
        return Collections.emptyMap();
      }
    }

    private Map<String, byte[]> _awsBytes;
    private Map<String, byte[]> _bgpTablesBytes;
    private Map<String, byte[]> _configurationBytes;
    private Map<String, byte[]> _hostsBytes;
    private Map<String, byte[]> _iptablesFilesBytes;
    private byte[] _layer1TopologyBytes;
    private Map<String, byte[]> _routingTablesBytes;
    private byte[] _runtimeDataBytes;

    public TestrigText build() {
      TestrigText testrigText = new TestrigText();
      testrigText.setAwsBytes(_awsBytes);
      testrigText.setBgpTablesBytes(_bgpTablesBytes);
      testrigText.setConfigurationBytes(_configurationBytes);
      testrigText.setHostsBytes(_hostsBytes);
      testrigText.setIptablesFilesBytes(_iptablesFilesBytes);
      testrigText.setLayer1TopologyBytes(_layer1TopologyBytes);
      testrigText.setRoutingTablesBytes(_routingTablesBytes);
      testrigText.setRuntimeDataBytes(_runtimeDataBytes);
      return testrigText;
    }

    public Builder setAwsBytes(Map<String, byte[]> awsText) {
      _awsBytes = awsText;
      return this;
    }

    public Builder setAwsFiles(String testrigResourcePrefix, Iterable<String> filenames) {
      _awsBytes =
          readTestrigResources(testrigResourcePrefix, BfConsts.RELPATH_AWS_CONFIGS_DIR, filenames);
      return this;
    }

    public Builder setBgpTablesBytes(Map<String, byte[]> bgpTablesText) {
      _bgpTablesBytes = bgpTablesText;
      return this;
    }

    public Builder setBgpTablesFiles(String testrigResourcePrefix, String... filenames) {
      return setBgpTablesFiles(testrigResourcePrefix, Arrays.asList(filenames));
    }

    public Builder setBgpTablesFiles(String testrigResourcePrefix, Iterable<String> filenames) {
      _bgpTablesBytes =
          readTestrigResources(
              testrigResourcePrefix, BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES, filenames);
      return this;
    }

    public Builder setConfigurationText(Map<String, String> configurationText) {
      _configurationBytes =
          configurationText.entrySet().stream()
              .collect(
                  ImmutableMap.toImmutableMap(Entry::getKey, e -> e.getValue().getBytes(UTF_8)));
      return this;
    }

    public Builder setConfigurationBytes(Map<String, byte[]> configurationText) {
      _configurationBytes = configurationText;
      return this;
    }

    public Builder setConfigurationFiles(String testrigResourcePrefix, String... filenames) {
      return setConfigurationFiles(testrigResourcePrefix, Arrays.asList(filenames));
    }

    public Builder setConfigurationFiles(String testrigResourcePrefix, Iterable<String> filenames) {
      _configurationBytes =
          readTestrigResources(
              testrigResourcePrefix, BfConsts.RELPATH_CONFIGURATIONS_DIR, filenames);
      return this;
    }

    public Builder setHostsBytes(Map<String, byte[]> hostsText) {
      _hostsBytes = hostsText;
      return this;
    }

    public Builder setHostsFiles(String testrigResourcePrefix, String... filenames) {
      return setHostsFiles(testrigResourcePrefix, Arrays.asList(filenames));
    }

    public Builder setHostsFiles(String testrigResourcePrefix, Iterable<String> filenames) {
      _hostsBytes =
          readTestrigResources(testrigResourcePrefix, BfConsts.RELPATH_HOST_CONFIGS_DIR, filenames);
      return this;
    }

    public Builder setIptablesBytes(Map<String, byte[]> iptablesFilesText) {
      _iptablesFilesBytes = iptablesFilesText;
      return this;
    }

    public Builder setIptablesFiles(String testrigResourcePrefix, Iterable<String> filenames) {
      _iptablesFilesBytes = readTestrigResources(testrigResourcePrefix, "iptables", filenames);
      return this;
    }

    public @Nonnull Builder setLayer1TopologyPrefix(@Nonnull String testrigResourcePrefix) {
      _layer1TopologyBytes =
          readTestrigResources(
                  testrigResourcePrefix, null, ImmutableList.of(BfConsts.RELPATH_L1_TOPOLOGY_PATH))
              .values()
              .iterator()
              .next();
      return this;
    }

    public @Nonnull Builder setRuntimeDataPrefix(@Nonnull String testrigResourcePrefix) {
      _runtimeDataBytes =
          readTestrigResources(
                  testrigResourcePrefix, null, ImmutableList.of(BfConsts.RELPATH_RUNTIME_DATA_FILE))
              .values()
              .iterator()
              .next();
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private Map<String, byte[]> _awsBytes;
  private Map<String, byte[]> _bgpTablesBytes;
  private Map<String, byte[]> _configurationBytes;
  private Map<String, byte[]> _hostsBytes;
  private Map<String, byte[]> _iptablesFilesBytes;
  private byte[] _layer1TopologyBytes;
  private Map<String, byte[]> _routingTablesBytes;
  private byte[] _runtimeDataBytes;

  public Map<String, byte[]> getAwsBytes() {
    return _awsBytes;
  }

  public Map<String, byte[]> getBgpTablesBytes() {
    return _bgpTablesBytes;
  }

  public Map<String, byte[]> getConfigurationBytes() {
    return _configurationBytes;
  }

  public Map<String, byte[]> getHostsBytes() {
    return _hostsBytes;
  }

  public Map<String, byte[]> getIptablesFilesBytes() {
    return _iptablesFilesBytes;
  }

  public @Nullable byte[] getLayer1TopologyBytes() {
    return _layer1TopologyBytes;
  }

  public Map<String, byte[]> getRoutingTablesBytes() {
    return _routingTablesBytes;
  }

  public byte[] getRuntimeDataBytes() {
    return _runtimeDataBytes;
  }

  public void setAwsBytes(Map<String, byte[]> awsBytes) {
    _awsBytes = awsBytes;
  }

  public void setBgpTablesBytes(Map<String, byte[]> bgpTablesText) {
    _bgpTablesBytes = bgpTablesText;
  }

  public void setConfigurationBytes(Map<String, byte[]> configurationText) {
    _configurationBytes = configurationText;
  }

  public void setHostsBytes(Map<String, byte[]> hostsBytes) {
    _hostsBytes = hostsBytes;
  }

  public void setIptablesFilesBytes(Map<String, byte[]> iptablesFilesBytes) {
    _iptablesFilesBytes = iptablesFilesBytes;
  }

  public void setLayer1TopologyBytes(@Nullable byte[] layer1TopologyBytes) {
    _layer1TopologyBytes = layer1TopologyBytes;
  }

  public void setRoutingTablesBytes(Map<String, byte[]> routingTablesBytes) {
    _routingTablesBytes = routingTablesBytes;
  }

  public void setRuntimeDataBytes(byte[] runtimeDataBytes) {
    _runtimeDataBytes = runtimeDataBytes;
  }
}
