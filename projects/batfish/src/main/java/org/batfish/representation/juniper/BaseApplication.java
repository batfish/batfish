package org.batfish.representation.juniper;

import com.google.common.collect.Iterables;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public final class BaseApplication implements Application, Serializable {

  public static final class Term implements Serializable {

    private HeaderSpace _headerSpace;

    // _tracingName is null if and only if this term does not appear in the config (i.e., generated
    // by BF)
    private @Nullable String _tracingName;

    public Term() {
      _headerSpace = HeaderSpace.builder().build();
      _tracingName = null;
    }

    public Term(String tracingName) {
      _headerSpace = HeaderSpace.builder().build();
      _tracingName = tracingName;
    }

    @Nullable
    public String getTracingName() {
      return _tracingName;
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

  private boolean _ipv6;

  private Term _mainTerm;

  private final Map<String, Term> _terms;

  private String _name;

  public BaseApplication(String name) {
    _mainTerm = new Term();
    _terms = new LinkedHashMap<>();
    _name = name;
  }

  @Override
  public void applyTo(
      JuniperConfiguration jc,
      HeaderSpace.Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<? super ExprAclLine> lines,
      Warnings w) {
    HeaderSpace oldHeaderSpace = srcHeaderSpaceBuilder.build();
    if (_terms.isEmpty()) {
      lines.add(termToExprAclLine(_mainTerm, oldHeaderSpace, action));
    } else {
      _terms.values().forEach((term) -> lines.add(termToExprAclLine(term, oldHeaderSpace, action)));
    }
  }

  private ExprAclLine termToExprAclLine(Term term, HeaderSpace oldHeaderSpace, LineAction action) {
    HeaderSpace.Builder newHeaderSpaceBuilder =
        HeaderSpace.builder()
            .setDstIps(oldHeaderSpace.getDstIps())
            .setSrcIps(oldHeaderSpace.getSrcIps());
    term.applyTo(newHeaderSpaceBuilder);
    String termName = term.getTracingName();
    String termDesc = termName == null ? "" : String.format(" term %s", termName);
    return ExprAclLine.builder()
        .setAction(action)
        .setMatchCondition(new MatchHeaderSpace(newHeaderSpaceBuilder.build()))
        .setTraceElement(
            TraceElement.of(String.format("Matched application %s%s", _name, termDesc)))
        .build();
  }

  @Override
  public boolean getIpv6() {
    return _ipv6;
  }

  public Term getMainTerm() {
    return _mainTerm;
  }

  public Map<String, Term> getTerms() {
    return _terms;
  }

  public void setIpv6(boolean ipv6) {
    _ipv6 = true;
  }
}
