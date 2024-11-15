package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
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

    public Term(@Nonnull String tracingName) {
      _headerSpace = HeaderSpace.builder().build();
      _tracingName = tracingName;
    }

    public @Nullable String getTracingName() {
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

    HeaderSpace toHeaderSpace() {
      return HeaderSpace.builder()
          .setIpProtocols(_headerSpace.getIpProtocols())
          .setDstPorts(_headerSpace.getDstPorts())
          .setSrcPorts(_headerSpace.getSrcPorts())
          .build();
    }

    public AclLineMatchExpr toAclLineMatchExpr() {
      HeaderSpace destinationHeaderSpace = toHeaderSpace();
      return _tracingName != null
          ? new MatchHeaderSpace(
              destinationHeaderSpace,
              TraceElement.of(String.format("Matched term %s", _tracingName)))
          : new MatchHeaderSpace(destinationHeaderSpace);
    }
  }

  private final boolean _builtIn;

  private boolean _ipv6;

  private Term _mainTerm;

  private final Map<String, Term> _terms;

  private final String _name;

  public BaseApplication(String name, boolean builtIn) {
    _mainTerm = new Term();
    _terms = new LinkedHashMap<>();
    _name = name;
    _builtIn = builtIn;
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
    return ExprAclLine.builder()
        .setAction(action)
        .setMatchCondition(new MatchHeaderSpace(newHeaderSpaceBuilder.build()))
        .setTraceElement(getTermTraceElement(term.getTracingName()))
        .build();
  }

  TraceElement getTermTraceElement(@Nullable String termTracingName) {
    String termDesc = termTracingName == null ? "" : String.format(" term %s", termTracingName);
    return TraceElement.of(String.format("Matched application %s%s", _name, termDesc));
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

  /**
   * Convert this {@link BaseApplication} to an {@link AclLineMatchExpr}, but using the given {@link
   * TraceElement}. This enables the same conversion code to be used for built-in and user-defined
   * applications.
   */
  public AclLineMatchExpr toAclLineMatchExpr(TraceElement topLevelTraceElement) {
    if (_terms.isEmpty()) {
      return new MatchHeaderSpace(_mainTerm.toHeaderSpace(), topLevelTraceElement);
    }

    return or(
        _terms.values().stream()
            .map(Term::toAclLineMatchExpr)
            .collect(ImmutableList.toImmutableList()),
        topLevelTraceElement);
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Warnings w) {
    TraceElement traceElement =
        isBuiltIn()
            ? JunosApplication.getTraceElementForBuiltInApplication(_name)
            : ApplicationSetMember.getTraceElementForUserApplication(
                jc.getFilename(), JuniperStructureType.APPLICATION, _name);
    return toAclLineMatchExpr(traceElement);
  }

  @Override
  public boolean isBuiltIn() {
    return _builtIn;
  }
}
