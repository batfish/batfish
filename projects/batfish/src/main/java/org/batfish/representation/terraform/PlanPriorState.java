package org.batfish.representation.terraform;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ARN;
import static org.batfish.representation.terraform.Constants.JSON_KEY_MODE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_NAME;
import static org.batfish.representation.terraform.Constants.JSON_KEY_OWNER_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_PROVIDER_NAME;
import static org.batfish.representation.terraform.Constants.JSON_KEY_RESOURCES;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ROOT_MODULE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_TYPE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_VALUES;
import static org.batfish.representation.terraform.Utils.checkMandatoryAttributes;
import static org.batfish.representation.terraform.Utils.getArnAcccount;
import static org.batfish.representation.terraform.Utils.getArnPartition;
import static org.batfish.representation.terraform.Utils.getArnRegion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.representation.terraform.CommonResourceProperties.Mode;

/** Represents a generic Terraform resource that appears in a state or a plan file */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
class PlanPriorState implements Serializable {

  /** A resource that appears in the prior state section of plan file */
  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static class Resource extends TerraformResource {

    @Nonnull private final Map<String, Object> _values;

    Resource(CommonResourceProperties common, Map<String, Object> values) {
      super(common);
      _values = values;
    }

    @Nonnull
    public Map<String, Object> getValues() {
      return _values;
    }
  }

  @JsonCreator
  private static PlanPriorState fromJson(
      @Nullable @JsonProperty(JSON_KEY_VALUES) Map<String, Object> values) {
    checkArgument(values != null, "'values' cannot be null for a terraform prior_state");
    return new PlanPriorState(extractResources(values));
  }

  @SuppressWarnings("unchecked")
  private static List<Resource> extractResources(Map<String, Object> values) {
    checkArgument(
        values.containsKey(JSON_KEY_ROOT_MODULE),
        "prior_state->values in Terraform plan must have 'root_module'");
    Map<String, Object> rootModule = (Map<String, Object>) values.get(JSON_KEY_ROOT_MODULE);
    checkArgument(
        rootModule.containsKey(JSON_KEY_RESOURCES),
        "prior_state->values->root_module in Terraform plan must have 'resources'");
    List<Map<String, Object>> resources =
        (List<Map<String, Object>>) rootModule.get(JSON_KEY_RESOURCES);
    return resources.stream()
        .map(PlanPriorState::extractResource)
        .collect(ImmutableList.toImmutableList());
  }

  private static final List<String> MANDATORY_ATTRIBUTES =
      ImmutableList.of(
          JSON_KEY_MODE, JSON_KEY_TYPE, JSON_KEY_NAME, JSON_KEY_PROVIDER_NAME, JSON_KEY_VALUES);

  @SuppressWarnings("unchecked")
  private static Resource extractResource(Map<String, Object> resource) {
    checkMandatoryAttributes(resource, MANDATORY_ATTRIBUTES, "resource in Terraform prior_state");
    return new Resource(
        new CommonResourceProperties(
            Mode.fromString(resource.get(JSON_KEY_MODE).toString()),
            resource.get(JSON_KEY_TYPE).toString(),
            resource.get(JSON_KEY_NAME).toString(),
            "provider." + resource.get(JSON_KEY_PROVIDER_NAME).toString()),
        (Map<String, Object>) resource.get(JSON_KEY_VALUES));
  }

  @Nonnull private final List<Resource> _resources;

  PlanPriorState(List<Resource> resources) {
    _resources = resources;
  }

  public Map<String, AwsProviderInfo> mineAwsProviderInfo(Warnings warnings) {
    Map<String, AwsProviderInfo> awsProviderInfo = new HashMap<>();
    _resources.forEach(
        r -> {
          if (!r.IsAwsResource() || r.getCommon().getMode() == Mode.DATA) {
            return;
          }
          r.getValues()
              .forEach(
                  (k, v) -> {
                    if (k.equals(JSON_KEY_ARN)) {
                      AwsProviderInfo info =
                          new AwsProviderInfo(
                              getArnPartition(v.toString()),
                              getArnAcccount(v.toString()),
                              getArnRegion(v.toString()));
                      updateProviderMap(
                          awsProviderInfo, r.getCommon().getProvider(), info, warnings);
                    } else if (k.equals(JSON_KEY_OWNER_ID)) {
                      AwsProviderInfo info = new AwsProviderInfo(null, v.toString(), null);
                      updateProviderMap(
                          awsProviderInfo, r.getCommon().getProvider(), info, warnings);
                    }
                  });
        });
    return ImmutableMap.copyOf(awsProviderInfo);
  }

  private static void updateProviderMap(
      Map<String, AwsProviderInfo> awsProviderInfo,
      String provider,
      AwsProviderInfo info,
      Warnings warnings) {
    if (!awsProviderInfo.containsKey(provider)) {
      awsProviderInfo.put(provider, info);
      return;
    }
    AwsProviderInfo oldInfo = awsProviderInfo.get(provider);
    if (oldInfo.getPartition() != null) {
      if (info.getPartition() != null && !oldInfo.getPartition().equals(info.getPartition())) {
        warnings.redFlag(
            String.format(
                "Inconsistent partition for %s: %s and %s",
                provider, oldInfo.getPartition(), info.getPartition()));
      }
    } else {
      awsProviderInfo.put(
          provider,
          new AwsProviderInfo(
              awsProviderInfo.get(provider).getPartition(),
              info.getAccount(),
              awsProviderInfo.get(provider).getAccount()));
    }
    if (oldInfo.getAccount() != null) {
      if (info.getAccount() != null && !oldInfo.getAccount().equals(info.getAccount())) {
        warnings.redFlag(
            String.format(
                "Inconsistent account id for %s: %s and %s",
                provider, oldInfo.getAccount(), info.getAccount()));
      }
    } else {
      awsProviderInfo.put(
          provider,
          new AwsProviderInfo(
              awsProviderInfo.get(provider).getPartition(),
              info.getAccount(),
              awsProviderInfo.get(provider).getAccount()));
    }
    if (oldInfo.getRegion() != null) {
      if (info.getRegion() != null && !oldInfo.getRegion().equals(info.getRegion())) {
        warnings.redFlag(
            String.format(
                "Inconsistent region for %s: %s and %s",
                provider, oldInfo.getRegion(), info.getRegion()));
      }
    } else {
      awsProviderInfo.put(
          provider,
          new AwsProviderInfo(
              awsProviderInfo.get(provider).getPartition(),
              awsProviderInfo.get(provider).getAccount(),
              info.getRegion()));
    }
  }
}
