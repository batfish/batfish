package org.batfish.symbolic.ainterpreter;

import org.batfish.datamodel.GeneratedRoute;

public class AggregateTransformer {

  private String _router;

  private String _policyName;

  private GeneratedRoute _generatedRoute;

  public AggregateTransformer(String router, String policyName, GeneratedRoute gr) {
    this._router = router;
    this._policyName = policyName;
    this._generatedRoute = gr;
  }

  public String getRouter() {
    return _router;
  }

  public String getPolicyName() {
    return _policyName;
  }

  public GeneratedRoute getGeneratedRoute() {
    return _generatedRoute;
  }
}
