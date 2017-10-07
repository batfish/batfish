package org.batfish.datamodel.questions.smt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.questions.IQuestion;
import org.batfish.datamodel.questions.Question;

public class HeaderQuestion extends Question implements IQuestion {

  private static final String DST_IPS_VAR = "dstIps";

  private static final String DST_PORTS_VAR = "dstPorts";

  private static final String FRAGMENT_OFFSETS_VAR = "fragmentOffsets";

  private static final String ICMP_CODES_VAR = "icmpCodes";

  private static final String ICMP_TYPES_VAR = "icmpTypes";

  private static final String IP_PROTOCOLS_VAR = "ipProtocols";

  private static final String NOT_DST_IPS_VAR = "notDstIps";

  private static final String NOT_DST_PORTS_VAR = "notDstPorts";

  private static final String NOT_FRAGMENT_OFFSETS_VAR = "notFragmentOffsets";

  private static final String NOT_ICMP_CODE_VAR = "notIcmpCodes";

  private static final String NOT_ICMP_TYPE_VAR = "notIcmpTypes";

  private static final String NOT_IP_PROTOCOLS_VAR = "notIpProtocols";

  private static final String NOT_SRC_IPS_VAR = "notSrcIps";

  private static final String NOT_SRC_PORTS_VAR = "notSrcPorts";

  private static final String SRC_IPS_VAR = "srcIps";

  private static final String SRC_OR_DST_IPS_VAR = "srcOrDstIps";

  private static final String SRC_OR_DST_PORTS_VAR = "srcOrDstPorts";

  private static final String SRC_PORTS_VAR = "srcPorts";

  private static final String FAILURES_VAR = "failures";

  private static final String FULL_MODEL_VAR = "fullModel";

  private static final String NO_ENVIRONMENT_VAR = "noEnvironment";

  private static final String MINIMIZE_VAR = "minimize";

  private static final String DIFF_TYPE_VAR = "diffType";

  private Set<ForwardingAction> _actions;

  private final HeaderSpace _headerSpace;

  private int _failures;

  private boolean _fullModel;

  private boolean _noEnvironment;

  private boolean _minimize;

  private DiffType _diffType;

  public HeaderQuestion() {
    _actions = EnumSet.of(ForwardingAction.ACCEPT);
    _headerSpace = new HeaderSpace();
    _failures = 0;
    _fullModel = false;
    _noEnvironment = false;
    _minimize = false;
    _diffType = null;
  }

