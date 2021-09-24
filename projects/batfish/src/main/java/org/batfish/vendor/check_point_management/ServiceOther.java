package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.vendor.check_point_management.parsing.parboiled.BooleanExprAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.EmptyAstNode;
import org.batfish.vendor.check_point_management.parsing.parboiled.ServiceOtherMatchExpr;

/**
 * A custom {@link Service} matching packets by protocol number, and an optional program in INSPECT
 * syntax. The program can match on arbitrary features of the packet - including its classified
 * direction - and the firewall's state.
 */
public final class ServiceOther extends TypedManagementObject implements Service {
  @Override
  public <T> T accept(ServiceVisitor<T> visitor) {
    return visitor.visitServiceOther(this);
  }

  /** Docs: IP protocol number. */
  public int getIpProtocol() {
    return _ipProtocol;
  }

  /** Docs: A string in INSPECT syntax on packet matching. */
  public @Nullable String getMatch() {
    return _match;
  }

  /** An abstract syntax tree resulting from parsing the {@code match} INSPECT string. */
  public @Nonnull BooleanExprAstNode getMatchAst() {
    return _matchAst;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    ServiceOther other = (ServiceOther) o;
    return _ipProtocol == other._ipProtocol && Objects.equals(_match, other._match);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _ipProtocol, _match);
  }

  @Override
  public String toString() {
    return baseToStringHelper()
        .add(PROP_IP_PROTOCOL, _ipProtocol)
        .add(PROP_MATCH, _match)
        .toString();
  }

  @JsonCreator
  private static @Nonnull ServiceOther create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_IP_PROTOCOL) @Nullable Integer ipProtocol,
      @JsonProperty(PROP_MATCH) @Nullable String match,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(ipProtocol != null, "Missing %s", PROP_IP_PROTOCOL);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return of(name, ipProtocol, match, uid);
  }

  @VisibleForTesting
  public static @Nonnull ServiceOther of(
      String name, int ipProtocol, @Nullable String match, Uid uid) {
    BooleanExprAstNode matchAst =
        Optional.ofNullable(match)
            .map(ServiceOtherMatchExpr::parse)
            .orElse(EmptyAstNode.instance());
    return new ServiceOther(name, ipProtocol, match, matchAst, uid);
  }

  private ServiceOther(
      String name, int ipProtocol, @Nullable String match, BooleanExprAstNode matchAst, Uid uid) {
    super(name, uid);
    _ipProtocol = ipProtocol;
    _match = match;
    _matchAst = matchAst;
  }

  private static final String PROP_IP_PROTOCOL = "ip-protocol";
  private static final String PROP_MATCH = "match";

  private final int _ipProtocol;
  private final @Nullable String _match;
  private final @Nonnull BooleanExprAstNode _matchAst;
}
