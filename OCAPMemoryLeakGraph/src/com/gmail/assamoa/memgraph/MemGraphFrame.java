package com.gmail.assamoa.memgraph;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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

public class MemGraphFrame extends JFrame {
	private static final long serialVersionUID = -4464998464680176802L;

	private Container contentPane; // content pane of thie Frame

	private JButton buttonRefresh; // 다시그리기 버튼
	private JRadioButton rButtonYounGen; // YoungGen포함 라디오 버튼
	private JLabel minHeapText; // min Heap 라벨
	private JLabel maxHeapText; // max Heap 라벨
	private JTextField minHeapEdit; // min Heap 입력상자
	private JTextField maxHeapEdit; // max Heap 입력상자

	private JLabel minNativeText; // min Native 라벨
	private JLabel maxNativeText; // max Native 라벨
	private JTextField minNativeEdit; // min Native 입력상자
	private JTextField maxNativeEdit; // max Native 입력상자

	private JRadioButton rButtonTime; // 시간값포함 라디오 버튼
	private JLabel timeFormatText; // 시간값포멧 라벨
	private JTextField timeFormat; // 시간값 포멧 입력상자

	private boolean includeYounGen = true;
	private boolean includeTime = false;

	private GraphComponent graphComponent; // 그래프를 그릴 Component

	private JMenu menu; // 메뉴 바
	private JMenuItem menuItemOPEN; // 열기 메뉴
	private JMenuItem menuItemSAVE; // 저장 메뉴

	private JFileChooser chooser; // 읽기/저장 파일탐색기

	private File logFile;
	private MemoryGraph memoryGraph;
	private OCAPLogParser logParser;

	public MemGraphFrame(String title) {
		super(title);
		setResizable(false);
		contentPane = getContentPane();
		graphComponent = new GraphComponent(960, 540);
		contentPane.add(graphComponent);

		// 파일탐색기 생성
		chooser = new JFileChooser();

		display();

		this.invalidate();
	}

	public void display() {
		JMenuBar mb = new JMenuBar();

		buttonRefresh = new JButton("다시그리기");
		buttonRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 다시 그리기
				if (logFile != null) {
					boolean younGen = rButtonYounGen.isSelected();
					boolean time = rButtonTime.isSelected();
					// young gen 포함여부나 시간포함여부에 변경이 있으면, 로그 파싱을 다시해야 한다.
					if (younGen != includeYounGen) {
						includeYounGen = younGen;
						parseLog();
					} else if (time != includeTime) {
						includeTime = time;
						parseLog();
					} else {
						// youn gen 포함여부나 시간포함여부 변경이 없으면 기존 parse 데이터로 X축만 업데이트 하여 다시 그린다.
						drawGraph();
					}
				}
			}
		});

		rButtonYounGen = new JRadioButton("YounGen포함");
		rButtonYounGen.setSelected(includeYounGen);

		minHeapEdit = new JTextField(3);
		maxHeapEdit = new JTextField(3);
		minHeapText = new JLabel("  최소Heap: ");
		maxHeapText = new JLabel("  최대Heap: ");

		minNativeEdit = new JTextField(3);
		maxNativeEdit = new JTextField(3);
		minNativeText = new JLabel("  최소Native: ");
		maxNativeText = new JLabel("  최대Native: ");

		rButtonTime = new JRadioButton("시간포함");
		rButtonTime.setSelected(includeTime);
		rButtonTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeFormatText.setEnabled(rButtonTime.isSelected());
				timeFormat.setEnabled(rButtonTime.isSelected());
			}
		});
		timeFormatText = new JLabel("  시간포맷: ");
		timeFormat = new JTextField(3);
		timeFormatText.setEnabled(rButtonTime.isSelected());
		timeFormat.setEnabled(rButtonTime.isSelected());

		menuItemOPEN = new JMenuItem("열기");
		menuItemSAVE = new JMenuItem("저장");

		menuItemOPEN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileFilter(new FileNameExtensionFilter("로그파일", new String[] { "txt", "log", "\\d+" })); // "\\d+" : 숫자(스마트박스 USB 로그캡쳐시 100MB단위로 구분되어 숫자로 확장자가 생성됨
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					logFile = chooser.getSelectedFile();
					// 로그파일 분석
					parseLog();

					// 저장버튼을 활성화 시킨다
					menuItemSAVE.setEnabled(true);
				}
			}
		});

		menuItemSAVE.setEnabled(false);
		menuItemSAVE.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileFilter(new FileNameExtensionFilter("jpg", new String[] { "jpg" }));
				chooser.setSelectedFile(new File(chooser.getSelectedFile().getName() + ".jpg"));
				int returnVal = chooser.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File outFile = chooser.getSelectedFile();
					if (outFile.exists()) {
						outFile.delete();
					}
					// 챠트 이미지 저장
					if (memoryGraph.storeGraphImage(outFile)) {
						// 성공
					} else {
						// 실패
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

		mb.add(rButtonTime);
		mb.add(timeFormatText);
		mb.add(timeFormat);
		setJMenuBar(mb);
	}

	/**
	 * 로그파일을 파싱하고 그래프를 그린다.
	 */
	private void parseLog() {
		logParser = new OCAPLogParser(rButtonYounGen.isSelected(), rButtonTime.isSelected());
		logParser.readLogFile(logFile);
		memoryGraph = new MemoryGraph(logParser.getTotalHeapSize(), logParser.getTotalNativeSize(), logFile.getName());
		// memoryGraph.setData(logParser.getDatasetHeap(), logParser.getDatasetNative());
		memoryGraph.setData(logParser.getHeapSeries(), logParser.getNativeSeries());
		drawGraph();
	}

	/**
	 * 그래프를 그린다.
	 */
	private void drawGraph() {
		memoryGraph.checkMinMaxY(minHeapEdit, maxHeapEdit, minNativeEdit, maxNativeEdit);
		add(memoryGraph.getChartPanel());
		// graphComponent.drawChart(memoryGraph.getChart());
	}
}
