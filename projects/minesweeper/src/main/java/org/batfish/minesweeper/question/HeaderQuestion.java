package org.batfish.minesweeper.question;

import static org.batfish.minesweeper.utils.PrefixUtils.asNegativeIpWildcards;
import static org.batfish.minesweeper.utils.PrefixUtils.asPositiveIpWildcards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.smt.BgpDecisionVariable;
import org.batfish.datamodel.questions.smt.DiffType;
import org.batfish.datamodel.questions.smt.EnvironmentType;

public class HeaderQuestion extends Question {
  private static final String PROP_DST_IPS = "dstIps";
  private static final String PROP_DST_PORTS = "dstPorts";
  private static final String PROP_FRAGMENT_OFFSETS = "fragmentOffsets";
  private static final String PROP_ICMP_CODES = "icmpCodes";
  private static final String PROP_ICMP_TYPES = "icmpTypes";
  private static final String PROP_IP_PROTOCOLS = "ipProtocols";
  private static final String PROP_NOT_DST_IPS = "notDstIps";
  private static final String PROP_NOT_DST_PORTS = "notDstPorts";
  private static final String PROP_NOT_FRAGMENT_OFFSETS = "notFragmentOffsets";
  private static final String PROP_NOT_ICMP_CODES = "notIcmpCodes";
  private static final String PROP_NOT_ICMP_TYPES = "notIcmpTypes";
  private static final String PROP_NOT_IP_PROTOCOLS = "notIpProtocols";
  private static final String PROP_NOT_SRC_IPS = "notSrcIps";
  private static final String PROP_NOT_SRC_PORTS = "notSrcPorts";
  private static final String PROP_SRC_IPS = "srcIps";
  private static final String PROP_SRC_OR_DST_IPS = "srcOrDstIps";
  private static final String PROP_SRC_OR_DST_PORTS = "srcOrDstPorts";
  private static final String PROP_SRC_PORTS = "srcPorts";
  private static final String PROP_FAILURES = "failures";
  private static final String PROP_NODE_FAILURES = "nodeFailures";
  private static final String PROP_FULL_MODEL = "fullModel";
  private static final String PROP_MINIMIZE = "minimize";
  private static final String PROP_DIFF_TYPE = "diffType";
  private static final String PROP_BGP_RANKING = "bgpRanking";
  private static final String PROP_ENV_DIFF = "envDiff";
  private static final String PROP_BASE_ENV_TYPE = "baseEnvType";
  private static final String PROP_DIFF_ENV_TYPE = "deltaEnvType";
  private static final String PROP_MODEL_OVERFLOW = "modelOverflow";
  private static final String PROP_USE_ABSTRACTION = "useAbstraction";
  private static final String PROP_BENCHMARK = "benchmark";

  private Set<FlowDisposition> _actions;

  private HeaderSpace _headerSpace;

  private int _failures;

  private int _nodeFailures;

  private boolean _fullModel;

  private boolean _noEnvironment;

  private boolean _minimize;

  private DiffType _diffType;

  private List<BgpDecisionVariable> _bgpRanking;

  private boolean _envDiff;

  private EnvironmentType _baseEnvType;

  private EnvironmentType _deltaEnvType;

  private boolean _modelOverflow;

  private boolean _useAbstraction;

  private boolean _stats;

  private boolean _benchmark;

  public HeaderQuestion() {
    _actions = EnumSet.of(FlowDisposition.ACCEPTED);
    _headerSpace = new HeaderSpace();
    _failures = 0;
    _nodeFailures = 0;
    _fullModel = false;
    _noEnvironment = false;
    _minimize = false;
    _diffType = null;
    _bgpRanking = new ArrayList<>();
    _envDiff = false;
    _baseEnvType = EnvironmentType.ANY;
    _deltaEnvType = EnvironmentType.ANY;
    _modelOverflow = false;
    _useAbstraction = false;
    _stats = false;
    _benchmark = false;
    _bgpRanking.add(BgpDecisionVariable.LOCALPREF);
    _bgpRanking.add(BgpDecisionVariable.PATHLEN);
    _bgpRanking.add(BgpDecisionVariable.MED);
    _bgpRanking.add(BgpDecisionVariable.EBGP_PREF_IBGP);
    _bgpRanking.add(BgpDecisionVariable.IGPCOST);
  }

