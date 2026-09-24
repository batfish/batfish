package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Class-of-service settings applied directly to an interface expression. */
@ParametersAreNonnullByDefault
public final class ClassOfServiceInterface implements Serializable {

  public enum RewriteRuleType {
    DSCP,
    DSCP_IPV6,
    EXP,
    IEEE_802_1,
    INET_PRECEDENCE
  }

  public static final class RewriteRule implements Serializable {
    private final String _name;
    private final @Nullable String _protocol;

    public RewriteRule(String name, @Nullable String protocol) {
      _name = name;
      _protocol = protocol;
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getProtocol() {
      return _protocol;
    }
  }

  private final String _name;
  private final Map<RewriteRuleType, RewriteRule> _rewriteRules;

  public ClassOfServiceInterface(String name) {
    _name = name;
    _rewriteRules = new EnumMap<>(RewriteRuleType.class);
  }

  public String getName() {
    return _name;
  }

  public Map<RewriteRuleType, RewriteRule> getRewriteRules() {
    return _rewriteRules;
  }
}
