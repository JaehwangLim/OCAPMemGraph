package com.gmail.assamoa.memgraph;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class MemoryGraph {
	public static final int GRAPH_WIDTH = 960;
	public static final int GRAPH_HEIGHT = 540;

	private static final int MEMORY_SIZE_UNKNOWN = -1;

	private JFreeChart chart;
	private ChartPanel chartPanel;
	private CategoryPlot plot;

	private long minHeapAxis = MEMORY_SIZE_UNKNOWN;
	private long maxHeapAxis = MEMORY_SIZE_UNKNOWN;
	private long minNativeAxis = MEMORY_SIZE_UNKNOWN;
	private long maxNativeAxis = MEMORY_SIZE_UNKNOWN;

	private long maxFreeHeap;
	private long maxFreeNative;

	private NumberAxis heapAxis;
	private NumberAxis nativeAxis;

	private String fileName;

	public MemoryGraph(long maxFreeHeap, long maxFreeNative, String fileName) {
		this.maxFreeHeap = maxFreeHeap;
		this.maxFreeNative = maxFreeNative;
		this.fileName = fileName;

		// 챠트를 생성한다.
		chart = ChartFactory.createLineChart("MEM", "time", "byte", null);
		chartPanel = new ChartPanel(chart);

		// x,y 축 편집
		plot = chart.getCategoryPlot();

		// x축의 글자표시를 없앤다
		plot.getDomainAxis().setVisible(false);

		// x축의 좌우 여백을 없앤다
		plot.getDomainAxis().setLowerMargin(0);
		plot.getDomainAxis().setUpperMargin(0);

		// y축을 생성한다.heap-파랑, native-빨강
		heapAxis = new NumberAxis("Free Heap");
		heapAxis.setLabelPaint(Color.blue);
		nativeAxis = new NumberAxis("Free Native");
		nativeAxis.setLabelPaint(Color.red);

		// y축을 나눈다. heap-왼쪽, native-오른쪽
		plot.setRangeAxis(0, heapAxis);
		plot.setRangeAxis(1, nativeAxis);
		plot.mapDatasetToRangeAxis(0, 0);
		plot.mapDatasetToRangeAxis(1, 1);

		// heap/native 그래프의 속성 정의. heap-파랑, native-빨강. 둘다 선으로만 그리도록...
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
		chart.setTitle(this.fileName);
	}

	public void checkMinMaxY(JTextField minHeapEdit, JTextField maxHeapEdit, JTextField minNativeEdit, JTextField maxNativeEdit) {
		// 각 text상자의 값의 메모리 값을 읽는다.
		//
		String minHeapSet = minHeapEdit.getText();
		String maxHeapSet = maxHeapEdit.getText();
		String minNativeSet = minNativeEdit.getText();
		String maxNativeSet = maxNativeEdit.getText();
		try {
			minHeapAxis = Integer.parseInt(minHeapSet);
		} catch (Exception e) {
			minHeapEdit.setText("");
			minHeapAxis = MEMORY_SIZE_UNKNOWN;
		}
		try {
			maxHeapAxis = Integer.parseInt(maxHeapSet);
		} catch (Exception e) {
			maxHeapEdit.setText("");
			maxHeapAxis = MEMORY_SIZE_UNKNOWN;
		}
		try {
			minNativeAxis = Integer.parseInt(minNativeSet);
		} catch (Exception e) {
			minNativeEdit.setText("");
			minNativeAxis = MEMORY_SIZE_UNKNOWN;
		}
		try {
			maxNativeAxis = Integer.parseInt(maxNativeSet);
		} catch (Exception e) {
			maxNativeEdit.setText("");
			maxNativeAxis = MEMORY_SIZE_UNKNOWN;
		}

		// min/max heap 크기를 확인하여 Y축의 min/max 범위를 설정한다.
		if (minHeapAxis > MEMORY_SIZE_UNKNOWN) {
			if (maxHeapAxis > MEMORY_SIZE_UNKNOWN) {
				if (maxHeapAxis <= minHeapAxis) { // max가 min보다 작거나 같으면 min의 두배로 자동 설정한다.
					maxHeapAxis = minHeapAxis * 2;
					maxHeapEdit.setText(String.valueOf(maxHeapAxis)); // text상자의 값도 바꾼다.
					maxHeapEdit.setForeground(Color.red);
				} else {
					maxHeapEdit.setForeground(Color.black);
				}
				heapAxis.setRange(minHeapAxis, maxHeapAxis);
			} else {
				heapAxis.setRange(minHeapAxis, maxFreeHeap);
			}
		} else if (maxHeapAxis > MEMORY_SIZE_UNKNOWN) {
			heapAxis.setRange(0, maxHeapAxis);
		} else {
			// 입력된게 없으면, 0 ~ max
			heapAxis.setRange(0, maxFreeHeap);
		}

		// min/max native 크기 입력을 확인하여 Y축의 min/max 범위를 설정한다.
		if (minNativeAxis > MEMORY_SIZE_UNKNOWN) {
			if (maxNativeAxis > MEMORY_SIZE_UNKNOWN) {
				if (maxNativeAxis <= minNativeAxis) { // max가 min보다 작거나 같으면 min의 두배로 자동 설정한다.
					maxNativeAxis = minNativeAxis * 2;
					maxNativeEdit.setText(String.valueOf(maxNativeAxis)); // text상자의 값도 바꾼다.
					maxNativeEdit.setForeground(Color.red);
				} else {
					maxNativeEdit.setForeground(Color.black);
				}
				nativeAxis.setRange(minNativeAxis, maxNativeAxis);
			} else {
				nativeAxis.setRange(minNativeAxis, maxFreeNative);
			}
		} else if (maxNativeAxis > MEMORY_SIZE_UNKNOWN) {
			nativeAxis.setRange(0, maxNativeAxis);
		} else {
			nativeAxis.setRange(0, maxFreeNative);
		}
	}

	public void setData(DefaultCategoryDataset heapMem, DefaultCategoryDataset nativeMem) {
		plot.setDataset(0, heapMem);
		plot.setDataset(1, nativeMem);
	}

	public boolean storeGraphImage(File outFile) {
		try {
			ChartUtilities.saveChartAsPNG(outFile, chart, GRAPH_WIDTH, GRAPH_HEIGHT);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	public JFreeChart getChart() {
		return chart;
	}

	public ChartPanel getChartPanel() {
		return chartPanel;
	}
}
