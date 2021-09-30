package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing the condition that the packet protocol is UDP. */
@ParametersAreNonnullByDefault
public final class UdpAstNode implements BooleanExprAstNode, HasInspectText {

  public static @Nonnull UdpAstNode instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(BooleanExprAstNodeVisitor<T, U> visitor, U arg) {
    return visitor.visitUdpAstNode(this, arg);
  }

  @Override
  public @Nonnull String getInspectText() {
    return "udp";
  }

  private static final UdpAstNode INSTANCE = new UdpAstNode();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof UdpAstNode;
  }

  @Override
  public int hashCode() {
    return 0xD423CF19; // randomly generated
  }

  private UdpAstNode() {}
}
