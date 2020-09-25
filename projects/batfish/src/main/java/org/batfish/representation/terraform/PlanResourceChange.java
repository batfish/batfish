package org.batfish.representation.terraform;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ACTIONS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_AFTER;
import static org.batfish.representation.terraform.Constants.JSON_KEY_AFTER_UNKNOWN;
import static org.batfish.representation.terraform.Constants.JSON_KEY_CHANGE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_MODE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_NAME;
import static org.batfish.representation.terraform.Constants.JSON_KEY_PROVIDER_NAME;
import static org.batfish.representation.terraform.Constants.JSON_KEY_TYPE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.representation.terraform.CommonResourceProperties.Mode;

/** Represents a generic Terraform resource that appears in a state or a plan file */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
class PlanResourceChange extends TerraformResource {

  enum ChangeAction {
    CREATE,
    DELETE,
    NO_OP,
    READ,
    UPDATE;

    @JsonCreator
    private static ChangeAction fromString(String action) {
      return ChangeAction.valueOf(action.toUpperCase().replace("-", "_"));
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Change implements Serializable {
    @JsonCreator
    private static Change fromJson(
        @Nullable @JsonProperty(JSON_KEY_ACTIONS) List<ChangeAction> actions,
        @Nullable @JsonProperty(JSON_KEY_AFTER) Map<String, Object> after,
        @Nullable @JsonProperty(JSON_KEY_AFTER_UNKNOWN) Map<String, Object> afterUnknown) {
      checkArgument(actions != null, "Missing 'actions' list for change");
      checkArgument(after != null, "Missing 'after' values for change");
      checkArgument(afterUnknown != null, "Missing 'after_unknown' values for change");
      return new Change(actions, after, afterUnknown);
    }

    @Nonnull private final List<ChangeAction> _actions;
    @Nonnull private final Map<String, Object> _after;
    @Nonnull private final Map<String, Object> _afterUnknown;

    Change(
        List<ChangeAction> actions, Map<String, Object> after, Map<String, Object> afterUnknown) {
      _actions = actions;
      _after = after;
      _afterUnknown = afterUnknown;
    }

    @Nonnull
    public List<ChangeAction> getActions() {
      return _actions;
    }

    @Nonnull
    public Map<String, Object> getAfter() {
      return _after;
    }

    @Nonnull
    public Map<String, Object> getAfterUnknown() {
      return _afterUnknown;
    }
  }

  @JsonCreator
  private static PlanResourceChange fromJson(
      @Nullable @JsonProperty(JSON_KEY_MODE) Mode mode,
      @Nullable @JsonProperty(JSON_KEY_TYPE) String type,
      @Nullable @JsonProperty(JSON_KEY_NAME) String name,
      @Nullable @JsonProperty(JSON_KEY_PROVIDER_NAME) String providerName,
      @Nullable @JsonProperty(JSON_KEY_CHANGE) Change change) {
    checkArgument(mode != null, "'mode' cannot be null for a terraform resource_change");
    checkArgument(type != null, "'type' cannot be null for a terraform resource_change");
    checkArgument(name != null, "'name' cannot be null for a terraform resource_change");
    checkArgument(
        providerName != null, "'provider_name' cannot be null for a terraform resource_change");
    checkArgument(change != null, "'change' cannot be null for a terraform resource");

    return new PlanResourceChange(
        new CommonResourceProperties(mode, type, name, "provider." + providerName), change);
  }

  @Nonnull private final Change _change;

  PlanResourceChange(CommonResourceProperties common, Change change) {
    super(common);
    _change = change;
  }

  @Nonnull
  public Change getChange() {
    return _change;
  }
}
