package org.batfish.symbolic.ainterpreter;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Flow;

public class AbstractFlowTrace {

  private static final String PROP_FLOW = "flow";

  private static final String PROP_INGRESS_ROUTER = "ingressRouter";

  private static final String PROP_FINAL_ROUTER = "finalRouter";

  private Flow _flow;

  private String _ingressRouter;

  private String _finalRouter;

  @JsonProperty(PROP_FLOW)
  public Flow getFlow() {
    return _flow;
  }

  @JsonProperty(PROP_FLOW)
  public void setFlow(Flow flow) {
    this._flow = flow;
  }

  @JsonProperty(PROP_INGRESS_ROUTER)
  public String getIngressRouter() {
    return _ingressRouter;
  }

  @JsonProperty(PROP_INGRESS_ROUTER)
  public void setIngressRouter(String ingressRouter) {
    this._ingressRouter = ingressRouter;
  }

  @JsonProperty(PROP_FINAL_ROUTER)
  public String getFinalRouter() {
    return _finalRouter;
  }

  @JsonProperty(PROP_FINAL_ROUTER)
  public void setFinalRouter(String finalRouter) {
    this._finalRouter = finalRouter;
  }
}
