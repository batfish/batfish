package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.Serializable;
import org.batfish.common.util.BatfishObjectMapper;

/** Custom deserializer for {@link AciPolUniInternal} with heterogeneous child objects. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciPolUniDeserializer extends JsonDeserializer<AciPolUniInternal>
    implements Serializable {
  @Override
  public AciPolUniInternal deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    JsonNode node = p.getCodec().readTree(p);

    AciPolUniInternal polUni = new AciPolUniInternal();

    JsonNode attributesNode = node.get("attributes");
    if (attributesNode != null) {
      polUni.setAttributes(
          BatfishObjectMapper.mapper()
              .treeToValue(attributesNode, AciPolUniInternal.AciPolUniInternalAttributes.class));
    }

    JsonNode childrenNode = node.get("children");
    if (childrenNode != null && childrenNode.isArray()) {
      com.google.common.collect.ImmutableList.Builder<AciPolUniInternal.PolUniChild> children =
          com.google.common.collect.ImmutableList.builder();
      for (JsonNode childNode : childrenNode) {
        AciPolUniInternal.PolUniChild child = new AciPolUniInternal.PolUniChild();
        if (childNode.has("fvTenant")) {
          child.setFvTenant(
              BatfishObjectMapper.mapper().treeToValue(childNode.get("fvTenant"), AciTenant.class));
        } else if (childNode.has("fabricInst")) {
          child.setFabricInst(parseFabricInst(childNode.get("fabricInst"), p, ctxt));
        } else if (childNode.has("ctrlrInst")) {
          child.setCtrlrInst(parseCtrlrInst(childNode.get("ctrlrInst"), p, ctxt));
        }
        children.add(child);
      }
      polUni.setChildren(children.build());
    }

    return polUni;
  }

  private static AciCtrlrInst parseCtrlrInst(
      JsonNode node, JsonParser p, DeserializationContext ctxt) throws IOException {
    AciCtrlrInst ctrlrInst = new AciCtrlrInst();

    JsonNode childrenNode = node.get("children");
    if (childrenNode != null && childrenNode.isArray()) {
      com.google.common.collect.ImmutableList.Builder<AciCtrlrInst.CtrlrInstChild> children =
          com.google.common.collect.ImmutableList.builder();
      for (JsonNode childNode : childrenNode) {
        AciCtrlrInst.CtrlrInstChild child = new AciCtrlrInst.CtrlrInstChild();
        if (childNode.has("fabricNodeIdentPol")) {
          child.setFabricNodeIdentPol(
              parseFabricNodeIdentPol(childNode.get("fabricNodeIdentPol"), p));
        }
        children.add(child);
      }
      ctrlrInst.setChildren(children.build());
    }

    return ctrlrInst;
  }

  private static AciFabricInst parseFabricInst(
      JsonNode node, JsonParser p, DeserializationContext ctxt) throws IOException {
    AciFabricInst fabricInst = new AciFabricInst();

    JsonNode attributesNode = node.get("attributes");
    if (attributesNode != null) {
      fabricInst.setAttributes(
          BatfishObjectMapper.mapper()
              .treeToValue(attributesNode, AciFabricInst.AciFabricInstAttributes.class));
    }

    JsonNode childrenNode = node.get("children");
    if (childrenNode != null && childrenNode.isArray()) {
      com.google.common.collect.ImmutableList.Builder<AciFabricInst.FabricInstChild> children =
          com.google.common.collect.ImmutableList.builder();
      for (JsonNode childNode : childrenNode) {
        AciFabricInst.FabricInstChild child = new AciFabricInst.FabricInstChild();
        if (childNode.has("fabricProtPol")) {
          child.setFabricProtPol(
              BatfishObjectMapper.mapper()
                  .treeToValue(childNode.get("fabricProtPol"), AciFabricProtPol.class));
        } else if (childNode.has("fabricNodeIdentPol")) {
          child.setFabricNodeIdentPol(
              parseFabricNodeIdentPol(childNode.get("fabricNodeIdentPol"), p));
        }
        children.add(child);
      }
      fabricInst.setChildren(children.build());
    }

    return fabricInst;
  }

  private static AciFabricNodeIdentPol parseFabricNodeIdentPol(JsonNode node, JsonParser p)
      throws IOException {
    AciFabricNodeIdentPol identPol = new AciFabricNodeIdentPol();

    JsonNode childrenNode = node.get("children");
    if (childrenNode != null && childrenNode.isArray()) {
      com.google.common.collect.ImmutableList.Builder<AciFabricNodeIdentP> children =
          com.google.common.collect.ImmutableList.builder();
      for (JsonNode childNode : childrenNode) {
        if (childNode.has("fabricNodeIdentP")) {
          AciFabricNodeIdentP nodeIdentP =
              BatfishObjectMapper.mapper()
                  .treeToValue(childNode.get("fabricNodeIdentP"), AciFabricNodeIdentP.class);
          children.add(nodeIdentP);
        }
      }
      identPol.setChildren(children.build());
    }
    return identPol;
  }
}
