package application;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

public class Data {
	int width;
	int height;
	double[][] psldata;
	double pslmax;
	double pslmin;
	BufferedImage bimg;
	WritableImage image;
	FileInformation info;
	
	static final double m = 9.10939e-31;
	static final double c = 2.99792458e+8;
	static final double e = 1.60218e-19;
	static final double mc2 = (m*c*c)/(e*1e+6); /*Unit : Mev*/

	public class Profile{
		double length;
		double net;
		double signal;
		double background;
		double net_psl;
		double background_psl;
		double sensitivity;
		double transmittance;
		double fading;
		boolean invalid;
	}
	Profile[] profile;

	Data(File file) {
		info = new FileInformation(file);
		try {
			short pixels[];
			bimg = ImageIO.read(file);
			pixels = ((DataBufferUShort) bimg.getRaster().getDataBuffer()).getData();
			width = bimg.getWidth();
			height = bimg.getHeight();
			psldata = new double[height][width];
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					psldata[i][j] = 0xFFFF & ~pixels[i * width + j];
				}
			}
			ILEConvert();
			pslmin = pslmax = psldata[0][0];
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if (pslmin > psldata[i][j]) {
						pslmin = psldata[i][j];
					}
					if (pslmax < psldata[i][j]) {
						pslmax = psldata[i][j];
					}
				}
			}
			image = SwingFXUtils.toFXImage(bimg, null);
			// applyHeatMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	private void applyHeatMap() {
