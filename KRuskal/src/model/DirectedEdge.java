package model;

/**
 * directed edge
 * edge where roles of origin and target are known
 */
public class DirectedEdge {
	/**
	 * origin vertex
	 */
	private Vertex origin;
	
	/**
	 * target vertex
	 */
	private Vertex target;
	
	/**
	 * edge connecting the two
	 */
	private Edge edge;
	
	/**
	 * default constructor
	 * 
	 * @param origin origin
	 * @param target target
	 * @param edge edge
	 */
	public DirectedEdge(Vertex origin, Vertex target, Edge edge) {
		this.origin = origin;
		this.target = target;
		this.edge = edge;
	}
}
