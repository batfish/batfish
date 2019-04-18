package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.common.AnswerRowsOptions;
import org.batfish.common.ColumnFilter;
import org.batfish.common.ColumnSortOption;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.Main;
import org.batfish.datamodel.answers.Answer;

/** Resource for handling requests about a specific ad-hoc or analysis question's answer */
@ParametersAreNonnullByDefault
public final class AnswerResource {

  private final String _analysis;

  private final String _network;

  private final String _questionName;

  public AnswerResource(String network, @Nullable String analysis, String questionName) {
    _analysis = analysis;
    _network = network;
    _questionName = questionName;
  }

  /**
   * Get the answer for the question, regarding the specified {@code snapshot} and optionally {@code
   * referenceSnapshot}.
   *
   * @param snapshot Name of the snapshot the question was run on
   * @param referenceSnapshot Optional name of the reference snapshot for the differential question
   * @param columns Optional set of column names to return, if {@code null}, all answer columns are
   *     returned
   * @param filters Optional list of {@link ColumnFilter} to be applied to the answer rows
   * @param maxRows Optional number of rows to include in the answer
   * @param rowOffset Optional first row to include in the answer
   * @param sortOrder Optional list of {@link ColumnSortOption}, priority-ordered list of columns to
   *     sort
   * @param uniqueRows Optional boolean whether or not to only include unique rows, duplicate rows
   *     are included by default
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAnswer(
      @Nullable @QueryParam("snapshot") String snapshot,
      @Nullable @QueryParam("referenceSnapshot") String referenceSnapshot,
      @Nullable @QueryParam("columns") String columns,
      @Nullable @QueryParam("filters") String filters,
      @Nullable @QueryParam("maxRows") Integer maxRows,
      @Nullable @QueryParam("rowOffset") Integer rowOffset,
      @Nullable @QueryParam("sortOrder") String sortOrder,
      @Nullable @QueryParam("uniqueRows") Boolean uniqueRows) {
    try {
      checkArgument(snapshot != null, "Snapshot must be specified to fetch question answer");
      Answer ans =
          Main.getWorkMgr()
              .getAnswer(_network, snapshot, _questionName, referenceSnapshot, _analysis);
      if (ans == null) {
        return Response.status(Status.NOT_FOUND)
            .entity(
                String.format(
                    "Answer not found for question %s on network: %s, snapshot: %s, referenceSnapshot: %s, analysis: %s",
                    _questionName, _network, snapshot, referenceSnapshot, _analysis))
            .build();
      }

      // Filter the resulting answer, if applicable
      ans =
          filterAnswer(
              ans,
              columns == null
                  ? ImmutableSet.of()
                  : BatfishObjectMapper.mapper()
                      .readValue(columns, new TypeReference<Set<String>>() {}),
              filters == null
                  ? ImmutableList.of()
                  : BatfishObjectMapper.mapper()
                      .readValue(filters, new TypeReference<List<ColumnFilter>>() {}),
              maxRows,
              rowOffset,
              sortOrder == null
                  ? ImmutableList.of()
                  : BatfishObjectMapper.mapper()
                      .readValue(sortOrder, new TypeReference<List<ColumnSortOption>>() {}),
              uniqueRows);

      return Response.ok().entity(ans).build();
    } catch (IllegalArgumentException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (IOException e) {
      // Other inputs should be validated by this point, don't expect to run into this exception
      // under normal circumstances
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(Throwables.getStackTraceAsString(e))
          .build();
    }
  }

  private static Answer filterAnswer(
      @Nullable Answer answer,
      @Nullable Set<String> columns,
      @Nullable List<ColumnFilter> filters,
      @Nullable Integer maxRows,
      @Nullable Integer rowOffset,
      @Nullable List<ColumnSortOption> sortOrder,
      @Nullable Boolean uniqueRows) {
    if ((columns != null && !columns.isEmpty())
        || (filters != null && !filters.isEmpty())
        || maxRows != null
        || rowOffset != null
        || (sortOrder != null && !sortOrder.isEmpty())
        || uniqueRows != null) {
      return Main.getWorkMgr()
          .filterAnswer(
              answer,
              new AnswerRowsOptions(
                  firstNonNull(columns, ImmutableSet.of()),
                  firstNonNull(filters, ImmutableList.of()),
                  firstNonNull(maxRows, Integer.MAX_VALUE),
                  firstNonNull(rowOffset, 0),
                  firstNonNull(sortOrder, ImmutableList.of()),
                  firstNonNull(uniqueRows, false)));
    }
    return answer;
  }
}
