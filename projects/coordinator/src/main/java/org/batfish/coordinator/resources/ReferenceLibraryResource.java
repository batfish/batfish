package org.batfish.coordinator.resources;

import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.Main;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;

/**
 * The {@link ReferenceLibraryResource} is a resource for servicing client API calls for the address
 * library. It is a subresource of {@link NetworkResource}.
 */
@Produces(MediaType.APPLICATION_JSON)
public class ReferenceLibraryResource {

  private BatfishLogger _logger = Main.getLogger();
  private String _network;

  public ReferenceLibraryResource(String network) {
    _network = network;
  }

  /**
   * Adds a new {@link ReferenceBook} to the network's {@link ReferenceLibrary}. Deprecated in favor
   * of {@link ReferenceBookResource#putReferenceBook(ReferenceBookBean)}
   */
  @Deprecated
  @POST
  public Response addReferenceBook(ReferenceBookBean referenceBookBean) {
    _logger.infof("WMS2: addReferenceBook '%s'\n", _network);
    if (referenceBookBean.name == null) {
      throw new BadRequestException("ReferenceBook must have a name");
    }
    try {
      ReferenceLibrary library = Main.getWorkMgr().getReferenceLibrary(_network);
      if (library.getReferenceBook(referenceBookBean.name).isPresent()) {
        throw new BadRequestException("Duplicate bookname: " + referenceBookBean.name);
      }
      Main.getWorkMgr()
          .putReferenceLibrary(
              Main.getWorkMgr()
                  .getReferenceLibrary(_network)
                  .mergeReferenceBooks(ImmutableSortedSet.of(referenceBookBean.toAddressBook())),
              _network);
      return Response.ok().build();
    } catch (IOException e) {
      throw new InternalServerErrorException("ReferenceLibrary resource is corrupted");
    }
  }

  /** Relocate the request to {@link ReferenceBookResource}. */
  @Path("/{book}")
  public ReferenceBookResource getReferenceBookResource(@PathParam("book") String book) {
    return new ReferenceBookResource(_network, book);
  }

  /** Returns information about reference library in the network */
  @GET
  public ReferenceLibraryBean getReferenceLibrary() {
    _logger.infof("WMS2: getReferenceLibrary '%s'\n", _network);
    try {
      return new ReferenceLibraryBean(Main.getWorkMgr().getReferenceLibrary(_network));
    } catch (IOException e) {
      throw new InternalServerErrorException("ReferenceLibrary resource is corrupted");
    }
  }
}
