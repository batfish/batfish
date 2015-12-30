'use strict';

var previousView = "";
var layoutData;
var view;

//-----------------------------------------------------------------------------

/*
 * Layout functions.
 */
function DoAutoLayout()
{
	//var layout = cy.makeLayout({ name: 'circle' });
	var layout = cy.makeLayout({
            name: 'concentric',
            concentric: function( node ){
              return node.degree();
            },
            levelWidth: function( nodes ){
              return 2;
            }
        });
	layout.run();
}


function RunPresetayout() {
	var layout = cy.makeLayout({
		name : 'preset',
		positions : function(node) {
			return layoutData[node.id()];
		}
	});
	layout.run();
}


function DoPresetLayout()
{
	$.ajax ({
		url: defaultLayoutURL,
		dataType: 'json',
		success: function (data, status) {
			if (status == 'success')
			{
				layoutData = data;
				RunPresetayout();
			}
		},
		error : function (xhr, status, error) {
			console.log(status);
			console.log(error);
		}
	});	
}

/*
 * Save current layout.
 */
function SaveLayout() {
	var nodes = cy.nodes("*");
	var index;
	var nodePositions = {};
	for (index = 0; index < nodes.length; index++)
	{
		nodePositions[nodes[index].id()] = nodes[index].position();
	}
	console.log(JSON.stringify(nodePositions));
}
//-----------------------------------------------------------------------------

/*
 * Tooltip functions.
 */

function NodeToolTip(n)
{
	return n.id() + "<br> " + n.data('vtt');
}

function LinkToolTip(l)
{
	var endPoints = l.id().split("#");
	return endPoints[0] + '<br>' + endPoints[1] + '<br>' + l.data('linktype') + '<br>' + l.data('vtt');
}

function SetupToolTips() {
	// qtip api on cy elements
	cy.elements().qtip({
		content : function() {
			if (this.isNode())
			{
				return NodeToolTip(this);
			}
			else if (this.isEdge())
			
			return LinkToolTip(this);
		},
		position : {
			target : $('#cy'),
			my : 'top left',
			at : 'bottom right'
		},
		style : {
			classes : 'qtip-bootstrap',
			tip : {
				width : 16,
				height : 8
			}
		}
	});
}

//-----------------------------------------------------------------------------

/*
 *  Highlighting
 */

 function HighlightElement(id, onoff, vtt, color)
 {
 	var e = cy.getElementById(id);
 	var property = e.isNode() ? 'background-color' : 'line-color';
 	e.style(property, (onoff == 'on') ? colors[color] : colors.defaultColor);
 	e.data('vtt', (onoff == 'on') ? vtt : '');
 }
 
 function HighlightNodes(nodes, onoff, parentColor)
 {
 	if (defined(nodes))
	{
		for (var i = 0; i < nodes.length; i++) {
			var node = nodes[i];
			console.log(node.name);
			HighlightElement(node.name, onoff, node.description, defined(node.color) ? nodes.color : parentColor);
		}
	}
 }
 
 function HighlightLinks(links, onoff, parentColor)
 {
	if (defined(links)) {
		for (var i = 0; i < links.length; i++) {
			var link = links[i];
			var linkId = GetLinkIds(link)[2];
			console.log(linkId);
			HighlightElement(linkId, onoff, link.description, defined(link.color) ? link.color : parentColor);
		}
	}	
 }
 
 function HighlightView(viewId, onoff)
 {
 	if (viewId == "")
 	{
 		return;
 	}
 	var parentColor = view.color;
 	HighlightNodes(view.views[viewId].nodes, onoff, parentColor);
 	HighlightLinks(view.views[viewId].links, onoff, parentColor);
 }
 
function SetupHighlightsMenu(data)
 {
    try {
        view = JSON.parse(data);

        cy.ready(function () {
            // Add a new select element 
            $('<select>').attr({ 'name': 'hs', 'id': 'hs', 'data-native-menu': 'false' }).appendTo('[data-role="content"]');
            $('<option>').html(view.name).appendTo('#hs');

            // Add choices.
            var index;
            for (index = 0; index < view.views.length; index++) {
                //console.log(nodes[index].id());
                $('<option>').attr({ 'value': index }).html(view.views[index].name).appendTo('#hs');
            }

            // Add handler
            $('select').selectmenu({
                select: function (event, ui) {
                    console.log(ui.item.value);
                    HighlightView(previousView, "off");
                    HighlightView(ui.item.value, 'on');
                    previousView = ui.item.value;
                }
            });
        });
    } catch (e) {
    	console.log(e);
        return false;
    }

    return true;
}

function AddHighlightMenu()
{
	$.ajax ({
		url: defaultHighlightsURL,
		dataType: 'json',
		success: function (data, status) {
			if (status == 'success')
			{
				SetupHighlightsMenu(data);
			}
		},
		error : function (xhr, status, error) {
			console.log(status);
			console.log(error);
		}
	});	
       
}

