package com.marklogic.mgmt.util;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * Strategy interface for sorting a list of ObjectNode objects. Initially created to hide how Role objects must be
 * sorted.
 */
public interface ObjectNodesSorter {

	List<ObjectNode> sortObjectNodes(List<ObjectNode> objectNodes);

}
