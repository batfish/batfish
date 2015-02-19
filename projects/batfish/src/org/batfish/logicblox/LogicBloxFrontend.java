package org.batfish.logicblox;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.main.BatfishException;
import org.batfish.representation.Ip;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.io.ByteArrayBuffer;

import com.logicblox.bloxweb.client.DelimImportOptions;
import com.logicblox.bloxweb.client.DelimServiceClient;
import com.logicblox.bloxweb.client.DelimTxn;
import com.logicblox.bloxweb.client.DelimTxnServiceClient;
import com.logicblox.bloxweb.client.ServiceClientException;
import com.logicblox.bloxweb.client.ServiceConnector;
import com.logicblox.bloxweb.client.TCPTransport;
import com.logicblox.bloxweb.client.Transports;
import com.logicblox.common.Option;
import com.logicblox.common.ProtoBufSession.Exception;
import com.logicblox.common.protocol.CommonProto;
import com.logicblox.connect.ConnectBlox.RevertDatabase;
import com.logicblox.connect.ConnectBloxSession;
import com.logicblox.connect.ConnectBloxWorkspace;
import com.logicblox.connect.ConnectBloxWorkspace.CreateBuilder;
import com.logicblox.connect.ProtoBufException.ExceptionContainer;
import com.logicblox.connect.Workspace;
import com.logicblox.connect.ConnectBlox.Request;
import com.logicblox.connect.ConnectBlox.Response;
import com.logicblox.connect.Workspace.Command.AddProject;
import com.logicblox.connect.Workspace.Relation;
import com.logicblox.connect.Workspace.Relation.Column;
import com.logicblox.connect.Workspace.Relation.DoubleColumn;
import com.logicblox.connect.Workspace.Relation.EntityColumn;
import com.logicblox.connect.Workspace.Relation.Int64Column;
import com.logicblox.connect.Workspace.Relation.StringColumn;
import com.logicblox.connect.Workspace.Relation.UInt64Column;
import com.logicblox.connect.Workspace.Result;
import com.logicblox.connect.Workspace.Result.Failure;
import com.logicblox.connect.Workspace.Result.QueryPredicate;

public class LogicBloxFrontend {

   private static final String LB_WEB_ADMIN_URI = "/lb-web/admin";
   private static final String LB_WEB_PROTOCOL = "http";
   public static final long LB_WEB_TIMEOUT_MS = 31536000000l;
   private static final String SERVICE_DIR = "batfish";

   private static void closeSession(
         ConnectBloxSession<Request, Response> session) {
      try {
         // close down a regular session
         if (session != null && session.isActive()) {
            session.close();
         }
      }
      catch (ConnectBloxSession.Exception e) {
         throw new BatfishException(
               "Encountered error while closing a ConnectBloxSession", e);
      }
   }

   private final boolean _assumedToExist;
   private ConnectBloxSession<Request, Response> _cbSession;
   private EntityTable _entityTable;
   private final String _lbHost;
   private final int _lbPort;
   private final int _lbWebAdminPort;
   private final HttpClient _lbWebClient;
   private int _lbWebPort;
   private final TCPTransport _lbWebTransport;
   private ConnectBloxWorkspace _workspace;
   private final String _workspaceName;

   public LogicBloxFrontend(String lbHost, int lbPort, int lbWebPort,
         int lbWebAdminPort, String workspaceName, boolean assumedToExist) {
      _workspaceName = workspaceName;
      _lbHost = lbHost;
      _lbPort = lbPort;
      _lbWebPort = lbWebPort;
      _lbWebAdminPort = lbWebAdminPort;
      _assumedToExist = assumedToExist;
      if (_workspaceName.contains("\"")) {
         throw new BatfishException("Invalid workspace name: \""
               + _workspaceName + "\"");
      }
      _lbWebTransport = Transports.tcp(false);
      _lbWebClient = initLbWebClient();
   }

