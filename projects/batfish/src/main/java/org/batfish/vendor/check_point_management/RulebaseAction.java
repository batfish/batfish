package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** An action taken when an access-rule is matched, e.g. {@code Accept} or {@code Drop}. */
public class RulebaseAction extends TypedManagementObject {
  public enum Action {
    ACCEPT,
    DROP,
    UNHANDLED
  }

  @JsonCreator
  private static @Nonnull RulebaseAction create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_UID) @Nullable Uid uid,
      @JsonProperty(PROP_COMMENTS) @Nullable String comments) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(comments != null, "Missing %s", PROP_COMMENTS);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new RulebaseAction(name, uid, comments);
  }

  @VisibleForTesting
  public RulebaseAction(String name, Uid uid, String comments) {
    super(name, uid);
    _comments = comments;

    // There isn't a real `action` field in the object definition...
    // So just rely on name for now
    switch (getName()) {
      case NAME_ACCEPT:
        _action = Action.ACCEPT;
        break;
      case NAME_DROP:
        _action = Action.DROP;
        break;
      default:
        _action = Action.UNHANDLED;
    }
  }

  public @Nonnull String getComments() {
    return _comments;
  }

  /** Get the {@link Action} for this {@code RulebaseAction} object. */
  public @Nonnull Action getAction() {
    return _action;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    RulebaseAction network = (RulebaseAction) o;
    return _comments.equals(network._comments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _comments);
  }

  @Override
  public String toString() {
    return baseToStringHelper().add(PROP_COMMENTS, _comments).toString();
  }

  protected static final String NAME_ACCEPT = "Accept";
  protected static final String NAME_DROP = "Drop";
  private static final String PROP_COMMENTS = "comments";

  private final @Nonnull Action _action;
  private final @Nonnull String _comments;
}
