package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.LineAction;

/** Entry in a route-map supplying match conditions and transformations for a route */
@ParametersAreNonnullByDefault
public final class RouteMapEntry implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull LineAction _action;
  private @Nullable RouteMapMatchInterface _matchInterface;
  private final int _number;

  public RouteMapEntry(int number, LineAction action) {
    _number = number;
    _action = action;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  /** Return stream of match statements for this entry. */
  public @Nonnull Stream<RouteMapMatch> getMatches() {
    return Stream.<RouteMapMatch>of(_matchInterface).filter(Objects::nonNull);
  }

  public @Nullable RouteMapMatchInterface getMatchInterface() {
    return _matchInterface;
  }

  public int getNumber() {
    return _number;
  }

  /** Return stream of set statements for this entry. */
  public @Nonnull Stream<RouteMapSet> getSets() {
    return Stream.<RouteMapSet>of().filter(Objects::nonNull);
  }

  public void setMatchInterface(@Nullable RouteMapMatchInterface matchInterface) {
    _matchInterface = matchInterface;
  }
}