/*-----------------------------------------*/
/*
 * Core Plotting Functions
 */
/*------------------------------------------*/

function GetIfIds(link)
{
	var ifIds = [];
	ifIds.push(link.interface1.node + ":" + link.interface1.name);
	ifIds.push(link.interface2.node + ":" + link.interface2.name);
	return ifIds;
}

function AddNode(nodeId) {
	if (cy.getElementById(nodeId).length == 0) {
		cy.add({
			group : 'nodes',
			data : {id : nodeId, vtt: ''}
		});
		nodesAdded++;
	}
}

function GetLinkIds(link) {
	var linkIds = [];
	var ifIds = GetIfIds(link);
	var ifIds = GetIfIds(link);
	linkIds[0] = ifIds[0] + "#" + ifIds[1];
	linkIds[1] = ifIds[1] + "#" + ifIds[0];
	linkIds[2] = linkIds[0];
	if (linkIds[0].localeCompare(linkIds[1]) > 0) {
		linkIds[2] = linkIds[1];
	}
	return linkIds;
}

function GetSrcDst(link) {
	var linkSource = link.interface1.node;
	var linkTarget = link.interface2.node;
	if (linkSource.localeCompare(linkTarget) > 0) {
		linkSource = link.interface2.node;
		linkTarget = link.interface1.node;
	}
	return [linkSource, linkTarget];
}

function AddLink(link) {
	AddNode(link.interface1.node);
	AddNode(link.interface2.node);

	var linkIds = GetLinkIds(link); 
	var srcDst = GetSrcDst(link);
	if (cy.getElementById(linkIds[0]).length == 0 && cy.getElementById(linkIds[1]).length == 0) {
		cy.add({
			group : 'edges',
			data : {
				id : linkIds[2],
				source : srcDst[0],
				target : srcDst[1],
				linktype : link.interface1.type,
				vtt: ''
			}
		});
		linksAdded++;
	}
}

function ParseJsonTopology(dataRaw)
{
    try {
        var data = JSON.parse(dataRaw);

        //TODO: more sanity checking
        var edges = data.topology.edges;

        cy.ready(function() {
            var index;
            for (index = 0; index < edges.length; index++)
            {
                AddLink(edges[index]);
            }
            SetupToolTips();
            //AddHighlightMenu();
            DoAutoLayout();

            cy.center();
        });
    } catch (e) {
        return false;
    }

    return true;
}

function PlotJsonTopology(myURL) {
	$.ajax ({
		url: myURL,
		dataType: 'json',
		success: function (data, status) {
			if (status == 'success')
			{
				ParseJsonTopology(data);
			}
		},
		error : function (xhr, status, error) {
			console.log(status);
			console.log(error);
		}
	});
}

function SetupCy() {
	cy = cytoscape({
		container : document.getElementById('cy'), // container to render in
		elements : [],
		style : [// the stylesheet for the graph
		{
			selector : 'node',
			css : {
				'width' : 80,
				'height' : 80,
				'background-color': '#888',
				'label' : 'data(id)'
			}
		}, {
			selector : 'edge',
			css : {
				'width': 3,
				'line-color' : "#888"
			}
		}]
	});
}

$(document).ready(function() {
	SetupCy();
});


//----------------------------------

/*
 * Helper functions.
 */
function PrintLinkIdsToConsole()
{
	var links = cy.edges("*");
	var index;
	for (index = 0; index < links.length; index++)
	{
		console.log(links[index].id() + links[index].data('linktype'));
	}
}
function GenerateViewJson()
{
	var views = [];
	
	views[0] = {};
	
	views[0].name = "DuplicateIP";
	views[0].descritption = "Duplicate IP";
	views[0].nodes = [];
	views[0].links = [];
	
	views[0].nodes[0] = {};
	views[0].nodes[0].name = 'VC1';
	views[0].nodes[0].description = 'arrrgh';
	
	views[0].nodes[1] = {};
	views[0].nodes[1].name = 'pvmg';
	views[0].nodes[1].description = 'awsome';

	
	views[1] = {};
	views[1].name = "SelfLinks";
	views[1].description = "SelfLinks";
	views[1].nodes = [];
	views[1].links = [];
		
	views[1].links[0] = {};
	views[1].links[0].interface1 = {};
	views[1].links[0].interface2 = {};
	views[1].links[0].interface1.node = "20151110-10.178.0.1";
	views[1].links[0].interface1.name = "st0.22";
	views[1].links[0].interface2.node = "20151110-10.178.0.1";
	views[1].links[0].interface2.name = "st0.20";
	
	views[1].links[1] = {};
	views[1].links[1].interface1 = {};
	views[1].links[1].interface2 = {};
	views[1].links[1].interface1.node = "20151110-10.178.0.1";
	views[1].links[1].interface1.name = "st0.103";
	views[1].links[1].interface2.node = "20151110-10.178.0.1";
	views[1].links[1].interface2.name = "st0.104";
	
	
	console.log(JSON.stringify(views));
	
}

