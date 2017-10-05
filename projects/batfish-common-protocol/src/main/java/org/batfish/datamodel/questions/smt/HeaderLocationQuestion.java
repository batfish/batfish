package org.batfish.datamodel.questions.smt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HeaderLocationQuestion extends HeaderQuestion {

  private static final String DEFAULT_FINAL_IFACE_REGEX = ".*";

  private static final String DEFAULT_FINAL_NODE_REGEX = ".*";

  private static final String DEFAULT_INGRESS_NODE_REGEX = ".*";

  private static final String DEFAULT_NOT_FINAL_NODE_REGEX = "";

  private static final String DEFAULT_NOT_FINAL_IFACE_REGEX = "";

  private static final String DEFAULT_NOT_INGRESS_NODE_REGEX = "";

  private static final String PROP_FINAL_NODE_REGEX = "finalNodeRegex";

  private static final String PROP_FINAL_IFACE_REGEX = "finalIfaceRegex";

  private static final String PROP_INGRESS_NODE_REGEX = "ingressNodeRegex";

  private static final String PROP_NOT_FINAL_NODE_REGEX = "notFinalNodeRegex";

  private static final String PROP_NOT_FINAL_IFACE_REGEX = "notFinalIfaceRegex";

  private static final String PROP_NOT_INGRESS_NODE_REGEX = "notIngressNodeRegex";

  private String _finalNodeRegex;

  private String _finalIfaceRegex;

  private String _ingressNodeRegex;

  private String _notFinalNodeRegex;

  private String _notFinalIfaceRegex;

  private String _notIngressNodeRegex;

  public HeaderLocationQuestion() {
    super();
    _finalNodeRegex = DEFAULT_FINAL_NODE_REGEX;
    _finalIfaceRegex = DEFAULT_FINAL_IFACE_REGEX;
    _ingressNodeRegex = DEFAULT_INGRESS_NODE_REGEX;
    _notFinalNodeRegex = DEFAULT_NOT_FINAL_NODE_REGEX;
    _notFinalIfaceRegex = DEFAULT_NOT_FINAL_IFACE_REGEX;
    _notIngressNodeRegex = DEFAULT_NOT_INGRESS_NODE_REGEX;
  }

  public HeaderLocationQuestion(HeaderLocationQuestion other) {
    super(other);
    this._finalNodeRegex = other._finalNodeRegex;
    this._finalIfaceRegex = other._finalIfaceRegex;
    this._ingressNodeRegex = other._ingressNodeRegex;
    this._notFinalNodeRegex = other._notFinalNodeRegex;
    this._notFinalIfaceRegex = other._notFinalIfaceRegex;
    this._notIngressNodeRegex = other._notIngressNodeRegex;
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

  @Override
  public String prettyPrint() {
    return String.format("headerlocation %s", prettyPrintParams());
  }

  protected String prettyPrintParams() {
    try {
      String retString = super.prettyPrintParams();
      if (!_finalNodeRegex.equals(DEFAULT_FINAL_NODE_REGEX)) {
        retString += String.format(", %s=%s", PROP_FINAL_NODE_REGEX, _finalNodeRegex);
      }
      if (!_finalIfaceRegex.equals(DEFAULT_FINAL_IFACE_REGEX)) {
        retString += String.format(", %s=%s", PROP_FINAL_IFACE_REGEX, _finalIfaceRegex);
      }
      if (!_ingressNodeRegex.equals(DEFAULT_INGRESS_NODE_REGEX)) {
        retString += String.format(", %s=%s", PROP_INGRESS_NODE_REGEX, _ingressNodeRegex);
      }
      if (!_notFinalNodeRegex.equals(DEFAULT_NOT_FINAL_NODE_REGEX)) {
        retString += String.format(", %s=%s", PROP_NOT_FINAL_NODE_REGEX, _notFinalNodeRegex);
      }
      if (!_notFinalIfaceRegex.equals(DEFAULT_NOT_FINAL_IFACE_REGEX)) {
        retString += String.format(", %s=%s", PROP_NOT_FINAL_IFACE_REGEX, _notFinalIfaceRegex);
      }
      if (!_notIngressNodeRegex.equals(DEFAULT_NOT_INGRESS_NODE_REGEX)) {
        retString += String.format(", %s=%s", PROP_NOT_INGRESS_NODE_REGEX, _notIngressNodeRegex);
      }
      return retString;
    } catch (Exception e) {
      return "Pretty printing failed. Printing Json\n" + toJsonString();
    }
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
}