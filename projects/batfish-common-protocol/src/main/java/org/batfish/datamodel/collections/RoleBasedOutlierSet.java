package org.batfish.datamodel.collections;

import java.util.Optional;
import java.util.SortedSet;

/** An outlier set for a role-based consistency policy. */
public interface RoleBasedOutlierSet {
  SortedSet<String> getConformers();

  SortedSet<String> getOutliers();

  Optional<String> getRole();

  void setRole(String role);
}
