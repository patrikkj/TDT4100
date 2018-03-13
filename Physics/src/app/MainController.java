package app;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;

import enums.Inertia;
import enums.Integration;
import enums.Interpolation;
import enums.Style;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

public class MainController {
	
	/////////////////////////
	/////  FXML FIELDS  /////
	/////////////////////////
	
	// ROOT 
    @FXML private VBox rootNode;

    // CHART
    @FXML private StackPane chartPane;
    private LineChart<Number, Number> lineChart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    // Chart properties
    @FXML private JFXTextField chartTitle;
    @FXML private JFXTextField chartWidth;
    @FXML private JFXTextField chartHeight;
    // X-Axis properties
    @FXML private JFXTextField xAxisName;
    @FXML private JFXTextField xAxisTickSize;
    @FXML private JFXTextField xAxisMinRange;
    @FXML private JFXTextField xAxisMaxRange;
    @FXML private JFXToggleButton xAxisAutoRange;
    // Y-Axis properties
    @FXML private JFXTextField yAxisName;
    @FXML private JFXTextField yAxisTickSize;
    @FXML private JFXTextField yAxisMinRange;
    @FXML private JFXTextField yAxisMaxRange;
    @FXML private JFXToggleButton yAxisAutoRange;
    
    // TRACES
    @FXML private JFXListView<Trace> traceListView;
    @FXML private JFXTabPane traceTabPane;
    // Trace properties
    @FXML private JFXTextField traceName;
    @FXML private JFXComboBox<File> traceFile;
    @FXML private JFXComboBox<Integration> traceIntegration;
    @FXML private JFXComboBox<Interpolation> traceInterpolation;
    @FXML private JFXComboBox<Inertia> traceInertia;
    @FXML private JFXTextField traceMass;
    @FXML private JFXTextField traceMinX;
    @FXML private JFXTextField traceMaxX;
    @FXML private JFXTextField traceInitV;
    @FXML private JFXTextField traceStep;
    // Trace details
    @FXML private Label funcTypeLabel;
    @FXML private Label integrationTypeLabel;
    @FXML private Label stepSizeLabel;
    @FXML private Label iterationsLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Label computationTimeLabel;
    @FXML private Label energyDifferenceLabel;

    // GRAPHS
    @FXML private JFXListView<Graph> graphListView;
    // Graph properties
    @FXML private JFXTextField graphName;
    @FXML private JFXComboBox<String> graphXData;
    @FXML private JFXComboBox<String> graphYData;
    @FXML private JFXComboBox<Trace> graphTrace;
    @FXML private JFXTextField graphMinX;
    @FXML private JFXTextField graphMaxX;
    // Graph layout
    @FXML private JFXColorPicker graphColor;
    @FXML private JFXSlider graphDetail;
    @FXML private JFXComboBox<Style> graphStyle;
    @FXML private JFXSlider graphWidth;
    @FXML private JFXToggleButton graphVisible; 
    
    
    
	/////////////////////////////
	/////  NON-FXML FIELDS  /////
	/////////////////////////////
    
    /**
     * Used in Bidirectional bindings to convert String <-> Double.
     * <dt>String to Double:</dt>
     * <li>Allows both ',' and '.' as decimal separator.
     * <li>Returns null if String is empty ("").
     * <br></br>
     * <dt>Double to String:</dt>
     * <li>Returns null if Double is null.
     */
    private StringConverter<Double> customStringConverter;
    private StringConverter<Number> customStringDoubleConverter;
    
    /*
     * Cached traces and graphs, used to manage property bindings.
     */
    private Trace selectedTrace;
    private Graph selectedGraph;
    private ChangeListener<Trace> traceChangeListener;
    private ChangeListener<Graph> graphChangeListener;
    private ChangeListener<String> traceNameChangeListener;
    private ChangeListener<String> graphNameChangeListener;
    private ChangeListener<Color> graphColorChangeListener;
    private ChangeListener<Object> chartSeriersChangeListener;
    
