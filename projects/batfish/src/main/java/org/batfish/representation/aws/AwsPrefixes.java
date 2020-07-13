package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Prefix;

/**
 * A class to provide access to AWS prefixes. It reads the prefix list from a JSON file that is
 * checked in as a resource after fetching https://ip-ranges.amazonaws.com/ip-ranges.json on Feb 26,
 * 2020.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public class AwsPrefixes {

  private static final String PREFIXES_FILE = "org/batfish/representation/aws/ip-ranges.json";

  private static final String PROP_PREFIXES = "prefixes";

  private static final String PROP_IP_PREFIX = "ip_prefix";

  private static final String PROP_SERVICE = "service";

  static final String SERVICE_AMAZON = "AMAZON";

  static final String SERVICE_EC2 = "EC2";

  static final String SERVICE_S3 = "S3";

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static class AwsPrefix {

    @Nonnull private final Prefix _prefix;

    @Nonnull private final String _serviceName;

    @JsonCreator
    private static AwsPrefix create(
        @Nullable @JsonProperty(PROP_IP_PREFIX) Prefix prefix,
        @Nullable @JsonProperty(PROP_SERVICE) String serviceName) {
      checkArgument(prefix != null, "No prefix found in JSON object for AwsPrefix");
      checkArgument(serviceName != null, "Service name not found in JSON object for AwsPrefix");
      return new AwsPrefix(prefix, serviceName);
    }

    AwsPrefix(Prefix prefix, String serviceName) {
      _prefix = prefix;
      _serviceName = serviceName;
    }

    @Nonnull
    public Prefix getPrefix() {
      return _prefix;
    }

    @Nonnull
    public String getServiceName() {
      return _serviceName;
    }
  }

  private static AwsPrefixes INSTANCE;

  static {
    try {
      INSTANCE =
          BatfishObjectMapper.mapper()
              .readValue(readResource(PREFIXES_FILE, UTF_8), AwsPrefixes.class);
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

  public static List<Prefix> getPrefixes(String serviceName) {
    return INSTANCE._awsPrefixes.stream()
        .filter(awsPrefix -> awsPrefix.getServiceName().equals(serviceName))
        .map(AwsPrefix::getPrefix)
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns prefixes for (first party) AWS services.
   *
   * <p>Online documentation
   * (https://docs.aws.amazon.com/general/latest/gr/aws-ip-ranges.html#aws-ip-egress-control)
   * suggests that this is AMAZON prefixes minus EC2 prefixes. But in June 2020 data, there is at
   * least one prefix (3.5.16.0/21) that is listed as both EC2 and S3. This function deems such
   * prefixes to be AWS prefixes because the counter-example was also listed in the S3 prefix list
   * (which is more likely to be reliable) and it is a less bad error to make than the other way
   * around. It will interfere with analysis only when an address in conflicting range appears in
   * the snapshot.
   */
  public static Set<Prefix> getAwsServicesPrefixes() {
    return getAwsServicesPrefixes(INSTANCE._awsPrefixes);
  }

  public static List<Prefix> getAwsS3Prefixes() {
    return getPrefixes(SERVICE_S3);
  }

  @VisibleForTesting
  static Set<Prefix> getAwsServicesPrefixes(List<AwsPrefix> allPrefixes) {
    Set<Prefix> exclusivelyEc2Prefixes =
        allPrefixes.stream()
            .filter(
                prefix ->
                    // is an EC2 prefix and is not listed for any service other than AMAZON
                    prefix.getServiceName().equals(SERVICE_EC2)
                        && allPrefixes.stream()
                            .noneMatch(
                                otherPrefix ->
                                    otherPrefix.getPrefix().equals(prefix.getPrefix())
                                        && !otherPrefix.getServiceName().equals(SERVICE_AMAZON)
                                        && !otherPrefix.getServiceName().equals(SERVICE_EC2)))
            .map(AwsPrefix::getPrefix)
            .collect(ImmutableSet.toImmutableSet());

    return allPrefixes.stream()
        .filter(
            prefix ->
                prefix.getServiceName().equals(SERVICE_AMAZON)
                    && !exclusivelyEc2Prefixes.contains(prefix.getPrefix()))
        .map(AwsPrefix::getPrefix)
        .collect(ImmutableSet.toImmutableSet());
  }
}
