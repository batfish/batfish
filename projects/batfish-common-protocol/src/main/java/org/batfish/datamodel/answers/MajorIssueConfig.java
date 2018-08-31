package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;

/** Represents the configuration of a major issue type */
public class MajorIssueConfig {

  private static final String PROP_MAJOR_ISSUE = "majorIssue";
  private static final String PROP_MINOR_ISSUE_CONFIGS = "minorIssueConfigs";

  @Nonnull private final String _majorIssue;
  @Nonnull private Set<MinorIssueConfig> _minorIssueConfigs;

  @JsonCreator
  public MajorIssueConfig(
      @JsonProperty(PROP_MAJOR_ISSUE) String majorIssue,
      @JsonProperty(PROP_MINOR_ISSUE_CONFIGS) List<MinorIssueConfig> minorIssueConfigs) {
    checkArgument(majorIssue != null, "'majorIssue' cannot be null");
    _majorIssue = majorIssue;
    _minorIssueConfigs = ImmutableSet.copyOf(firstNonNull(minorIssueConfigs, ImmutableSet.of()));
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MajorIssueConfig)) {
      return false;
    }
    return Objects.equals(_majorIssue, ((MajorIssueConfig) o)._majorIssue)
        && Objects.equals(_minorIssueConfigs, ((MajorIssueConfig) o)._minorIssueConfigs);
  }

  /** Deletes the specified {@code minorIssue} */
  public boolean delMinorIssueConfig(String minorIssue) {
    List<MinorIssueConfig> tmpList = new LinkedList<MinorIssueConfig>(_minorIssueConfigs);
    boolean result = tmpList.removeIf(i -> i.getMinor().equals(minorIssue));
    _minorIssueConfigs = ImmutableSet.copyOf(tmpList);
    return result;
  }

  @JsonProperty(PROP_MAJOR_ISSUE)
  @Nonnull
  public String getMajorIssue() {
    return _majorIssue;
  }

  /** Returns the MinorIssueConfig for the specified {@code minorIssue} */
  public Optional<MinorIssueConfig> getMinorIssueConfig(String minorIssue) {
    return _minorIssueConfigs.stream().filter(m -> m.getMinor().equals(minorIssue)).findAny();
  }

  @JsonProperty(PROP_MINOR_ISSUE_CONFIGS)
  @Nonnull
  public Set<MinorIssueConfig> getMinorIssueConfigs() {
    return _minorIssueConfigs;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_majorIssue, _minorIssueConfigs);
  }

  /** Insert a {@link MinorIssueConfig} in this object */
  public void put(MinorIssueConfig config) {
    List<MinorIssueConfig> tmpList = new LinkedList<MinorIssueConfig>(_minorIssueConfigs);
    tmpList.removeIf(i -> i.getMinor().equals(config.getMinor()));
    tmpList.add(config);
    _minorIssueConfigs = ImmutableSet.copyOf(tmpList);
  }

  /**
   * Reads the {@link MajorIssueConfig} object from the provided Path. If the path does not exist,
   * initializes a new object.
   *
   * @param dataPath The Path to read from
   * @return The read data
   * @throws IOException If file exists but its contents could not be cast to {@link
   *     MajorIssueConfig}
   */
  public static MajorIssueConfig read(Path dataPath, String majorIssue) throws IOException {
    if (Files.exists(dataPath)) {
      return BatfishObjectMapper.mapper()
          .readValue(CommonUtil.readFile(dataPath), MajorIssueConfig.class);
    } else {
      return new MajorIssueConfig(majorIssue, null);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(MajorIssueConfig.class)
        .add(PROP_MAJOR_ISSUE, _majorIssue)
        .add(PROP_MINOR_ISSUE_CONFIGS, _minorIssueConfigs)
        .toString();
  }
}
