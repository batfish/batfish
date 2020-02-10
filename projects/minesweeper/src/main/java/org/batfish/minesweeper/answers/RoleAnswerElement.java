package org.batfish.minesweeper.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.SortedSet;
import org.batfish.datamodel.answers.AnswerElement;

public class RoleAnswerElement extends AnswerElement {
  private static final String PROP_IMPORT_BGP_ECS = "importBgpEcs";
  private static final String PROP_EXPORT_BGP_ECS = "exportBgpEcs";
  private static final String PROP_INCOMING_ACL_ECS = "incomingAclEcs";
  private static final String PROP_OUTGOING_BGP_ECS = "outgoingAclEcs";
  private static final String PROP_INTERFACE_ECS = "interfaceEcs";
  private static final String PROP_NODE_ECS = "nodeEcs";

  private List<SortedSet<String>> _importBgpEcs;

  private List<SortedSet<String>> _exportBgpEcs;

  private List<SortedSet<String>> _incomingAclEcs;

  private List<SortedSet<String>> _outgoingAclEcs;

  private List<SortedSet<String>> _interfaceEcs;

  private List<SortedSet<String>> _nodeEcs;

  @JsonProperty(PROP_IMPORT_BGP_ECS)
  public List<SortedSet<String>> getImportBgpEcs() {
    return _importBgpEcs;
  }

  @JsonProperty(PROP_IMPORT_BGP_ECS)
  public void setImportBgpEcs(List<SortedSet<String>> x) {
    _importBgpEcs = x;
  }

  @JsonProperty(PROP_EXPORT_BGP_ECS)
  public List<SortedSet<String>> getExportBgpEcs() {
    return _exportBgpEcs;
  }

  @JsonProperty(PROP_EXPORT_BGP_ECS)
  public void setExportBgpEcs(List<SortedSet<String>> x) {
    _exportBgpEcs = x;
  }

  @JsonProperty(PROP_INCOMING_ACL_ECS)
  public List<SortedSet<String>> getIncomingAclEcs() {
    return _incomingAclEcs;
  }

  @JsonProperty(PROP_INCOMING_ACL_ECS)
  public void setIncomingAclEcs(List<SortedSet<String>> x) {
    _incomingAclEcs = x;
  }

  @JsonProperty(PROP_OUTGOING_BGP_ECS)
  public List<SortedSet<String>> getOutgoingAclEcs() {
    return _outgoingAclEcs;
  }

  @JsonProperty(PROP_OUTGOING_BGP_ECS)
  public void setOutgoingAclEcs(List<SortedSet<String>> x) {
    _outgoingAclEcs = x;
  }

  @JsonProperty(PROP_INTERFACE_ECS)
  public List<SortedSet<String>> getInterfaceEcs() {
    return _interfaceEcs;
  }

  @JsonProperty(PROP_INTERFACE_ECS)
  public void setInterfaceEcs(List<SortedSet<String>> x) {
    _interfaceEcs = x;
  }

  @JsonProperty(PROP_NODE_ECS)
  public List<SortedSet<String>> getNodeEcs() {
    return _nodeEcs;
  }

  @JsonProperty(PROP_NODE_ECS)
  public void setNodeEcs(List<SortedSet<String>> x) {
    _nodeEcs = x;
  }
}
