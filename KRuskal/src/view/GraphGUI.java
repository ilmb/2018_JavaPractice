package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import misc.Point;
import misc.VertexFactory;
import model.Edge;
import model.Graph;
import model.Vertex;
import algorithm.GraphAlgorithm;
import algorithm.Kruskal;



/**
 * main GUI class
 */
public class GraphGUI {
	/**
	 * currently active graphing algorithm
	 */
	private GraphAlgorithm algo;

	/**
	 * main gui frame
	 */
	private JFrame frame = new JFrame("Kruskul");

	/**
	 * canvas for visualization
	 */
	private GraphCanvas canvas;

	/**
	 * was the algorithm configured
	 */
	private boolean isConfigured = false;

	/**
	 * list of graphing algorithms
	 */
	private Vector<GraphAlgorithm> algorithms;

	/**
	 * factory for pretty-name vertices
	 */
	private VertexFactory vertexFactory = new VertexFactory();

	/**
	 * the active graph
	 */
	private Graph graph;

	/**
	 * right-click context menu
	 */
	private JPopupMenu popup = new JPopupMenu();

	/**
	 * mouse click location
	 */
	private Point mouseLocation = new Point();

	/**
	 * vertex currently selected by mouse
	 * used for dragging
	 */
	private Vertex selectedVertex = null;

	/**
	 * default constructor
	 */
	public GraphGUI(Vector<GraphAlgorithm> algorithms, Graph graph) {
		this.graph = graph;

		canvas = new GraphCanvas(graph);

		this.algorithms = algorithms;

		algo = algorithms.firstElement();
		algo.setGUI(this);
	}

	/**
	 * constructor
	 */
	public GraphGUI(Vector<GraphAlgorithm> algorithms) {
		this(algorithms, new Graph());
	}

	/**
	 * set up all components, set visible
	 */
	public void init() {
		// set up look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// window
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 1000);
		frame.setLayout(new BorderLayout());
		frame.setContentPane(new JPanel());
		frame.getContentPane().add(canvas, BorderLayout.CENTER);


		// graph canvas
		canvas.setBackground(Color.white);
		canvas.setSize(650, 650);
		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// select a vertex with the mouse
				// find the vertex for moving
				for (Vertex v : canvas.getMouseVertices(e.getX() / GraphCanvas.STEP, e.getY() / GraphCanvas.STEP)) {
					selectedVertex = v;
					break;
				}

				mouseLocation.setPoint(e.getX(), e.getY());