   public String addProject(File projectPath, String additionalLibraryPath) {
      AddProject ap = Workspace.Command.addProject(projectPath, true, true,
            additionalLibraryPath);
      List<Workspace.Result> results = null;
      try {
         results = _workspace.transaction(Collections.singletonList(ap));
      }
      catch (com.logicblox.connect.WorkspaceReader.Exception e) {
         throw new BatfishException("Error adding project", e);
      }
      Result result = results.get(0);
      if (result instanceof Result.AddProject) {
         return null;
      }
      else {
         return results.toString();
      }
   }

   public void close() {
      if (_workspace != null && _workspace.isOpen()) {
         try {
            _workspace.close();
         }
         catch (com.logicblox.connect.WorkspaceReader.Exception e) {
            throw new BatfishException("Error closing workspace", e);
         }
      }
      closeSession(_cbSession);
      if (_lbWebClient != null) {
         if (_lbWebClient.isStarted()) {
            try {
               _lbWebClient.stop();
            }
            catch (java.lang.Exception e) {
               throw new BatfishException("Failed to stop lb-web admin client",
                     e);
            }
         }

      }
   }

   public boolean connected() {
      return _cbSession != null;
   }

   private ConnectBloxSession<Request, Response> createRegularSession()
         throws LBInitializationException {
      try {
         ConnectBloxSession.Builder b = ConnectBloxSession.Builder
               .newInstance();
         b.setHost(_lbHost);
         b.setPort(_lbPort);
         return b.build();
      }
      catch (ConnectBloxSession.Exception e) {
         throw new LBInitializationException(e);
      }
   }

   private void createWorkspace(boolean overwrite)
         throws LBInitializationException {

      CreateBuilder cb = CreateBuilder.newInstance(_workspaceName);
      cb.setOverwrite(true);
      cb.setSession(_cbSession);
      try {
         _workspace = cb.build();
      }
      catch (com.logicblox.connect.WorkspaceReader.Exception e) {
         throw new LBInitializationException(e);
      }
   }

   public String execNamedBlock(String blockName) {
      Workspace.Command.ExecuteNamedBlock command = Workspace.Command
            .executeNamedBlock(blockName);
      List<Result> results = null;
      try {
         results = _workspace.transaction(Collections.singletonList(command));
      }
      catch (com.logicblox.connect.WorkspaceReader.Exception e) {
         throw new BatfishException("Error executing named block", e);
      }
      if (results.get(0) instanceof Workspace.Result.ExecuteNamedBlock) {
         return null;
      }
      else {
         return results.toString();
      }
   }

