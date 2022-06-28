package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.ReceivedFromVisitor;

/**
 * Information identifying how a {@link BgpRoute} was received, one of:
 *
 * <ul>
 *   <li>IP of a numbered BGP session
 *   <li>Interface and link-local IP of a BGP unnumbered session
 *   <li>Originated locally by self
 * </ul>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@ParametersAreNonnullByDefault
public interface ReceivedFrom extends Serializable {

  <T> T accept(ReceivedFromVisitor<T> visitor);
}