//		WritableImage newimage = new WritableImage(width, height);
//		PixelWriter pixelWriter = newimage.getPixelWriter();
//		double hue;
//		double BLUE_HUE = Color.BLUE.getHue();
//		double RED_HUE = Color.RED.getHue();
//		double max = psldata[0][0], min = psldata[0][0];
//		for (int i = 0; i < height; i++) {
//			for (int j = 0; j < width; j++) {
//				if (psldata[i][j] < min) {
//					min = psldata[i][j];
//				}
//				if (psldata[i][j] > max) {
//					max = psldata[i][j];
//				}
//			}
//		}
//		for (int i = 0; i < height; i++) {
//			for (int j = 0; j < width; j++) {
//				hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * (psldata[i][j] - min) / (max - min);
//				pixelWriter.setColor(j, i, Color.hsb(hue, 1.0, 1.0));
//			}
//		}
//		bimg = SwingFXUtils.fromFXImage(newimage, null);
//	}

	private void ILEConvert() {
		double coef1, coef2;
		coef1 = 0.092906 + 1370.8 * Math.exp(-0.014874 * info.voltage) + 654.24 * Math.exp(-0.011026 * info.voltage);
		coef2 = (0.000023284 / 100000.0) * ((info.resolution * 1e+6) / 100.0) * ((info.resolution * 1e+6) / 100.0)
				* coef1 * Math.pow(10.0, info.latitude / 2.0);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				psldata[i][j] = coef2 * psldata[i][j] * psldata[i][j];
			}
		}
	}
	
	public void rescaleImage(double scale) {
		WritableRaster raster = bimg.getRaster();
		int graylevel;
		int min = 0;
		int max = 0xFFFF;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (psldata[i][j] > scale * pslmax) {
					graylevel = max;
				} else {
					graylevel = (int) (min + (psldata[i][j] - pslmin) * (max - min) / (pslmax * scale - pslmin));
				}
				// if(i%100==0 && j%100==0) {System.out.printf("pixel[%d][%d] :
				// %d\n",i,j,graylevel);}
				raster.setSample(j, i, 0, graylevel);

				// pw.setArgb(j, i, ((0xFF)<<24) + (graylevel << 16) + (graylevel << 8) +
				// graylevel);
			}
		}
		image = SwingFXUtils.toFXImage(bimg, image);
	}

	public void setMinMax(double min, double max) {
		WritableRaster raster = bimg.getRaster();
		int graylevel;
		int imin = 0;
		int imax = 0xFFFF;
		double dmax = pslmin + (pslmax - pslmin) * max;
		double dmin = pslmin + (pslmax - pslmin) * min;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (psldata[i][j] > dmax) {
					graylevel = imax;
				} else if (psldata[i][j] < dmin) {
					graylevel = imin;
				} else {
					graylevel = (int) (imin + (psldata[i][j] - dmin) * (imax - imin) / (dmax - dmin));
				}
				// if(i%100==0 && j%100==0) {System.out.printf("pixel[%d][%d] :
				// %d\n",i,j,graylevel);}
				raster.setSample(j, i, 0, graylevel);

				// pw.setArgb(j, i, ((0xFF)<<24) + (graylevel << 16) + (graylevel << 8) +
				// graylevel);
			}
		}
		image = SwingFXUtils.toFXImage(bimg, image);
	}	
	

	private double dot(double[] a, double[] b) {return a[0] * b[0] + a[1] * b[1];}
	private double norm(double[] a) {return Math.sqrt(dot(a, a));}

	public void lineProfile(double xs, double ys, double xe, double ye, ESM esm) {
		double[] p = { xe - xs, ye - ys };
		double[] q = { 0, 0 };
		double normp = norm(p);
		int length = (int) normp;
		double signal_half_width = 0.5 * esm.collimatorDiametor / info.resolution;
		double bg_half_width = esm.bgBandHalfWidth / info.resolution;
		int ceil = (int) Math.ceil(signal_half_width);
		int count_net,count_background;
		
		//System.out.printf("signal_half_width : %e\n", signal_half_width);
		//System.out.printf("bg_half_width : %e\n", bg_half_width);
		//System.out.printf("ceil : %d\n", ceil);
		profile = new Profile[length];
		int ix, iy, ix2, iy2;
		double s,t,s2,t2,h,v;
		double R,K,energy,theta,H,L,coef;
		
		for (int i = 0; i < length; i++) {
			profile[i] = new Profile();
			profile[i].length = (normp*i/length)*info.resolution;
			profile[i].invalid = false;
			profile[i].net_psl = 0; /*net signal*/
			profile[i].background_psl = 0; /*background*/
			
			/*main signal*/
			s = i * p[0] / length;
			t = i * p[1] / length;
			ix = (int) (xs + s);
			iy = (int) (ys + t);
			count_net = 0;
			for (int j = -ceil; j < ceil; j++) {
				for (int k = -ceil; k < ceil; k++) {
					q[0] = s + j;
					q[1] = t + k;
					h = dot(p, q) / normp;
					v = Math.sqrt( (dot(q, q) * dot(p, p) - dot(p, q) * dot(p, q) ) / dot(p, p) );
					if (Math.abs(h - i) < 0.5 && v < signal_half_width) {
						if (0 <= ix + j && ix + j < width) {
							if (0 <= iy + k && iy + k < height) {
								//if(i%200<100) {image.getPixelWriter().setArgb(ix + j,iy + k, 0xFF << 24 | 0xFF);}
								profile[i].net_psl += psldata[iy + k][ix + j];
								count_net++;
							}
						}

					}
				}
			}
			profile[i].net_psl /= (count_net/(2*signal_half_width));
			//System.out.printf("%f,",(count_net/(2*signal_half_width)));
			/*background left and right*/
			s = i * p[0] / length - (p[1]/normp)*bg_half_width;
			t = i * p[1] / length + (p[0]/normp)*bg_half_width;
			s2 = i * p[0] / length + (p[1]/normp)*bg_half_width;
			t2 = i * p[1] / length - (p[0]/normp)*bg_half_width;
			ix = (int) (xs + s);
			iy = (int) (ys + t);
			ix2 = (int) (xs + s2);
			iy2 = (int) (ys + t2);
			count_background=0;
			for (int j = -2*ceil; j < 2*ceil; j++) {
				for (int k = -2*ceil; k < 2*ceil; k++) {
					/*h and v for s is same for s2*/
					q[0] = s + j;
					q[1] = t + k;
					h = dot(p, q) / normp;
					v = Math.sqrt( (dot(q, q) * dot(p, p) - dot(p, q) * dot(p, q) ) / dot(p, p) );
					if(Math.abs(h - i) < 0.5) {
						if (v < bg_half_width && bg_half_width - signal_half_width < v) {
							if (0 <= ix + j && ix + j < width) {
								if (0 <= iy + k && iy + k < height) {
									profile[i].background_psl += psldata[iy + k][ix + j];
									count_background++;
									// if(i%20<10) {image.getPixelWriter().setArgb(ix + j,iy + k, 0xFF << 24 |
									// 0xFF);}
								}
							}
							if (0 <= ix2 - j && ix2 - j < width) {
								if (0 <= iy2 - k && iy2 - k < height) {
									profile[i].background_psl += psldata[iy2 - k][ix2 - j];
									count_background++;
									// if(i%20<10) {image.getPixelWriter().setArgb(ix2 - j,iy2 - k, 0xFF << 24 |
									// 0xFF);}
								}
							}
						}
					}
				}
			}
			profile[i].background /= (count_background/(2*signal_half_width));
			//System.out.printf("%f\n",(count_background/(2*signal_half_width)));
			/*calculate energy spectrum*/
			//profile[i].length -= esm.length;
			profile[i].net_psl-= profile[i].background_psl;
			H = esm.height;
			L = profile[i].length;
			R = (L*L + H*H)/(2*H);
			K = (e*esm.magnetStrength)/(m*c);
			/*total energy : mc2*(sqrt(1+(p/mc)^2))*/
			/*kinetic energy : mc2*(sqrt(1+(p/mc)^2)-1)*/
			/*R = (gamma*mv/eB) = p/eB*/
			/*p = ReB*/
			/*K*R = p/(mc) = (ReB/mc)*/
			energy = mc2*(Math.sqrt(R*R*K*K+1)-1);
			theta = Math.atan2(L*L-H*H,2*H*L);
			/*E = mc2**(sqrt(1+(R*K)^2)-1)*/
			/*dE/dL = mc2*(K^2)*R*(L/H)/sqrt(1+(K*R)^2) */
			/*signa/(dE*SolidAngle) = (signal/(res*SolidAngle)) * sqrt(1+(K*R)^2) * (H/L) / (mc2*K*K*R)*/
			coef = Math.sqrt(1+1/K*K*R*R)*(H/L)/((mc2*K)*(info.resolution*esm.solidAngle));
			coef /= transmittance(energy,theta,esm.filterThickness*1e+2);
			coef /= sensitivity(energy,theta);
			coef /= fading(info.fadingTime);
			profile[i].transmittance = transmittance(energy,theta,esm.filterThickness*1e+2);
			profile[i].sensitivity = sensitivity(energy,theta);
			profile[i].fading = fading(info.fadingTime);

			profile[i].length = energy;
			profile[i].net = profile[i].net_psl*coef;
			profile[i].signal = (profile[i].net_psl+profile[i].background_psl)*coef;
			profile[i].background = profile[i].background_psl*coef;
			if(profile[i].length < 0 || profile[i].net < 0 || energy < 0.7) {
				profile[i].invalid = true;
				continue;
			}
		}
		//System.out.printf("end\n");
	}
	
	private double transmittance(double energy,double theta,double althickness){
		/*ここはcgs単位系*/
		double range,taurange,AlA,AlZ,AlRho,ta,tb,tc,td,te;
		
		AlA=26.981538;
		AlZ=13.0;
		AlRho=2.69;
		ta=0.537;
		tb=0.9815;
		tc=3.1230;
		td=9.2*Math.pow(AlZ,-0.2)+16*Math.pow(AlZ,-2.2);
		te=0.63*AlZ/AlA+0.27;
		
		range=ta*energy*(1-tb/(1+tc*energy));
		/*taurange=(althickness*AlRho)/range;*/ /*斜入射無視*/
		taurange=(althickness*AlRho)/(range*Math.cos(theta)); /*斜入射有効*/
		return (1+Math.exp(-td*te))/(1+Math.exp(td*(taurange-te)));
	}
	
	private double sensitivity(double energy, double theta){
		double sa,sb,sc,sd,s;
		if(energy<0.05){
			sa=-1.37;
			sb=4.8955e-1;
			sc=9.34307e-3;
			s = sa*Math.pow(energy,2)+sb*energy+sc;
		}else if(energy>=0.05&&energy<0.12){
			sa=29.6061;
			sb=-10.7906;
			sc=1.24418;
			sd=-8.48498e-3;
			s = sa*Math.pow(energy,3)+sb*Math.pow(energy,2)+sc*energy+sd;
		}else if(energy>=0.12&&energy<0.137){
			sa=-1.30643e-1;
			sb=-2.64656e-2;
			sc=4.16476e-2;
			s = sa*Math.pow(energy,2)+sb*energy+sc;
		}else if(energy>=0.137&&energy<0.46){
			sa=4.06997e-2;
			sb=3.27383;
			sc=9.59762e-3;
			s = sa*Math.exp(-sb*energy)+sc;
		}else if(energy>=0.46&&energy<0.79){
			sa=4.14759e-2;
			sb=3.07174;
			sc=8.5134e-3;
			s = sa*Math.exp(-sb*energy)+sc;
		}else if(energy>=0.79&&energy<1.1){
			sa=1.40477e-2;
			sb=-3.53998e-2;
			sc=3.13743e-2;
			s = sa*Math.pow(energy,2)+sb*energy+sc;
		}else if(energy>=1.1&&energy<1.6){
			sa=1.49836;
			sb=7.00353;
			sc=-2.45725e-4;
			sd=9.05957e-3;
			s = sa*Math.exp(-sb*energy)+sc*Math.pow(energy,2)+sd;
		}else if(energy>=1.6&&energy<2.7){
			sa=4.4432e-3;
			sb=9.45248e-1;
			sc=7.46777e-3;
			s = sa*Math.exp(-sb*energy)+sc;
		}else if(energy>=2.7&&energy<5.7){
			sa=2.74341e-3;
			sb=7.27153e-1;
			sc=-5.07852e-5;
			sd=7.56518e-3;
			s = sa*Math.exp(-sb*energy)+sc*energy+sd;
		}else if(energy>=5.7&&energy<9.8){
			sa=1.51122e-3;
			sb=4.05074e-1;
			sc=-1.98777e-5;
			sd=7.28229e-3;
			s = sa*Math.exp(-sb*energy)+sc*energy+sd;
		}else if(energy>=9.8&&energy<13){
			sa=1.15864e-3;
			sb=2.9994e-1;
			sc=-1.29653e-5;
			sd=7.18185e-3;
			s = sa*Math.exp(-sb*energy)+sc*energy+sd;
		}else if(energy>=13&&energy<37){
			sa=5.42261e-4;
			sb=1.37381e-1;
			sc=-6.97495e-6;
			sd=7.03642e-3;
			s = sa*Math.exp(-sb*energy)+sc*energy+sd;
		}else{
			sa=2.04741e-4;
			sb=5.29949e-2;
			sc=-5.73563e-6;
			sd=6.96509e-3;
			s = sa*Math.exp(-sb*energy)+sc*energy+sd;
		}
		return s/Math.cos(theta); /*射入射により感度が上がる効果 RSI K.A.Tanaka et al*/
	}
	
	private double fading(double ftime){
		double ffa,ffb,fta,ftb,ftc;
		
		ffa=0.156;/*0.155857*/
		ffb=0.209;/*0.209367*/
		fta=0.558;/*0.557602*/
		ftb=11.3;/*11.2649*/
		ftc=2000;/*1999.96*/
		
		return ffa*Math.pow(0.5,(ftime/fta))+ffb*Math.pow(0.5,(ftime/ftb))+(1-(ffa+ffb))*Math.pow(0.5,(ftime/ftc));
	}

	public void printProfile(File file, ESM esm, String header) {
		BufferedWriter bw;
		if(profile==null) {return;}
		try {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(header(esm));
			bw.write(header + "\n");
			bw.write("\"Energy [MeV]\"\t" + 
					"\"d2N/(dE dOmega) [/(MeV Sr)]\"\t" +
					"\"Total Count [/(MeV Sr)]\"\t" +
					"\"Background Count [/(MeV Sr)]\"\t" +
					"\"Net PSL Count [/(MeV Sr)]\"\t" +
					"\"Background PSL Count [/(MeV Sr)]\"\n"
					);
			for(int i=0;i<profile.length;i++) {
				if(profile[i].invalid) {bw.write("#");}
				bw.write(
					String.format("%g\t%g\t%g\t#\t%g\t%g\n",
						profile[i].length,
						profile[i].signal,
						profile[i].background,
						profile[i].net_psl,
						profile[i].background_psl
						)
				);
			}
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	private String header(ESM esm) {
		return info.header() + esm.header();
	}
	public double calculateSlopeTemp(double min, double max) {
		Array2D array = new Array2D(profile.length,2);
		for(int i=0;i<profile.length;i++) {
			array.elem[i][0] = profile[i].length;
			array.elem[i][1] = -Math.log(profile[i].net);
		}
		/*exp(-E/(e Te))*/
		/*b.elem[0][1] offset*/
		/*b.elem[1][1] slope*/
		Array2D b = array.linefit(min,max);
		return 1.0/(b.elem[1][1]);
	}
}
