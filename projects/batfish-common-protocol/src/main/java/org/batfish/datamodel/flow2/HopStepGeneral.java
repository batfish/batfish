package org.batfish.datamodel.flow2;

import java.util.SortedSet;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class HopStepGeneral extends TraceHopStep {

  public static class HopStepGeneralDetail extends StepDetail {
    private NodeInterfacePair _inputInterface;
    private NodeInterfacePair _outputInterface;
    private String _filterIn;
    private String _filterOut;
    private Flow _originalFlow;
    private Flow _transformedFlow;
    private SortedSet<Route> _routes;

    public HopStepGeneralDetail(
        NodeInterfacePair inputInterface,
        NodeInterfacePair outputInterface,
        String filterIn,
        String filterOut,
        Flow originalFlow,
        Flow transformedFlow,
        SortedSet<Route> routes) {
      super("generalStepDetail");
      _inputInterface = inputInterface;
      _outputInterface = outputInterface;
      _filterIn = filterIn;
      _filterOut = filterOut;
      _originalFlow = originalFlow;
      _transformedFlow = transformedFlow;
      _routes = routes;
    }

    public NodeInterfacePair getInputInterface() {
      return _inputInterface;
    }

    public NodeInterfacePair getOutputInterface() {
      return _outputInterface;
    }

    public String getFilterIn() {
      return _filterIn;
    }

    public String getFilterOut() {
      return _filterOut;
    }

    public Flow getOriginalFlow() {
      return _originalFlow;
    }

    public Flow getTransformedFlow() {
      return _transformedFlow;
    }

    public SortedSet<Route> getRoutes() {
      return _routes;
    }

    public void setFilterOut(String filterOut){
      _filterOut = filterOut;
    }

    public void setFilterIn(String filterIn){
      _filterIn = filterIn;
    }

    public void setOutputInterfacee(NodeInterfacePair outputInterface){
      _outputInterface = outputInterface;
    }
  }

  public static class HopStepGeneralAction extends StepAction{

    private FlowDisposition _actionResult;

    public HopStepGeneralAction(FlowDisposition result){
      super("generalStepAction");
      _actionResult = result;
    }

    public FlowDisposition getActionResult() {
      return _actionResult;
    }

    public void setActionResult(FlowDisposition actionResult){
      _actionResult = actionResult;
    }
  }

  public HopStepGeneral(HopStepGeneralDetail stepDetail, HopStepGeneralAction stepAction){
    super(stepDetail, stepAction);
  }
}
