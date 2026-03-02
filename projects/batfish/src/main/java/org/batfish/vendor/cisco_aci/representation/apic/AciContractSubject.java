package org.batfish.vendor.cisco_aci.representation.apic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Data model class representing an ACI vzSubj (Contract Subject) object.
 *
 * <p>Contract subjects are contained within contracts (vzBrCP) and define specific communication
 * rules. Each subject contains filter references that specify the allowed traffic patterns.
 * Subjects determine if filters are unidirectional or bidirectional and control how the filter
 * rules are applied between provider and consumer endpoint groups.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciContractSubject implements Serializable {

  @JsonProperty("attributes")
  private @Nullable AciContractSubjectAttributes _attributes;

  @JsonProperty("children")
  private @Nullable List<Object> _children;

  public @Nullable AciContractSubjectAttributes getAttributes() {
    return _attributes;
  }

  public void setAttributes(@Nullable AciContractSubjectAttributes attributes) {
    _attributes = attributes;
  }

  public @Nullable List<Object> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<Object> children) {
    _children = children;
  }

  /** Attributes of an ACI Contract Subject (vzSubj). */

  /** Attributes of an ACI Contract Subject (vzSubj). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciContractSubjectAttributes implements Serializable {

    @JsonProperty("annotation")
    private @Nullable String _annotation;

    @JsonProperty("consMatchT")
    private @Nullable String _consumerMatchType;

    @JsonProperty("descr")
    private @Nullable String _description;

    @JsonProperty("dn")
    private @Nullable String _distinguishedName;

    @JsonProperty("name")
    private @Nullable String _name;

    @JsonProperty("nameAlias")
    private @Nullable String _nameAlias;

    @JsonProperty("prio")
    private @Nullable String _priority;

    @JsonProperty("provMatchT")
    private @Nullable String _providerMatchType;

    @JsonProperty("revFltPorts")
    private @Nullable String _reverseFilterPorts;

    @JsonProperty("targetDscp")
    private @Nullable String _targetDscp;

    @JsonProperty("userdom")
    private @Nullable String _userDomain;

    public @Nullable String getAnnotation() {
      return _annotation;
    }

    public void setAnnotation(@Nullable String annotation) {
      _annotation = annotation;
    }

    public @Nullable String getConsumerMatchType() {
      return _consumerMatchType;
    }

    public void setConsumerMatchType(@Nullable String consumerMatchType) {
      _consumerMatchType = consumerMatchType;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(@Nullable String description) {
      _description = description;
    }

    public @Nullable String getDistinguishedName() {
      return _distinguishedName;
    }

    public void setDistinguishedName(@Nullable String distinguishedName) {
      _distinguishedName = distinguishedName;
    }

    public @Nullable String getName() {
      return _name;
    }

    public void setName(@Nullable String name) {
      _name = name;
    }

    public @Nullable String getNameAlias() {
      return _nameAlias;
    }

    public void setNameAlias(@Nullable String nameAlias) {
      _nameAlias = nameAlias;
    }

    public @Nullable String getPriority() {
      return _priority;
    }

    public void setPriority(@Nullable String priority) {
      _priority = priority;
    }

    public @Nullable String getProviderMatchType() {
      return _providerMatchType;
    }

    public void setProviderMatchType(@Nullable String providerMatchType) {
      _providerMatchType = providerMatchType;
    }

    public @Nullable String getReverseFilterPorts() {
      return _reverseFilterPorts;
    }

    public void setReverseFilterPorts(@Nullable String reverseFilterPorts) {
      _reverseFilterPorts = reverseFilterPorts;
    }

    public @Nullable String getTargetDscp() {
      return _targetDscp;
    }

    public void setTargetDscp(@Nullable String targetDscp) {
      _targetDscp = targetDscp;
    }

    public @Nullable String getUserDomain() {
      return _userDomain;
    }

    public void setUserDomain(@Nullable String userDomain) {
      _userDomain = userDomain;
    }
  }
}
