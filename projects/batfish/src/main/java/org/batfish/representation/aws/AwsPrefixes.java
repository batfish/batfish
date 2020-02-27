package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Prefix;

/**
 * A class to provide access to AWS prefixes. It reads the prefix list from a JSON file that is
 * checked in as a resource after fetching https://ip-ranges.amazonaws.com/ip-ranges.json on Feb 26,
 * 2020.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
class AwsPrefixes {

  private static final String PREFIXES_FILE = "org/batfish/representation/aws/ip-ranges.json";

  private static final String PROP_PREFIXES = "prefixes";

  private static final String PROP_IP_PREFIX = "ip_prefix";

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class AwsPrefix {

    private final Prefix _prefix;

    @JsonCreator
    private static AwsPrefix create(@Nullable @JsonProperty(PROP_IP_PREFIX) Prefix prefix) {
      checkArgument(prefix != null, "No prefix found in JSON object for AwsPrefix");
      return new AwsPrefix(prefix);
    }

    AwsPrefix(Prefix prefix) {
      _prefix = prefix;
    }

    public Prefix getPrefix() {
      return _prefix;
    }
  }

  private static AwsPrefixes INSTANCE;

  static {
    try {
      INSTANCE =
          BatfishObjectMapper.mapper()
              .readValue(CommonUtil.readResource(PREFIXES_FILE), AwsPrefixes.class);
    } catch (IOException e) {
      INSTANCE = new AwsPrefixes(ImmutableList.of());
    }
  }

  private final List<AwsPrefix> _awsPrefixes;

  @JsonCreator
  private static AwsPrefixes create(
      @Nullable @JsonProperty(PROP_PREFIXES) List<AwsPrefix> prefixes) {
    checkArgument(prefixes != null, "List of prefixes not found in JSON object for AwsPrefixes");
    return new AwsPrefixes(prefixes);
  }

  AwsPrefixes(List<AwsPrefix> prefixes) {
    _awsPrefixes = prefixes;
  }

  public static List<Prefix> getPrefixes() {
    return INSTANCE._awsPrefixes.stream()
        .map(AwsPrefix::getPrefix)
        .collect(ImmutableList.toImmutableList());
  }
}
