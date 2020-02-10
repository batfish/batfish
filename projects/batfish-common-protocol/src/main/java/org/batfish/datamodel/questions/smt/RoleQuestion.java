package org.batfish.datamodel.questions.smt;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.questions.Question;

public class RoleQuestion extends Question {
  private static final String PROP_NODE_REGEX = "nodeRegex";
  private static final String PROP_DST_IPS = "dstIps";
  private static final String PROP_EQUIVALENCE_TYPE = "equivType";

  private String _nodeRegex = ".*";

  private List<Prefix> _dstIps;

  private EquivalenceType _type = EquivalenceType.NODE;

  @JsonProperty(PROP_NODE_REGEX)
  public String getNodeRegex() {
    return _nodeRegex;
  }

  @JsonProperty(PROP_DST_IPS)
  public List<Prefix> getDstIps() {
    return _dstIps;
  }

  @JsonProperty(PROP_EQUIVALENCE_TYPE)
  public EquivalenceType getType() {
    return _type;
  }

  @JsonProperty(PROP_NODE_REGEX)
  public void setNodeRegex(String x) {
    _nodeRegex = x;
  }

  @JsonProperty(PROP_DST_IPS)
  public void setDstIps(List<Prefix> x) {
    _dstIps = x;
  }

  @JsonProperty(PROP_EQUIVALENCE_TYPE)
  public void setType(EquivalenceType x) {
    _type = x;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "smt-roles";
  }
}
