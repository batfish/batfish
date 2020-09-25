package org.batfish.representation.terraform;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.terraform.Constants.JSON_KEY_RESOURCES;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.representation.terraform.CommonResourceProperties.Mode;

/** Represents information in Terraform state files */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TerraformState implements TerraformFileContent {

  @JsonCreator
  private static TerraformState fromJson(
      @Nullable @JsonProperty(JSON_KEY_RESOURCES) List<StateResource> resources) {
    checkArgument(resources != null, "Missing 'resources' list in state file");
    return new TerraformState(resources);
  }

  private final List<StateResource> _genericResources;

  TerraformState(List<StateResource> genericResources) {
    _genericResources = ImmutableList.copyOf(genericResources);
  }

  @Override
  public List<TerraformResource> toConvertedResources(Warnings warnings) {
    return _genericResources.stream()
        .filter(r -> r.getCommon().getMode() != Mode.DATA)
        .map(r -> specialize(r, warnings))
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
  }

  @Nullable
  static TerraformResource specialize(StateResource resource, Warnings warnings) {
    if (resource.getInstances().size() != 1) {
      warnings.redFlag(
          "Skipping %s because it has multiple instances, which I don't know how to handle.",
          resource.getCommon().getName());
      return resource;
    }
    return TerraformResource.create(
        resource.getCommon(), resource.getInstances().get(0).getAttributes(), warnings);
  }
}
