package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public final class BaseApplication implements Application, Serializable {

  public static final class Term implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    private HeaderSpace _headerSpace;

    public Term() {
      _headerSpace = HeaderSpace.builder().build();
    }

    public void applyTo(HeaderSpace.Builder destinationHeaderSpace) {
      destinationHeaderSpace.setIpProtocols(
          Iterables.concat(destinationHeaderSpace.getIpProtocols(), _headerSpace.getIpProtocols()));
      destinationHeaderSpace.setDstPorts(
          Iterables.concat(destinationHeaderSpace.getDstPorts(), _headerSpace.getDstPorts()));
      destinationHeaderSpace.setSrcPorts(
          Iterables.concat(destinationHeaderSpace.getSrcPorts(), _headerSpace.getSrcPorts()));
    }

    public HeaderSpace getHeaderSpace() {
      return _headerSpace;
    }

    public void setHeaderSpace(HeaderSpace headerSpace) {
      _headerSpace = headerSpace;
    }
  }

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _ipv6;

  private Term _mainTerm;

  private final String _name;

  private final Map<String, Term> _terms;

  public BaseApplication(String name) {
    _name = name;
    _mainTerm = new Term();
    _terms = new LinkedHashMap<>();
  }

  @Override
  public void applyTo(
      JuniperConfiguration jc,
      HeaderSpace.Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<IpAccessListLine> lines,
      Warnings w) {
    Collection<Term> terms;
    if (_terms.isEmpty()) {
      terms = ImmutableList.of(_mainTerm);
    } else {
      terms = _terms.values();
    }
    for (Term term : terms) {
      HeaderSpace oldHeaderSpace = srcHeaderSpaceBuilder.build();
      HeaderSpace.Builder newHeaderSpaceBuilder =
          HeaderSpace.builder()
              .setDstIps(oldHeaderSpace.getDstIps())
              .setSrcIps(oldHeaderSpace.getSrcIps());
      term.applyTo(newHeaderSpaceBuilder);
      IpAccessListLine newLine =
          IpAccessListLine.builder()
              .setAction(action)
              .setMatchCondition(new MatchHeaderSpace(newHeaderSpaceBuilder.build()))
              .build();
      lines.add(newLine);
    }
  }

  @Override
  public boolean getIpv6() {
    return _ipv6;
  }

  public Term getMainTerm() {
    return _mainTerm;
  }

  private String getMainTermName() {
    return "~MAIN_TERM~" + _name;
  }

  public Map<String, Term> getTerms() {
    return _terms;
  }

  public void setIpv6(boolean ipv6) {
    _ipv6 = true;
  }
}