  public HeaderQuestion(HeaderQuestion q) {
    _actions = q._actions;
    _headerSpace = q._headerSpace;
    _failures = q._failures;
    _nodeFailures = q._nodeFailures;
    _fullModel = q._fullModel;
    _noEnvironment = q._noEnvironment;
    _minimize = q._minimize;
    _diffType = q._diffType;
    _bgpRanking = new ArrayList<>(q._bgpRanking);
    _envDiff = q._envDiff;
    _baseEnvType = q._baseEnvType;
    _deltaEnvType = q._deltaEnvType;
    _modelOverflow = q._modelOverflow;
    _useAbstraction = q._useAbstraction;
    _stats = q._stats;
    _benchmark = q._benchmark;
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @JsonProperty(PROP_DST_IPS)
  public SortedSet<IpWildcard> getDstIps() {
    return asPositiveIpWildcards(_headerSpace.getDstIps());
  }

  @JsonProperty(PROP_DST_PORTS)
  public Set<SubRange> getDstPorts() {
    return _headerSpace.getDstPorts();
  }

  @JsonProperty(PROP_FRAGMENT_OFFSETS)
  public Set<SubRange> getFragmentOffsets() {
    return _headerSpace.getFragmentOffsets();
  }

  @JsonIgnore
  public HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  @JsonIgnore
  public void setHeaderSpace(HeaderSpace h) {
    _headerSpace = h;
  }

  @JsonProperty(PROP_ICMP_CODES)
  public Set<SubRange> getIcmpCodes() {
    return _headerSpace.getIcmpCodes();
  }

  @JsonProperty(PROP_ICMP_TYPES)
  public Set<SubRange> getIcmpTypes() {
    return _headerSpace.getIcmpTypes();
  }

  @JsonProperty(PROP_IP_PROTOCOLS)
  public Set<IpProtocol> getIpProtocols() {
    return _headerSpace.getIpProtocols();
  }

  @Override
  public String getName() {
    throw new BatfishException("Unimplemented getEnvName");
  }

  @JsonProperty(PROP_NOT_DST_IPS)
  public SortedSet<IpWildcard> getNotDstIps() {
    return asNegativeIpWildcards(_headerSpace.getNotDstIps());
  }

  @JsonProperty(PROP_NOT_DST_PORTS)
  public Set<SubRange> getNotDstPorts() {
    return _headerSpace.getNotDstPorts();
  }

  @JsonProperty(PROP_NOT_FRAGMENT_OFFSETS)
  private Set<SubRange> getNotFragmentOffsets() {
    return _headerSpace.getNotFragmentOffsets();
  }

  @JsonProperty(PROP_NOT_ICMP_CODES)
  public Set<SubRange> getNotIcmpCodes() {
    return _headerSpace.getNotIcmpCodes();
  }

  @JsonProperty(PROP_NOT_ICMP_TYPES)
  public Set<SubRange> getNotIcmpTypes() {
    return _headerSpace.getNotIcmpTypes();
  }

  @JsonProperty(PROP_NOT_IP_PROTOCOLS)
  public Set<IpProtocol> getNotIpProtocols() {
    return _headerSpace.getNotIpProtocols();
  }

  @JsonProperty(PROP_NOT_SRC_IPS)
  public SortedSet<IpWildcard> getNotSrcIps() {
    return asNegativeIpWildcards(_headerSpace.getNotSrcIps());
  }

  @JsonProperty(PROP_NOT_SRC_PORTS)
  public Set<SubRange> getNotSrcPorts() {
    return _headerSpace.getNotSrcPorts();
  }

  @JsonProperty(PROP_SRC_IPS)
  public SortedSet<IpWildcard> getSrcIps() {
    return asPositiveIpWildcards(_headerSpace.getSrcIps());
  }

  @JsonProperty(PROP_SRC_OR_DST_IPS)
  public SortedSet<IpWildcard> getSrcOrDstIps() {
    return asPositiveIpWildcards(_headerSpace.getSrcOrDstIps());
  }

  @JsonProperty(PROP_SRC_OR_DST_PORTS)
  public Set<SubRange> getSrcOrDstPorts() {
    return _headerSpace.getSrcOrDstPorts();
  }

  @JsonProperty(PROP_SRC_PORTS)
  public Set<SubRange> getSrcPorts() {
    return _headerSpace.getSrcPorts();
  }

  @JsonProperty(PROP_FAILURES)
  public int getFailures() {
    return _failures;
  }

  @JsonProperty(PROP_NODE_FAILURES)
  public int getNodeFailures() {
    return _nodeFailures;
  }

  @JsonProperty(PROP_FULL_MODEL)
  public boolean getFullModel() {
    return _fullModel;
  }

  @JsonProperty(PROP_MINIMIZE)
  public boolean getMinimize() {
    return _minimize;
  }

  @JsonProperty(PROP_DIFF_TYPE)
  public DiffType getDiffType() {
    return _diffType;
  }

  /**
   * Represents the list of criteria according to which BGP best path selection occurs. Criteria are
   * used in the order specified by the list.
   */
  @JsonProperty(PROP_BGP_RANKING)
  public List<BgpDecisionVariable> getBgpRanking() {
    return _bgpRanking;
  }

  @JsonProperty(PROP_ENV_DIFF)
  public boolean getEnvDiff() {
    return _envDiff;
  }

  @JsonProperty(PROP_BASE_ENV_TYPE)
  public EnvironmentType getBaseEnvironmentType() {
    return _baseEnvType;
  }

  @JsonProperty(PROP_DIFF_ENV_TYPE)
  public EnvironmentType getDeltaEnvironmentType() {
    return _deltaEnvType;
  }

  @JsonProperty(PROP_MODEL_OVERFLOW)
  public boolean getModelOverflow() {
    return _modelOverflow;
  }

  @JsonProperty(PROP_USE_ABSTRACTION)
  public boolean getUseAbstraction() {
    return _useAbstraction;
  }

  @JsonProperty(PROP_BENCHMARK)
  public boolean getBenchmark() {
    return _benchmark;
  }

  @JsonProperty(PROP_NOT_DST_IPS)
  public void setNotDstIps(Set<IpWildcard> notDstIps) {
    _headerSpace.setNotDstIps(new TreeSet<>(notDstIps));
  }

  @JsonProperty(PROP_NOT_DST_PORTS)
  public void setNotDstPorts(Set<SubRange> notDstPorts) {
    _headerSpace.setNotDstPorts(new TreeSet<>(notDstPorts));
  }

  @JsonProperty(PROP_NOT_ICMP_CODES)
  public void setNotIcmpCodes(Set<SubRange> notIcmpCodes) {
    _headerSpace.setNotIcmpCodes(new TreeSet<>(notIcmpCodes));
  }

  @JsonProperty(PROP_NOT_ICMP_TYPES)
  public void setNotIcmpTypes(Set<SubRange> notIcmpType) {
    _headerSpace.setNotIcmpTypes(new TreeSet<>(notIcmpType));
  }

  @JsonProperty(PROP_NOT_IP_PROTOCOLS)
  public void setNotIpProtocols(Set<IpProtocol> notIpProtocols) {
    _headerSpace.setNotIpProtocols(new TreeSet<>(notIpProtocols));
  }

  @JsonProperty(PROP_NOT_SRC_IPS)
  public void setNotSrcIps(Set<IpWildcard> notSrcIps) {
    _headerSpace.setNotSrcIps(new TreeSet<>(notSrcIps));
  }

  @JsonProperty(PROP_NOT_SRC_PORTS)
  public void setNotSrcPortRange(Set<SubRange> notSrcPorts) {
    _headerSpace.setNotSrcPorts(new TreeSet<>(notSrcPorts));
  }

  @JsonProperty(PROP_SRC_IPS)
  public void setSrcIps(Set<IpWildcard> srcIps) {
    _headerSpace.setSrcIps(new TreeSet<>(srcIps));
  }

  @JsonProperty(PROP_SRC_OR_DST_IPS)
  public void setSrcOrDstIps(Set<IpWildcard> srcOrDstIps) {
    _headerSpace.setSrcOrDstIps(new TreeSet<>(srcOrDstIps));
  }

  @JsonProperty(PROP_SRC_OR_DST_PORTS)
  public void setSrcOrDstPorts(Set<SubRange> srcOrDstPorts) {
    _headerSpace.setSrcOrDstPorts(new TreeSet<>(srcOrDstPorts));
  }

  @JsonProperty(PROP_SRC_PORTS)
  public void setSrcPorts(Set<SubRange> srcPorts) {
    _headerSpace.setSrcPorts(new TreeSet<>(srcPorts));
  }

  @JsonProperty(PROP_DST_IPS)
  public void setDstIps(Set<IpWildcard> dstIps) {
    _headerSpace.setDstIps(new TreeSet<>(dstIps));
  }

  @JsonProperty(PROP_DST_PORTS)
  public void setDstPorts(Set<SubRange> dstPorts) {
    _headerSpace.setDstPorts(new TreeSet<>(dstPorts));
  }

  @JsonProperty(PROP_ICMP_CODES)
  public void setIcmpCodes(Set<SubRange> icmpCodes) {
    _headerSpace.setIcmpCodes(new TreeSet<>(icmpCodes));
  }

  @JsonProperty(PROP_ICMP_TYPES)
  public void setIcmpTypes(Set<SubRange> icmpTypes) {
    _headerSpace.setIcmpTypes(new TreeSet<>(icmpTypes));
  }

  @JsonProperty(PROP_IP_PROTOCOLS)
  public void setIpProtocols(Set<IpProtocol> ipProtocols) {
    _headerSpace.setIpProtocols(new TreeSet<>(ipProtocols));
  }

  @JsonProperty(PROP_FAILURES)
  public void setFailures(int k) {
    _failures = k;
  }

  @JsonProperty(PROP_NODE_FAILURES)
  public void setNodeFailures(int k) {
    _nodeFailures = k;
  }

  @JsonProperty(PROP_FULL_MODEL)
  public void setFullModel(boolean b) {
    _fullModel = b;
  }

  @JsonProperty(PROP_MINIMIZE)
  public void setMinimize(boolean b) {
    _minimize = b;
  }

  @JsonProperty(PROP_DIFF_TYPE)
  public void setDiffType(DiffType d) {
    _diffType = d;
  }

  @JsonProperty(PROP_BGP_RANKING)
  public void setBgpRanking(List<BgpDecisionVariable> r) {

    EnumSet<BgpDecisionVariable> rset = EnumSet.noneOf(BgpDecisionVariable.class);
    rset.addAll(r);
    if (rset.size() != r.size()) {
      throw new BatfishException("Duplicate BGP decision variable in question");
    }
    _bgpRanking = r;
  }

  @JsonProperty(PROP_ENV_DIFF)
  public void setEnvDiff(boolean b) {
    _envDiff = b;
  }

  @JsonProperty(PROP_BASE_ENV_TYPE)
  public void setBaseEnvironmentType(EnvironmentType e) {
    _baseEnvType = e;
  }

  @JsonProperty(PROP_DIFF_ENV_TYPE)
  public void setDeltaEnvironmentType(EnvironmentType e) {
    _deltaEnvType = e;
  }

  @JsonProperty(PROP_MODEL_OVERFLOW)
  public void setModelOverflow(boolean x) {
    _modelOverflow = x;
  }

  @JsonProperty(PROP_USE_ABSTRACTION)
  public void setUseAbstraction(boolean x) {
    _useAbstraction = x;
  }

  @JsonProperty(PROP_BENCHMARK)
  public void setBenchmark(boolean x) {
    _benchmark = x;
  }
}
