package com.gmail.assamoa.memgraph;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.jfree.chart.JFreeChart;

public class GraphComponent extends Component {
	private static final long serialVersionUID = 3181710662925671350L;
	// 그래프 이미지를 그릴 버퍼
	private BufferedImage graphImage;
	private Rectangle area;

	public GraphComponent(int w, int h) {
		setBounds(0, 0, w, h);
		area = new Rectangle(0, 0, w, h);
		graphImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	}

	public void drawChart(JFreeChart chart) {
		Graphics g = graphImage.getGraphics();
		g.clearRect(area.x, area.y, area.width, area.height);
		chart.draw((Graphics2D) g, area);
		repaint();
	}

	public void paint(Graphics g) {
		g.drawImage(graphImage, 0, 0, null);
	}
}
