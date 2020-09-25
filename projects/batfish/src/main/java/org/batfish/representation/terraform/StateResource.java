package org.batfish.representation.terraform;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ATTRIBUTES;
import static org.batfish.representation.terraform.Constants.JSON_KEY_INSTANCES;
import static org.batfish.representation.terraform.Constants.JSON_KEY_MODE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_NAME;
import static org.batfish.representation.terraform.Constants.JSON_KEY_PROVIDER;
import static org.batfish.representation.terraform.Constants.JSON_KEY_SCHEMA_VERSION;
import static org.batfish.representation.terraform.Constants.JSON_KEY_TYPE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.representation.terraform.CommonResourceProperties.Mode;

/** Represents a generic Terraform resource that appears in the state file */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
class StateResource extends TerraformResource {

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static class Instance implements Serializable {

    @JsonCreator
    private static Instance fromJson(
        @Nullable @JsonProperty(JSON_KEY_SCHEMA_VERSION) Integer schemaVersion,
        @Nullable
            @JsonProperty(JSON_KEY_ATTRIBUTES)
            @JsonSetter(nulls = Nulls.AS_EMPTY, contentNulls = Nulls.SKIP)
            Map<String, Object> attributes) {
      checkArgument(
          schemaVersion != null,
          "'schema_version' cannot be null for a terraform resource instance");
      checkArgument(attributes != null, "'attributes' cannot be null for a terraform resource");

      return new Instance(schemaVersion, attributes);
    }

    private final int _schemaVersion;
    @Nonnull private final Map<String, Object> _attributes;

    Instance(int schemaVersion, Map<String, Object> attributes) {
      _schemaVersion = schemaVersion;
      _attributes = ImmutableMap.copyOf(attributes);
    }

    public int getSchemaVersion() {
      return _schemaVersion;
    }

    @Nonnull
    public Map<String, Object> getAttributes() {
      return _attributes;
    }
  }

  @JsonCreator
  private static StateResource fromJson(
      @Nullable @JsonProperty(JSON_KEY_MODE) Mode mode,
      @Nullable @JsonProperty(JSON_KEY_TYPE) String type,
      @Nullable @JsonProperty(JSON_KEY_NAME) String name,
      @Nullable @JsonProperty(JSON_KEY_PROVIDER) String provider,
      @Nullable @JsonProperty(JSON_KEY_INSTANCES) List<Instance> instances) {
    checkArgument(mode != null, "'mode' cannot be null for a terraform resource");
    checkArgument(type != null, "'type' cannot be null for a terraform resource");
    checkArgument(name != null, "'name' cannot be null for a terraform resource");
    checkArgument(provider != null, "'provider' cannot be null for a terraform resource");

    return new StateResource(
        new CommonResourceProperties(mode, type, name, provider),
        firstNonNull(instances, ImmutableList.of()));
  }

  @Nonnull private final List<Instance> _instances;

  StateResource(CommonResourceProperties common, List<Instance> instances) {
    super(common);
    _instances = ImmutableList.copyOf(instances);
  }

  @Nonnull
  public List<Instance> getInstances() {
    return _instances;
  }
}