    /*
     * Observable lists used in ListViews and ChoiceBoxes
     */
    private ObservableList<Trace> traceList;
    private ObservableList<Graph> graphList;
    private ObservableList<File> fileList;
    private ObservableList<String> dataList;
    
    /*
     * Alert list for cells within choiceBox
     */
    private List<ListCell<Trace>> listenerList = new ArrayList<>();
    
    
    
	/////////////////////
	/////  METHODS  /////
	/////////////////////
    
    // Initialization
    /**
     * Initializes application, called after FXML fields has been invoked.
     */
	@FXML private void initialize() {    
    	initializeConverters();
		initializeChangeListeners();
    	initializeLists();
    	initializeTraceView();
    	initializeGraphView();
    	
    }

	/**
     * Initializes and binds ListViews, loads imported files and initializes ChoiceBoxes.
     */
    private void initializeLists() {
    	// Initialize observable lists
    	traceList = FXCollections.observableArrayList();
    	graphList = FXCollections.observableArrayList();
    	fileList = FXCollections.observableArrayList();
    	dataList = FXCollections.observableList(Arrays.asList(Trace.MAP_KEYS));
    	
    	// Import tracker files from 'import'
    	File folder = new File(getClass().getResource("../imports").getPath());
    	importFolder(folder);
    	
    	// Bind observable lists
    	traceListView.setItems(traceList);
    	graphListView.setItems(graphList);
    	traceFile.setItems(fileList);
    	
    	// Apply change listeners to lists
		traceListView.getSelectionModel().selectedItemProperty().addListener(traceChangeListener);
		graphListView.getSelectionModel().selectedItemProperty().addListener(graphChangeListener);
    	
    	// Fill trace choiceBoxes
    	traceIntegration.setItems(FXCollections.observableList(Integration.getElements()));
        traceInterpolation.setItems(FXCollections.observableList(Interpolation.getElements()));
        traceInertia.setItems(FXCollections.observableList(Inertia.getElements()));

        // Fill graph choiceBoxes
        graphTrace.setItems(traceList);
        graphXData.setItems(dataList);
        graphYData.setItems(dataList);
        graphStyle.setItems(FXCollections.observableList(Style.getElements()));
    }
    
	/**
	 * Initializes trace view and adds default trace to listView.
	 */
	private void initializeTraceView() {
		// Set default trace
    	traceList.add(new Trace());
    	traceListView.getSelectionModel().selectFirst();
    	
    	// Add name change listener
		traceName.textProperty().addListener(traceNameChangeListener);
		
    	// Updaters
    	updateTraceView();
	}
	
	/**
	 * Initializes chart, sets default chart and graph properties.
	 */
	private void initializeGraphView() {
		initializeChart();
		
		// Set default graph
		addGraph(new Graph(selectedTrace));
		graphListView.getSelectionModel().selectFirst();
		
		// Set graph trace cell factory
		graphTrace.setCellFactory(new Callback<ListView<Trace>, ListCell<Trace>>(){
			@Override
            public ListCell<Trace> call(ListView<Trace> arg) {
                ListCell<Trace> cell = new JFXListCell<Trace>(); 
                listenerList.add(cell);
                return cell;
            }
		});
		
		// Set graph style cell factory
		graphStyle.setCellFactory(cell -> new StyleCell(false));
		graphStyle.setButtonCell(new StyleCell(true));
		
		// Add name listener
		lineChart.dataProperty().addListener(chartSeriersChangeListener);
		graphName.textProperty().addListener(graphNameChangeListener);
		
		// Updaters
		updateGraphView();
	}
	
