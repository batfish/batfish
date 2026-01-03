package org.batfish.datamodel.routing_policy.as_path;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;

/** A visitor for evaluating an {@link AsPathMatchExpr} under an {@link AsPathContext}. */
public final class AsPathExprEvaluator implements AsPathExprVisitor<AsPath, AsPathContext> {

  public static @Nonnull AsPathExprEvaluator instance() {
    return INSTANCE;
  }

  private static final AsPathExprEvaluator INSTANCE = new AsPathExprEvaluator();

  private AsPathExprEvaluator() {}

  @Override
  public AsPath visitAsPathExprReference(
      AsPathExprReference asPathExprReference, AsPathContext arg) {
    AsPathExpr expr = arg.getAsPathExprs().get(asPathExprReference.getName());
    // conversion to VI should guarantee expr is not null
    assert expr != null;
    return expr.accept(this, arg);
  }

  @Override
  public AsPath visitDedupedAsPath(DedupedAsPath dedupedAsPath, AsPathContext arg) {
    AsPath input = dedupedAsPath.getAsPathExpr().accept(this, arg);
    AsSet lastAsSet = null;
    ImmutableList.Builder<AsSet> builder = ImmutableList.builder();
    for (AsSet asSet : input.getAsSets()) {
      if (asSet.equals(lastAsSet)) {
        continue;
      }
      lastAsSet = asSet;
      builder.add(asSet);
    }
    return AsPath.of(builder.build());
  }

  @Override
  public AsPath visitInputAsPath(InputAsPath inputAsPath, AsPathContext arg) {
    return arg.getInputAsPath();
  }
}