   public void fillColumn(LBValueType valueType, List<String> textColumn,
         Column column) {
      EntityColumn ec;
      UInt64Column indexColumn;
      try {
         switch (valueType) {

         case ENTITY_REF_IP:
            ec = (EntityColumn) column;
            long[] ips = ((Int64Column) ec.getRefModeColumn().unwrap())
                  .getRows();
            for (long ipAsLong : ips) {
               textColumn.add(new Ip(ipAsLong).toString());
            }
            break;

         case ENTITY_INDEX_FLOW:
            ec = (EntityColumn) column;
            BigInteger[] flowIndices = ((UInt64Column) ec.getIndexColumn()
                  .unwrap()).getRows();
            for (BigInteger index : flowIndices) {
               textColumn.add(_entityTable.getFlow(index));
            }
            break;

         case ENTITY_INDEX_NETWORK:
            ec = (EntityColumn) column;
            BigInteger[] networkIndices = ((UInt64Column) ec.getIndexColumn()
                  .unwrap()).getRows();
            for (BigInteger index : networkIndices) {
               textColumn.add(_entityTable.getNetwork(index));
            }
            break;

         case ENTITY_INDEX_INT:
            ec = (EntityColumn) column;
            indexColumn = (UInt64Column) ec.getIndexColumn().unwrap();
            for (BigInteger i : indexColumn.getRows()) {
               textColumn.add(i.toString());
            }
            break;

         case ENTITY_REF_INT:
            ec = (EntityColumn) column;
            long[] refIntLongs = ((Int64Column) ec.getRefModeColumn().unwrap())
                  .getRows();
            for (Long l : refIntLongs) {
               textColumn.add(l.toString());
            }
            break;

         case ENTITY_REF_STRING:
            ec = (EntityColumn) column;
            String[] strings = ((StringColumn) ec.getRefModeColumn().unwrap())
                  .getRows();
            for (String s : strings) {
               textColumn.add(s);
            }
            break;

         case STRING:
            StringColumn sColumn = (StringColumn) column;
            textColumn.addAll(Arrays.asList(sColumn.getRows()));
            break;

         case IP:
            long[] ipsAsLongs = ((Int64Column) column).getRows();
            for (Long ipAsLong : ipsAsLongs) {
               textColumn.add(new Ip(ipAsLong).toString());
            }
            break;

         case FLOAT:
            double[] doubles = ((DoubleColumn) column).getRows();
            for (Double d : doubles) {
               textColumn.add(d.toString());
            }
            break;

         case INT:
            long[] longs = ((Int64Column) column).getRows();
            for (Long l : longs) {
               textColumn.add(l.toString());
            }
            break;
         case ENTITY_INDEX_BGP_ADVERTISEMENT:
            ec = (EntityColumn) column;
            BigInteger[] advertIndices = ((UInt64Column) ec.getIndexColumn()
                  .unwrap()).getRows();
            for (BigInteger index : advertIndices) {
               textColumn.add(_entityTable.getBgpAdvertisement(index));
            }
            break;

         case ENTITY_INDEX_ROUTE:
            ec = (EntityColumn) column;
            BigInteger[] routeIndices = ((UInt64Column) ec.getIndexColumn()
                  .unwrap()).getRows();
            for (BigInteger index : routeIndices) {
               textColumn.add(_entityTable.getRoute(index));
            }
            break;

         default:
            throw new Error("Invalid LBValueType");
         }
      }
      catch (Option.Exception e) {
         throw new BatfishException(
               "Error pretty-printing logicblox query result", e);
      }
   }

   public List<String> getPredicate(PredicateInfo predicateInfo,
         Relation relation, String relationName) throws QueryException {
      List<LBValueType> valueTypes = predicateInfo
            .getPredicateValueTypes(relationName);
      if (valueTypes == null) {
         throw new QueryException("Missing type information for relation: "
               + relationName);
      }
      boolean isFunction = predicateInfo.isFunction(relationName);
      String outputLine;
      List<String> output = new ArrayList<String>();

      List<Column> columns = relation.getColumns();
      ArrayList<ArrayList<String>> tableByColumns = new ArrayList<ArrayList<String>>();
      for (int i = 0; i < columns.size(); i++) {
         Column column = columns.get(i);
         ArrayList<String> textColumn = new ArrayList<String>();
         tableByColumns.add(textColumn);
         fillColumn(valueTypes.get(i), textColumn, column);
      }
      ArrayList<ArrayList<String>> tableByRows = new ArrayList<ArrayList<String>>();
      int numRows = tableByColumns.get(0).size();
      for (int i = 0; i < numRows; i++) {
         ArrayList<String> textRow = new ArrayList<String>();
         tableByRows.add(textRow);
         for (ArrayList<String> textColumn : tableByColumns) {
            textRow.add(textColumn.get(i));
         }
      }
      if (isFunction) {
         for (ArrayList<String> textRow : tableByRows) {
            String value;
            outputLine = relationName + "[";
            for (int i = 0; i < textRow.size() - 1; i++) {
               value = textRow.get(i);
               outputLine += value + ", ";
            }
            outputLine = outputLine.substring(0, outputLine.length() - 2);
            value = textRow.get(textRow.size() - 1);
            outputLine += "] = " + value;
            output.add(outputLine);
         }
      }
      else {
         for (ArrayList<String> textRow : tableByRows) {
            outputLine = relationName + "(";
            for (String value : textRow) {
               outputLine += value + ", ";
            }
            outputLine = outputLine.substring(0, outputLine.length() - 2);
            outputLine += ")";
            output.add(outputLine);
         }
      }
      return output;
   }

