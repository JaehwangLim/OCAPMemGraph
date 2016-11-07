package com.gmail.assamoa.MemGraph;

import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

class MEMGraph extends JFrame {
	/**
	 *  
	 */
	private static final long serialVersionUID = -4464998464680176802L;

	private Container thisContainer;
	private int minHeapAxis = -1;
	private int maxHeapAxis = -1;
	private int maxHeap = 1;

	private int minNativeAxis = -1;
	private int maxNativeAxis = -1;
	private int maxNative = 1;

	private File logFile;
	private File lastDirectory;

	private JButton buttonRefresh;
	private JRadioButton rButtonYounGen;
	private JLabel minHeapText;
	private JLabel maxHeapText;
	private JTextField minHeapEdit;
	private JTextField maxHeapEdit;

	private JLabel minNativeText;
	private JLabel maxNativeText;
	private JTextField minNativeEdit;
	private JTextField maxNativeEdit;

	private boolean largeData = false;
	private boolean includeYounGen = true;

	private DefaultCategoryDataset datasetHeap;
	private DefaultCategoryDataset datasetNative;

	private GraphComponent graphComponent;

	private JMenu menu;
	private JMenuItem menuItemOPEN;
	private JMenuItem menuItemSAVE;

	private JFreeChart chart;

	public MEMGraph(String title) {
		super(title);
		setResizable(false);
		thisContainer = getContentPane();
		graphComponent = new GraphComponent(960, 540);
		thisContainer.add(graphComponent);

		display();

		this.invalidate();
	}

