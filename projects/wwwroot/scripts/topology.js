'use strict';

var cy;
var previousView = "";
var layoutData;
var view;
var nodesAdded = 0;
var linksAdded = 0;

$(document).ready(function() {
	SetupCy();
});

//-----------------------------------------------------------------------------

/*
 * Tooltip functions.
 */

function NodeToolTip(n) {
	return n.id() + "<br> " + n.data('vtt');
}

function LinkToolTip(l) {
	var endPoints = l.id().split("#");
	return endPoints[0] + '<br>' + endPoints[1] + '<br>' + l.data('linktype') + '<br>' + l.data('vtt');
}

function SetupToolTips() {
	// qtip api on cy elements
	cy.elements().qtip({
		content : function() {
			if (this.isNode()) {
				return NodeToolTip(this);
			} else if (this.isEdge())

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

function HighlightElement(id, onoff, vtt, color) {
	var e = cy.getElementById(id);
	var property = e.isNode() ? 'background-color' : 'line-color';
	if (onoff == 'off') {
		e.style(property, ColorEnum.default);
		e.data('vtt', '');
	} else {
		e.data('vtt', vtt);
		if (e.style(property) == ColorEnum.default) {
			e.style(property, ColorEnum[color]);
		} else {
			if (e.style(property) != ColorEnum[color]) {
				e.style(property, ColorEnum.maybe);
			}
		}
	}

}

function HighlightNodes(nodes, onoff, parentColor, parentDescription) {
	for (var i in nodes) {
		if (nodes.hasOwnProperty(i)) {
			var node = nodes[i];
			console.log(node.name);
			HighlightElement(node.name, onoff, defined(node.description) ? node.description : parentDescription, defined(node.color) ? node.color : parentColor);
		}
	}
}

function HighlightLinks(links, onoff, parentColor, parentDescription) {
	for (var i in links) {
		if (links.hasOwnProperty(i)) {
			var link = links[i];
			var linkId = GetLinkIds(link)[2];
			HighlightElement(linkId, onoff, defined(link.description) ? link.description : parentDescription, defined(link.color) ? link.color : parentColor);
			HighlightElement(link.interface1.node, onoff, defined(link.description) ? link.description : parentDescription, defined(link.color) ? link.color : parentColor);
			HighlightElement(link.interface2.node, onoff, defined(link.description) ? link.description : parentDescription, defined(link.color) ? link.color : parentColor);
		}
	}
}

function HighlightPaths(paths, onoff, parentColor, parentDescription) {
	for (var i in paths) {
		if (paths.hasOwnProperty(i)) {
			var path = paths[i];
			HighlightLinks(path.links, onoff, defined(path.color) ? path.color : parentColor);
		}
	}
}

function HighlightView(viewId, onoff) {
	if (viewId != "") {
		var parentColor = view.color;
		var parentDescription = view.description;
		var thisView = view.views[viewId];
		if (defined(thisView)) {
			if (defined(thisView.color))
				parentColor = thisView.color;
			if (defined(thisView.description))
				parentDescription = thisView.description;
			if (defined(thisView.nodes))
				HighlightNodes(view.views[viewId].nodes, onoff, parentColor, parentDescription);
			if (defined(thisView.links))
				HighlightLinks(view.views[viewId].links, onoff, parentColor, parentDescription);
			if (defined(thisView.paths))
				HighlightPaths(view.views[viewId].paths, onoff, parentColor, parentDescription);
		}
	}
}

function ClearHighlights()
{
	cy.nodes().forEach(function( e ){
		e.style('background-color', ColorEnum.default);
		e.data('vtt', '');
	});
	cy.edges().forEach(function( e ){
		e.style('line-color', ColorEnum.default);
		e.data('vtt', '');
	});
}
function SetupHighlightsMenu(data) {
	try {
		view = JSON.parse(data);

		cy.ready(function() {
			ClearHighlights();
			// add a new select element if we do not have a previous one.
			if (document.getElementById('hs') != null) {
				$('#hs').remove();
			}
			$('<select>').attr({
				'name' : 'hs',
				'id' : 'hs',
				'data-native-menu' : 'false'
			}).appendTo('[data-role="content"]');

			$('<option>').html(view.name).appendTo('#hs'); 

			// Add choices.
			var viewList = view.views;
			for (var viewName in viewList) {
				if (viewList.hasOwnProperty(viewName)) {
					console.log(viewName);
					$('<option>').attr({
						'value' : viewName
					}).html(viewName).appendTo('#hs');
				}
			}

			// Add handler
			$('#hs').selectmenu({
				select : function(event, ui) {
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

/*-----------------------------------------*/
/*
 * Core Plotting Functions
 */
/*------------------------------------------*/
function SaveLayout () {
	var nodes = cy.nodes("*");
	var index;
	var nodePositions = {};
	for ( index = 0; index < nodes.length; index++) {
		nodePositions[nodes[index].id()] = nodes[index].position();
	}
	bfPutObject(containerName, testrigName, layoutPath, JSON.stringify(nodePositions), testMeSuccess_cb, testMeFailure_cb, "testme", []);
	document.getElementById("txtOutput").value = JSON.stringify(nodePositions);
	//console.log(JSON.stringify(nodePositions));
}

function DoLayout()
{
	bfGetObject(containerName, testrigName, layoutPath, FoundSavedLayout, NoSavedLayout, "testme", []);
}

function NoSavedLayout(errMsg, entryPoint, remainingCalls)
{
	DoAutoLayout();
}

function FoundSavedLayout(dataRaw, entryPoint, remainingCalls)
{
	DoPresetLayout(dataRaw);
}

function DoAutoLayout() {
	//var layout = cy.makeLayout({ name: 'circle' });
	var layout = cy.makeLayout({
		name : 'concentric',
		concentric : function(node) {
			return node.degree();
		},
		levelWidth : function(nodes) {
			return 2;
		}
	});
	layout.run();
}

function DoPresetLayout(dataRaw) {
	layoutData = JSON.parse(dataRaw);
	var layout = cy.makeLayout({
		name : 'preset',
		positions : function(node) {
			return layoutData[node.id()];
		}
	});
	layout.run();
}

function GetIfIds(link) {
	var ifIds = [];

	if (defined(link.interface1.name) && defined(link.interface2.name)) {
		ifIds.push(link.interface1.node + ":" + link.interface1.name);
		ifIds.push(link.interface2.node + ":" + link.interface2.name);
	} else {
		ifIds.push(link.interface1.node + ":" + link.interface1.interface_name);
		ifIds.push(link.interface2.node + ":" + link.interface2.interface_name);
	}
	return ifIds;
}

function AddNode(nodeId) {
	if (cy.getElementById(nodeId).length == 0) {
		cy.add({
			group : 'nodes',
			data : {
				id : nodeId,
				vtt : ''
			}
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
				vtt : ''
			}
		});
	}
}

function ParseTopology(dataRaw) {
	try {
		var data = JSON.parse(dataRaw);
		var edges = data.topology.edges;
		cy.ready(function() {
			cy.batch(function() {
				var index;
				for ( index = 0; index < edges.length; index++) {
					AddLink(edges[index]);
					linksAdded++;
					if (linksAdded % 100 == 0) {
						console.log(linksAdded + " of " + edges.length);
					}
				}
			});
			SetupToolTips(); 
			DoAutoLayout();
			cy.center();
		});

	} catch (e) {
		return false;
	}
	return true;
}


function SetupCy() {
	cy = cytoscape({
		container : document.getElementById('cy'), // container to render in
		hideEdgesOnViewport: true,
  		hideLabelsOnViewport: true,
		elements : [],
		style : [// the stylesheet for the graph
		{
			selector : 'node',
			css : {
				'width' : 80,
				'height' :80,
				'background-color' : '#888',
				'label' : 'data(id)',
				"text-valign" : "center",
				"text-halign" : "center"
			}
		}, {
			selector : 'edge',
			css : {
				'width' : 3,
				'line-color' : "#888"
			}
		}]
	});
}

function Resize() {
	cy.ready(function() {
		cy.resize();
		cy.center();
	});
}
//----------------------------------

/*
 * Helper functions.
 */
function PrintLinkIdsToConsole() {
	var links = cy.edges("*");
	var index;
	for ( index = 0; index < links.length; index++) {
		console.log(links[index].id() + links[index].data('linktype'));
	}
}

function DownTopology(myURL) {
	$.ajax({
		url : myURL,
		success : function(data, status) {
			if (status == 'success') {
				ParseTopology(data);
			}
		},
		error : function(xhr, status, error) {
			console.log(status);
			console.log(error);
		}
	});
}

function AddHighlightMenu() {
	$.ajax({
		url : 'testdata/highlights.json.txt',
		success : function(data, status) {
			if (status == 'success') {
				SetupHighlightsMenu(data);
			}
		},
		error : function(xhr, status, error) {
			console.log(status);
			console.log(error);
		}
	});
}
