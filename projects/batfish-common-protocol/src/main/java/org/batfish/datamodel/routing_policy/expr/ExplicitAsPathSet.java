package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;

/**
 * An {@link AsPathSetExpr} that matches an {@link AsPath} if <em>any</em> of the nested {@link
 * AsPathSetElem} matches the path.
 *
 * <p>Initially added for for IOS-XR, see
 * https://www.cisco.com/c/en/us/td/docs/routers/crs/software/crs_r4-2/routing/command/reference/b_routing_cr42crs/b_routing_cr42crs_chapter_01000.html#wp1469900877.
 */
@ParametersAreNonnullByDefault
public final class ExplicitAsPathSet extends AsPathSetExpr {
  private static final String PROP_ELEMS = "elems";

  @Nonnull private List<AsPathSetElem> _elems;

  @JsonCreator
  private static ExplicitAsPathSet jsonCreator(
      @Nullable @JsonProperty(PROP_ELEMS) List<AsPathSetElem> elems) {
    return new ExplicitAsPathSet(firstNonNull(elems, ImmutableList.of()));
  }

  public ExplicitAsPathSet(AsPathSetElem... elems) {
    this(Arrays.asList(elems));
  }

  public ExplicitAsPathSet(Iterable<AsPathSetElem> elems) {
    _elems = ImmutableList.copyOf(elems);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof ExplicitAsPathSet)) {
      return false;
    }
    ExplicitAsPathSet other = (ExplicitAsPathSet) obj;
    return _elems.equals(other._elems);
  }

  @JsonProperty(PROP_ELEMS)
  @Nonnull
  public List<AsPathSetElem> getElems() {
    return _elems;
  }

  @Override
  public int hashCode() {
    return _elems.hashCode();
  }

  @Override
  public boolean matches(Environment environment) {
    AsPath asPath = null;
    if (environment.getUseOutputAttributes()
        && environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>) {
      BgpRoute.Builder<?, ?> bgpRouteBuilder =
          (BgpRoute.Builder<?, ?>) environment.getOutputRoute();
      asPath = bgpRouteBuilder.getAsPath();
    } else if (environment.getReadFromIntermediateBgpAttributes()) {
      asPath = environment.getIntermediateBgpAttributes().getAsPath();
    } else if (environment.getOriginalRoute() instanceof BgpRoute) {
      BgpRoute<?, ?> bgpRoute = (BgpRoute<?, ?>) environment.getOriginalRoute();
      asPath = bgpRoute.getAsPath();
    }
    if (asPath == null) {
      return false;
    }
    // TODO: need to validate regexes against complex AS-Paths that contain sets. For now, regexes
    // will not match against AsPaths for which set components have non-trivial filters.
    String asPathStr = asPath.size() == 0 ? "" : " " + asPath.getAsPathString();
    return _elems.stream()
        .map(AsPathSetElem::regex)
        .anyMatch(r -> Pattern.compile(r).matcher(asPathStr).find());
  }

  public void setElems(List<AsPathSetElem> elems) {
    _elems = elems;
  }
}
