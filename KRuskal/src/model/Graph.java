package model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

/**
 * graph
 * contains vertices and edges
 */
public class Graph {
	/**
	 * list of vertices
	 */
	private Vector<Vertex> vertices = new Vector<Vertex>();
	
	/**
	 * list of edges
	 */
	private Vector<Edge> edges = new Vector<Edge>();
	
	/**
	 * add any number of vertices
	 * 
	 * @param vertices vertices
	 */
	public void add(Vertex... vertices) {
		for (Vertex vertex : vertices) {
			this.vertices.add(vertex);
		}
	}
	
	/**
	 * create and add edge
	 * add vertices if needed
	 * 
	 * @param v1 v1
	 * @param v2 v2
	 * @param weight weight
	 * @return edge
	 */
	public Edge connect(Vertex v1, Vertex v2, int weight) {
		if (!vertices.contains(v1)) {
			add(v1);
		}
		if (!vertices.contains(v2)) {
			add(v2);
		}
		
		Edge e = new Edge(v1, v2, weight);
		edges.add(e);
		return e;
	}
	
	/**
	 * create and add edge
	 * 
	 * @param v1 v1
	 * @param v2 v2
	 * @return edge
	 */
	public Edge connect(Vertex v1, Vertex v2) {
		Edge e = new Edge(v1, v2);
		edges.add(e);
		return e;
	}
	
	/**
	 * getter for vertex list
	 * @return vertices
	 */
	public Vector<Vertex> getVertices() {
		return vertices;
	}
	
	/**
	 * getter for edge list
	 * @return edges
	 */
	public Vector<Edge> getEdges() {
		return edges;
	}
	
	/**
	 * remove vertex and connecting edges
	 * 
	 * @param v vertex
	 */
	public void remove(Vertex v) {
		HashSet<Edge> removeEdges = new HashSet<Edge>();
		for (Edge e : edges) {
			if (e.getV1() == v || e.getV2() == v) {
				removeEdges.add(e);
			}
		}
		edges.removeAll(removeEdges);
		
		vertices.remove(v);
	}
	
	/**
	 * remove edge
	 * 
	 * @param e edge
	 */
	public void remove(Edge e) {
		edges.remove(e);
	}
	
	/**
	 * remove vertices
	 * 
	 * @param vv vertices
	 */
	public void removeAll(Vector<Vertex> vv) {
		for (Vertex v : vv) {
			remove(v);
		}
	}

	/**
	 * sort edges
	 */
	public void sortEdges() {
		Collections.sort(edges);
	}

	/**
	 * reset to a neutral state
	 */
	public void reset() {
		for (Vertex v : vertices) {
			v.reset();
		}
		for (Edge e : edges) {
			e.reset();
		}
	}
}
