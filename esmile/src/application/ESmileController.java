package application;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.ColorAdjust;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ESmileController implements Initializable{
	@FXML private BorderPane rootPane;
	@FXML private VBox imagebox;
	@FXML private VBox infobox;
	@FXML private BorderPane pane_colorAdjust;
	@FXML private BorderPane pane_fileInformation;
	@FXML private TabPane tabPane;
	@FXML private StackPane stackPane;
	@FXML private Pane drawPane;
	@FXML private ImageView imageView;
	@FXML private Slider slider_contrast;
	@FXML private Slider slider_brightness;
	@FXML private Slider slider_min;
	@FXML private Slider slider_max;
	@FXML private Label label_fileName;
	@FXML private Label label_coordinate;
	@FXML private Label label_dragAndDrop;
	@FXML private TextField text_info_FadingTime;
	@FXML private TextField text_info_Latitude;
	@FXML private TextField text_info_Voltage;
	@FXML private TextField text_info_Resolution;
	@FXML private Label label_info_DateTime;
	@FXML private BorderPane chartPane;
	@FXML private Label label_chart_esm;
	@FXML private TextField text_esm_Length;
	@FXML private TextField text_esm_MagnetStrength;
	@FXML private TextField text_esm_Height;
	@FXML private TextField text_esm_Distance;
	@FXML private TextField text_esm_CollimatorDiametor;
	@FXML private TextField text_esm_FilterThickness;
	@FXML private TextField text_esm_SolidAngle;
	@FXML private TextField text_esm_BGBandHalfWidth;
	@FXML private TextField text_xmin;
	@FXML private TextField text_xmax;
	@FXML private TextField text_ymin;
	@FXML private TextField text_ymax;
	@FXML private ToggleGroup esmbuttons;
	@FXML private ToggleButton toggle_esm1;
	@FXML private ToggleButton toggle_esm2;
	@FXML private ToggleButton toggle_esm3;
	@FXML private CheckBox check_signal;
	@FXML private CheckBox check_net;
	@FXML private CheckBox check_background;
	@FXML private BorderPane borderPane_slopeTemp;
	
	private int WIDTH=800;
	private int HEIGHT=550;
	private Data data;
	private Bands bands = new Bands();
	private ColorAdjust colorAdjust = new ColorAdjust();
	XYSeries signal = new XYSeries("signal");
	XYSeries net = new XYSeries("(signal - bg)");
	XYSeries background = new XYSeries("bg");
	private final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	private final XYSeriesCollection collection = new XYSeriesCollection();
	private final JFreeChart chart = ChartFactory.createXYLineChart("", "Energy [MeV]", "PSL", 
			collection,PlotOrientation.VERTICAL,true,true,false);
	private final ChartViewer chartViewer = new ChartViewer(chart);
	private ESM[] esmlist = ESM.list();
	private ESM esm;
	private double xmin=0,xmax=100,ymin=1e+8,ymax=1e+13;
	File esmdir;
	private AppendableTabPane tabPane_slopeTemp = new AppendableTabPane();
	
	private void setESM(Toggle t) {
		ESM e;
		if(t == toggle_esm1) {
	 		e = esmlist[0];
	 	}else if(t == toggle_esm2) {
	 		e = esmlist[1];
	 	}else if(t == toggle_esm3) {
	 		e = esmlist[2];
	 	}else {return;}
		
		text_esm_Length.setText(String.format("%s", e.length));
		text_esm_Height.setText(String.format("%s", e.height));
		text_esm_Distance.setText(String.format("%s", e.distance));
		text_esm_MagnetStrength.setText(String.format("%s", e.magnetStrength));
		text_esm_CollimatorDiametor.setText(String.format("%s", e.collimatorDiametor));
		text_esm_FilterThickness.setText(String.format("%s", e.filterThickness));
		text_esm_SolidAngle.setText(String.format("%.3e", e.solidAngle));
		text_esm_BGBandHalfWidth.setText(String.format("%s", e.bgBandHalfWidth));
		esm = e;
	}
	private void setESM(String filename) {
		if(filename.contains("ESM1")) {
			esmbuttons.selectToggle(toggle_esm1);
			setESM(toggle_esm1);
		}else if(filename.contains("ESM2")) {
			esmbuttons.selectToggle(toggle_esm2);
			setESM(toggle_esm2);
		}else if(filename.contains("ESM3")) {
			esmbuttons.selectToggle(toggle_esm3);
			setESM(toggle_esm3);
		}
	}
	private void setInfoLabels() {
		text_info_FadingTime.setText(Integer.toString(data.info.fadingTime));
		text_info_Latitude.setText(Double.toString(data.info.latitude));
		text_info_Voltage.setText(Double.toString(data.info.voltage));
		text_info_Resolution.setText(Double.toString(data.info.resolution*1e+6));
		label_info_DateTime.setText(data.info.dateTime);
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		chartPane.setCenter(chartViewer);
		LogAxis yAxis = new LogAxis("d²N/(dE dΩ) [/(MeV Sr)]");
	    yAxis.setBase(10);
	    yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	    renderer.setBaseShapesVisible(false);
	    chart.getXYPlot().setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
	    chart.getXYPlot().setRenderer(renderer);
		chart.getXYPlot().setRangeAxis(0,yAxis);
		chart.getXYPlot().getDomainAxis(0).setRange(xmin,xmax);
		chart.getXYPlot().getRangeAxis(0).setRange(ymin,ymax);
		Font font = new Font("Helvetica",Font.BOLD,24);
		chart.getXYPlot().getDomainAxis(0).setLabelFont(font);
		chart.getXYPlot().getRangeAxis(0).setLabelFont(font);
		font = new Font("Helvetica",Font.PLAIN,15);
		chart.getXYPlot().getDomainAxis(0).setTickLabelFont(font);
		chart.getXYPlot().getRangeAxis(0).setTickLabelFont(font);
		text_ymin.setText(Double.toString(ymin));
		text_ymax.setText(Double.toString(ymax));
		text_xmin.setText(Double.toString(xmin));
		text_xmax.setText(Double.toString(xmax));
		chart.getXYPlot().getDomainAxis(0).addChangeListener(new AxisChangeListener() {
			@Override
			public void axisChanged(AxisChangeEvent event) {
				xmin = chart.getXYPlot().getDomainAxis(0).getLowerBound();
				xmax = chart.getXYPlot().getDomainAxis(0).getUpperBound();
				text_xmin.setText(String.format("%.2e",xmin));
				text_xmax.setText(String.format("%.2e",xmax));
			}
		});
		chart.getXYPlot().getRangeAxis(0).addChangeListener(new AxisChangeListener() {
			@Override
			public void axisChanged(AxisChangeEvent event) {
				ymin = chart.getXYPlot().getRangeAxis(0).getLowerBound();
				ymax = chart.getXYPlot().getRangeAxis(0).getUpperBound();
				text_ymin.setText(String.format("%.2e",ymin));
				text_ymax.setText(String.format("%.2e",ymax));
			}
		});
        drawPane.getChildren().addAll(bands.list);
        setESM(esmbuttons.getSelectedToggle());
        imageView.setVisible(false);
        infobox.setVisible(false);
        borderPane_slopeTemp.setCenter(tabPane_slopeTemp);
		this.slider_contrast.valueProperty().addListener(new ChangeListener<Number>() {
			@Override	
			public void changed(ObservableValue<? extends Number> obs, Number oldValue, Number newValue) {
				colorAdjust.setContrast(newValue.doubleValue());
				imageView.setEffect(colorAdjust);
			}
		});
		this.slider_brightness.valueProperty().addListener(new ChangeListener<Number>() {
			@Override	
			public void changed(ObservableValue<? extends Number> obs, Number oldValue, Number newValue) {
				colorAdjust.setBrightness(newValue.doubleValue());
				imageView.setEffect(colorAdjust);
			}
		});
		this.esmbuttons.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
			@Override
		    public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
		         if (esmbuttons.getSelectedToggle() != null) {
		        	 setESM(new_toggle);
		         }
		     } 
		});
		}

	private void loadData(File file) {
		data = new Data(file);
		data.setMinMax(slider_min.getValue(),slider_max.getValue());
        colorAdjust.setContrast(slider_contrast.getValue());
        colorAdjust.setBrightness(slider_brightness.getValue());
        imageView.setEffect(colorAdjust);
		setInfoLabels();	
		setESM(file.getName());
		HEIGHT = WIDTH * data.height / data.width;
		//System.out.println((esm.bgBandHalfWidth/data.info.resolution)*HEIGHT/data.height);
		bands.setBands(data,esm,HEIGHT);
		label_fileName.setText(String.format("%s %gx%gcm (%d,%d)",file.getName(),
				data.width*data.info.resolution*1e+2,
				data.height*data.info.resolution*1e+2,
				data.width,data.height
				)
		);
		imageView.setImage(data.image);
		imageView.setFitWidth(WIDTH);
		imageView.setFitHeight(HEIGHT);
		imageView.setVisible(true);
		infobox.setVisible(true);
		drawPane.setPrefWidth(WIDTH);
		drawPane.setPrefHeight(HEIGHT);
        collection.removeAllSeries();
	}
	@FXML
	private void unload(){
		imageView.setImage(null);
		label_fileName.setText(null);
		bands.setVisible(false);
		data = null;
		imageView.setVisible(false);
        infobox.setVisible(false);
        collection.removeAllSeries();
	}
	@FXML
	private void showFileChooser(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		 fileChooser.setTitle("ファイルを開く");
		 fileChooser.getExtensionFilters().addAll(
		         new FileChooser.ExtensionFilter("ESMデータファイル", "*.gel"),
		         new FileChooser.ExtensionFilter("全てのファイル", "*.*"));
		 File file = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
		 if (file != null) {loadData(file);}
	}
	
	@FXML
	private void fileout(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		 fileChooser.setTitle("ファイル出力");
		 fileChooser.getExtensionFilters().addAll(
		      new FileChooser.ExtensionFilter(".txtファイル", "*.txt")
		 );
		 fileChooser.setInitialFileName(data.info.fileName.substring(0, data.info.fileName.lastIndexOf('.')) + ".txt");
		 fileChooser.setInitialDirectory(new File(data.info.dirName));
		 File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
		 if(file!=null) {
			 data.printProfile(file,esm,tabPane_slopeTemp.getSlopeTempInfo());
		 }
	}
	
	@FXML
	private void showCoordinate(MouseEvent event) {
		double x,y;
		int ix,iy;
		x = data.width*event.getX()/WIDTH;
		y = data.height*event.getY()/HEIGHT;
		ix = (int)x;
		iy = (int)y;
		if(ix >= data.width) {ix=data.width-1;}
		if(ix < 0) {ix=0;}
		if(iy >= data.height) {iy=data.height-1;}
		if(iy < 0) {iy=0;}
		label_coordinate.setText( 
				String.format("cm : (%.1f,%.1f) pixel : (%d,%d)  psl: %.3f",
						x*data.info.resolution*1e+2,y*data.info.resolution*1e+2,
						ix,iy,
						data.psldata[iy][ix]
					)
				);
	}
	@FXML
	private void clearCoordinate(MouseEvent event) {
		label_coordinate.setText(null);
	}
	@FXML
	private void dragDropped(final DragEvent e) {
		final Dragboard db = e.getDragboard();
		boolean success = false;
		if(db.hasFiles()) {
			final File file = db.getFiles().get(0);
			success = true;
			Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (file != null) {loadData(file);}
					}
				});
			e.setDropCompleted(success);
			e.consume();
		}
	}
	@FXML
	private void dragOver(final DragEvent e) {
		final Dragboard db = e.getDragboard();
		final String filename = db.getFiles().get(0).getName().toLowerCase();
		final boolean isAccepted = filename.endsWith(".gel");
		if(db.hasFiles()) {
			if(isAccepted) {
				stackPane.setStyle("-fx-border-color: cyan;"
			              + "-fx-border-width: 5;"
			              + "-fx-background-color: #C6C6C6;"
			              + "-fx-border-style: solid;");
				label_dragAndDrop.setText(db.getFiles().get(0).getName());
				e.acceptTransferModes(TransferMode.COPY);
			}else {
				stackPane.setStyle("-fx-border-color: red;"
			              + "-fx-border-width: 5;"
			              + "-fx-background-color: #C6C6C6;"
			              + "-fx-border-style: solid;");
				label_dragAndDrop.setText("拡張子が.gelではありません");
			}
		}else {
			e.consume();
		}
	}
	@FXML
	private void closeFile(ActionEvent event) {
		imageView.setImage(null);
		imageView.setVisible(false);
	}
	
	@FXML
	private void dragExisted(final DragEvent event) {
		stackPane.setStyle("-fx-border-color: #C6C6C6;");
		label_dragAndDrop.setText("ここに.gelファイルをドロップしてください");
	}
	@FXML
	private void drawLine_start(MouseEvent event) {
		bands.setStartX(event.getX());
		bands.setStartY(event.getY());
		bands.setVisible(false);
		event.consume();
	}
	@FXML
	private void drawLine(MouseEvent event) {
		bands.setEndX(event.getX());
		bands.setEndY(event.getY());
		bands.draw();
		event.consume();
	}
	@FXML
	private void drawLine_end(MouseEvent event) {
		bands.setEndX(event.getX());
		bands.setEndY(event.getY());
		bands.draw();
		//long startTime = System.nanoTime();
		data.lineProfile(
				bands.start[0]*data.width/WIDTH,
				bands.start[1]*data.width/WIDTH,
				bands.end[0]*data.height/HEIGHT,
				bands.end[1]*data.height/HEIGHT,
				esm
		);
		if(
				bands.getLength() < WIDTH*0.1
		) {
			bands.setVisible(false);
			event.consume();
			return;
		}
		//long endTime = System.nanoTime();
		//System.out.println("LOAD : "+(endTime - startTime) + " ns"); 
		label_chart_esm.setText(String.format("ESM %d",esm.number));
		tabPane.getSelectionModel().selectNext();
		//startTime = System.nanoTime();
		signal.clear();
		net.clear();
		background.clear();
		collection.removeAllSeries();
		for(int i=0;i<data.profile.length;i++) {
			if(data.profile[i].invalid) {continue;}
			signal.add(data.profile[i].length,data.profile[i].signal);
			net.add(data.profile[i].length,data.profile[i].net);
			background.add(data.profile[i].length,data.profile[i].background);
		}
		collection.addSeries(signal);
		collection.addSeries(background);
		collection.addSeries(net);
		renderer.setSeriesPaint(collection.indexOf(signal),Color.red);
		renderer.setSeriesPaint(collection.indexOf(background),Color.blue);
		renderer.setSeriesPaint(collection.indexOf(net),Color.black);
		renderer.setSeriesVisible(collection.indexOf(signal),check_signal.isSelected());
		renderer.setSeriesVisible(collection.indexOf(background),check_background.isSelected());
		renderer.setSeriesVisible(collection.indexOf(net),check_net.isSelected());
        tabPane_slopeTemp.set(data,collection,renderer);
		//endTime = System.nanoTime();
		//System.out.println("DISPLAY : "+(endTime - startTime) + " ns"); 
		event.consume();
	}
	@FXML
	private void checkBoxChanged(ActionEvent event) {
		renderer.setSeriesVisible(collection.indexOf(signal),check_signal.isSelected());
		renderer.setSeriesVisible(collection.indexOf(background),check_background.isSelected());
		renderer.setSeriesVisible(collection.indexOf(net),check_net.isSelected());
	}
	
	@FXML
	private void rescale(MouseEvent event) {
		data.setMinMax(slider_min.getValue(),slider_max.getValue());
		event.consume();
	}
	@FXML private void esm_setLength(ActionEvent event) {
		esm.length = Double.parseDouble(text_esm_Length.getText());
		bands.setBands(data,esm,HEIGHT);
	}
	@FXML private void esm_setHeight(ActionEvent event) {esm.height = Double.parseDouble(text_esm_Height.getText());}
	@FXML private void esm_setDistance(ActionEvent event) {esm.distance = Double.parseDouble(text_esm_Distance.getText());}
	@FXML private void esm_setMagnetStrength(ActionEvent event) {esm.magnetStrength = Double.parseDouble(text_esm_MagnetStrength.getText());}
	@FXML private void esm_setFilterThickness(ActionEvent event) {esm.filterThickness = Double.parseDouble(text_esm_FilterThickness.getText());}
	@FXML private void esm_setCollimatorDiametor(ActionEvent event) {
		esm.collimatorDiametor = Double.parseDouble(text_esm_CollimatorDiametor.getText());
		bands.setBands(data,esm,HEIGHT);
	}
	@FXML private void esm_setSolidAngle(ActionEvent event) {esm.solidAngle = Double.parseDouble(text_esm_SolidAngle.getText());}
	@FXML private void esm_setBGBandHalfWidth(ActionEvent event) {
		esm.bgBandHalfWidth = Double.parseDouble(text_esm_BGBandHalfWidth.getText());
		bands.setBands(data,esm,HEIGHT);
	}
	@FXML private void info_setFadingTime(ActionEvent event){data.info.fadingTime = Integer.parseInt(text_info_FadingTime.getText());}
	@FXML private void info_setLatitude(ActionEvent event){data.info.latitude = Double.parseDouble(text_info_Latitude.getText());}
	@FXML private void info_setVoltage(ActionEvent event){data.info.voltage = Double.parseDouble(text_info_Voltage.getText());}
	@FXML private void info_setResolution(ActionEvent event){data.info.resolution = Double.parseDouble(text_info_Resolution.getText());}

	@FXML private void setAxisRange(ActionEvent event) {
		xmin = Double.parseDouble(text_xmin.getText()); 
		xmax = Double.parseDouble(text_xmax.getText());
		ymin = Double.parseDouble(text_ymin.getText());
		ymax = Double.parseDouble(text_ymax.getText());
		chart.getXYPlot().getDomainAxis(0).setRange(xmin,xmax);
		chart.getXYPlot().getRangeAxis(0).setRange(ymin,ymax);
	}
	
	@FXML private void showHelp() {
		Stage stage = new Stage();
		Pane pane = new Pane();
		ImageView helpview = 
				new ImageView(new Image(ESmile.class.getClassLoader().getResourceAsStream("esmile_help.png")));
		helpview.setFitHeight(485);
		helpview.setFitWidth(755);
		pane.getChildren().add(helpview);
		stage.setScene(new Scene(pane));
		stage.setTitle("Help");
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(rootPane.getScene().getWindow());
		stage.showAndWait();
	}
	/*
	private void calculateSlopeTemp() {
		double min,max;
		Array2D array = new Array2D(data.profile.length,2);
		for(int i=0;i<data.profile.length;i++) {
			array.elem[i][0] = data.profile[i].length;
			array.elem[i][1] = -Math.log(data.profile[i].net);
		}
		try {
			min = Double.parseDouble(text_min1.getText());
			max = Double.parseDouble(text_max1.getText());
		}catch(NullPointerException|IllegalArgumentException e){
			
		}
	}
	*/
	@FXML private void quit() {Platform.exit();}
}
