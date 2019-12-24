package application;
/*GMII6=850 GMII8(withoutfrange)=850 GXII6=862 GXII8(withoutfrange)=857*/
/*You have to consider thickness of conversion frange : 8-6変換フランジ(ILE)の厚さ　2cm*/
public class ESM {
	int number;
	double length; /*length between ip edge and magnet edge (not used now)*/
	double height;
	double magnetStrength;
	double distance;
	double collimatorDiametor;
	double filterThickness;
	double solidAngle;
	double bgBandHalfWidth;
	static ESM[] list() {
		ESM[] e = new ESM[3];
		double collimatorDiametor_common = 5.0e-3; /*5 mm*/
		double filterThickness_common = 12.0e-6; /*Thickness of Al Filter : 12 um*/
		double bgBandHalfWidth_common = 11.5e-3;
		/*ESM1 parameters*/
		e[0] = new ESM();
		e[0].number = 1;
		e[0].length = 6.0e-3;
		e[0].magnetStrength = 0.45; 
		e[0].height = 11.0e-3;
		e[0].distance = 388.0e-3+862.0e-3;
		e[0].collimatorDiametor = collimatorDiametor_common;
		e[0].filterThickness = filterThickness_common;
		e[0].bgBandHalfWidth = bgBandHalfWidth_common;
		e[0].setSolidAngle();
		
		/*ESM2 parameters*/	
		e[1] = new ESM();
		e[1].number = 2;
		e[1].length = 12.5e-3;
		e[1].magnetStrength = 0.45;
		e[1].height = 13.0e-3;
		e[1].distance = 395.5e-3+857.0e-3+20.0e-3;
		e[1].collimatorDiametor = collimatorDiametor_common;
		e[1].filterThickness = filterThickness_common;
		e[1].bgBandHalfWidth = bgBandHalfWidth_common;
		e[1].setSolidAngle();
		
		/*ESM3 parameters*/
		e[2] = new ESM();
 		e[2].number = 3;
		e[2].length = 17.5e-3;
		e[2].magnetStrength = 0.45;
		e[2].height = 11.7e-3;
		e[2].distance = 388.0e-3+862.0e-3;
		e[2].collimatorDiametor = collimatorDiametor_common;
		e[2].filterThickness = filterThickness_common;
		e[2].bgBandHalfWidth = bgBandHalfWidth_common;
		e[2].setSolidAngle();
		return e;
	}
	
	void setSolidAngle() {
		//solidAngle = 2*Math.PI*(1-Math.sqrt(distance*distance-collimatorDiametor*collimatorDiametor/4)/distance);
		solidAngle = (Math.PI*(collimatorDiametor*0.5)*(collimatorDiametor*0.5))/(distance*distance);
	}
	public String header() {
		return "# ESM Information -----------------------------------#\n" +
		String.format("# Number             : %-29d #\n",number) +
		String.format("# Length             : %-29s #\n",String.format("%s mm",length*1e+3)) +
		String.format("# Height             : %-29s #\n",String.format("%s mm",height*1e+3)) +
		String.format("# Magnet Strength    : %-29s #\n",String.format("%s T",magnetStrength)) +
		String.format("# Distance from TCC  : %-29s #\n",String.format("%s mm", distance*1e+3)) +
		String.format("# Colimator Diametor : %-29s #\n",String.format("%s mm", collimatorDiametor*1e+3)) +
		String.format("# Filter Thickness   : %-29s #\n",String.format("%s um", collimatorDiametor*1e+6)) +
		String.format("# Solid Angle        : %-29s #\n",String.format("%s sr", solidAngle)) +
		String.format("# BG Band Half Width : %-29s #\n",String.format("%s mm", bgBandHalfWidth*1e+3)) +
		"#----------------------------------------------------#\n\n";
	}
}
