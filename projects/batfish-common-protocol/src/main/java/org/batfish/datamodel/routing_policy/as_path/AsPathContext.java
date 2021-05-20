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
   * Returns an {@link AsPathContext} if there is a readable {@link AsPath} in the provided {@code
   * environment}, or else {@link Optional#empty()}.
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
