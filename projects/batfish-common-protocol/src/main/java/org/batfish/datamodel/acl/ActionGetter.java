package org.batfish.datamodel.acl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.LineAction;

/** {@link GenericAclLineVisitor} that returns the action that the line will take */
public class ActionGetter implements GenericAclLineVisitor<LineAction> {
  private static final ActionGetter INSTANCE = new ActionGetter();

  public static @Nullable LineAction getAction(AclLine aclLine) {
    return INSTANCE.visit(aclLine);
  }

  /**
   * Returns behavior of {@code aclLine} as {@link LineBehavior}. Note that this method returns
   * {@link LineBehavior#VARIABLE} for any {@link AclAclLine}, even if its referenced ACL can only
   * take one action, because {@link ActionGetter} has no ACL environment context.
   */
  public static @Nonnull LineBehavior getLineBehavior(AclLine aclLine) {
    LineAction lineAction = getAction(aclLine);
    return lineAction == null ? LineBehavior.VARIABLE : LineBehavior.fromLineAction(lineAction);
  }

  /** Possible behaviors for an {@link AclLine} */
  public enum LineBehavior {
    /** Line can only permit */
    PERMIT,
    /** Line can only deny */
    DENY,
    /** Line can permit and deny */
    VARIABLE;

    public static @Nonnull LineBehavior fromLineAction(@Nonnull LineAction lineAction) {
      return switch (lineAction) {
        case PERMIT -> PERMIT;
        case DENY -> DENY;
      };
    }
  }

  private ActionGetter() {}

  @Override
  public LineAction visitAclAclLine(AclAclLine aclAclLine) {
    return null;
  }

  @Override
  public LineAction visitExprAclLine(ExprAclLine exprAclLine) {
    return exprAclLine.getAction();
  }
}
