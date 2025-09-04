package org.batfish.datamodel.routing_policy.as_path;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.HasReadableAsPath;
import org.batfish.datamodel.routing_policy.Environment;

/** Context needed to evaluate an expression against a route's as-path attribute. */
public final class AsPathContext {

  public static final class Builder {

    public @Nonnull AsPathContext build() {
      checkArgument(_inputAsPath != null, "Missing asPath");
      return new AsPathContext(
          firstNonNull(_asPathExprs, ImmutableMap.of()),
          firstNonNull(_asPathMatchExprs, ImmutableMap.of()),
          _inputAsPath);
    }

    public @Nonnull Builder setAsPathExprs(Map<String, AsPathExpr> asPathExprs) {
      _asPathExprs = asPathExprs;
      return this;
    }

    public @Nonnull Builder setAsPathMatchExprs(Map<String, AsPathMatchExpr> asPathMatchExprs) {
      _asPathMatchExprs = asPathMatchExprs;
      return this;
    }

    public @Nonnull Builder setInputAsPath(AsPath inputAsPath) {
      _inputAsPath = inputAsPath;
      return this;
    }

    private @Nullable Map<String, AsPathExpr> _asPathExprs;
    private @Nullable Map<String, AsPathMatchExpr> _asPathMatchExprs;
    private @Nullable AsPath _inputAsPath;

    private Builder() {}
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  /**
   * Creates an {@link AsPathContext} from the provided {@link Environment} using the BGP attribute
   * handling decision tree. Returns {@link Optional#empty()} if no readable {@link AsPath} is
   * available in the environment.
   *
   * <p><strong>Decision Tree Implementation:</strong>
   *
   * <p>This method implements the same precedence hierarchy as the core BGP attribute handling
   * system:
   *
   * <ol>
   *   <li><strong>Highest Precedence:</strong> If {@link Environment#getUseOutputAttributes()} is
   *       {@code true} and output route has readable AS-path, use output route AS-path
   *   <li><strong>Medium Precedence:</strong> If {@link
   *       Environment#getReadFromIntermediateBgpAttributes()} is {@code true}, use intermediate BGP
   *       attributes AS-path
   *   <li><strong>Lowest Precedence:</strong> If original route has readable AS-path, use original
   *       route AS-path
   *   <li><strong>No AS-path Available:</strong> Return {@link Optional#empty()}
   * </ol>
   *
   * <p><strong>Vendor-Specific Behavior:</strong>
   *
   * <ul>
   *   <li><strong>Juniper:</strong> Typically uses output route AS-path (first case)
   *   <li><strong>Cisco:</strong> Typically uses original route AS-path (third case)
   * </ul>
   *
   * <p><strong>Route Type Compatibility:</strong> Only routes implementing {@link
   * HasReadableAsPath} can provide AS-path context. Routes without AS-path attributes will result
   * in {@link Optional#empty()}.
   *
   * @param environment the routing policy environment containing route and attribute information
   * @return {@link AsPathContext} if AS-path is available, {@link Optional#empty()} otherwise
   * @see Environment#getUseOutputAttributes()
   * @see Environment#getReadFromIntermediateBgpAttributes()
   * @see HasReadableAsPath
   */
  public static Optional<AsPathContext> fromEnvironment(Environment environment) {
    AsPath inputAsPath = null;
    if (environment.getUseOutputAttributes()
        && environment.getOutputRoute() instanceof HasReadableAsPath) {
      HasReadableAsPath outputRoute = (HasReadableAsPath) environment.getOutputRoute();
      inputAsPath = outputRoute.getAsPath();
    } else if (environment.getReadFromIntermediateBgpAttributes()) {
      inputAsPath = environment.getIntermediateBgpAttributes().getAsPath();
    } else if (environment.getOriginalRoute() instanceof HasReadableAsPath) {
      HasReadableAsPath originalRoute = (HasReadableAsPath) environment.getOriginalRoute();
      inputAsPath = originalRoute.getAsPath();
    }
    if (inputAsPath == null) {
      return Optional.empty();
    }
    // TODO: store information necessary to evaluate dynamic (stateful) expressions
    //       e.g. remote-as, local-as, variable values, etc.
    return Optional.of(
        new AsPathContext(
            environment.getAsPathExprs(), environment.getAsPathMatchExprs(), inputAsPath));
  }

  public @Nonnull AsPathMatchExprEvaluator getAsPathMatchExprEvaluator() {
    return _asPathMatchExprEvaluator;
  }

  public @Nonnull Map<String, AsPathExpr> getAsPathExprs() {
    return _asPathExprs;
  }

  public @Nonnull Map<String, AsPathMatchExpr> getAsPathMatchExprs() {
    return _asPathMatchExprs;
  }

  public @Nonnull AsPath getInputAsPath() {
    return _inputAsPath;
  }

  private AsPathContext(
      Map<String, AsPathExpr> asPathExprs,
      Map<String, AsPathMatchExpr> asPathMatchExprs,
      AsPath inputAsPath) {
    _asPathMatchExprEvaluator = new AsPathMatchExprEvaluator(this);
    _asPathExprs = asPathExprs;
    _asPathMatchExprs = asPathMatchExprs;
    _inputAsPath = inputAsPath;
  }

  private final @Nonnull AsPathMatchExprEvaluator _asPathMatchExprEvaluator;
  private final @Nonnull Map<String, AsPathExpr> _asPathExprs;
  private final @Nonnull Map<String, AsPathMatchExpr> _asPathMatchExprs;
  private final @Nonnull AsPath _inputAsPath;
}
