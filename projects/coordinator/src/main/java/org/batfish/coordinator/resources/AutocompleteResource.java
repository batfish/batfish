package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.common.CoordConstsV2.QP_MAX_SUGGESTIONS;
import static org.batfish.common.CoordConstsV2.QP_QUERY;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.batfish.coordinator.Main;
import org.batfish.datamodel.answers.AutocompleteResponse;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.InputValidationNotes;
import org.batfish.datamodel.questions.Variable;

/** Autocompletion resource for a network plus optional snapshot */
@Produces(MediaType.APPLICATION_JSON)
@ParametersAreNonnullByDefault
public final class AutocompleteResource {

  public AutocompleteResource(String network, @Nullable String snapshot) {
    _network = network;
    _snapshot = snapshot;
  }

  @GET
  @Path("/{type}")
  public @Nonnull AutocompleteResponse get(
      @PathParam("type") Variable.Type varType,
      @QueryParam(QP_QUERY) @Nullable String queryArg,
      @QueryParam(QP_MAX_SUGGESTIONS) @Nullable Integer maxSuggestions)
      throws IOException {
    String query = firstNonNull(queryArg, "");
    List<AutocompleteSuggestion> suggestions =
        Main.getWorkMgr()
            .autoComplete(
                _network,
                _snapshot,
                varType,
                query,
                firstNonNull(maxSuggestions, Integer.MAX_VALUE));
    if (suggestions == null) {
      throw new NotFoundException();
    }
    InputValidationNotes queryMetadata =
        Main.getWorkMgr().validateInput(_network, _snapshot, varType, query);
    if (queryMetadata == null) {
      throw new NotFoundException();
    }
    return AutocompleteResponse.of(queryMetadata, suggestions);
  }

  private final @Nonnull String _network;
  private final @Nullable String _snapshot;
}
