package org.batfish.datamodel.flow;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.flow.MatchSessionStep.MatchIncomingSessionStepDetail;
import org.batfish.datamodel.flow.MatchSessionStep.MatchOriginationSessionStepDetail;
import org.batfish.datamodel.flow.MatchSessionStep.MatchSessionStepDetail;
import org.batfish.datamodel.flow.SetupSessionStep.SetupIncomingSessionStepDetail;
import org.batfish.datamodel.flow.SetupSessionStep.SetupOriginationSessionStepDetail;
import org.batfish.datamodel.flow.SetupSessionStep.SetupSessionStepDetail;

/**
 * Converts a {@link SessionScope} to the set of incoming interfaces at which the corresponding
 * session may be set up, if any.
 */
@ParametersAreNonnullByDefault
public class SessionScopeUtils {

  private SessionScopeUtils() {}

  /** Returns the set of incoming interfaces corresponding to {@code sessionScope}, if any. */
  @Nonnull
  public static Set<String> toIncomingInterfaces(SessionScope sessionScope) {
    return sessionScope.accept(new ToIncomingInterfacesVisitor());
  }

  /** Returns the originating VRF corresponding to {@code sessionScope}, if any. */
  @Nullable
  public static String toOriginatingVrf(SessionScope sessionScope) {
    return sessionScope.accept(new ToOriginatingVrfVisitor());
  }

  /**
   * Returns a {@link SetupSessionStepDetail} builder with incoming interfaces or originating VRF
   * already set based on the given {@code sessionScope}.
   */
  @Nonnull
  public static SetupSessionStepDetail.Builder<?, ?> getSetupSessionStepDetailBuilder(
      SessionScope sessionScope) {
    return sessionScope.accept(new ToSetupSessionStepDetailBuilder());
  }

  /**
   * Returns a {@link MatchSessionStepDetail} builder with incoming interfaces or originating VRF
   * already set based on the given {@code sessionScope}.
   */
  @Nonnull
  public static MatchSessionStepDetail.Builder<?, ?> getMatchSessionStepDetailBuilder(
      SessionScope sessionScope) {
    return sessionScope.accept(new ToMatchSessionStepDetailBuilder());
  }

  private static final class ToIncomingInterfacesVisitor
      implements SessionScopeVisitor<Set<String>> {

    @Override
    public Set<String> visitIncomingSessionScope(IncomingSessionScope incomingSessionScope) {
      return incomingSessionScope.getIncomingInterfaces();
    }

    @Override
    public Set<String> visitOriginatingSessionScope(
        OriginatingSessionScope originatingSessionScope) {
      return ImmutableSet.of();
    }
  }

  private static final class ToOriginatingVrfVisitor implements SessionScopeVisitor<String> {

    @Override
    public String visitIncomingSessionScope(IncomingSessionScope incomingSessionScope) {
      return null;
    }

    @Override
    public String visitOriginatingSessionScope(OriginatingSessionScope originatingSessionScope) {
      return originatingSessionScope.getOriginatingVrf();
    }
  }

  private static final class ToSetupSessionStepDetailBuilder
      implements SessionScopeVisitor<SetupSessionStepDetail.Builder<?, ?>> {

    @Override
    public SetupSessionStepDetail.Builder<?, ?> visitIncomingSessionScope(
        IncomingSessionScope incomingSessionScope) {
      return SetupIncomingSessionStepDetail.builder()
          .setIncomingInterfaces(incomingSessionScope.getIncomingInterfaces());
    }

    @Override
    public SetupSessionStepDetail.Builder<?, ?> visitOriginatingSessionScope(
        OriginatingSessionScope originatingSessionScope) {
      return SetupOriginationSessionStepDetail.builder()
          .setOriginatingVrf(originatingSessionScope.getOriginatingVrf());
    }
  }

  private static final class ToMatchSessionStepDetailBuilder
      implements SessionScopeVisitor<MatchSessionStepDetail.Builder<?, ?>> {

    @Override
    public MatchSessionStepDetail.Builder<?, ?> visitIncomingSessionScope(
        IncomingSessionScope incomingSessionScope) {
      return MatchIncomingSessionStepDetail.builder()
          .setIncomingInterfaces(incomingSessionScope.getIncomingInterfaces());
    }

    @Override
    public MatchSessionStepDetail.Builder<?, ?> visitOriginatingSessionScope(
        OriginatingSessionScope originatingSessionScope) {
      return MatchOriginationSessionStepDetail.builder()
          .setOriginatingVrf(originatingSessionScope.getOriginatingVrf());
    }
  }
}
