package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BfConsts;
import org.batfish.datamodel.questions.Question.InstanceData.Variable;

public class AnswerInfo {

  private SortedMap<String, Variable> _variables;
  private double _executionTime;
  private boolean _neededDataplane;
  private AnswerInfoStatus _status;

  public AnswerInfo(Answer answer) {
    _variables = answer.getQuestion().getInstance().getVariables();
    _neededDataplane = answer.getQuestion().getDataPlane();
    if (answer.getSummary().getNumPassed() > 0 && answer.getSummary().getNumFailed() == 0) {
      _status = AnswerInfoStatus.SUCCESS;
    } else if (answer.getSummary().getNumFailed() > 0) {
      _status = AnswerInfoStatus.FAILURE;
    } else {
      _status = AnswerInfoStatus.NOT_APPLICABLE;
    }
  }

  public AnswerInfo() {
    _variables = new TreeMap<>();
  }

  @JsonProperty(BfConsts.PROP_STATUS)
  public AnswerInfoStatus getStatus() {
    return _status;
  }

  @JsonProperty(BfConsts.PROP_STATUS)
  public void setStatus(AnswerInfoStatus status) {
    _status = status;
  }

  @JsonProperty(BfConsts.PROP_VARIABLES)
  public SortedMap<String, Variable> getVariables() {
    return _variables;
  }

  @JsonProperty(BfConsts.PROP_VARIABLES)
  public void setVariables(SortedMap<String, Variable> variables) {
    this._variables = variables;
  }

  @JsonProperty(BfConsts.PROP_EXECUTION_TIME)
  public double getExecutionTime() {
    return _executionTime;
  }

  @JsonProperty(BfConsts.PROP_EXECUTION_TIME)
  public void setExecutionTime(double executionTime) {
    _executionTime = executionTime;
  }

  @JsonProperty(BfConsts.PROP_NEEDED_DATAPLANE)
  public boolean getNeededDataplane() {
    return _neededDataplane;
  }

  @JsonProperty(BfConsts.PROP_NEEDED_DATAPLANE)
  public void setNeededDataplane(boolean neededDataplane) {
    _neededDataplane = neededDataplane;
  }
}