	/**
	 * Initializes chart, set default values and binds chart properties.
	 */
	private void initializeChart() {
		// Initialize chart
		xAxis = new NumberAxis();
		yAxis = new NumberAxis();
		lineChart = new LineChart<>(xAxis, yAxis);

		// Bind chart properties
		chartTitle.textProperty().bindBidirectional(lineChart.titleProperty());
		chartWidth.textProperty().bindBidirectional(lineChart.prefWidthProperty(), customStringDoubleConverter);
		chartHeight.textProperty().bindBidirectional(lineChart.prefHeightProperty(), customStringDoubleConverter);
		
		// Bind x-axis properties
		xAxisName.textProperty().bindBidirectional(xAxis.labelProperty());
		xAxisTickSize.textProperty().bindBidirectional(xAxis.tickUnitProperty(), customStringDoubleConverter);
		xAxisMinRange.textProperty().bindBidirectional(xAxis.lowerBoundProperty(), customStringDoubleConverter);
		xAxisMaxRange.textProperty().bindBidirectional(xAxis.upperBoundProperty(), customStringDoubleConverter);
		xAxisTickSize.disableProperty().bindBidirectional(xAxisAutoRange.selectedProperty());
		xAxisMinRange.disableProperty().bindBidirectional(xAxisAutoRange.selectedProperty());
		xAxisMaxRange.disableProperty().bindBidirectional(xAxisAutoRange.selectedProperty());
		xAxisAutoRange.selectedProperty().bindBidirectional(xAxis.autoRangingProperty());
		
		// Bind y-axis properties
		yAxisName.textProperty().bindBidirectional(yAxis.labelProperty());
		yAxisTickSize.textProperty().bindBidirectional(yAxis.tickUnitProperty(), customStringDoubleConverter);
		yAxisMinRange.textProperty().bindBidirectional(yAxis.lowerBoundProperty(), customStringDoubleConverter);
		yAxisMaxRange.textProperty().bindBidirectional(yAxis.upperBoundProperty(), customStringDoubleConverter);
		yAxisTickSize.disableProperty().bindBidirectional(yAxisAutoRange.selectedProperty());
		yAxisMinRange.disableProperty().bindBidirectional(yAxisAutoRange.selectedProperty());
		yAxisMaxRange.disableProperty().bindBidirectional(yAxisAutoRange.selectedProperty());
		yAxisAutoRange.selectedProperty().bindBidirectional(yAxis.autoRangingProperty());
		
		// Set chart properties
		xAxis.setLabel("xAxis");
		yAxis.setLabel("yAxis");
		lineChart.setTitle("My Chart");
		lineChart.setCreateSymbols(false);
		lineChart.setAnimated(false);
		lineChart.minWidthProperty().bind(lineChart.prefWidthProperty());
		lineChart.maxWidthProperty().bind(lineChart.prefWidthProperty());
		lineChart.minHeightProperty().bind(lineChart.prefHeightProperty());
		lineChart.maxHeightProperty().bind(lineChart.prefHeightProperty());
		
		xAxis.setAutoRanging(true);
		xAxis.setAnimated(false);
		yAxis.setAutoRanging(true);
		yAxis.setAnimated(false);
		
		// Add chart to GUI
		chartPane.getChildren().setAll(lineChart);
	}
	
