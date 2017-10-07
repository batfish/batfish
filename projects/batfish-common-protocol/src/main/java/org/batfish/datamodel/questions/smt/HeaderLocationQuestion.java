package org.batfish.datamodel.questions.smt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HeaderLocationQuestion extends HeaderQuestion {

  private static final String DEFAULT_FINAL_IFACE_REGEX = ".*";

  private static final String DEFAULT_FINAL_NODE_REGEX = ".*";

  private static final String DEFAULT_INGRESS_NODE_REGEX = ".*";

  private static final String DEFAULT_FAIL_NODE1_REGEX = ".*";

  private static final String DEFAULT_FAIL_NODE2_REGEX = ".*";

  private static final String DEFAULT_NOT_FINAL_NODE_REGEX = "";

  private static final String DEFAULT_NOT_FINAL_IFACE_REGEX = "";

  private static final String DEFAULT_NOT_INGRESS_NODE_REGEX = "";

  private static final String FINAL_NODE_REGEX_VAR = "finalNodeRegex";

  private static final String FINAL_IFACE_REGEX_VAR = "finalIfaceRegex";

  private static final String INGRESS_NODE_REGEX_VAR = "ingressNodeRegex";

  private static final String FAIL_NODE1_REGEX_VAR = "failNode1Regex";

  private static final String FAIL_NODE2_REGEX_VAR = "failNode2Regex";

  private static final String NOT_FINAL_NODE_REGEX_VAR = "notFinalNodeRegex";

  private static final String NOT_FINAL_IFACE_REGEX_VAR = "notFinalIfaceRegex";

  private static final String NOT_INGRESS_NODE_REGEX_VAR = "notIngressNodeRegex";

  private String _finalNodeRegex;

  private String _finalIfaceRegex;

  private String _ingressNodeRegex;

  private String _failNode1Regex;

  private String _failNode2Regex;

  private String _notFinalNodeRegex;

  private String _notFinalIfaceRegex;

  private String _notIngressNodeRegex;

  public HeaderLocationQuestion() {
    super();
    _finalNodeRegex = DEFAULT_FINAL_NODE_REGEX;
    _finalIfaceRegex = DEFAULT_FINAL_IFACE_REGEX;
    _ingressNodeRegex = DEFAULT_INGRESS_NODE_REGEX;
    _failNode1Regex = DEFAULT_FAIL_NODE1_REGEX;
    _failNode2Regex = DEFAULT_FAIL_NODE2_REGEX;
    _notFinalNodeRegex = DEFAULT_NOT_FINAL_NODE_REGEX;
    _notFinalIfaceRegex = DEFAULT_NOT_FINAL_IFACE_REGEX;
    _notIngressNodeRegex = DEFAULT_NOT_INGRESS_NODE_REGEX;
  }

  public HeaderLocationQuestion(HeaderLocationQuestion other) {
    super(other);
    this._finalNodeRegex = other._finalNodeRegex;
    this._finalIfaceRegex = other._finalIfaceRegex;
    this._ingressNodeRegex = other._ingressNodeRegex;
    this._failNode1Regex = other._failNode1Regex;
    this._failNode2Regex = other._failNode2Regex;
    this._notFinalNodeRegex = other._notFinalNodeRegex;
    this._notFinalIfaceRegex = other._notFinalIfaceRegex;
    this._notIngressNodeRegex = other._notIngressNodeRegex;
  }

  @JsonProperty(FINAL_NODE_REGEX_VAR)
  public String getFinalNodeRegex() {
    return _finalNodeRegex;
  }

  @JsonProperty(FINAL_IFACE_REGEX_VAR)
  public String getFinalIfaceRegex() {
    return _finalIfaceRegex;
  }

  @JsonProperty(INGRESS_NODE_REGEX_VAR)
  public String getIngressNodeRegex() {
    return _ingressNodeRegex;
  }

  @JsonProperty(FAIL_NODE1_REGEX_VAR)
  public String getFailNode1Regex() {
    return _failNode1Regex;
  }

  @JsonProperty(FAIL_NODE2_REGEX_VAR)
  public String getFailNode2Regex() {
    return _failNode2Regex;
  }

  @JsonProperty(NOT_FINAL_NODE_REGEX_VAR)
  public String getNotFinalNodeRegex() {
    return _notFinalNodeRegex;
  }

  @JsonProperty(NOT_FINAL_IFACE_REGEX_VAR)
  public String getNotFinalIfaceRegex() {
    return _notFinalIfaceRegex;
  }

  @JsonProperty(NOT_INGRESS_NODE_REGEX_VAR)
  public String getNotIngressNodeRegex() {
    return _notIngressNodeRegex;
  }

  @JsonProperty(FINAL_NODE_REGEX_VAR)
  public void setFinalNodeRegex(String regex) {
    _finalNodeRegex = regex;
  }

  @JsonProperty(FINAL_IFACE_REGEX_VAR)
  public void setFinalIfaceRegex(String regex) {
    _finalIfaceRegex = regex;
  }

  @JsonProperty(INGRESS_NODE_REGEX_VAR)
  public void setIngressNodeRegex(String regex) {
    _ingressNodeRegex = regex;
  }

  @JsonProperty(FAIL_NODE1_REGEX_VAR)
  public void setFailNode1Regex(String regex) {
    _failNode1Regex = regex;
  }

  @JsonProperty(FAIL_NODE2_REGEX_VAR)
  public void setFailNode2Regex(String regex) {
    _failNode2Regex = regex;
  }

  @JsonProperty(NOT_FINAL_NODE_REGEX_VAR)
  public void setNotFinalNodeRegex(String notFinalNodeRegex) {
    _notFinalNodeRegex = notFinalNodeRegex;
  }

  @JsonProperty(NOT_FINAL_IFACE_REGEX_VAR)
  public void setNotFinalIfaceRegex(String notFinalIfaceRegex) {
    _notFinalIfaceRegex = notFinalIfaceRegex;
  }

  @JsonProperty(NOT_INGRESS_NODE_REGEX_VAR)
  public void setNotIngressNodeRegex(String notIngressNodeRegex) {
    _notIngressNodeRegex = notIngressNodeRegex;
  }
}