				if (e.getClickCount() == 2) {
					// double click
					// ...
				} else if (e.isPopupTrigger()) {
					// show right-click menu
					popup.show(e.getComponent(), e.getX(), e.getY());
				} else if (e.isAltDown() && e.isShiftDown()) {
					// find the vertices for deleting
					graph.removeAll(canvas.getMouseVertices(e.getX() / GraphCanvas.STEP, e.getY() / GraphCanvas.STEP));
					repaint();
				} else if (e.isAltDown()) {
					// create vertex
					Vertex v = vertexFactory.getVertex();
					v.setX((int) e.getX() / GraphCanvas.STEP);
					v.setY((int) e.getY() / GraphCanvas.STEP);
					graph.add(v);
					repaint();
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (canvas.isTempLine()) {
					// create the edge
					Vertex droppedVertex = null;
					for (Vertex v : canvas.getMouseVertices(e.getX() / GraphCanvas.STEP, e.getY() / GraphCanvas.STEP)) {
						droppedVertex = v;
						break;
					}
					if (selectedVertex != null && droppedVertex != null) {
						Edge edge = graph.connect(selectedVertex, droppedVertex);

						// show window to edit new edge
						final EdgeEditWindow w = new EdgeEditWindow(frame, "Правка ребра", graph, edge);
						w.setLocation(frame.getX(), frame.getY());
						w.addSaveListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								w.save();
								repaint();
							}
						});
						w.setVisible(true);
					}

					// reset the canvas temp line
					canvas.setTempLine(null, null);
					repaint();

					// reset selectedVertex
					selectedVertex = null;
				}
			}
		});
		canvas.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				if (e.isShiftDown()) {
					// draw hypothetical edge
					if (selectedVertex != null) {
						canvas.setTempLine(new Point(selectedVertex.getX(), selectedVertex.getY()), new Point(e.getX() / GraphCanvas.STEP, e.getY() / GraphCanvas.STEP));
						repaint();
					}
				} else {
					// drag and move a vertex
					if (selectedVertex != null) {
						// move using the center of the mouse
						// therefore subtract 15/2 = 7
						selectedVertex.setX((e.getX() - 7) / GraphCanvas.STEP);
						selectedVertex.setY((e.getY() - 7) / GraphCanvas.STEP);
						repaint();
					}
				}
			}
		});

		// menu
		JMenuBar menuBar = new JMenuBar();

		JMenu menuVertex = new JMenu("Вершина");
		menuBar.add(menuVertex);
		JMenuItem menuVertexAdd = new JMenuItem("Новая");
		menuVertexAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final VertexEditWindow w = new VertexEditWindow(frame, "Новая вершина");
				w.setLocation(frame.getX(), frame.getY());
				w.addSaveListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						graph.add(w.getVertex());
						repaint();
						System.out.println("Вершина создана");
					}
				});
				Vertex v = vertexFactory.getVertex();
				w.setNameField(v.getName());
				w.setVisible(true);
			}
		});
		menuVertex.add(menuVertexAdd);
		JMenuItem menuVertexEdit = new JMenuItem("Правка");
		menuVertexEdit.addActionListener(new ActionListener() {
			private Vertex selectedVertex = null;

			public void actionPerformed(ActionEvent e) {
				final ItemSelectWindow<Vertex> s = new ItemSelectWindow<Vertex>(frame, "Выбор вершины", "Вершина", graph.getVertices());
				s.setLocation(frame.getX(), frame.getY());
				s.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectedVertex = s.getItem();
					}
				});
				s.setVisible(true);

				if (selectedVertex == null) {
					System.out.println("Нет выбранной вершины");
					return;
				}

				final VertexEditWindow w = new VertexEditWindow(frame, "Правка вершины", selectedVertex);
				w.setLocation(frame.getX(), frame.getY());
				w.addSaveListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						w.save();
						repaint();
						System.out.println("Вершина создана");
					}
				});
				w.setVisible(true);
			}
		});
		menuVertex.add(menuVertexEdit);
		JMenuItem menuVertexRemove = new JMenuItem("Удаление");
		menuVertexRemove.addActionListener(new ActionListener() {
			private Vertex selectedVertex = null;

			public void actionPerformed(ActionEvent e) {
				final ItemSelectWindow<Vertex> s = new ItemSelectWindow<Vertex>(frame, "Выбор вершины", "Вершина", graph.getVertices());
				s.setLocation(frame.getX(), frame.getY());
				s.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectedVertex = s.getItem();
					}
				});
				s.setVisible(true);

				if (selectedVertex == null) {
					System.out.println("Нет выбранной вершины");
					return;
				}

				graph.remove(selectedVertex);
				repaint();
				System.out.println("Вершина удалена");
			}
		});
		menuVertex.add(menuVertexRemove);

		JMenu menuEdge = new JMenu("Ребро");
		menuBar.add(menuEdge);
		JMenuItem menuEdgeAdd = new JMenuItem("Новое");
		menuEdgeAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final EdgeEditWindow w = new EdgeEditWindow(frame, "Новое ребро", graph);
				w.setLocation(frame.getX(), frame.getY());

				w.addSaveListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						graph.connect(w.getEdge().getV1(), w.getEdge().getV2(), w.getEdge().getWeight());
						repaint();
						System.out.println("Ребро создано");
					}
				});

				w.setVisible(true);
			}
		});
		menuEdge.add(menuEdgeAdd);
		JMenuItem menuEdgeEdit = new JMenuItem("Правка");
		menuEdgeEdit.addActionListener(new ActionListener() {
			private Edge selectedEdge = null;

			public void actionPerformed(ActionEvent e) {
				final ItemSelectWindow<Edge> s = new ItemSelectWindow<Edge>(frame, "Выбор ребра", "Ребро", graph.getEdges());
				s.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectedEdge = s.getItem();
					}
				});
				s.setVisible(true);

				if (selectedEdge == null) {
					System.out.println("Нет выбранного ребра");
					return;
				}

				final EdgeEditWindow w = new EdgeEditWindow(frame, "Правка ребра", graph, selectedEdge);
				w.setLocation(frame.getX(), frame.getY());
				w.addSaveListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						w.save();
						repaint();
						System.out.println("Ребро изменено");
					}
				});
				w.setVisible(true);
			}
		});
		menuEdge.add(menuEdgeEdit);
		JMenuItem menuEdgeRemove = new JMenuItem("Удаление");
		menuEdgeRemove.addActionListener(new ActionListener() {
			private Edge selectedEdge = null;

			public void actionPerformed(ActionEvent e) {
				final ItemSelectWindow<Edge> s = new ItemSelectWindow<Edge>(frame, "Выбор ребра", "Ребро", graph.getEdges());
				s.setLocation(frame.getX(), frame.getY());
				s.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectedEdge = s.getItem();
					}
				});
				s.setVisible(true);

				if (selectedEdge == null) {
					System.out.println("Нет выбранного ребра");
					return;
				}

				graph.remove(selectedEdge);
				repaint();
				System.out.println("Ребро удалено");
			}
		});
		menuEdge.add(menuEdgeRemove);

		JMenu menuAlgo = new JMenu("Начать работу");
		menuBar.add(menuAlgo);
		JMenuItem menuAlgoStart = new JMenuItem("Старт");
		menuAlgoStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isConfigured) {
					isConfigured = true;
					algo.settingsFrame(frame);
				}
				algo.reset();
				algo.startAlgorithm(false);
			}
		});
		menuAlgoStart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuAlgo.add(menuAlgoStart);

		frame.setJMenuBar(menuBar);

		frame.setVisible(true);
	}

	/**
	 * repaint the canvas
	 */
	public void repaint() {
		canvas.repaint();
	}

	/**
	 * main method
	 * @param args args
	 */
	public static void main(String[] args) {
		Graph graph = new Graph();

		Vector<GraphAlgorithm> algorithms = new Vector<GraphAlgorithm>();

		algorithms.add(new Kruskal(graph));

		GraphGUI gui = new GraphGUI(algorithms, graph);
		gui.init();
	}
}