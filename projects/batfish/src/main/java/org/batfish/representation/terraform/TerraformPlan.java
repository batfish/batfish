package org.batfish.representation.terraform;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ARN;
import static org.batfish.representation.terraform.Constants.JSON_KEY_CONFIGURATION;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_OWNER_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_PRIOR_STATE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_RESOURCE_CHANGES;
import static org.batfish.representation.terraform.Constants.JSON_KEY_VARIABLES;
import static org.batfish.representation.terraform.Utils.makeArn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.representation.terraform.PlanConfigurationValue.References;
import org.batfish.representation.terraform.PlanConfigurationValue.StringValue;
import org.batfish.representation.terraform.PlanResourceChange.Change;

/** Represents information in Terraform plan files */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public class TerraformPlan implements TerraformFileContent {

  @JsonCreator
  private static TerraformPlan fromJson(
      @Nullable @JsonProperty(JSON_KEY_VARIABLES) Map<String, Map<String, String>> variableObjects,
      @Nullable @JsonProperty(JSON_KEY_CONFIGURATION) PlanConfiguration configuration,
      @Nullable @JsonProperty(JSON_KEY_RESOURCE_CHANGES) List<PlanResourceChange> resourceChanges,
      @Nullable @JsonProperty(JSON_KEY_PRIOR_STATE) PlanPriorState priorState) {
    checkArgument(variableObjects != null, "Map of 'variables' not found in Terraform plan");
    checkArgument(configuration != null, "'configuration' not found in Terraform plan");
    checkArgument(
        resourceChanges != null, "List of 'resource_changes' not found in Terraform plan");
    checkArgument(priorState != null, "'prior_state' not found in Terraform plan");

    // flatten the variable map from varname -> {"value" : varvalue} to varname -> varvalue
    Map<String, String> variables =
        variableObjects.entrySet().stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Entry::getKey, e -> e.getValue().get("value").toString()));
    return new TerraformPlan(variables, configuration, resourceChanges, priorState);
  }

  @Nonnull private final Map<String, String> _variables;
  @Nonnull private final PlanConfiguration _configuration;
  @Nonnull private final List<PlanResourceChange> _resourceChanges;
  @Nonnull private final PlanPriorState _priorState;

  TerraformPlan(
      Map<String, String> variables,
      PlanConfiguration configuration,
      List<PlanResourceChange> resourceChanges,
      PlanPriorState priorState) {
    _variables = variables;
    _configuration = configuration;
    _resourceChanges = ImmutableList.copyOf(resourceChanges);
    _priorState = priorState;
  }

  @Nullable
  private TerraformResource derive(
      PlanResourceChange resourceChange,
      Map<String, AwsProviderInfo> awsProviderInfo,
      Warnings warnings) {
    try {
      return TerraformResource.create(
          resourceChange.getCommon(), resourceChange.getChange().getAfter(), warnings);
    } catch (Exception e) {
      warnings.redFlag(
          String.format(
              "Could not derive resource %s; %s",
              resourceChange.getCommon().getName(), e.getMessage()));
      return null;
    }
  }

  @Override
  public List<TerraformResource> toConvertedResources(Warnings warnings) {
    Map<String, AwsProviderInfo> priorStateProviders = _priorState.mineAwsProviderInfo(warnings);
    Map<String, AwsProviderInfo> mergedProviders =
        mergeAwsProviderInfo(priorStateProviders, _configuration.getProviders(), _variables);

    List<PlanResourceChange> idFilledResources =
        _resourceChanges.stream()
            .map(rc -> fillUnknownIds(rc, mergedProviders, warnings))
            .collect(ImmutableList.toImmutableList());

    List<PlanResourceChange> refFilledResources =
        idFilledResources.stream()
            .map(
                rc ->
                    fillUnknownRefs(
                        rc, _configuration.getResources(), _variables, idFilledResources, warnings))
            .collect(ImmutableList.toImmutableList());

    return refFilledResources.stream()
        .map(rc -> derive(rc, mergedProviders, warnings))
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
  }

  private static PlanResourceChange fillUnknownIds(
      PlanResourceChange resourceChange,
      Map<String, AwsProviderInfo> awsProviders,
      Warnings warnings) {

    CommonResourceProperties common = resourceChange.getCommon();
    Change originalChange = resourceChange.getChange();
    Map<String, Object> knownAttributes = originalChange.getAfter();
    Set<String> unknownAttributes = originalChange.getAfterUnknown().keySet();

    if (unknownAttributes.isEmpty()) {
      return resourceChange;
    }

    if (unknownAttributes.contains(JSON_KEY_ID)) {
      // lets make up q unique id
      String id = String.format("%s-%s", common.getType(), common.getName());
      knownAttributes.put(JSON_KEY_ID, id);
    }
    if (unknownAttributes.contains(JSON_KEY_ARN)) {
      if (!awsProviders.containsKey(common.getProvider())) {
        warnings.redFlag(
            String.format(
                "Provider for %s not found. Skipping.", resourceChange.getCommon().getName()));
      } else {
        String id =
            knownAttributes.containsKey(JSON_KEY_ID)
                ? knownAttributes.get(JSON_KEY_ID).toString()
                : String.format("%s-%s", common.getType(), common.getName());
        // TODO: create a map of type (aws_instance) to service name (ec2)
        String arn = makeArn(awsProviders.get(common.getProvider()), "aws", common.getType(), id);
        knownAttributes.put(JSON_KEY_ARN, arn);
      }
    }
    if (unknownAttributes.contains(JSON_KEY_OWNER_ID)) {
      if (!awsProviders.containsKey(common.getProvider())) {
        warnings.redFlag(
            String.format(
                "Provider for %s not found. Skipping.", resourceChange.getCommon().getName()));
      } else {
        knownAttributes.put(JSON_KEY_OWNER_ID, awsProviders.get(common.getProvider()).getAccount());
      }
    }
    return new PlanResourceChange(
        common,
        new Change(
            originalChange.getActions(),
            knownAttributes,
            unknownAttributes.stream()
                .filter(
                    attr -> !knownAttributes.containsKey(attr)) // skip those just added to known
                .collect(
                    ImmutableMap.toImmutableMap(
                        attr -> attr, attr -> originalChange.getAfterUnknown().get(attr)))));
  }

  private static PlanResourceChange fillUnknownRefs(
      PlanResourceChange resourceChange,
      List<PlanConfiguration.Resource> configResources,
      Map<String, String> variables,
      List<PlanResourceChange> allResources,
      Warnings warnings) {

    CommonResourceProperties common = resourceChange.getCommon();
    Change originalChange = resourceChange.getChange();
    Map<String, Object> knownAttributes = originalChange.getAfter();
    Set<String> unknownAttributes = originalChange.getAfterUnknown().keySet();

    if (unknownAttributes.isEmpty()) {
      return resourceChange;
    }

    List<PlanConfiguration.Resource> matchingResources =
        configResources.stream()
            .filter(r -> common.equals(r.getCommon()))
            .collect(ImmutableList.toImmutableList());
    if (matchingResources.size() != 1) {
      warnings.redFlag(
          String.format(
              "Found %d matching resources in configuration for %s. Expected 1.",
              matchingResources.size(), common));
      return resourceChange;
    }

    PlanConfiguration.Resource configResource = matchingResources.get(0);
    for (String attribute : unknownAttributes) {
      if (!configResource.getExpressions().containsKey(attribute)) {
        continue;
      }
      PlanConfigurationValue configValue = configResource.getExpressions().get(attribute);
      if (configValue instanceof PlanConfigurationValue.StringValue) {
        knownAttributes.put(attribute, ((StringValue) configValue).getValue());
      } else if (configValue instanceof PlanConfigurationValue.References) {
        // we make a list out of all attributes .... hmmmm
        knownAttributes.put(
            attribute,
            ((References) configValue)
                .getValues().stream()
                    .map(ref -> resolveReference(ref, variables, allResources, warnings))
                    .filter(Objects::nonNull)
                    .collect(ImmutableList.toImmutableList()));
      }
    }

    return new PlanResourceChange(
        common,
        new Change(
            originalChange.getActions(),
            knownAttributes,
            unknownAttributes.stream()
                .filter(
                    attr -> !knownAttributes.containsKey(attr)) // skip those just added to known
                .collect(
                    ImmutableMap.toImmutableMap(
                        attr -> attr, attr -> originalChange.getAfterUnknown().get(attr)))));
  }

  @Nullable
  private static String resolveReference(
      String ref,
      Map<String, String> variables,
      List<PlanResourceChange> resourceChanges,
      Warnings warnings) {
    if (ref.startsWith("var.")) {
      String varName = variables.get(ref.substring(4));
      if (variables.containsKey(varName)) {
        return variables.get(varName);
      }
      warnings.redFlag(
          String.format("Value of variable %s needed for a reference not found", varName));
      return null;
    }
    if (ref.startsWith("aws_")) { // it is a resource
      List<PlanResourceChange> matchingResources =
          resourceChanges.stream()
              .filter(
                  r ->
                      ref.equals(
                          String.format("%s.%s", r.getCommon().getType(), r.getCommon().getName())))
              .collect(ImmutableList.toImmutableList());
      if (matchingResources.size() != 1) {
        warnings.redFlag(
            String.format(
                "Found %d matching resources for reference %s. Expected 1.",
                matchingResources.size(), ref));
        return null;
      }
      Map<String, Object> matchingResourceAttributes =
          matchingResources.get(0).getChange().getAfter();
      // TODO: we blindly assume that the reference is to the id of the resource
      if (matchingResourceAttributes.containsKey(JSON_KEY_ID)) {
        return matchingResourceAttributes.get(JSON_KEY_ID).toString();
      } else {
        warnings.redFlag(String.format("Id not found for referenced resources %s", ref));
        return null;
      }
    }
    warnings.redFlag(String.format("Unknown reference type for reference %s", ref));
    return null;
  }

  private Map<String, AwsProviderInfo> mergeAwsProviderInfo(
      Map<String, AwsProviderInfo> priorStateProviders,
      Map<String, PlanProvider> providers,
      Map<String, String> variables) {
    // TODO -- complete the picture
    // make sure that each provider has all fields filled out
    return priorStateProviders;
  }
}
