package org.batfish.representation.fortios;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;

/**
 * FortiOS datamodel interface representing objects which can be a member of firewall service groups
 */
public interface ServiceGroupMember extends FortiosRenameableObject {
  @Nullable
  String getComment();

  void setComment(String comment);

  Stream<HeaderSpace> toHeaderSpaces();
}
