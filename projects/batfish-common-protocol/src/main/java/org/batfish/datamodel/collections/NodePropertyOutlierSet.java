package org.batfish.datamodel.collections;

import java.util.Optional;
import java.util.SortedSet;

/** An outlier set for a role-based consistency policy pertaining to a particular property of
    a node (e.g., its DNS servers, its route maps). */
public interface NodePropertyOutlierSet {
  SortedSet<String> getConformers();
  SortedSet<String> getOutliers();
  String getName();
  Optional<String> getRole();
  void setRole(String role);
}
