package com.gmail.assamoa.memgraph;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class MEMGraph {// extends JFrame {
	/**
	 *  
	 */
	// private static final long serialVersionUID = -4464998464680176802L;
	//
	// private Container thisContainer;
	// private int minHeapAxis = -1;
	// private int maxHeapAxis = -1;
	// private int maxHeap = 1;
	//
	// private int minNativeAxis = -1;
	// private int maxNativeAxis = -1;
	// private int maxNative = 1;
	//
	// private File logFile;
	// private File lastDirectory;
	//
	// private JButton buttonRefresh;
	// private JRadioButton rButtonYounGen;
	// private JLabel minHeapText;
	// private JLabel maxHeapText;
	// private JTextField minHeapEdit;
	// private JTextField maxHeapEdit;
	//
	// private JLabel minNativeText;
	// private JLabel maxNativeText;
	// private JTextField minNativeEdit;
	// private JTextField maxNativeEdit;
	//
	// private JRadioButton rButtonTime;
	// private JLabel timeFormatText;
	// private JTextField timeFormat;
	//
	// private boolean largeData = false;
	// private boolean includeYounGen = true;
	// private boolean includeTime = false;
	//
	// private DefaultCategoryDataset datasetHeap;
	// private DefaultCategoryDataset datasetNative;
	// private DefaultCategoryDataset dataset;
	//
	// private GraphComponent graphComponent;
	//
	// private JMenu menu;
	// private JMenuItem menuItemOPEN;
	// private JMenuItem menuItemSAVE;
	//
	// private JFreeChart chart;
	//
	// public MEMGraph(String title) {
	// super(title);
	// setResizable(false);
	// thisContainer = getContentPane();
	// graphComponent = new GraphComponent(960, 540);
	// thisContainer.add(graphComponent);
	//
	// display();
	//
	// this.invalidate();
	// }
	//
	// public void display() {
	// JMenuBar mb = new JMenuBar();
	//
	// buttonRefresh = new JButton("�ٽñ׸���");
	// buttonRefresh.addActionListener(new ActionListener(){
	// public void actionPerformed(ActionEvent e) {
	// if (logFile != null) {
	// boolean younGen = rButtonYounGen.isSelected();
	// boolean time = rButtonTime.isSelected();
	// // young gen ���� ���̿� ��ư�� ��۵� ���� �α������� �ٽ� �о�� ��
	// if (younGen != includeYounGen) {
	// includeYounGen = younGen;
	// readLogFile(logFile);
	// } else if (time != includeTime) {
	// includeTime = time;
	// readLogFile(logFile);
	// } else {
	// // youn gen ���� ������ ������ ������ paint�� �ٽ� �ϸ� ��
	// refresh();
	// }
	// }
	// }
	//
	// });
	//
	// rButtonYounGen = new JRadioButton("YounGen����");
	// rButtonYounGen.setSelected(includeYounGen);
	//
	// minHeapEdit = new JTextField(3);
	// maxHeapEdit = new JTextField(3);
	// minHeapText = new JLabel(" �ּ�Heap: ");
	// maxHeapText = new JLabel(" �ִ�Heap: ");
	//
	// minNativeEdit = new JTextField(3);
	// maxNativeEdit = new JTextField(3);
	// minNativeText = new JLabel(" �ּ�Native: ");
	// maxNativeText = new JLabel(" �ִ�Native: ");
	//
	// rButtonTime = new JRadioButton("�ð�����");
	// rButtonTime.setSelected(includeTime);
	// rButtonTime.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// timeFormatText.setEnabled(rButtonTime.isSelected());
	// timeFormat.setEnabled(rButtonTime.isSelected());
	// }
	// });
	// timeFormatText = new JLabel(" �ð�����: ");
	// timeFormat = new JTextField(3);
	// timeFormatText.setEnabled(rButtonTime.isSelected());
	// timeFormat.setEnabled(rButtonTime.isSelected());
	//
	// menuItemOPEN = new JMenuItem("����");
	// menuItemSAVE = new JMenuItem("����");
	//
	// menuItemOPEN.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// JFileChooser chooser = new JFileChooser();
	// if (lastDirectory != null) {
	// chooser.setCurrentDirectory(lastDirectory);
	// }
	// int returnVal = chooser.showOpenDialog(null);
	// if (returnVal == JFileChooser.APPROVE_OPTION) {
	// lastDirectory = chooser.getCurrentDirectory();
	// logFile = chooser.getSelectedFile();
	// readLogFile(logFile);
	// menuItemSAVE.setEnabled(true);
	// }
	// }
	// });
	//
	// menuItemSAVE.setEnabled(false);
	// menuItemSAVE.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// JFileChooser chooser = new JFileChooser();
	// chooser.setCurrentDirectory(lastDirectory);
	// chooser.setFileFilter(new FileNameExtensionFilter("jpg", new String[] { "jpg" }));
	// chooser.setSelectedFile(new File(logFile.getName() + ".jpg"));
	// int returnVal = chooser.showSaveDialog(null);
	// if (returnVal == JFileChooser.APPROVE_OPTION) {
	// File outFile = chooser.getSelectedFile();
	// if (outFile.exists()) {
	// outFile.delete();
	// }
	// try {
	// ChartUtilities.saveChartAsPNG(outFile, chart, 960, 540);
	// } catch (IOException ee) {
	// ee.printStackTrace();
	// }
	// }
	// }
	// });
	//
	// menu = new JMenu("����");
	// menu.add(menuItemOPEN);
	// menu.add(menuItemSAVE);
	//
	// menu.addSeparator();
	//
	// JMenuItem exit = new JMenuItem("Exit");
	// exit.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// System.out.println("'Exit' clicked");
	// System.exit(0);
	// }
	// });
	// menu.add(exit);
	// mb.add(menu);
	//
	// mb.add(buttonRefresh);
	// mb.add(rButtonYounGen);
	// mb.add(minHeapText);
	// mb.add(minHeapEdit);
	// mb.add(maxHeapText);
	// mb.add(maxHeapEdit);
	//
	// mb.add(minNativeText);
	// mb.add(minNativeEdit);
	// mb.add(maxNativeText);
	// mb.add(maxNativeEdit);
	//
	// mb.add(rButtonTime);
	// mb.add(timeFormatText);
	// mb.add(timeFormat);
	// setJMenuBar(mb);
	// }
	//
	// private void readLogFile(File file) {
	// OCAPLogParser parser = new OCAPLogParser();
	// FileReader fr = null;
	// BufferedReader br = null;
	// largeData = false;
	// try {
	// fr = new FileReader(file);
	// br = new BufferedReader(fr);
	// String line = "";
	// datasetHeap = new DefaultCategoryDataset();
	// datasetNative = new DefaultCategoryDataset();
	// dataset = new DefaultCategoryDataset();
	//
	// int count = 1;
	// boolean oldGen = false;
	// boolean time = rButtonTime.isSelected();
	// boolean younGen = rButtonYounGen.isSelected();
	// long timeValue = System.currentTimeMillis();
	// while ((line = br.readLine()) != null) {
	// if (oldGen) {
	// String free = parser.getFreeMEM(line);
	// if (free != null) {
	// try {
	// StringTokenizer token = new StringTokenizer(free, "|");
	// int heapMem = Integer.parseInt(token.nextToken());
	// int nativeMem = Integer.parseInt(token.nextToken());
	//
	// if (time) {
	// datasetHeap.addValue(heapMem, "Free Heap", new Date(timeValue + count));
	// datasetNative.addValue(nativeMem, "Free Native", new Date(timeValue + count));
	// } else {
	// datasetHeap.addValue(heapMem, "Free Heap", "" + count);
	// datasetNative.addValue(nativeMem, "Free Native", "" + count);
	// }
	// count++;
	// if (count == Integer.MAX_VALUE) {
	// count = 0;
	// largeData = true;
	// }
	// if (maxHeap == 1) {
	// String maxMem = parser.getMaxMem(line);
	// StringTokenizer token2 = new StringTokenizer(maxMem, "|");
	// try {
	// maxHeap = Integer.parseInt(token2.nextToken());
	// maxNative = Integer.parseInt(token2.nextToken());
	// } catch (Exception e) {
	// }
	// }
	// oldGen = false;
	// } catch (Exception ex) {
	// }
	// }
	// } else if (line.indexOf("Manual GC END") > -1) {
	// oldGen = true;
	// } else if (younGen) {
	// if (line.indexOf("END GC") > -1) {
	// oldGen = true;
	// }
	// }
	// }
	// refresh();
	// System.out.println("DONE:" + (largeData ? "A LOT OF" : "" + count) + " memory logs");
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// } finally {
	// try {
	// br.close();
	// } catch (Exception ex) {
	// }
	// try {
	// fr.close();
	// } catch (Exception ex) {
	// }
	// }
	//
	// }
	//
	// private void refresh() {
	// System.out.println("refresh...");
	// chart = ChartFactory.createLineChart("MEM", "time", "byte", null);
	//
	// CategoryPlot plot = chart.getCategoryPlot();
	//
	// // plot.getDomainAxis().setVisible(false);
	// plot.getDomainAxis().setLowerMargin(0);
	// plot.getDomainAxis().setUpperMargin(0);
	//
	// plot.setDataset(0, datasetHeap);
	// plot.setDataset(1, datasetNative);
	//
	// String minHeapSet = minHeapEdit.getText();
	// String maxHeapSet = maxHeapEdit.getText();
	// String minNativeSet = minNativeEdit.getText();
	// String maxNativeSet = maxNativeEdit.getText();
	// try {
	// minHeapAxis = Integer.parseInt(minHeapSet);
	// } catch (Exception e) {
	// minHeapEdit.setText("");
	// minHeapAxis = -1;
	// }
	// try {
	// maxHeapAxis = Integer.parseInt(maxHeapSet);
	// } catch (Exception e) {
	// maxHeapEdit.setText("");
	// maxHeapAxis = -1;
	// }
	// try {
	// minNativeAxis = Integer.parseInt(minNativeSet);
	// } catch (Exception e) {
	// minNativeEdit.setText("");
	// minNativeAxis = -1;
	// }
	// try {
	// maxNativeAxis = Integer.parseInt(maxNativeSet);
	// } catch (Exception e) {
	// maxNativeEdit.setText("");
	// maxNativeAxis = -1;
	// }
	//
	// NumberAxis heapAxis = new NumberAxis("Free Heap");
	// heapAxis.setLabelPaint(Color.blue);
	// if (minHeapAxis > -1) {
	// if (maxHeapAxis > -1) {
	// heapAxis.setRange(minHeapAxis, maxHeapAxis);
	// } else {
	// heapAxis.setRange(minHeapAxis, maxHeap);
	// }
	// } else if (maxHeapAxis > -1) {
	// heapAxis.setRange(0, maxHeapAxis);
	// } else {
	// heapAxis.setRange(0, maxHeap);
	// }
	//
	// NumberAxis nativeAxis = new NumberAxis("Free Native");
	// nativeAxis.setLabelPaint(Color.red);
	// if (minNativeAxis > -1) {
	// if (maxNativeAxis > -1) {
	// nativeAxis.setRange(minNativeAxis, maxNativeAxis);
	// } else {
	// nativeAxis.setRange(minNativeAxis, maxNative);
	// }
	// } else if (maxNativeAxis > -1) {
	// nativeAxis.setRange(0, maxNativeAxis);
	// } else {
	// nativeAxis.setRange(0, maxNative);
	// }
	//
	// plot.setRangeAxis(0, heapAxis);
	// plot.setRangeAxis(1, nativeAxis);
	//
	// plot.mapDatasetToRangeAxis(0, 0);
	// plot.mapDatasetToRangeAxis(1, 1);
	//
	// CategoryItemRenderer renderer1 = new LineAndShapeRenderer();
	// renderer1.setSeriesPaint(0, Color.blue);
	// Rectangle r = new Rectangle(0, 0, 0, 0);
	// renderer1.setBaseShape(r);
	// renderer1.setSeriesShape(0, r);
	//
	// CategoryItemRenderer renderer2 = new LineAndShapeRenderer();
	// renderer2.setSeriesPaint(0, Color.red);
	// renderer2.setBaseShape(r);
	// renderer2.setSeriesShape(0, r);
	//
	// plot.setRenderer(0, renderer1);
	// plot.setRenderer(1, renderer2);
	//
	// chart.setBackgroundPaint(java.awt.Color.white);
	// chart.setTitle(logFile.getName());
	//
	// ChartPanel chartPanel = new ChartPanel(chart);
	// chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	// thisContainer.add(chartPanel);
	// thisContainer.invalidate();
	//
	// graphComponent.drawChart(chart);
	// }

	public static void main(String[] args) {
		// MEMGraph hs = new MEMGraph("OCAP Memory Graph Generator");
		// hs.addWindowListener(new WindowAdapter() {
		// public void windowClosing(WindowEvent we) {
		// System.exit(0);
		// }
		// });
		// hs.pack();
		// hs.setSize(980, 600);
		// hs.setVisible(true);

		MemGraphFrame frame = new MemGraphFrame("OCAP Memory Graph Gen");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});

		frame.pack();
		frame.setSize(MemoryGraph.GRAPH_WIDTH + 20, MemoryGraph.GRAPH_HEIGHT + 60);
		frame.setVisible(true);
	}
}