   public List<? extends List<String>> getPredicateRows(
         PredicateInfo predicates, Relation relation, String relationName) {
      List<LBValueType> valueTypes = predicates
            .getPredicateValueTypes(relationName);
      List<Column> columns = relation.getColumns();
      ArrayList<ArrayList<String>> tableByColumns = new ArrayList<ArrayList<String>>();
      for (int i = 0; i < columns.size(); i++) {
         Column column = columns.get(i);
         ArrayList<String> textColumn = new ArrayList<String>();
         tableByColumns.add(textColumn);
         fillColumn(valueTypes.get(i), textColumn, column);
      }
      ArrayList<ArrayList<String>> tableByRows = new ArrayList<ArrayList<String>>();
      int numRows = tableByColumns.get(0).size();
      for (int i = 0; i < numRows; i++) {
         ArrayList<String> textRow = new ArrayList<String>();
         tableByRows.add(textRow);
         for (ArrayList<String> textColumn : tableByColumns) {
            textRow.add(textColumn.get(i));
         }
      }
      return tableByRows;
   }

   public void initEntityTable() {
      _entityTable = new EntityTable(this);
   }

   public void initialize() throws LBInitializationException {
      _cbSession = createRegularSession();
      if (!_assumedToExist) {
         createWorkspace(true);
      }
      else {
         openWorkspace();
      }
   }

   private HttpClient initLbWebClient() {
      HttpClient client = _lbWebTransport.getHttpClient();
      client.setTimeout(LB_WEB_TIMEOUT_MS);
      client.setIdleTimeout(LB_WEB_TIMEOUT_MS);
      return client;
   }

   private ContentExchange newLbWebAdminExchange(String msg) {
      ContentExchange exchange = new ContentExchange();
      exchange.setMethod("POST");
      exchange.setRequestHeader("Accept", "application/json");
      exchange.setRequestHeader("Content-Type", "application/json");
      exchange.setURL(LB_WEB_PROTOCOL + "://" + _lbHost + ":" + _lbWebAdminPort
            + LB_WEB_ADMIN_URI);
      exchange.setRequestContent(new ByteArrayBuffer(msg.getBytes()));
      return exchange;
   }

   private void openWorkspace() throws LBInitializationException {
      ConnectBloxWorkspace.OpenBuilder ob = ConnectBloxWorkspace.OpenBuilder
            .newInstance(_workspaceName);
      ob.setSession(_cbSession);
      try {
         _workspace = ob.build();
      }
      catch (com.logicblox.connect.WorkspaceReader.Exception e) {
         throw new LBInitializationException(e);
      }
   }

   public void postFacts(Map<String, StringBuilder> factBins)
         throws ServiceClientException {
      String base = LB_WEB_PROTOCOL + "://" + _lbHost + ":" + _lbWebPort + "/"
            + SERVICE_DIR + "/";
      ServiceConnector connector = ServiceConnector.create()
            .setTransport(_lbWebTransport).setGZIP(false);
      DelimTxnServiceClient txnClient = connector.setURI(base + "txn")
            .createDelimTxnClient();
      DelimTxn transaction = txnClient.start().result();
      for (String suffix : factBins.keySet()) {
         DelimServiceClient currentClient = connector.setURI(base + suffix)
               .createDelimClient();
         DelimImportOptions input = new DelimImportOptions(factBins.get(suffix)
               .toString().getBytes());
         currentClient.postDelimitedFile(input, transaction);
      }
      transaction.commit().result();
   }

   public Relation queryPredicate(String qualifiedPredicateName) {
      Workspace.Command.QueryPredicate qp = Workspace.Command
            .queryPredicate(qualifiedPredicateName);
      List<Result> results = null;
      try {
         results = _workspace.transaction(Collections.singletonList(qp));
      }
      catch (com.logicblox.connect.WorkspaceReader.Exception e) {
         throw new BatfishException("Error querying predicate: "
               + qualifiedPredicateName, e);
      }
      try {
         QueryPredicate qpr = (QueryPredicate) results.get(0);
         return qpr.getRelation();
      }
      catch (ClassCastException e) {
         Failure failure = (Failure) results.get(0);
         try {
            _cbSession.close();
         }
         catch (Exception e1) {
            throw new Error(ExceptionUtils.getStackTrace(e1));
         }
         throw new Error(failure.getMessage());
      }
   }

