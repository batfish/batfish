package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.BfConsts;

/**
 * Configuration and template description for a specific instance of a {@link
 * org.batfish.datamodel.questions.Question}.
 */
public final class InstanceData {

  private String _description;

  private String _instanceName;

  private String _longDescription;

  private SortedSet<String> _tags;

  private SortedMap<String, Variable> _variables;

  public InstanceData() {
    _tags = new TreeSet<>();
    _variables = new TreeMap<>();
  }

  @JsonProperty(BfConsts.PROP_DESCRIPTION)
  public String getDescription() {
    return _description;
  }

  @JsonProperty(BfConsts.PROP_INSTANCE_NAME)
  public String getInstanceName() {
    return _instanceName;
  }

  @JsonProperty(BfConsts.PROP_LONG_DESCRIPTION)
  public String getLongDescription() {
    return _longDescription;
  }

  @JsonProperty(BfConsts.PROP_TAGS)
  public SortedSet<String> getTags() {
    return _tags;
  }

  @JsonProperty(BfConsts.PROP_VARIABLES)
  public SortedMap<String, Variable> getVariables() {
    return _variables;
  }

  @JsonProperty(BfConsts.PROP_DESCRIPTION)
  public void setDescription(String description) {
    _description = description;
  }

  @JsonProperty(BfConsts.PROP_INSTANCE_NAME)
  public void setInstanceName(String instanceName) {
    _instanceName = instanceName;
  }

  @JsonProperty(BfConsts.PROP_LONG_DESCRIPTION)
  public void setLongDescription(String longDescription) {
    _longDescription = longDescription;
  }

  @JsonProperty(BfConsts.PROP_TAGS)
  public void setTags(SortedSet<String> tags) {
    _tags = tags;
  }

  @JsonProperty(BfConsts.PROP_VARIABLES)
  public void setVariables(SortedMap<String, Variable> variables) {
    _variables = variables;
  }
}
