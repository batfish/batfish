package org.batfish.representation.terraform;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.terraform.Constants.JSON_KEY_EXPRESSIONS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_MODE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_NAME;
import static org.batfish.representation.terraform.Constants.JSON_KEY_PROVIDER_CONFIG;
import static org.batfish.representation.terraform.Constants.JSON_KEY_PROVIDER_CONFIG_KEY;
import static org.batfish.representation.terraform.Constants.JSON_KEY_RESOURCES;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ROOT_MODULE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_TYPE;
import static org.batfish.representation.terraform.Utils.checkMandatoryAttributes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.representation.terraform.CommonResourceProperties.Mode;

/** Represents information in Terraform plan files */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public class PlanConfiguration implements Serializable {

  /** A resource that appears in the configuration section of the plan file */
  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static class Resource extends TerraformResource {

    @Nonnull private final Map<String, PlanConfigurationValue> _expressions;

    Resource(CommonResourceProperties common, Map<String, PlanConfigurationValue> expressions) {
      super(common);
      _expressions = expressions;
    }

    @Nonnull
    public Map<String, PlanConfigurationValue> getExpressions() {
      return _expressions;
    }
  }

  @JsonCreator
  private static PlanConfiguration fromJson(
      @Nullable @JsonProperty(JSON_KEY_PROVIDER_CONFIG)
          Map<String, Map<String, Object>> providerConfig,
      @Nullable @JsonProperty(JSON_KEY_ROOT_MODULE) Map<String, Object> rootModule) {
    checkArgument(
        providerConfig != null, "'provider_config' not found in Terraform plan configuration");
    checkArgument(rootModule != null, "'root_module' not found in Terraform plan configuration");
    checkArgument(
        rootModule.containsKey(JSON_KEY_RESOURCES),
        "'resources' not found in root_module of Terraform plan configuration");
    Map<String, PlanProvider> providers =
        providerConfig.entrySet().stream()
            .collect(
                ImmutableMap.toImmutableMap(Entry::getKey, e -> new PlanProvider(e.getValue())));
    List<Resource> resources = extractResources(rootModule.get(JSON_KEY_RESOURCES));
    return new PlanConfiguration(providers, resources);
  }

  @SuppressWarnings("unchecked")
  private static List<Resource> extractResources(Object resources) {
    return ((List<Map<String, Object>>) resources)
        .stream().map(PlanConfiguration::extractResource).collect(ImmutableList.toImmutableList());
  }

  private static final List<String> MANDATORY_ATTRIBUTES =
      ImmutableList.of(JSON_KEY_MODE, JSON_KEY_TYPE, JSON_KEY_NAME, JSON_KEY_PROVIDER_CONFIG_KEY);

  @SuppressWarnings("unchecked")
  private static Resource extractResource(Map<String, Object> resource) {
    checkMandatoryAttributes(resource, MANDATORY_ATTRIBUTES, "resource in Terraform configuration");
    CommonResourceProperties common =
        new CommonResourceProperties(
            Mode.fromString(resource.get(JSON_KEY_MODE).toString()),
            resource.get(JSON_KEY_TYPE).toString(),
            resource.get(JSON_KEY_NAME).toString(),
            "provider." + resource.get(JSON_KEY_PROVIDER_CONFIG_KEY).toString());
    Map<String, PlanConfigurationValue> expressions =
        ((Map<String, Object>) firstNonNull(resource.get(JSON_KEY_EXPRESSIONS), ImmutableMap.of()))
            .entrySet().stream()
                .filter(
                    e ->
                        e.getValue() instanceof Map
                            && !((Map<String, Object>) e.getValue()).isEmpty())
                .collect(
                    ImmutableMap.toImmutableMap(
                        Entry::getKey, e -> PlanConfigurationValue.create(e.getValue())));
    return new Resource(common, expressions);
  }

  @Nonnull private final Map<String, PlanProvider> _providers;
  @Nonnull private final List<Resource> _resources;

  PlanConfiguration(Map<String, PlanProvider> providers, List<Resource> resources) {
    _providers = ImmutableMap.copyOf(providers);
    _resources = resources;
  }

  //  public void resolveProvidersUsingVariables(Map<String, String> variables, Warnings warnings) {
  //    _providers
  //        .values()
  //        .forEach(planProvider -> planProvider.resolveUsingVariables(variables, warnings));
  //  }

  @Nonnull
  public Map<String, PlanProvider> getProviders() {
    return _providers;
  }

  @Nonnull
  public List<Resource> getResources() {
    return _resources;
  }
}
