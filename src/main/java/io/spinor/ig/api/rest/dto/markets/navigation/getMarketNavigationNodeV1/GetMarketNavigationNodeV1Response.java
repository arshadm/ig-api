package io.spinor.ig.api.rest.dto.markets.navigation.getMarketNavigationNodeV1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
Market hierarchy node
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetMarketNavigationNodeV1Response {

/*
Child market hierarchy nodes
*/
private java.util.List<NodesItem> nodes;

/*
List of markets (applicable only to leaf nodes of the hierarchy tree)
*/
private java.util.List<MarketsItem> markets;

public java.util.List<NodesItem> getNodes() { return nodes; }
public void setNodes(java.util.List<NodesItem> nodes) { this.nodes=nodes; }
public java.util.List<MarketsItem> getMarkets() { return markets; }
public void setMarkets(java.util.List<MarketsItem> markets) { this.markets=markets; }
}
