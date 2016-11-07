package com.gmail.assamoa.MemGraph;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.jfree.chart.JFreeChart;

public class GraphComponent extends Component {
	private static final long serialVersionUID = 3181710662925671350L;
	private BufferedImage graphImage;

	private Rectangle area;

	public GraphComponent() {
		setBounds(0, 0, 960, 540);
		area = new Rectangle(0, 0, 960, 540);
		graphImage = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
	}

	public void drawChart(JFreeChart chart) {
		Graphics g = graphImage.getGraphics();
		g.clearRect(0, 0, 960, 540);
		chart.draw((Graphics2D) g, area);
		invalidate();
		repaint();
	}

	public void paint(Graphics g) {
		g.drawImage(graphImage, 0, 0, null);
	}
}
