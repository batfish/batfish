package org.batfish.question.traceroute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.questions.IPacketTraceQuestion;

/**
 * A question to perform a traceroute.
 *
 * <p>This question performs a virtual traceroute in the network from ingress node. The destination
 * IP is randomly picked if not explicitly specified. Other IP headers are also randomly picked if
 * unspecified, with a bias toward generating packets similar to a real traceroute (see below).
 *
 * <p>Unlike a real traceroute, this traceroute is directional. That is, for it to succeed, the
 * reverse connectivity is not needed. This feature can help debug connectivity issues by decoupling
 * the two directions.
 */
public class TracerouteQuestion extends IPacketTraceQuestion {
  private static final String PROP_IGNORE_ACLS = "ignoreAcls";
  private static final String PROP_INGRESS_NODE = "ingressNode";
  private static final String PROP_INGRESS_VRF = "ingressVrf";

  private boolean _ignoreAcls;
  private String _ingressNode;
  private String _ingressVrf;

  public TracerouteQuestion() {
    _ignoreAcls = false;
  }

  private Flow.Builder createFlowBuilder() {
    Flow.Builder flowBuilder = createBaseFlowBuilder();
    if (_ingressNode != null) {
      flowBuilder.setIngressNode(_ingressNode);
    }
    if (_ingressVrf != null) {
      flowBuilder.setIngressVrf(_ingressVrf);
    }
    return flowBuilder;
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @JsonProperty(PROP_IGNORE_ACLS)
  public boolean getIgnoreAcls() {
    return _ignoreAcls;
  }

  @JsonIgnore
  public Set<Builder> getFlowBuilders() {
    return Collections.singleton(createFlowBuilder());
  }

  @JsonProperty(PROP_INGRESS_NODE)
  public String getIngressNode() {
    return _ingressNode;
  }

  @JsonProperty(PROP_INGRESS_VRF)
  public String getIngressVrf() {
    return _ingressVrf;
  }

  @Override
  public String getName() {
    return "traceroute2";
  }

  @Override
  public String prettyPrint() {
    try {
      String retString =
          String.format("traceroute %singressNode=%s", prettyPrintBase(), _ingressNode);
      if (_ingressVrf != null) {
        retString += String.format(", %s=%s", PROP_INGRESS_VRF, _ingressVrf);
      }
      retString += toString(); // calls parent toString()
      return retString;
    } catch (Exception e) {
      try {
        return "Pretty printing failed. Printing Json\n" + toJsonString();
      } catch (BatfishException e1) {
        throw new BatfishException("Both pretty and json printing failed\n");
      }
    }
  }

  @JsonProperty(PROP_IGNORE_ACLS)
  public void setIgnoreAcls(boolean ignoreAcls) {
    _ignoreAcls = ignoreAcls;
  }

  @JsonProperty(PROP_INGRESS_NODE)
  public void setIngressNode(String ingressNode) {
    _ingressNode = ingressNode;
  }

  @JsonProperty(PROP_INGRESS_VRF)
  public void setIngressVrf(String ingressVrf) {
    _ingressVrf = ingressVrf;
  }
}
