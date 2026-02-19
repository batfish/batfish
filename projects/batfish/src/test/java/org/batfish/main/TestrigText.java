package org.batfish.main;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResourceBytes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.batfish.vendor.ConversionContext;

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
    private Map<String, byte[]> _azureBytes;
    private Map<String, byte[]> _bgpTablesBytes;
    private Map<String, byte[]> _ciscoAciConfigBytes;
    private Map<String, byte[]> _checkpointMgmtBytes;
    private Map<String, byte[]> _configurationBytes;
    private ConversionContext _conversionContext;
    private byte[] _externalBgpAnnouncementsBytes;
    private Map<String, byte[]> _hostsBytes;
    private Map<String, byte[]> _iptablesFilesBytes;
    private byte[] _ispConfigBytes;
    private byte[] _layer1TopologyBytes;
    private Map<String, byte[]> _routingTablesBytes;
    private byte[] _runtimeDataBytes;
    private Map<String, byte[]> _sonicConfigBytes;

    public TestrigText build() {
      TestrigText testrigText = new TestrigText();
      testrigText.setAwsBytes(_awsBytes);
      testrigText.setAzureBytes(_azureBytes);
      testrigText.setBgpTablesBytes(_bgpTablesBytes);
      testrigText.setCiscoAciConfigBytes(_ciscoAciConfigBytes);
      testrigText.setCheckpointMgmtBytes(_checkpointMgmtBytes);
      testrigText.setConfigurationBytes(_configurationBytes);
      testrigText.setConversionContext(_conversionContext);
      testrigText.setExternalBgpAnnouncements(_externalBgpAnnouncementsBytes);
      testrigText.setHostsBytes(_hostsBytes);
      testrigText.setIptablesFilesBytes(_iptablesFilesBytes);
      testrigText.setIspConfigBytes(_ispConfigBytes);
      testrigText.setLayer1TopologyBytes(_layer1TopologyBytes);
      testrigText.setRoutingTablesBytes(_routingTablesBytes);
      testrigText.setRuntimeDataBytes(_runtimeDataBytes);
      testrigText.setSonicConfigBytes(_sonicConfigBytes);
      return testrigText;
    }

    public Builder setAwsBytes(Map<String, byte[]> awsText) {
      _awsBytes = awsText;
      return this;
    }

    public Builder setAzureBytes(Map<String, byte[]> azureText) {
      _azureBytes = azureText;
      return this;
    }

    public Builder setAwsFiles(String testrigResourcePrefix, Iterable<String> filenames) {
      _awsBytes =
          readTestrigResources(testrigResourcePrefix, BfConsts.RELPATH_AWS_CONFIGS_DIR, filenames);
      return this;
    }

    public Builder setAzureFiles(String testrigResourcePrefix, Iterable<String> filenames) {
      _azureBytes =
          readTestrigResources(
              testrigResourcePrefix, BfConsts.RELPATH_AZURE_CONFIGS_DIR, filenames);
      return this;
    }

    public Builder setBgpTablesBytes(Map<String, byte[]> bgpTablesText) {
      _bgpTablesBytes = bgpTablesText;
      return this;
    }

    public Builder setCiscoAciConfigFiles(
        String testrigResourcePrefix, Iterable<String> filenames) {
      _ciscoAciConfigBytes =
          readTestrigResources(
              testrigResourcePrefix, BfConsts.RELPATH_CISCO_ACI_CONFIGS_DIR, filenames);
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

    public Builder setCheckpointMgmtFiles(String testrigResourcePrefix, String... filenames) {
      return setConfigurationFiles(testrigResourcePrefix, Arrays.asList(filenames));
    }

    public Builder setCheckpointMgmtFiles(
        String testrigResourcePrefix, Iterable<String> filenames) {
      _checkpointMgmtBytes =
          readTestrigResources(
              testrigResourcePrefix, BfConsts.RELPATH_CHECKPOINT_MANAGEMENT_DIR, filenames);
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

    public @Nonnull Builder setIspConfigBytes(@Nonnull byte[] ispConfigBytes) {
      _ispConfigBytes = ispConfigBytes;
      return this;
    }

    public @Nonnull Builder setIspConfigPrefix(@Nonnull String testrigResourcePrefix) {
      _ispConfigBytes =
          readTestrigResources(
                  testrigResourcePrefix, null, ImmutableList.of(BfConsts.RELPATH_ISP_CONFIG_FILE))
              .values()
              .iterator()
              .next();
      return this;
    }

    public @Nonnull Builder setLayer1TopologyBytes(@Nonnull byte[] layer1TopologyBytes) {
      _layer1TopologyBytes = layer1TopologyBytes;
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

    public @Nonnull Builder setRuntimeDataBytes(@Nonnull byte[] runtimeDataBytes) {
      _runtimeDataBytes = runtimeDataBytes;
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

    public Builder setSonicConfigFiles(String testrigResourcePrefix, Iterable<String> filenames) {
      _sonicConfigBytes =
          readTestrigResources(
              testrigResourcePrefix, BfConsts.RELPATH_SONIC_CONFIGS_DIR, filenames);
      return this;
    }

    public @Nonnull Builder setExternalBgpAnnouncements(String testrigResourcePrefix) {
      _externalBgpAnnouncementsBytes =
          readTestrigResources(
                  testrigResourcePrefix,
                  null,
                  ImmutableList.of(BfConsts.RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS))
              .values()
              .iterator()
              .next();
      return this;
    }

    /**
     * Sets conversion context to be used during conversion. Note that this has no effect when the
     * snapshot input text contains information that would populate conversion context.
     */
    public @Nonnull Builder setConversionContext(ConversionContext conversionContext) {
      _conversionContext = conversionContext;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Load a {@link TestrigText} from the specified directory. */
  public static TestrigText loadTestrig(String dir) throws IOException {
    TestrigText.Builder builder = TestrigText.builder();

    Path snapshotDir = Paths.get(dir);
    checkArgument(snapshotDir.toFile().exists(), "%s does not exist.", dir);
    checkArgument(snapshotDir.toFile().isDirectory(), "%s is not a directory.", dir);

    // layer 1 topology
    Path l1TopologyPath = snapshotDir.resolve("batfish").resolve("layer1_topology.json");
    if (l1TopologyPath.toFile().exists()) {
      builder.setLayer1TopologyBytes(Files.readAllBytes(l1TopologyPath));
    }

    // isp config
    Path ispConfigPath = snapshotDir.resolve("batfish").resolve("isp_config.json");
    if (ispConfigPath.toFile().exists()) {
      builder.setIspConfigBytes(Files.readAllBytes(ispConfigPath));
    }

    // runtime data
    Path runtimeDataPath = snapshotDir.resolve("batfish").resolve("runtime_data.json");
    if (runtimeDataPath.toFile().exists()) {
      builder.setRuntimeDataBytes(Files.readAllBytes(runtimeDataPath));
    }

    // configs
    Path configsDir = snapshotDir.resolve("configs");
    checkArgument(configsDir.toFile().exists(), "%s does not exist.", configsDir);
    checkArgument(configsDir.toFile().isDirectory(), "%s is not a directory.", configsDir);
    builder.setConfigurationText(
        Arrays.stream(configsDir.toFile().listFiles())
            .collect(
                ImmutableMap.toImmutableMap(
                    File::getName,
                    f -> {
                      try {
                        return fileText(f);
                      } catch (IOException e) {
                        throw new RuntimeException(e);
                      }
                    })));

    // hosts
    File hostsDir = snapshotDir.resolve("hosts").toFile();
    if (hostsDir.exists()) {
      builder.setHostsBytes(
          Arrays.stream(hostsDir.listFiles())
              .collect(
                  ImmutableMap.toImmutableMap(
                      File::getName,
                      f -> {
                        try {
                          return Files.readAllBytes(f.toPath());
                        } catch (IOException e) {
                          throw new RuntimeException(e);
                        }
                      })));
    }

    return builder.build();
  }

  private static String fileText(File f) throws IOException {
    return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
  }

  private Map<String, byte[]> _awsBytes;
  private Map<String, byte[]> _azureBytes;
  private Map<String, byte[]> _bgpTablesBytes;
  private Map<String, byte[]> _ciscoAciConfigBytes;
  private Map<String, byte[]> _checkpointMgmtBytes;
  private Map<String, byte[]> _configurationBytes;
  private ConversionContext _conversionContext;
  private byte[] _externalBgpAnnouncementBytes;
  private Map<String, byte[]> _hostsBytes;
  private Map<String, byte[]> _iptablesFilesBytes;
  private byte[] _ispConfigBytes;
  private byte[] _layer1TopologyBytes;
  private Map<String, byte[]> _routingTablesBytes;
  private byte[] _runtimeDataBytes;
  private Map<String, byte[]> _sonicConfigBytes;

  public Map<String, byte[]> getAwsBytes() {
    return _awsBytes;
  }

  public Map<String, byte[]> getAzureBytes() {
    return _azureBytes;
  }

  public Map<String, byte[]> getBgpTablesBytes() {
    return _bgpTablesBytes;
  }

  public Map<String, byte[]> getCiscoAciConfigBytes() {
    return _ciscoAciConfigBytes;
  }

  public Map<String, byte[]> getCheckpointMgmtBytes() {
    return _checkpointMgmtBytes;
  }

  public Map<String, byte[]> getConfigurationBytes() {
    return _configurationBytes;
  }

  public ConversionContext getConversionContext() {
    return _conversionContext;
  }

  public @Nullable byte[] getExternalBgpAnnouncementBytes() {
    return _externalBgpAnnouncementBytes;
  }

  public Map<String, byte[]> getHostsBytes() {
    return _hostsBytes;
  }

  public Map<String, byte[]> getIptablesFilesBytes() {
    return _iptablesFilesBytes;
  }

  public @Nullable byte[] getIspConfigBytes() {
    return _ispConfigBytes;
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

  public Map<String, byte[]> getSonicConfigBytes() {
    return _sonicConfigBytes;
  }

  public void setAwsBytes(Map<String, byte[]> awsBytes) {
    _awsBytes = awsBytes;
  }

  public void setAzureBytes(Map<String, byte[]> azureBytes) {
    _azureBytes = azureBytes;
  }

  public void setBgpTablesBytes(Map<String, byte[]> bgpTablesText) {
    _bgpTablesBytes = bgpTablesText;
  }

  public void setCiscoAciConfigBytes(Map<String, byte[]> ciscoAciConfigBytes) {
    _ciscoAciConfigBytes = ciscoAciConfigBytes;
  }

  public void setCheckpointMgmtBytes(Map<String, byte[]> checkpointMgmtBytes) {
    _checkpointMgmtBytes = checkpointMgmtBytes;
  }

  public void setConfigurationBytes(Map<String, byte[]> configurationText) {
    _configurationBytes = configurationText;
  }

  public void setConversionContext(ConversionContext conversionContext) {
    _conversionContext = conversionContext;
  }

  public void setExternalBgpAnnouncements(byte[] externalBgpAnnouncementsBytes) {
    _externalBgpAnnouncementBytes = externalBgpAnnouncementsBytes;
  }

  public void setHostsBytes(Map<String, byte[]> hostsBytes) {
    _hostsBytes = hostsBytes;
  }

  public void setIptablesFilesBytes(Map<String, byte[]> iptablesFilesBytes) {
    _iptablesFilesBytes = iptablesFilesBytes;
  }

  public void setIspConfigBytes(@Nullable byte[] ispConfigBytes) {
    _ispConfigBytes = ispConfigBytes;
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

  public void setSonicConfigBytes(Map<String, byte[]> sonicConfigBytes) {
    _sonicConfigBytes = sonicConfigBytes;
  }
}
