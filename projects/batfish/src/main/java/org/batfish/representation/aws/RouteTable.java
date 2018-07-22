package org.batfish.representation.aws;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class RouteTable implements AwsVpcEntity, Serializable {

  private static final long serialVersionUID = 1L;

  private List<Route> _routes = new LinkedList<>();

  private List<RouteTableAssociation> _routeTableAssociations = new LinkedList<>();

  private String _routeTableId;

  private String _vpcId;

  public RouteTable(JSONObject jObj) throws JSONException {
    _routeTableId = jObj.getString(JSON_KEY_ROUTE_TABLE_ID);
    _vpcId = jObj.getString(JSON_KEY_VPC_ID);

    JSONArray associations = jObj.getJSONArray(JSON_KEY_ASSOCIATIONS);
    initAssociations(associations);

    JSONArray routes = jObj.getJSONArray(JSON_KEY_ROUTES);
    initRoutes(routes);
  }

  public List<RouteTableAssociation> getAssociations() {
    return _routeTableAssociations;
  }

  @Override
  public String getId() {
    return _routeTableId;
  }

  public List<Route> getRoutes() {
    return _routes;
  }

  public String getVpcId() {
    return _vpcId;
  }

  private void initAssociations(JSONArray associations) throws JSONException {

    for (int index = 0; index < associations.length(); index++) {
      JSONObject childObject = associations.getJSONObject(index);
      _routeTableAssociations.add(new RouteTableAssociation(childObject));
    }
  }

  private void initRoutes(JSONArray routes) throws JSONException {

    for (int index = 0; index < routes.length(); index++) {
      JSONObject childObject = routes.getJSONObject(index);
      _routes.add(new Route(childObject));
    }
  }
}