  public HeaderQuestion(HeaderQuestion q) {
    _actions = q._actions;
    _headerSpace = q._headerSpace;
    _failures = q._failures;
    _fullModel = q._fullModel;
    _noEnvironment = q._noEnvironment;
    _minimize = q._minimize;
    _diffType = q._diffType;
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @JsonProperty(DST_IPS_VAR)
  public Set<IpWildcard> getDstIps() {
    return _headerSpace.getDstIps();
  }

  @JsonProperty(DST_PORTS_VAR)
  public Set<SubRange> getDstPorts() {
    return _headerSpace.getDstPorts();
  }

  @JsonProperty(FRAGMENT_OFFSETS_VAR)
  public Set<SubRange> getFragmentOffsets() {
    return _headerSpace.getFragmentOffsets();
  }

  @JsonIgnore
  public HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  @JsonProperty(ICMP_CODES_VAR)
  public Set<SubRange> getIcmpCodes() {
    return _headerSpace.getIcmpCodes();
  }

  @JsonProperty(ICMP_TYPES_VAR)
  public Set<SubRange> getIcmpTypes() {
    return _headerSpace.getIcmpTypes();
  }

  @JsonProperty(IP_PROTOCOLS_VAR)
  public Set<IpProtocol> getIpProtocols() {
    return _headerSpace.getIpProtocols();
  }

  @Override
  public String getName() {
    throw new BatfishException("Unimplemented getName");
  }

  @JsonProperty(NOT_DST_IPS_VAR)
  public Set<IpWildcard> getNotDstIps() {
    return _headerSpace.getNotDstIps();
  }

  @JsonProperty(NOT_DST_PORTS_VAR)
  public Set<SubRange> getNotDstPorts() {
    return _headerSpace.getNotDstPorts();
  }

  @JsonProperty(NOT_FRAGMENT_OFFSETS_VAR)
  private Set<SubRange> getNotFragmentOffsets() {
    return _headerSpace.getNotFragmentOffsets();
  }

  @JsonProperty(NOT_ICMP_CODE_VAR)
  public Set<SubRange> getNotIcmpCodes() {
    return _headerSpace.getNotIcmpCodes();
  }

  @JsonProperty(NOT_ICMP_TYPE_VAR)
  public Set<SubRange> getNotIcmpTypes() {
    return _headerSpace.getNotIcmpTypes();
  }

  @JsonProperty(NOT_IP_PROTOCOLS_VAR)
  public Set<IpProtocol> getNotIpProtocols() {
    return _headerSpace.getNotIpProtocols();
  }

  @JsonProperty(NOT_SRC_IPS_VAR)
  public Set<IpWildcard> getNotSrcIps() {
    return _headerSpace.getNotSrcIps();
  }

  @JsonProperty(NOT_SRC_PORTS_VAR)
  public Set<SubRange> getNotSrcPorts() {
    return _headerSpace.getNotSrcPorts();
  }

  @JsonProperty(SRC_IPS_VAR)
  public Set<IpWildcard> getSrcIps() {
    return _headerSpace.getSrcIps();
  }

  @JsonProperty(SRC_OR_DST_IPS_VAR)
  public Set<IpWildcard> getSrcOrDstIps() {
    return _headerSpace.getSrcOrDstIps();
  }

  @JsonProperty(SRC_OR_DST_PORTS_VAR)
  public Set<SubRange> getSrcOrDstPorts() {
    return _headerSpace.getSrcOrDstPorts();
  }

  @JsonProperty(SRC_PORTS_VAR)
  public Set<SubRange> getSrcPorts() {
    return _headerSpace.getSrcPorts();
  }

  @JsonProperty(FAILURES_VAR)
  public int getFailures() {
    return _failures;
  }

  @JsonProperty(FULL_MODEL_VAR)
  public boolean getFullModel() {
    return _fullModel;
  }

  @JsonProperty(NO_ENVIRONMENT_VAR)
  public boolean getNoEnvironment() {
    return _noEnvironment;
  }

  @JsonProperty(MINIMIZE_VAR)
  public boolean getMinimize() {
    return _minimize;
  }

  @JsonProperty(DIFF_TYPE_VAR)
  public DiffType getDiffType() {
    return _diffType;
  }

  @Override
  public boolean getTraffic() {
    return true;
  }

  @Override
  public String prettyPrint() {
    try {
      String retString = String.format("reachability %sactions=%s",
          prettyPrintBase(), _actions.toString());
      if (getDstPorts() != null && getDstPorts().size() != 0) {
        retString += String.format(" | dstPorts=%s", getDstPorts());
      }
      if (getDstIps() != null && getDstIps().size() != 0) {
        retString += String.format(" | dstIps=%s", getDstIps());
      }
      if (getFragmentOffsets() != null
          && getFragmentOffsets().size() != 0) {
        retString += String.format(" | fragmentOffsets=%s",
            getFragmentOffsets());
      }
      if (getIcmpCodes() != null && getIcmpCodes().size() != 0) {
        retString += String.format(" | icmpCodes=%s", getIcmpCodes());
      }
      if (getIcmpTypes() != null && getIcmpTypes().size() != 0) {
        retString += String.format(" | icmpTypes=%s", getIcmpTypes());
      }
      if (getIpProtocols() != null && getIpProtocols().size() != 0) {
        retString += String.format(" | ipProtocols=%s",
            getIpProtocols().toString());
      }
      if (getSrcOrDstPorts() != null && getSrcOrDstPorts().size() != 0) {
        retString += String.format(" | srcOrDstPorts=%s",
            getSrcOrDstPorts());
      }
      if (getSrcOrDstIps() != null && getSrcOrDstIps().size() != 0) {
        retString += String.format(" | srcOrDstIps=%s",
            getSrcOrDstIps());
      }
      if (getSrcIps() != null && getSrcIps().size() != 0) {
        retString += String.format(" | srcIps=%s", getSrcIps());
      }
      if (getSrcPorts() != null && getSrcPorts().size() != 0) {
        retString += String.format(" | srcPorts=%s", getSrcPorts());
      }
      if (getNotDstPorts() != null && getNotDstPorts().size() != 0) {
        retString += String.format(" | notDstPorts=%s",
            getNotDstPorts());
      }
      if (getNotDstIps() != null && getNotDstIps().size() != 0) {
        retString += String.format(" | notDstIps=%s", getNotDstIps());
      }
      if (getNotFragmentOffsets() != null
          && getNotFragmentOffsets().size() != 0) {
        retString += String.format(" | notFragmentOffsets=%s",
            getNotFragmentOffsets());
      }
      if (getNotIcmpCodes() != null && getNotIcmpCodes().size() != 0) {
        retString += String.format(" | notIcmpCodes=%s",
            getNotIcmpCodes());
      }
      if (getNotIcmpTypes() != null && getNotIcmpTypes().size() != 0) {
        retString += String.format(" | notIcmpTypes=%s",
            getNotIcmpTypes());
      }
      if (getNotIpProtocols() != null
          && getNotIpProtocols().size() != 0) {
        retString += String.format(" | notIpProtocols=%s",
            getNotIpProtocols().toString());
      }
      if (getNotSrcIps() != null && getNotSrcIps().size() != 0) {
        retString += String.format(" | notSrcIps=%s", getNotSrcIps());
      }
      if (getNotSrcPorts() != null && getNotSrcPorts().size() != 0) {
        retString += String.format(" | notSrcPorts=%s",
            getNotSrcPorts());
      }
      return retString;
    } catch (Exception e) {
      return "Pretty printing failed. Printing Json\n" + toJsonString();
    }
  }

  @JsonProperty(NOT_DST_IPS_VAR)
  public void setNotDstIps(Set<IpWildcard> notDstIps) {
    _headerSpace.setNotDstIps(new TreeSet<>(notDstIps));
  }

  @JsonProperty(NOT_DST_PORTS_VAR)
  public void setNotDstPorts(Set<SubRange> notDstPorts) {
    _headerSpace.setNotDstPorts(new TreeSet<>(notDstPorts));
  }

  @JsonProperty(NOT_ICMP_CODE_VAR)
  public void setNotIcmpCodes(Set<SubRange> notIcmpCodes) {
    _headerSpace.setNotIcmpCodes(new TreeSet<>(notIcmpCodes));
  }

  @JsonProperty(NOT_ICMP_TYPE_VAR)
  public void setNotIcmpTypes(Set<SubRange> notIcmpType) {
    _headerSpace.setNotIcmpTypes(new TreeSet<>(notIcmpType));
  }

  @JsonProperty(NOT_IP_PROTOCOLS_VAR)
  public void setNotIpProtocols(Set<IpProtocol> notIpProtocols) {
    _headerSpace.setNotIpProtocols(new TreeSet<>(notIpProtocols));
  }

  @JsonProperty(NOT_SRC_IPS_VAR)
  public void setNotSrcIps(Set<IpWildcard> notSrcIps) {
    _headerSpace.setNotSrcIps(new TreeSet<>(notSrcIps));
  }

  @JsonProperty(NOT_SRC_PORTS_VAR)
  public void setNotSrcPortRange(Set<SubRange> notSrcPorts) {
    _headerSpace.setNotSrcPorts(new TreeSet<>(notSrcPorts));
  }

  @JsonProperty(SRC_IPS_VAR)
  public void setSrcIps(Set<IpWildcard> srcIps) {
    _headerSpace.setSrcIps(new TreeSet<>(srcIps));
  }

  @JsonProperty(SRC_OR_DST_IPS_VAR)
  public void setSrcOrDstIps(Set<IpWildcard> srcOrDstIps) {
    _headerSpace.setSrcOrDstIps(new TreeSet<>(srcOrDstIps));
  }

  @JsonProperty(SRC_OR_DST_PORTS_VAR)
  public void setSrcOrDstPorts(Set<SubRange> srcOrDstPorts) {
    _headerSpace.setSrcOrDstPorts(new TreeSet<>(srcOrDstPorts));
  }

  @JsonProperty(SRC_PORTS_VAR)
  public void setSrcPorts(Set<SubRange> srcPorts) {
    _headerSpace.setSrcPorts(new TreeSet<>(srcPorts));
  }

  @JsonProperty(DST_IPS_VAR)
  public void setDstIps(Set<IpWildcard> dstIps) {
    _headerSpace.setDstIps(new TreeSet<>(dstIps));
  }

  @JsonProperty(DST_PORTS_VAR)
  public void setDstPorts(Set<SubRange> dstPorts) {
    _headerSpace.setDstPorts(new TreeSet<>(dstPorts));
  }

  @JsonProperty(ICMP_CODES_VAR)
  public void setIcmpCodes(Set<SubRange> icmpCodes) {
    _headerSpace.setIcmpCodes(new TreeSet<>(icmpCodes));
  }

  @JsonProperty(ICMP_TYPES_VAR)
  public void setIcmpTypes(Set<SubRange> icmpTypes) {
    _headerSpace.setIcmpTypes(new TreeSet<>(icmpTypes));
  }

  @JsonProperty(IP_PROTOCOLS_VAR)
  public void setIpProtocols(Set<IpProtocol> ipProtocols) {
    _headerSpace.setIpProtocols(new TreeSet<>(ipProtocols));
  }

  @JsonProperty(FAILURES_VAR)
  public void setFailures(int k) {
    _failures = k;
  }

  @JsonProperty(FULL_MODEL_VAR)
  public void setFullModel(boolean b) {
    _fullModel = b;
  }

  @JsonProperty(NO_ENVIRONMENT_VAR)
  public void setNoEnvironment(boolean b) {
    _noEnvironment = b;
  }

  @JsonProperty(MINIMIZE_VAR)
  public void setMinimize(boolean b) {
    _minimize = b;
  }

  @JsonProperty(DIFF_TYPE_VAR)
  public void setDiffType(DiffType d) {
    _diffType = d;
  }

}