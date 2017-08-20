package application;

import java.net.URL;
import java.util.ResourceBundle;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SlopeTempTabController implements Initializable{
	@FXML Label label_slopeTemp;
	@FXML TextField textfield_min;
	@FXML TextField textfield_max;
	@FXML Button button_calculate;
	double min;
	double max;
	double temp;
	int index;
	String name;
	Data data;
	XYLineAndShapeRenderer renderer;
	XYSeriesCollection collection;
	XYSeries slope;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		button_calculate.defaultButtonProperty().bind(button_calculate.focusedProperty());
	}
	@FXML void calculateSlopeTemp() {
		if(data!=null) {
			try {
				min = Double.parseDouble(textfield_min.getText());
				max = Double.parseDouble(textfield_max.getText());
				Array2D array = new Array2D(data.profile.length,2);
				for(int i=0;i<data.profile.length;i++) {
					array.elem[i][0] = data.profile[i].length;
					array.elem[i][1] = -Math.log(data.profile[i].net);
				}
				/*exp(-E/(e Te))*/
				/*b.elem[0][1] offset*/
				/*b.elem[1][1] slope*/
				Array2D b = array.linefit(min,max);
				//System.out.printf("offset : %g\n", b.elem[0][1]);
				//System.out.printf("slope : %g\n", b.elem[1][1]);
				temp = 1.0/(b.elem[1][1]);
				label_slopeTemp.setText(
						String.format("%.2g MeV", temp)			
				);
				if(slope!=null) {
					collection.removeSeries(slope);
				}
				slope = new XYSeries(String.format("%.2g MeV", temp));
				for(int i=0;i<data.profile.length;i++) {
					if(i%10==0) {
						slope.add(data.profile[i].length,Math.exp(-(b.elem[0][1]+data.profile[i].length*b.elem[1][1])));
					}
				}
				collection.addSeries(slope);
				//renderer.setSeriesPaint(collection.indexOf(slope),new Color(50*(index-1),50*(index-1),50*(index-1)));
			}catch(NullPointerException | NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}
	void clear() {
		label_slopeTemp.setText(null);
		textfield_min.setText(null);
		textfield_max.setText(null);
		if(collection!=null && slope!=null) {
			collection.removeSeries(slope);
		}
	}
	@FXML
	void selectNextField() {
		textfield_max.requestFocus();
	}
	@FXML
	void selectButton() {
		button_calculate.requestFocus();
	}
}
