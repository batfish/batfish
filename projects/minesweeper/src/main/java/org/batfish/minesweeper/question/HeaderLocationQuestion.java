package org.batfish.minesweeper.question;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HeaderLocationQuestion extends HeaderQuestion {

  private static final boolean DEFAULT_NEGATE = false;

  private static final String DEFAULT_FINAL_IFACE_REGEX = ".*";

  private static final String DEFAULT_FINAL_NODE_REGEX = ".*";

  private static final String DEFAULT_INGRESS_NODE_REGEX = ".*";

  private static final String DEFAULT_FAIL_NODE1_REGEX = ".*";

  private static final String DEFAULT_FAIL_NODE2_REGEX = ".*";

  private static final String DEFAULT_NOT_FINAL_NODE_REGEX = "";

  private static final String DEFAULT_NOT_FINAL_IFACE_REGEX = "";

  private static final String DEFAULT_NOT_INGRESS_NODE_REGEX = "";

  private static final String DEFAULT_NOT_FAIL_NODE1_REGEX = "";

  private static final String DEFAULT_NOT_FAIL_NODE2_REGEX = "";

  private static final String DEFAULT_FAIL_NODE_REGEX = ".*";

  private static final String DEFAULT_NOT_FAIL_NODE_REGEX = "";
  private static final String PROP_NEGATE = "negate";
  private static final String PROP_FINAL_NODE_REGEX = "finalNodeRegex";
  private static final String PROP_FINAL_IFACE_REGEX = "finalIfaceRegex";
  private static final String PROP_INGRESS_NODE_REGEX = "ingressNodeRegex";

  private static final String FAIL_NODE1_REGEX_VAR = "failNode1Regex";

  private static final String FAIL_NODE2_REGEX_VAR = "failNode2Regex";
  private static final String PROP_NOT_FINAL_NODE_REGEX = "notFinalNodeRegex";
  private static final String PROP_NOT_FINAL_IFACE_REGEX = "notFinalIfaceRegex";
  private static final String PROP_NOT_INGRESS_NODE_REGEX = "notIngressNodeRegex";

  private static final String NOT_FAIL_NODE1_REGEX_VAR = "notFailNode1Regex";

  private static final String NOT_FAIL_NODE2_REGEX_VAR = "notFailNode2Regex";
  private static final String PROP_FAIL_NODE_REGEX = "failNodeRegex";
  private static final String PROP_NOT_FAIL_NODE_REGEX = "notFailNodeRegex";

  private boolean _negate;

  private String _finalNodeRegex;

  private String _finalIfaceRegex;

  private String _ingressNodeRegex;

  private String _failNode1Regex;

  private String _failNode2Regex;

  private String _failNodeRegex;

  private String _notFinalNodeRegex;

  private String _notFinalIfaceRegex;

  private String _notIngressNodeRegex;

  private String _notFailNode1Regex;

  private String _notFailNode2Regex;

  private String _notFailNodeRegex;

  public HeaderLocationQuestion() {
    super();
    _negate = DEFAULT_NEGATE;
    _finalNodeRegex = DEFAULT_FINAL_NODE_REGEX;
    _finalIfaceRegex = DEFAULT_FINAL_IFACE_REGEX;
    _ingressNodeRegex = DEFAULT_INGRESS_NODE_REGEX;
    _failNode1Regex = DEFAULT_FAIL_NODE1_REGEX;
    _failNode2Regex = DEFAULT_FAIL_NODE2_REGEX;
    _failNodeRegex = DEFAULT_FAIL_NODE_REGEX;
    _notFinalNodeRegex = DEFAULT_NOT_FINAL_NODE_REGEX;
    _notFinalIfaceRegex = DEFAULT_NOT_FINAL_IFACE_REGEX;
    _notIngressNodeRegex = DEFAULT_NOT_INGRESS_NODE_REGEX;
    _notFailNode1Regex = DEFAULT_NOT_FAIL_NODE1_REGEX;
    _notFailNode2Regex = DEFAULT_NOT_FAIL_NODE2_REGEX;
    _notFailNodeRegex = DEFAULT_NOT_FAIL_NODE_REGEX;
  }

  public HeaderLocationQuestion(HeaderLocationQuestion other) {
    super(other);
    _negate = other._negate;
    _finalNodeRegex = other._finalNodeRegex;
    _finalIfaceRegex = other._finalIfaceRegex;
    _ingressNodeRegex = other._ingressNodeRegex;
    _failNode1Regex = other._failNode1Regex;
    _failNode2Regex = other._failNode2Regex;
    _failNodeRegex = other._failNodeRegex;
    _notFinalNodeRegex = other._notFinalNodeRegex;
    _notFinalIfaceRegex = other._notFinalIfaceRegex;
    _notIngressNodeRegex = other._notIngressNodeRegex;
    _notFailNode1Regex = other._notFailNode1Regex;
    _notFailNode2Regex = other._notFailNode2Regex;
    _notFailNodeRegex = other._notFailNodeRegex;
  }

  @JsonProperty(PROP_NEGATE)
  public boolean getNegate() {
    return _negate;
  }

  @JsonProperty(PROP_FINAL_NODE_REGEX)
  public String getFinalNodeRegex() {
    return _finalNodeRegex;
  }

  @JsonProperty(PROP_FINAL_IFACE_REGEX)
  public String getFinalIfaceRegex() {
    return _finalIfaceRegex;
  }

  @JsonProperty(PROP_INGRESS_NODE_REGEX)
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

  @JsonProperty(PROP_FAIL_NODE_REGEX)
  public String getFailNodeRegex() {
    return _failNodeRegex;
  }

  @JsonProperty(PROP_NOT_FINAL_NODE_REGEX)
  public String getNotFinalNodeRegex() {
    return _notFinalNodeRegex;
  }

  @JsonProperty(PROP_NOT_FINAL_IFACE_REGEX)
  public String getNotFinalIfaceRegex() {
    return _notFinalIfaceRegex;
  }

  @JsonProperty(PROP_NOT_INGRESS_NODE_REGEX)
  public String getNotIngressNodeRegex() {
    return _notIngressNodeRegex;
  }

  @JsonProperty(NOT_FAIL_NODE1_REGEX_VAR)
  public String getNotFailNode1Regex() {
    return _notFailNode1Regex;
  }

  @JsonProperty(NOT_FAIL_NODE2_REGEX_VAR)
  public String getNotFailNode2Regex() {
    return _notFailNode2Regex;
  }

  @JsonProperty(PROP_NOT_FAIL_NODE_REGEX)
  public String getNotFailNodeRegex() {
    return _notFailNodeRegex;
  }

  @JsonProperty(PROP_NEGATE)
  public void setNegate(boolean negate) {
    _negate = negate;
  }

  @JsonProperty(PROP_FINAL_NODE_REGEX)
  public void setFinalNodeRegex(String regex) {
    _finalNodeRegex = regex;
  }

  @JsonProperty(PROP_FINAL_IFACE_REGEX)
  public void setFinalIfaceRegex(String regex) {
    _finalIfaceRegex = regex;
  }

  @JsonProperty(PROP_INGRESS_NODE_REGEX)
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

  @JsonProperty(PROP_FAIL_NODE_REGEX)
  public void setFailNodeRegex(String regex) {
    _failNodeRegex = regex;
  }

  @JsonProperty(PROP_NOT_FINAL_NODE_REGEX)
  public void setNotFinalNodeRegex(String notFinalNodeRegex) {
    _notFinalNodeRegex = notFinalNodeRegex;
  }

  @JsonProperty(PROP_NOT_FINAL_IFACE_REGEX)
  public void setNotFinalIfaceRegex(String notFinalIfaceRegex) {
    _notFinalIfaceRegex = notFinalIfaceRegex;
  }

  @JsonProperty(PROP_NOT_INGRESS_NODE_REGEX)
  public void setNotIngressNodeRegex(String notIngressNodeRegex) {
    _notIngressNodeRegex = notIngressNodeRegex;
  }

  @JsonProperty(NOT_FAIL_NODE1_REGEX_VAR)
  public void setNotFailNode1Regex(String regex) {
    _notFailNode1Regex = regex;
  }

  @JsonProperty(NOT_FAIL_NODE2_REGEX_VAR)
  public void setNotFailNode2Regex(String regex) {
    _notFailNode2Regex = regex;
  }

  @JsonProperty(PROP_NOT_FAIL_NODE_REGEX)
  public void setNotFailNodeRegex(String regex) {
    _notFailNodeRegex = regex;
  }
}
