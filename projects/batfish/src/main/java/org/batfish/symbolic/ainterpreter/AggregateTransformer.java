package org.batfish.symbolic.ainterpreter;

import org.batfish.datamodel.GeneratedRoute;
import org.batfish.symbolic.bdd.BDDTransferFunction;

public class AggregateTransformer {

  private BDDTransferFunction _transferFunction;

  private GeneratedRoute _generatedRoute;

  public AggregateTransformer(BDDTransferFunction t, GeneratedRoute gr) {
    this._transferFunction = t;
    this._generatedRoute = gr;
  }

  public BDDTransferFunction getTransferFunction() {
    return _transferFunction;
  }

  public GeneratedRoute getGeneratedRoute() {
    return _generatedRoute;
  }
}
