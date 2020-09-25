package org.batfish.representation.terraform;

import static org.batfish.representation.terraform.Constants.JSON_KEY_TAGS;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
class Utils {

  public static void checkMandatoryAttributes(
      Map<String, Object> resourceAttributes,
      List<String> mandatoryAttributes,
      String resourceDescription) {
    for (String key : mandatoryAttributes) {
      if (!resourceAttributes.containsKey(key)) {
        throw new IllegalArgumentException(
            String.format("Attribute '%s' not found for %s", key, resourceDescription));
      }
    }
  }

  //  https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html
  //  arn:partition:service:region:account-id:resource-id
  //  arn:partition:service:region:account-id:resource-type/resource-id
  //  arn:partition:service:region:account-id:resource-type:resource-id

  private static String[] splitArn(String arn) {
    String[] components = arn.split(":");
    if (components.length < 6) {
      throw new IllegalArgumentException("Invalid AWS ARN " + arn);
    }
    return components;
  }

  @Nonnull
  public static String getArnPartition(String arn) {
    return splitArn(arn)[1];
  }

  @Nonnull
  public static String getArnRegion(String arn) {
    return splitArn(arn)[3];
  }

  @Nonnull
  public static String getArnAcccount(String arn) {
    return splitArn(arn)[4];
  }

  @Nonnull
  public static String makeArn(
      AwsProviderInfo awsProviderInfo, String service, String resourceType, String resourceId) {
    return String.format(
        "arn:%s:%s:%s:%s:%s/%s",
        awsProviderInfo.getPartition(),
        service,
        awsProviderInfo.getRegion(),
        awsProviderInfo.getAccount(),
        resourceType,
        resourceId);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, String> getAwsResourceTags(Map<String, Object> attributes) {
    return (attributes.containsKey(JSON_KEY_TAGS))
        ? ImmutableMap.copyOf((Map<String, String>) attributes.get(JSON_KEY_TAGS))
        : ImmutableMap.of();
  }

  public static Optional<String> getOptionalString(Map<String, Object> attributes, String key) {
    return attributes.containsKey(key)
        ? Optional.of(attributes.get(key).toString())
        : Optional.empty();
  }

  @SuppressWarnings("unchecked")
  public static List<String> getStrings(Map<String, Object> attributes, String key) {
    return ImmutableList.copyOf((List<String>) attributes.get(key));
  }
}