	/**
	 * Initializes change listeners.
	 */
	private void initializeChangeListeners() {
		traceChangeListener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Trace> traceProperty, Trace oldTrace, Trace newTrace) {
				// Unbind previous trace
				if (oldTrace != null)
					unbindTrace(oldTrace);
					
				// Bind new trace
				if (newTrace != null) 
					bindTrace(newTrace);
				
				// Set seleced trace
				selectedTrace = newTrace;
			}
		};
		
		graphChangeListener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Graph> graphProperty, Graph oldGraph, Graph newGraph) {
				// Unbind previous graph
				if (oldGraph != null)
					unbindGraph(oldGraph);
				
				// Bind new graph or clear UI if there is no selected graph
				if (newGraph != null)
					bindGraph(newGraph);
				else 
					clearGraphView();
				
				// Set selected graph
				selectedGraph = newGraph;
			}
		};
		
		traceNameChangeListener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends String> traceNameProperty, String oldName, String newName) {
				// Update ListView entries
				traceListView.refresh();
				
				// Update choicebox entries
				listenerList.stream()
					.filter(cell -> cell != null)
					.filter(cell -> cell.getItem() != null)
					.forEach(cell -> cell.setText(cell.getItem().getName()));
				graphTrace.getButtonCell().setText(selectedGraph.getTrace().getName());
			}
		};

		chartSeriersChangeListener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Object> arg0, Object arg1, Object arg2) {
				updateGraphView();
				System.out.println("Dataset changed");
			}
		};
		
		graphNameChangeListener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends String> graphNameProperty, String oldName, String newName) {
				graphListView.refresh();
				updateChartStyles();
			}
		};
	
		graphColorChangeListener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Color> colorProperty, Color oldColor, Color newColor) {
				updateChartStyles();
			}
		};
	}
	
	/**
	 * Initializes converters.
	 */
	private void initializeConverters() {
		// Initialize converter (used for trace bindings)
    	customStringConverter = new StringConverter<>() {
			@Override
			public Double fromString(String arg0) {
				return (arg0.equals("")) ? null : Double.valueOf(arg0.replace(',', '.'));}
			
			@Override
			public String toString(Double arg0) {
				return (arg0 == null) ? null : String.valueOf(arg0);}
		};
		
		customStringDoubleConverter = new NumberStringConverter() {
			@Override
			public Number fromString(String arg0) {
				return Double.valueOf(arg0.replace(',', '.'));
			}
			
			@Override
			public String toString(Number arg0) {
				return String.valueOf(arg0);
			}
		};

	}
	
    /**
     * Runs any process that requires the scene to be loaded.
     */
    protected void postInitialize() {
    	graphStyle.lookup(".list-view").setStyle("-fx-pref-width: 120;");

    }
    
    
    // GUI Update
    /**
     * Manages trace bindings and updates trace details.
     * Called when selected trace has been changed.
     */
	private void updateTraceView() {
		// Now replaced by traceChangeListener
    }
    
	/**
     * Updates graph list ordering, chart and chart layout.
     * Called upon when there is a change to graph order.
     */
	private void updateGraphView() {
    	// Update chart
    	lineChart.getData().setAll(graphList.stream().map(graph -> graph.getSeries()).collect(Collectors.toList()));
    	
    	// Update graphs
    	graphList.forEach(graph -> graph.updateGraph());
    	
    	// Update chart layout
    	updateChartStyles();
    }
	
	/**
	 * Clears  chart and graph view if there are no graphs to display.
	 */
	private void clearGraphView() {
		// Clear graph properties
		graphName.setText(null);
		graphXData.setValue(null);
		graphYData.setValue(null);
		graphTrace.setValue(null);
		graphMinX.setText(null);
		graphMaxX.setText(null);
		
		// Clear graph layout properties
		graphStyle.setValue(null);
		graphColor.setValue(Color.valueOf("#FFFFFF"));
		graphDetail.setValue(50);
		graphWidth.setValue(50);
		graphVisible.setSelected(false);
		
		
	}

	/**
	 * Update chart layout in order to maintain label colors.
	 * Iterates through every label to reapply styling.
	 * This method must be called whenever there is a change
	 * to the chart data structure, as the chart automatically 
	 * refactors all node styling when any data is updated.
	 */
	private void updateChartStyles() {
		for (Node node : lineChart.lookupAll(".chart-legend-item")) {
			Labeled labeledNode = (Labeled) node;
			Node graphicNode = labeledNode.getGraphic();
			String nodeSeries = graphicNode.getStyleClass().get(2);
			int seriesIndex = Character.getNumericValue(nodeSeries.charAt(nodeSeries.length() - 1));
			
			String graphStyle =  String.format("-fx-background-color: #%s, #FFFFFF; -fx-background-insets: 0, 2;", graphList.get(seriesIndex).getHexColor());
			
			graphicNode.setStyle(graphStyle);
		}
	}
    
	
	// Bindings
	/**
	 * Bind TraceView inputs to selected trace.
	 */
	private void bindTrace(Trace trace) {
		// Bind trace properties
		traceName					.textProperty().bindBidirectional(trace.getNameProperty());
	    traceFile					.valueProperty().bindBidirectional(trace.getFileProperty());
	    traceIntegration			.valueProperty().bindBidirectional(trace.getIntegrationProperty());
	    traceInterpolation			.valueProperty().bindBidirectional(trace.getInterpolationProperty());
	    traceInertia				.valueProperty().bindBidirectional(trace.getInertiaProperty());
	    traceMass					.textProperty().bindBidirectional(trace.getMassProperty(), customStringConverter);
	    traceMinX					.textProperty().bindBidirectional(trace.getMinXProperty(), customStringConverter);
	    traceMaxX					.textProperty().bindBidirectional(trace.getMaxXProperty(), customStringConverter);
	    traceInitV					.textProperty().bindBidirectional(trace.getInitVProperty(), customStringConverter);
	    traceStep					.textProperty().bindBidirectional(trace.getStepProperty(), customStringConverter);
		
		// Set trace details
	    funcTypeLabel.textProperty().bind(trace.getInterpolationTypeProperty());
	    integrationTypeLabel.textProperty().bind(trace.getIntegrationTypeProperty());
	    stepSizeLabel.textProperty().bind(trace.getStepSizeProperty());
	    iterationsLabel.textProperty().bind(trace.getIterationsProperty());
	    totalTimeLabel.textProperty().bind(trace.getTotalTimeProperty());
	    computationTimeLabel.textProperty().bind(trace.getComputationTimeProperty());
	    energyDifferenceLabel.textProperty().bind(trace.getEnergyDifferenceProperty());
	}
	
	/**
	 * Bind GraphView inputs to selected graph.
	 */
	private void bindGraph(Graph graph) {
		// Bind graph properties
		graphName					.textProperty().bindBidirectional(graph.getNameProperty());
		graphXData					.valueProperty().bindBidirectional(graph.getXDataProperty());
		graphYData					.valueProperty().bindBidirectional(graph.getYDataProperty());
		graphTrace					.valueProperty().bindBidirectional(graph.getTraceProperty());
		graphMinX					.textProperty().bindBidirectional(graph.getMinXProperty(), customStringConverter);
		graphMaxX					.textProperty().bindBidirectional(graph.getMaxXProperty(), customStringConverter);
		
		// Bind graph layout properties
		graphStyle					.valueProperty().bindBidirectional(graph.getStyleProperty());
		graphColor					.valueProperty().bindBidirectional(graph.getColorProperty());
		graphDetail					.valueProperty().bindBidirectional(graph.getDetailProperty());
		graphWidth					.valueProperty().bindBidirectional(graph.getWidthProperty());
		graphVisible				.selectedProperty().bindBidirectional(graph.getVisibleProperty());
	}
	
	/**
	 * Unbinds TraceView inputs from previously selected trace.
	 */
	private void unbindTrace(Trace trace) {
		// Unbind trace properties
		traceName				.textProperty().unbindBidirectional(trace.getNameProperty());
		traceFile				.valueProperty().unbindBidirectional(trace.getFileProperty());
		traceIntegration		.valueProperty().unbindBidirectional(trace.getIntegrationProperty());
		traceInterpolation		.valueProperty().unbindBidirectional(trace.getInterpolationProperty());
		traceInertia			.valueProperty().unbindBidirectional(trace.getInertiaProperty());
		traceMass				.textProperty().unbindBidirectional(trace.getMassProperty());
		traceMinX				.textProperty().unbindBidirectional(trace.getMinXProperty());
		traceMaxX				.textProperty().unbindBidirectional(trace.getMaxXProperty());
		traceInitV				.textProperty().unbindBidirectional(trace.getInitVProperty());
		traceStep				.textProperty().unbindBidirectional(trace.getStepProperty());
	}
	
	/**
	 * Unbinds GraphView inputs from previously selected graph.
	 */
	private void unbindGraph(Graph graph) {
		// Unbind graph properties
		graphName				.textProperty().unbindBidirectional(graph.getNameProperty());
		graphXData				.valueProperty().unbindBidirectional(graph.getXDataProperty());
		graphYData				.valueProperty().unbindBidirectional(graph.getYDataProperty());
		graphTrace				.valueProperty().unbindBidirectional(graph.getTraceProperty());
		graphMinX				.textProperty().unbindBidirectional(graph.getMinXProperty());
		graphMaxX				.textProperty().unbindBidirectional(graph.getMaxXProperty());
		
		// Unbind graph layout properties
		graphStyle				.valueProperty().unbindBidirectional(graph.getStyleProperty());
		graphColor				.valueProperty().unbindBidirectional(graph.getColorProperty());
		graphDetail				.valueProperty().unbindBidirectional(graph.getDetailProperty());
		graphWidth				.valueProperty().unbindBidirectional(graph.getWidthProperty());
		graphVisible			.selectedProperty().unbindBidirectional(graph.getVisibleProperty());
	}
    
	
	//File IO
	/**
	 * Imports all text files in specified folder.
	 */
	private void importFolder(File folder) {
    	// Import tracker files from 'import'
    	FilenameFilter fileFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".txt");
			}
		};
    	for (File dataFile : folder.listFiles(fileFilter))
    		fileList.add(dataFile);
	}
	
	/**
	 * Imports all text files from list of files.
	 */
	private void importFiles(List<File> files) {
		files.forEach(file -> fileList.add(file));
	}
	
	
	// Helpers
	/**
	 * Adds a new graph to graph list and applies change listeners.
	 */
	private void addGraph(Graph graph) {
		// Add graph to graph list
		graphList.add(graph);
		
		// Apply change listeners
		graph.getColorProperty().addListener(graphColorChangeListener);
	}
	
	/**
	 * Unbinds and removes specified graph.
	 */
	private void deleteGraph(Graph graph) {
		// Remove property bindings
		graph.removeTraceLink();
    	
    	// Remove graph
    	graphList.remove(graph);
	}
	
	
	
	/////////////////////////////
	/////  ACTION HANDLERS  /////
	/////////////////////////////
	
    // Main menu 
    @FXML private void handleNewProjectClick(ActionEvent event) {}
    
    @FXML private void handleSaveClick(ActionEvent event) {}
    
    @FXML private void handleLoadClick(ActionEvent event) {}
    
    @FXML private void handleImportClick(ActionEvent event) {}
    
    @FXML private void handleExportClick(ActionEvent event) throws IOException {
    	// Retrive parent for file chooser
    	Stage mainStage = (Stage) rootNode.getScene().getWindow();
    	
    	// Construct file chooser
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Export chart");
    	fileChooser.getExtensionFilters().addAll(
    	         new ExtensionFilter("Image file (*.png)", "*.png"),
    	         new ExtensionFilter("All Files", "*.*"));
    	
    	// Launch file chooser and retrive selected file
    	File selectedFile = fileChooser.showSaveDialog(mainStage);
    	
    	// Break if no file has been selected
    	if (selectedFile == null) return;
    	
    	// Write chart image to file
    	SnapshotParameters snapshotParameters = new SnapshotParameters();
    	snapshotParameters.setFill(Paint.valueOf("#EEEEEE"));
    	WritableImage image = lineChart.snapshot(snapshotParameters, null);
    	ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", selectedFile);
    }
    
    
    // Trace button handlers
    @FXML private void handleNewTraceClick(ActionEvent event) {
    	// Add ned trace
    	traceList.add(new Trace());
    	
    	// Select new trace
    	traceListView.getSelectionModel().selectLast();
    	
    	// Update
    	updateTraceView();
    }
    
    @FXML private void handleDeleteTraceClick(ActionEvent event) {
    	// If there is no selected trace, break
    	if (selectedTrace == null) return;
    	
    	// Delete all graphs connected to selected trace
    	selectedTrace.linkedGraphs.forEach(graph -> deleteGraph(graph));
    	
    	// Remove trace
    	traceList.remove(selectedTrace);
    	
    	// Update
    	updateTraceView();
    	updateGraphView();
    }
    
    @FXML private void handleComputeClick(ActionEvent event) {
    	// Select trace details view
    	traceTabPane.getSelectionModel().selectLast();
    	
    	// Run trace in parallel
    	selectedTrace.parallelTrace();
    	
    	// Update
    	updateTraceView();
    }
    
    @FXML private void handleComputeAllClick(ActionEvent event) {
    	// Select trace details view
    	traceTabPane.getSelectionModel().selectLast();
    	
    	// Run all traces in parallel
    	traceList.forEach(trace -> trace.parallelTrace());
    	
    	// Update
    	updateTraceView();
    }
    
    @FXML private void handleTraceListClick(Event event) {
    	// Update
    	updateTraceView();
    }

    @FXML private void handleFileOpenClick(ActionEvent event) {
    	// Retrive parent for file chooser
    	Stage mainStage = (Stage) rootNode.getScene().getWindow();
    	
    	// Construct file chooser
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Open Data File");
    	fileChooser.getExtensionFilters().addAll(
    	         new ExtensionFilter("Tracker file (*.txt)", "*.txt"),
    	         new ExtensionFilter("All Files", "*.*"));
    	
    	// Launch file chooser and retrive selected file
    	List<File> selectedFiles = fileChooser.showOpenMultipleDialog(mainStage);
    	
    	// Break if no files was selected
    	if (selectedFiles == null) return;
    	
    	// Import and select file(s)
    	if (selectedFiles.size() == 1) {
    		fileList.add(selectedFiles.get(0));
    		selectedTrace.setFile(selectedFiles.get(0));
    	} else {
    		importFiles(selectedFiles);
    	}
    }
    
    
    // Graph button handlers
    @FXML private void handleNewGraphClick(ActionEvent event) {
    	// Add new graph
    	addGraph(new Graph(selectedTrace));
    	
    	// Select new graph
    	graphListView.getSelectionModel().selectLast();
    	
    	// Update
    	updateGraphView();
    }

    @FXML private void handleDeleteGraphClick(ActionEvent event) {
    	// If there is no selected graph, break
    	if (selectedGraph == null) return;
    	
    	// Delete graph
    	deleteGraph(selectedGraph);
    	
    	// Update
    	updateGraphView();
    }

    @FXML private void handleGraphUpClick(ActionEvent event) {
    	// Assign local variable
    	Graph graph;
    	int graphIndex;
    	
    	// If there is no selected graph, break
    	if (selectedGraph == null) return;
    	
    	// Retrive graph index
    	graphIndex = graphList.indexOf(selectedGraph);
    	
    	// If graph already has highest priority, break
    	if (graphIndex == 0) return;
    	
    	// Remove graph from list
    	graph = graphList.remove(graphIndex);
    	
    	// Add graph at new index
    	graphList.add(graphIndex - 1, graph);
    	
    	// Reselect moved graph
    	graphListView.getSelectionModel().select(graph);
    	
    	// Update
    	updateGraphView();
    }
    
    @FXML private void handleGraphDownClick(ActionEvent event) {
    	// Assign local variable
    	Graph graph;
    	int graphIndex;
    	
    	// If there is no selected graph, break
    	if (selectedGraph == null) return;
    	
    	// Retrive graph index
    	graphIndex = graphList.indexOf(selectedGraph);
    	
    	// If graph already has lowest priority, break
    	if (graphIndex == graphList.size() - 1) return;
    	
    	// Remove graph from list
    	graph = graphList.remove(graphIndex);
    	
    	// Add graph at new index
    	graphList.add(graphIndex + 1, graph);
    	
    	// Reselect moved graph
    	graphListView.getSelectionModel().select(graph);
    	
    	// Update
    	updateGraphView();
    }
    
    @FXML private void handleGraphListClick(Event event) {
    	// Update
    	updateGraphView();
    }
    
    
    
    
	/////////////////////////////
	/////  PRIVATE CLASSES  /////
	/////////////////////////////
    
    // Custom JFXListCell used to respresent line styles
    private static class StyleCell extends JFXListCell<Style>{
		Line cellLine = new Line(0, 0, 90, 0);
		StackPane cellPane = new StackPane(cellLine);
		boolean isButtonCell = false;
		
		public StyleCell(boolean isButtonCell) {
			super();
			this.isButtonCell = isButtonCell;
		}
		
		@Override
		protected void updateItem(Style style, boolean empty) {
			super.updateItem(style, empty);
			
			setText(null);
			setGraphic(null);
			
			if (style != null && !empty) {
				setGraphic(cellPane);
				cellLine.getStrokeDashArray().setAll(style.getStroke());
				
				if (isSelected() && !isButtonCell)
					cellLine.setStyle("-fx-stroke: #FFFFFF;");
				else
					cellLine.setStyle("-fx-stroke: #454545;");
			}
		}
	}
}

