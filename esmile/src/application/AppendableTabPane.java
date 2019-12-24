package application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class AppendableTabPane extends TabPane{
	Tab lasttab;
	Data data;
	XYSeriesCollection collection;
	XYLineAndShapeRenderer renderer;
	public AppendableTabPane() {
		lasttab = new Tab();
		lasttab.setClosable(false);
		Label label = new Label(	"+");
		lasttab.setGraphic(label);
		lasttab.setClosable(false);
		getTabs().add(lasttab);
		lasttab.setOnSelectionChanged(new EventHandler<Event>() {
			@Override
			public void handle(Event e){
				if(lasttab.isSelected()) {
					addTab();
				}
			}
		});
		addTab();
	}
	public void addTab() {
		String name = "T" + Integer.toString(getTabs().size());
		Tab tab = new Tab(name);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("SlopeTempTab.fxml"));
		try {
			tab.setContent(loader.load());
			SlopeTempTabController controller = (SlopeTempTabController)loader.getController();
			controller.name = name;
			controller.index = getTabs().size();
			controller.data = data;
			controller.collection = collection;
			controller.renderer = renderer;
			tab.setUserData(controller);
			tab.setOnClosed(new EventHandler<Event>() {
				@Override
				public void handle(Event e){
					controller.clear();
				}
			});
			getTabs().add(getTabs().size()-1,tab);
			getSelectionModel().select(tab);
			Platform.runLater(() -> controller.textfield_min.requestFocus());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void set(Data data, XYSeriesCollection collection, XYLineAndShapeRenderer renderer) {
		this.data = data;
		this.collection = collection;
		this.renderer = renderer;
		for(Tab tab : getTabs()) {
			if(tab != lasttab) {
				SlopeTempTabController controller = (SlopeTempTabController)tab.getUserData();
				controller.clear();
				controller.data = data;
				controller.collection = collection;
				controller.renderer = renderer;
			}
		}
	}
	public String getSlopeTempInfo() {
		List<String> list = new ArrayList<String>(getTabs().size());
		for(Tab tab : getTabs()) {
			if(tab != lasttab) {
				SlopeTempTabController controller = (SlopeTempTabController)tab.getUserData();
				if(controller.slope != null) {
						list.add(
								String.format(
						    "# %s ------------------------------------------------#\n",
						    		controller.name)	
							+	String.format("# Slope Temperature  : %-29s #\n",
										String.format("%g MeV",controller.temp)
									)
						    +
								String.format("# Range Min          : %-29s #\n",
										String.format("%g MeV",controller.min)
									)
							+   String.format("# Range Max          : %-29s #\n",
										String.format("%g MeV",controller.max)
									) 
							+
							"#----------------------------------------------------#\n"
						);
				}
			}
		}
		return String.join("\n", list);
	}
	public Collection<SlopeTempTabController> getControllers(){
		Stack<SlopeTempTabController> stack = new Stack<SlopeTempTabController>();
		for(Tab tab : getTabs()) {
			if(tab != lasttab) {
				SlopeTempTabController controller = (SlopeTempTabController)tab.getUserData();
				if(controller.slope != null) {
					stack.push(controller);
				}
			}
		}
		return stack;
	}
}
