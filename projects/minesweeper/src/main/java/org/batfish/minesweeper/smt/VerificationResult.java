package org.batfish.minesweeper.smt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.z3.BoolExpr;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nullable;

/**
 * The result of trying to verify properties of the network model built with an encoder. Either the
 * model is verified, or there is a counter example and the counter example is provided as a model
 * for each encoding variable.
 *
 * @author Ryan Beckett
 */
public class VerificationResult {
  private static final String PROP_VERIFIED = "verified";
  private static final String PROP_MODEL = "model";
  private static final String PROP_PACKET_MODEL = "packetModel";
  private static final String PROP_ENV_MODEL = "environmentModel";
  private static final String PROP_FWD_MODEL = "forwardingModel";
  private static final String PROP_FAILURE_MODEL = "failuresModel";
  private static final String PROP_STATS = "statistics";

  private boolean _verified;

  private SortedMap<String, String> _model;

  private SortedMap<String, String> _packetModel;

  private SortedMap<String, SortedMap<String, String>> _envModel;

  private SortedSet<String> _fwdModel;

  private SortedSet<String> _failures;

  private VerificationStats _stats;

  @JsonCreator
  public VerificationResult(
      @JsonProperty(PROP_VERIFIED) boolean verified,
      @Nullable @JsonProperty(PROP_MODEL) SortedMap<String, String> model,
      @Nullable @JsonProperty(PROP_PACKET_MODEL) SortedMap<String, String> packetModel,
      @Nullable @JsonProperty(PROP_ENV_MODEL) SortedMap<String, SortedMap<String, String>> envModel,
      @Nullable @JsonProperty(PROP_FWD_MODEL) SortedSet<String> fwdModel,
      @Nullable @JsonProperty(PROP_FAILURE_MODEL) SortedSet<String> failures,
      @Nullable @JsonProperty(PROP_STATS) VerificationStats stats) {
    _verified = verified;
    _model = model;
    _packetModel = packetModel;
    _envModel = envModel;
    _fwdModel = fwdModel;
    _failures = failures;
    _stats = stats;
  }

  @JsonProperty(PROP_VERIFIED)
  public boolean isVerified() {
    return _verified;
  }

  @JsonProperty(PROP_MODEL)
  public SortedMap<String, String> getModel() {
    return _model;
  }

  @JsonProperty(PROP_PACKET_MODEL)
  public SortedMap<String, String> getPacketModel() {
    return _packetModel;
  }

  @JsonProperty(PROP_ENV_MODEL)
  public SortedMap<String, SortedMap<String, String>> getEnvModel() {
    return _envModel;
  }

  @JsonProperty(PROP_FWD_MODEL)
  public SortedSet<String> getFwdModel() {
    return _fwdModel;
  }

  @JsonProperty(PROP_FAILURE_MODEL)
  public SortedSet<String> getFailures() {
    return _failures;
  }

  @JsonProperty(PROP_STATS)
  public VerificationStats getStats() {
    return _stats;
  }

  @JsonProperty(PROP_STATS)
  public void setStats(VerificationStats x) {
    _stats = x;
  }

  public void debug(EncoderSlice enc, boolean showConstraints, String filter) {
    if (showConstraints) {
      System.out.println("================= Constraints ==================");
      for (BoolExpr be : enc.getSolver().getAssertions()) {
        String x = be.simplify().toString();
        if (filter == null || x.contains(filter)) {
          System.out.println(x);
        }
      }
    }
    if (_verified) {
      System.out.println("verified");
    } else {
      System.out.println("================= Model ================");
      enc.getSymbolicDecisions()
          .getDataForwarding()
          .forEach(
              (router, map) ->
                  map.forEach(
                      (edge, e) -> {
                        String expr = e.toString();
                        if (expr.contains("DATA-")) {
                          String result = _model.get(expr);
                          if ("true".equals(result)) {
                            System.out.println(edge);
                          }
                        }
                      }));
      System.out.println();
      _model.forEach(
          (var, val) -> {
            if (filter == null || var.contains(filter)) {
              System.out.println(var + "=" + val);
            }
          });
    }

    if (enc.getUnsatCore().getDoTrack()) {
      System.out.println("================= Unsat Core ================");
      for (BoolExpr be : enc.getSolver().getUnsatCore()) {
        BoolExpr constraint = enc.getUnsatCore().getTrackingVars().get(be.toString());
        System.out.println("Var: " + be);
        System.out.println(constraint);
        System.out.println();
      }
    }
  }
}