   public void removeBlock(String blockName) {
      try {
         Workspace.Command.RemoveBlock rem = Workspace.Command
               .removeBlock(blockName);

         // Execute the command as part of a self-contained transaction
         List<Workspace.Result> results = _workspace.transaction(Collections
               .singletonList(rem));
         // Now to check that our command succeeded
         if (results.size() == 1) {
            Workspace.Result result = results.get(0);
            if (result instanceof Workspace.Result.Failure) {
               Workspace.Result.Failure failResult = (Workspace.Result.Failure) result;
               System.err.println("RemoveBlock failed: "
                     + failResult.getMessage());
            }
            else if (result instanceof Workspace.Result.AddBlock) {
               Workspace.Result.AddBlock addResult = (Workspace.Result.AddBlock) result;
               Option<CommonProto.CompilationProblems> optProblems = addResult
                     .getProblems();
               if (optProblems.isSome()) {
                  System.err.println("There were problems removing the block: "
                        + optProblems.unwrap());
               }
               else {
                  System.out
                        .println("Block successfully removed from workspace!");
               }
            }
            else {
               System.err.println("Unexpected result "
                     + result.getClass().getName() + "!");
            }
         }
         else {
            System.err.println("Incorrect number of results!");
         }
      }
      catch (Workspace.Exception e) {
         System.err.println("Encountered error " + e.errorSort());
      }
   }

   public String revertDatabase(String branchName) {
      RevertDatabase.Builder rb = RevertDatabase.newBuilder();
      rb.setWorkspace(_workspaceName);
      rb.setOlderBranch(branchName);
      RevertDatabase r = rb.build();
      Request.Builder reqBuild = Request.newBuilder();
      reqBuild.setRevertDatabase(r);
      Request revertRequest = reqBuild.build();
      try {
         Future<Response> futureResponse = _cbSession.call(revertRequest);
         Response response = futureResponse.get();
         if (response.hasException()) {
            ExceptionContainer exception = response.getException();
            if (exception.hasMessage()) {
               String message = exception.getMessage();
               return message;
            }
         }
      }
      catch (Exception | InterruptedException | ExecutionException e) {
         return ExceptionUtils.getStackTrace(e);
      }
      return null;
   }

   private void sendLbWebAdminMessage(String msg) {
      if (!_lbWebClient.isStarted()) {
         try {
            _lbWebTransport.start();
         }

         catch (java.lang.Exception e) {
            throw new BatfishException("Failed to start lb-web admin client", e);
         }
      }
      ContentExchange exchange = newLbWebAdminExchange(msg);
      try {
         _lbWebClient.send(exchange);
         int exchangeState = exchange.waitForDone();
         if (exchangeState != HttpExchange.STATUS_COMPLETED) {
            throw new BatfishException("Failed to send lb-web admin message: "
                  + msg);
         }
      }
      catch (IOException | InterruptedException e) {
         throw new BatfishException("Failed to send lb-web admin message: "
               + msg, e);
      }
      String response;
      try {
         response = exchange.getResponseContent();
      }
      catch (UnsupportedEncodingException e) {
         throw new BatfishException(
               "Invalid lb-web admin client response encoding", e);
      }
      if (!response.equals("{}")) {
         throw new BatfishException(
               "lb-web-server responded with failure message: " + response);
      }
   }

   public void startLbWebServices() {
      String startJsonMessage = "{\"start\": {\"workspace\":[\""
            + _workspaceName + "\"] } }";
      sendLbWebAdminMessage(startJsonMessage);
   }

   public void stopLbWebServices() {
      String startJsonMessage = "{\"stop\": {\"workspace\":[\""
            + _workspaceName + "\"] } }";
      sendLbWebAdminMessage(startJsonMessage);
   }

}
