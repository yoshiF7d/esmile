package application;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

public class Bands {
	Line signal;
	Line bg1;
	Line bg2;
	//Line offset;
	Line[] list;
	
	double[] start;
	double[] end;
	double[] p;
	double[] pbg1;
	double[] pbg2;
	double bgBandHalfWidth;
	double collimatorDiametor;
	
	public Bands() {
		list = new Line[3];
		start = new double[2];
		end = new double[2];
		p = new double[2];
		pbg1 = new double[2];
		pbg2 = new double[2];
//		pofs = new double[2];
		
		this.signal = list[0] = new Line();
//		this.offset = list[1] = new Line();
		this.bg1 = list[1] = new Line();
		this.bg2 = list[2] = new Line();
		
		for(Line line : list) {
			line.setFill(null);
			line.setStrokeLineCap(StrokeLineCap.BUTT);
			line.setVisible(false);
			line.setMouseTransparent(true);
		}
		this.signal.setStroke(Color.color(1,0,0,0.1));
		//this.offset.setStroke(Color.color(1,0.647,0,0.1));
		this.bg1.setStroke(Color.color(0,0,1,0.1));
		this.bg2.setStroke(Color.color(0,0,1,0.1));
	} 
	void setVisible(boolean b) {
		for(Line line : list) {
			line.setVisible(b);
		}
	}
	void setStartX(double x) {start[0] = x;}
	void setStartY(double y) {start[1] = y;}
	void setEndX(double x) {end[0] = x;}
	void setEndY(double y) {end[1] = y;}

	private double dot(double[] a, double[] b) {return a[0] * b[0] + a[1] * b[1];}
	private double norm(double[] a) {return Math.sqrt(dot(a, a));}

	double getLength() {
		p[0] = end[0] - start[0];
		p[1] = end[1] - start[1];
		return norm(p);
	}

	void draw() {
		p[0] = end[0] - start[0];
		p[1] = end[1] - start[1];
		double normp = norm(p);
		pbg1[0] = -(p[1]/normp)*(bgBandHalfWidth-0.5*collimatorDiametor);
		pbg1[1] = (p[0]/normp)*(bgBandHalfWidth-0.5*collimatorDiametor);
		pbg2[0] = (p[1]/normp)*(bgBandHalfWidth-0.5*collimatorDiametor);
		pbg2[1] = -(p[0]/normp)*(bgBandHalfWidth-0.5*collimatorDiametor);
		//pofs[0] = (p[0]/normp)*length;
		//pofs[1] = (p[1]/normp)*length;
		signal.setStartX(start[0]); 
		signal.setStartY(start[1]);
		bg1.setStartX(start[0] + pbg1[0]); 
		bg1.setStartY(start[1] + pbg1[1]);
		bg2.setStartX(start[0] + pbg2[0]); 
		bg2.setStartY(start[1] + pbg2[1]);
		//offset.setStartX(start[0]); 
		//offset.setStartY(start[1]);
		signal.setEndX(end[0]); 
		signal.setEndY(end[1]);
		bg1.setEndX(end[0] + pbg1[0]); 
		bg1.setEndY(end[1] + pbg1[1]);
		bg2.setEndX(end[0] + pbg2[0]); 
		bg2.setEndY(end[1] + pbg2[1]);
		//offset.setEndX(start[0]); offset.setEndY(start[1]);
		setVisible(true);
	}
	
	void setBands(Data data, ESM esm, double height) {
		bgBandHalfWidth = (esm.bgBandHalfWidth/data.info.resolution)*height/data.height;
		collimatorDiametor = (esm.collimatorDiametor/data.info.resolution)*height/data.height;
		//length = (esm.length/data.info.resolution)*height/data.height;
		bg1.setStrokeWidth(collimatorDiametor);
		bg2.setStrokeWidth(collimatorDiametor);
		signal.setStrokeWidth(collimatorDiametor);
		//offset.setStrokeWidth(2*bgBandHalfWidth);
		draw();
	}
}