	public void display() {
		JMenuBar mb = new JMenuBar();

		buttonRefresh = new JButton("다시그리기");
		buttonRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (logFile != null) {
					boolean younGen = rButtonYounGen.isSelected();
					// young gen 포함 라이오 버튼이 토글된 경우는 로그파일을 다시 읽어야 함
					if (younGen != includeYounGen) {
						includeYounGen = younGen;
						readLogFile(logFile);
					} else {
						// youn gen 포함 여부의 변경이 없으면 paint만 다시 하면 됨
						refresh();
					}
				}
			}
		});

		rButtonYounGen = new JRadioButton("YounGen포함");
		rButtonYounGen.setSelected(true);

		minHeapEdit = new JTextField(3);
		maxHeapEdit = new JTextField(3);
		minHeapText = new JLabel("  최소Heap: ");
		maxHeapText = new JLabel("  최대Heap: ");

		minNativeEdit = new JTextField(3);
		maxNativeEdit = new JTextField(3);
		minNativeText = new JLabel("  최소Native: ");
		maxNativeText = new JLabel("  최대Native: ");

		menuItemOPEN = new JMenuItem("열기");
		menuItemSAVE = new JMenuItem("저장");

		menuItemOPEN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				if (lastDirectory != null) {
					chooser.setCurrentDirectory(lastDirectory);
				}
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					lastDirectory = chooser.getCurrentDirectory();
					logFile = chooser.getSelectedFile();
					readLogFile(logFile);
					menuItemSAVE.setEnabled(true);
				}
			}
		});

		menuItemSAVE.setEnabled(false);
		menuItemSAVE.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(lastDirectory);
				chooser.setFileFilter(new FileNameExtensionFilter("jpg", new String[] { "jpg" }));
				chooser.setSelectedFile(new File(logFile.getName() + ".jpg"));
				int returnVal = chooser.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File outFile = chooser.getSelectedFile();
					if (outFile.exists()) {
						outFile.delete();
					}
					try {
						ChartUtilities.saveChartAsPNG(outFile, chart, 960, 540);
					} catch (IOException ee) {
						ee.printStackTrace();
					}
				}
			}
		});

		menu = new JMenu("파일");
		menu.add(menuItemOPEN);
		menu.add(menuItemSAVE);

		menu.addSeparator();

		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("'Exit' clicked");
				System.exit(0);
			}
		});
		menu.add(exit);
		mb.add(menu);

		mb.add(buttonRefresh);
		mb.add(rButtonYounGen);
		mb.add(minHeapText);
		mb.add(minHeapEdit);
		mb.add(maxHeapText);
		mb.add(maxHeapEdit);

		mb.add(minNativeText);
		mb.add(minNativeEdit);
		mb.add(maxNativeText);
		mb.add(maxNativeEdit);

		setJMenuBar(mb);
	}

	private void readLogFile(File file) {
		OCAPLogParser parser = new OCAPLogParser();
		FileReader fr = null;
		BufferedReader br = null;
		largeData = false;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line = "";
			datasetHeap = new DefaultCategoryDataset();
			datasetNative = new DefaultCategoryDataset();

			int count = 1;
			boolean oldGen = false;
			while ((line = br.readLine()) != null) {
				if (oldGen) {
					String free = parser.getFreeMEM(line);
					if (free != null) {
						try {
							StringTokenizer token = new StringTokenizer(free, "|");
							int heapMem = Integer.parseInt(token.nextToken());
							int nativeMem = Integer.parseInt(token.nextToken());

							datasetHeap.addValue(heapMem, "Free Heap", "" + count);
							datasetNative.addValue(nativeMem, "Free Native", "" + count);
							count++;
							if (count == Integer.MAX_VALUE) {
								count = 0;
								largeData = true;
							}
							if (maxHeap == 1) {
								String maxMem = parser.getMaxMem(line);
								StringTokenizer token2 = new StringTokenizer(maxMem, "|");
								try {
									maxHeap = Integer.parseInt(token2.nextToken());
									maxNative = Integer.parseInt(token2.nextToken());
								} catch (Exception e) {
								}
							}
							oldGen = false;
						} catch (Exception ex) {
						}
					}
				} else if (line.indexOf("Manual GC END") > -1) {
					oldGen = true;
				} else if (rButtonYounGen.isSelected()) {
					if (line.indexOf("END GC") > -1) {
						oldGen = true;
					}
				}
			}
			refresh();
			System.out.println("DONE:" + (largeData ? "A LOT OF" : "" + count) + " memory logs");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception ex) {
			}
			try {
				fr.close();
			} catch (Exception ex) {
			}
		}

	}

	private void refresh() {
		System.out.println("refresh...");
		chart = ChartFactory.createLineChart("MEM", "time", "byte", null);

		CategoryPlot plot = chart.getCategoryPlot();

		plot.getDomainAxis().setVisible(false);
		plot.getDomainAxis().setLowerMargin(0);
		plot.getDomainAxis().setUpperMargin(0);

		plot.setDataset(0, datasetHeap);
		plot.setDataset(1, datasetNative);

		String minHeapSet = minHeapEdit.getText();
		String maxHeapSet = maxHeapEdit.getText();
		String minNativeSet = minNativeEdit.getText();
		String maxNativeSet = maxNativeEdit.getText();
		try {
			minHeapAxis = Integer.parseInt(minHeapSet);
		} catch (Exception e) {
			minHeapAxis = -1;
		}
		try {
			maxHeapAxis = Integer.parseInt(maxHeapSet);
		} catch (Exception e) {
			maxHeapAxis = -1;
		}
		try {
			minNativeAxis = Integer.parseInt(minNativeSet);
		} catch (Exception e) {
			minNativeAxis = -1;
		}
		try {
			maxNativeAxis = Integer.parseInt(maxNativeSet);
		} catch (Exception e) {
			maxNativeAxis = -1;
		}

		NumberAxis heapAxis = new NumberAxis("Free Heap");
		heapAxis.setLabelPaint(Color.blue);
		if (minHeapAxis > -1) {
			if (maxHeapAxis > -1) {
				heapAxis.setRange(minHeapAxis, maxHeapAxis);
			} else {
				heapAxis.setRange(minHeapAxis, maxHeap);
			}
		} else if (maxHeapAxis > -1) {
			heapAxis.setRange(0, maxHeapAxis);
		} else {
			heapAxis.setRange(0, maxHeap);
		}

		NumberAxis nativeAxis = new NumberAxis("Free Native");
		nativeAxis.setLabelPaint(Color.red);
		if (minNativeAxis > -1) {
			if (maxNativeAxis > -1) {
				nativeAxis.setRange(minNativeAxis, maxNativeAxis);
			} else {
				nativeAxis.setRange(minNativeAxis, maxNative);
			}
		} else if (maxNativeAxis > -1) {
			nativeAxis.setRange(0, maxNativeAxis);
		} else {
			nativeAxis.setRange(0, maxNative);
		}

		plot.setRangeAxis(0, heapAxis);
		plot.setRangeAxis(1, nativeAxis);

		plot.mapDatasetToRangeAxis(0, 0);
		plot.mapDatasetToRangeAxis(1, 1);

		CategoryItemRenderer renderer1 = new LineAndShapeRenderer();
		renderer1.setSeriesPaint(0, Color.blue);
		Rectangle r = new Rectangle(0, 0, 0, 0);
		renderer1.setBaseShape(r);
		renderer1.setSeriesShape(0, r);

		CategoryItemRenderer renderer2 = new LineAndShapeRenderer();
		renderer2.setSeriesPaint(0, Color.red);
		renderer2.setBaseShape(r);
		renderer2.setSeriesShape(0, r);

		plot.setRenderer(0, renderer1);
		plot.setRenderer(1, renderer2);

		chart.setBackgroundPaint(java.awt.Color.white);
		chart.setTitle(logFile.getName());

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		thisContainer.add(chartPanel);
		thisContainer.invalidate();

		graphComponent.drawChart(chart);
	}

	public static void main(String[] args) {
		MEMGraph hs = new MEMGraph("OCAP Memory Graph Generator");
		hs.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		hs.pack();
		hs.setSize(980, 600);
		hs.setVisible(true);
	}
}