//Dump code
//.forEach(elem -> ((Labeled) elem).getGraphic().setStyle(nodeStyle));
//String dotStyle = String.format("-fx-background-color: #%s;", graphList.get(i).getHexColor());
//System.out.println(nodeSet);
//nodeSet.forEach(item -> item.setStyle(String.format("-fx-background-color: #000000;")));
//List<Node> nodeList = nodeSet.stream()
//		.filter(test -> ((Parent) test).getChildrenUnmodifiable().size() != 0)
//		.map(parent -> ((Parent) parent).getChildrenUnmodifiable().get(0)).collect(Collectors.toList());

//System.out.println("Series are: " + lineChart.getData() + " Size: " + lineChart.getData().size());
//System.out.println("Hei");
//ObservableList<Node> nodeList = treeTraversal(lineChart, 2).getChildrenUnmodifiable();
////Set<Node> nodelist = lineChart.lookupAll(".default-color0");
////System.out.println("Nodes found: " + nodelist + " Size: " + nodelist.size());
//System.out.println("Node found: " + nodeList);
//if (nodeList.size() == 2)
//	System.out.println();;
//	for (int i = 0; i < nodeList.size(); i++) {
//		if (nodeList.get(i).lookup(".default-color0") != null)
//			nodeList.get(i).lookup(".default-color0").setStyle("-fx-background-color: #000000;");
//		System.out.println("Children: " + ((Parent) nodeList.get(i)).getChildrenUnmodifiable());
//		
//	}

//				.stream()
////				.filter(node -> node instanceof Label)
//				.forEach(node -> node.setStyle("-fx-background-color: #000000"));
//	.forEach(elem -> System.out.println("Hei"));


//// Prevent duplicate graphs
//if (!lineChart.getData().contains(selectedGraph.getSeries()))
//	lineChart.getData().add(selectedGraph.getSeries());

//// Fix graph order
//graphList.forEach(g -> g.getSeries().getNode().setViewOrder(graphList.indexOf(g)));

//String nodeStyle = String.format("-fx-background-color: #%s, #FFFFFF; -fx-background-insets: 0, 2;", "ff0000");
//lineChart.lookupAll(".chart-legend-item").stream()
// chart-legend-item-symbol chart-line-symbol series0 default-color0
//.map(elem -> ((Labeled) elem).getGraphic());
//.forEach(elem -> ((Labeled) elem).getGraphic().getStyleClass().get(3));