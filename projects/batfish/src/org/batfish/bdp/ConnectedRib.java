package org.batfish.bdp;

import org.batfish.datamodel.ConnectedRoute;

public class ConnectedRib extends AbstractRib<ConnectedRoute> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public int comparePreference(ConnectedRoute lhs, ConnectedRoute rhs) {
      return 0;
   }

}
