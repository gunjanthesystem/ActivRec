package org.activity.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.activity.io.ReadingFromFile;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.stats.StatsUtils;
import org.activity.ui.colors.ColorPalette;
import org.giscience.utils.geogrid.gunjanUtils.GLatLonToGridTransformer;

import com.gluonhq.charm.down.ServiceFactory;
import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.Position;
import com.gluonhq.charm.down.plugins.PositionService;
import com.gluonhq.charm.down.plugins.StorageService;
import com.gluonhq.impl.maps.BaseMap;
import com.gluonhq.impl.maps.ImageRetriever;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import com.gluonhq.maps.demo.PoiLayer;
import com.gluonhq.maps.demo.PoiLayer2Faster;
import com.gluonhq.maps.demo.PoiLayerPolygon;
import com.jfoenix.controls.JFXSlider;

import jViridis.ColorMap;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * Demo class showing a simple map app
 */
public class GluonOSMMap extends Application
{

	// private static final Logger LOGGER = Logger.getLogger(GluonOSMMap.class.getName());
	// static
	// {
	// try
	// {
	// LogManager.getLogManager().readConfiguration(GluonOSMMap.class.getResourceAsStream("/logging.properties"));
	// }
	// catch (IOException e)
	// {
	// LOGGER.log(Level.SEVERE, "Error reading logging properties file", e);
	// }
	// }

	private MapPoint mapPoint;

	public static void main(String[] args)
	{
		// System.setProperty("prism.allowhidpi", "true");
		Application.launch(args);
	}

	public GluonOSMMap()
	{
		setCacheStorage();
		// LOGGER.setFilter(newFilter);
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		setCacheStorage();
		String absFileNameForLatLonToReadAsMarker = "./dataToRead/Mar12/gowalla_spots_subset1_fromRaw28Feb2018smallerFileWithSampleWithTZ1.csv";
		String delimiter = ",";
		int latColIndex = 3, lonColIndex = 2, labelColIndex = 1;

		List<Triple<Double, Double, String>> listOfLocs = ReadingFromFile.readListOfLocationsV2(
				absFileNameForLatLonToReadAsMarker, delimiter, latColIndex, lonColIndex, labelColIndex);

		BorderPane bp = getMapPaneForListOfLocations(listOfLocs, 3, Color.rgb(193, 49, 34, 0.65), false, false,
				"Info label test");

		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		Scene scene = new Scene(bp, bounds.getWidth(), bounds.getHeight());
		stage.setScene(scene);
		stage.show();

		// view.flyTo(1., mapPoint, 2.);
	}

	/**
	 * 
	 * @param listOfLocs
	 * @param sizeOfIcon
	 * @param colorOfIcon
	 * @param colorByLabel
	 * @param clearMapCache
	 * @param infoLabelText
	 * @return
	 */
	public BorderPane getMapPaneForListOfLocations(List<Triple<Double, Double, String>> listOfLocs, int sizeOfIcon,
			Color colorOfIcon, boolean colorByLabel, boolean clearMapCache, String infoLabelText)
	// String absFileNameForLatLonToReadAsMarker, String delimiter, int latColIndex,
	// int lonColIndex, int labelColIndex, boolean useCustomMarker) throws Exception
	{
		boolean drawCellGrids = false;// added on June 23 2019
		// List<Triple<Double, Double, String>> listOfLocs = ReadingFromFile.readListOfLocationsV2(
		// absFileNameForLatLonToReadAsMarker, delimiter, latColIndex, lonColIndex, labelColIndex);

		MapView view = new MapView();

		if (clearMapCache)
		{
			ImageRetriever.clearCachedImages();
		}
		view.setCenter(42.472309, 6.897996);
		view.setZoom(4);

		System.out.println("listOfLocs.size() = " + listOfLocs.size());
		// view.addLayer(positionLayerV2(listOfLocs, sizeOfIcon, colorOfIcon));
		if (colorByLabel)
		{
			view.addLayer(positionLayerV3_colorByLabelScaled(listOfLocs, sizeOfIcon));
		}
		else
		{
			view.addLayer(positionLayerV3(listOfLocs, sizeOfIcon, colorOfIcon));
		}
		// $listOfLocs.clear();
		// view.setZoom(4);
		// Scene scene;

		// Start of April 8 2018
		// Pane

		if (drawCellGrids)
		{
			view.addLayer(positionLayerPolygons1(GLatLonToGridTransformer.getBoundaryCoordinatesOfHexCells(),
					sizeOfIcon, Color.PINK));
		}

		BorderPane toolPane = new BorderPane();

		JFXSlider slider = new JFXSlider(1, BaseMap.MAX_ZOOM, 1);
		// Slider slider = new Slider(1, BaseMap.MAX_ZOOM, 1);
		slider.setOrientation(Orientation.VERTICAL);
		slider.setShowTickMarks(true);
		slider.setShowTickLabels(true);
		slider.setMajorTickUnit(1f);
		slider.setBlockIncrement(1f);
		// slider.setMino
		Bindings.bindBidirectional(slider.valueProperty(), view.getBaseMap().zoom());
		// slider.setPrefWidth(200);
		// slider.setPrefHeight(700);
		toolPane.setCenter(slider);
		toolPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.0);");

		// defines a viewport into the source image (achieving a "zoom" effect) and
		// displays it rotated
		// ImageView iv3 = new ImageView();
		// iv3.setImage(new Image(
		// "https://www.fiftyflowers.com/site_files/FiftyFlowers/Image/Product/salmon-dahlia-flower-350_5ae0c998.jpg"));
		// Rectangle2D viewportRect = new Rectangle2D(40, 35, 110, 110);
		// iv3.setViewport(viewportRect);
		// iv3.setRotate(90);
		// toolPane.setBottom(iv3);
		// End of April 8 2018

		// start of July 24 2018
		Label infoLabel = new Label(infoLabelText);

		// end of July 24 2018
		BorderPane bp = new BorderPane();
		bp.setCenter(view);
		bp.setRight(toolPane);
		bp.setBottom(infoLabel);// added on July24 2018
		toolPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.0);");
		// final Label label = new Label("Gluon Maps Demo");
		// label.setAlignment(Pos.CENTER);
		// label.setMaxWidth(Double.MAX_VALUE);
		// label.setStyle("-fx-background-color: dimgrey; -fx-text-fill: white;");
		// bp.setTop(label);

		return bp;
	}

	/**
	 * 
	 * @param absFileNameForLatLonToReadAsMarker
	 * @param delimiter
	 * @param latColIndex
	 * @param lonColIndex
	 * @param labelColIndex
	 * @param sizeOfIcon
	 * @param colorOfIcon
	 * @return
	 */
	public BorderPane getMapPane2(String absFileNameForLatLonToReadAsMarker, String delimiter, int latColIndex,
			int lonColIndex, int labelColIndex, int sizeOfIcon, Color colorOfIcon)
	// String absFileNameForLatLonToReadAsMarker, String delimiter, int latColIndex,
	// int lonColIndex, int labelColIndex, boolean useCustomMarker) throws Exception
	{
		MapView view = new MapView();

		// view.
		view.setCenter(42.472309, 6.897996);
		view.setZoom(4);

		List<Triple<Double, Double, String>> listOfLocs = ReadingFromFile.readListOfLocationsV2(
				absFileNameForLatLonToReadAsMarker, delimiter, latColIndex, lonColIndex, labelColIndex);

		System.out.println("listOfLocs.size() = " + listOfLocs.size());
		// view.addLayer(positionLayerV2(listOfLocs, sizeOfIcon, colorOfIcon));
		view.addLayer(positionLayerV3(listOfLocs, sizeOfIcon, colorOfIcon));
		listOfLocs.clear();
		// view.setZoom(4);
		// Scene scene;
		BorderPane bp = new BorderPane();
		bp.setCenter(view);

		// final Label label = new Label("Gluon Maps Demo");
		// label.setAlignment(Pos.CENTER);
		// label.setMaxWidth(Double.MAX_VALUE);
		// label.setStyle("-fx-background-color: dimgrey; -fx-text-fill: white;");
		// bp.setTop(label);

		return bp;
	}

	/**
	 * 
	 * @param absFileNameForLatLonToReadAsMarker
	 * @param delimiter
	 * @param latColIndex
	 * @param lonColIndex
	 * @param labelColIndex
	 * @param fillValColIndex
	 * @param sizeOfIcon
	 * @return
	 */
	public BorderPane getMapPane2(String absFileNameForLatLonToReadAsMarker, String delimiter, int latColIndex,
			int lonColIndex, int labelColIndex, int fillValColIndex, int sizeOfIcon)
	// String absFileNameForLatLonToReadAsMarker, String delimiter, int latColIndex,
	// int lonColIndex, int labelColIndex, boolean useCustomMarker) throws Exception
	{
		BorderPane bp = new BorderPane();
		try
		{
			MapView view = new MapView();

			view.setCenter(42.472309, 6.897996);
			view.setZoom(4);

			List<List<String>> lines = ReadingFromFile.nColumnReaderStringLargeFileSelectedColumns(
					new FileInputStream(new File(absFileNameForLatLonToReadAsMarker)), ",", true, false,
					new int[] { latColIndex, lonColIndex, labelColIndex, fillValColIndex });

			lines.remove(0);// remove headers

			lines = lines.stream().skip((int) Math.floor(lines.size() * 0.75)).collect(Collectors.toList());

			System.out.println("lines.size()=" + lines.size());

			// view.addLayer(positionLayerV2(listOfLocs, sizeOfIcon, colorOfIcon));
			MapLayer layer1 = positionLayerV3(lines, sizeOfIcon, 0, 1, 2, 3);
			System.out.println("layer1.toString()= " + layer1.toString());
			view.addLayer(layer1);
			// lines.clear();
			// view.setZoom(4);
			bp.setCenter(view);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return bp;
	}
	// private MapLayer myDemoLayer () {
	// PoiLayer answer = new PoiLayer();
	// Node icon1 = new Circle(7, Color.BLUE);
	// answer.addPoint(new MapPoint(50.8458,4.724), icon1);
	// Node icon2 = new Circle(7, Color.GREEN);
	// answer.addPoint(new MapPoint(37.396256,-121.953847), icon2);
	// return answer;
	// }

	// private MapLayer positionLayer()
	// {
	// return Services.get(PositionService.class).map(positionService ->
	// {
	// ReadOnlyObjectProperty<Position> positionProperty = positionService.positionProperty();
	// Position position = positionProperty.get();
	// if (position == null)
	// {
	// position = new Position(50., 4.);
	// }
	// mapPoint = new MapPoint(position.getLatitude(), position.getLongitude());
	// // LOGGER.log(Level.INFO, "Initial Position: " + position.getLatitude() + ", " +
	// // position.getLongitude());
	//
	// PoiLayer answer = new PoiLayer();
	// answer.addPoint(mapPoint, new Circle(7, Color.RED));
	//
	// positionProperty.addListener(e ->
	// {
	// Position pos = positionProperty.get();
	// // LOGGER.log(Level.INFO, "New Position: " + pos.getLatitude() + ", " + pos.getLongitude());
	// mapPoint.update(pos.getLatitude(), pos.getLongitude());
	// });
	// return answer;
	// }).orElseGet(() ->
	// {
	// System.out.println("Position Service not available");
	// PoiLayer answer = new PoiLayer();
	// mapPoint = new MapPoint(50., 4.);
	// answer.addPoint(mapPoint, new Circle(7, Color.RED));
	// return answer;
	// });
	// }

	/**
	 * 
	 * @param listOfLocs
	 * @param sizeOfIcon
	 * @param colorOfIcon
	 * @return
	 */
	private MapLayer positionLayerV2(List<Triple<Double, Double, String>> listOfLocs, int sizeOfIcon, Color colorOfIcon)
	{
		List<MapPoint> mapPoints = new ArrayList<>();

		for (Triple<Double, Double, String> locEntry : listOfLocs)
		{
			mapPoints.add(new MapPoint(locEntry.getFirst(), locEntry.getSecond()));
		}

		// System.out.println(listOfLocs.toString());
		// System.out.println("listOfLocs.size()=" + listOfLocs.size());
		// System.out.println("mapPoints.size()=" + mapPoints.size());
		return Services.get(PositionService.class).map(positionService ->
			{
				ReadOnlyObjectProperty<Position> positionProperty = positionService.positionProperty();
				// Position position = positionProperty.get();

				// if (position == null)
				// {
				// position = new Position(50., 4.);
				// }
				// mapPoint = new MapPoint(position.getLatitude(), position.getLongitude());

				PoiLayer answer = new PoiLayer();
				// answer.addPoint(mapPoint, new Circle(7, Color.RED));
				// Circle c = new Circle(5,new Paint)
				// Circle c = new Circle(5, Color.rgb(193, 49, 34, 0.65));
				for (MapPoint mp : mapPoints)
				{
					// Circle c = new Circle(3, Color.rgb(193, 49, 34, 0.65));
					Circle c = new Circle(sizeOfIcon, colorOfIcon);
					c.setStroke(Color.BLACK);
					answer.addPoint(mp, c);
				}
				// $mapPoints.stream().forEach(mp -> answer.addPoint(mp, new Circle(5, Color.rgb(0, 105, 106, 0.65))));
				positionProperty.addListener(e ->
					{
						Position pos = positionProperty.get();
						// LOGGER.log(Level.INFO, "New Position: " + pos.getLatitude() + ", " + pos.getLongitude());
						mapPoint.update(pos.getLatitude(), pos.getLongitude());
					});
				return answer;
			}).orElseGet(() ->
				{
					System.out.println("Position Service not available");
					// LOGGER.log(Level.WARNING, "Position Service not available");
					PoiLayer answer = new PoiLayer();
					// mapPoint = new MapPoint(50., 4.);
					// answer.addPoint(mapPoint, new Circle(7, Color.RED));
					// Circle c = new Circle(5, Color.rgb(193, 49, 34, 0.65));
					for (MapPoint mp : mapPoints)
					{
						// Circle c = new Circle(3, Color.rgb(193, 49, 34, 0.65));
						Circle c = new Circle(sizeOfIcon, colorOfIcon);
						c.setStroke(Color.BLACK);
						answer.addPoint(mp, c);
					}

					// $$mapPoints.stream().forEach(mp -> answer.addPoint(mp, new Circle(5, Color.rgb(193, 49, 34,
					// 0.65))));
					return answer;
				});
	}

	/**
	 * 
	 * @param listOfLocs
	 * @param sizeOfIcon
	 * @param colorOfIcon
	 * @return
	 */
	private MapLayer positionLayerV3(List<Triple<Double, Double, String>> listOfLocs, int sizeOfIcon, Color colorOfIcon)
	{
		List<MapPoint> mapPoints = new ArrayList<>();

		for (Triple<Double, Double, String> locEntry : listOfLocs)
		{
			mapPoints.add(new MapPoint(locEntry.getFirst(), locEntry.getSecond()));
		}
		// System.out.println(listOfLocs.toString());
		// System.out.println("listOfLocs.size()=" + listOfLocs.size());
		// System.out.println("mapPoints.size()=" + mapPoints.size());
		// LOGGER.log(Level.WARNING, "Position Service not available");
		PoiLayer2Faster answer = new PoiLayer2Faster(mapPoints.size());
		// mapPoint = new MapPoint(50., 4.);
		// answer.addPoint(mapPoint, new Circle(7, Color.RED));
		// Circle c = new Circle(5, Color.rgb(193, 49, 34, 0.65));
		for (MapPoint mp : mapPoints)
		{
			// Circle c = new Circle(3, Color.rgb(193, 49, 34, 0.65));
			Circle c = new Circle(sizeOfIcon, colorOfIcon);
			// c.setStroke(Color.BLACK);
			answer.addPoint(mp, c);
		}

		// $$mapPoints.stream().forEach(mp -> answer.addPoint(mp, new Circle(5, Color.rgb(193, 49, 34,
		// 0.65))));
		return answer;

	}

	// start of added on June 23 2019
	/**
	 * 
	 * @param listOfLocs
	 * @param sizeOfIcon
	 * @param colorOfIcon
	 * @return
	 * @since June 23 2019
	 */
	private MapLayer positionLayerPolygons1(Map<String, ArrayList<String[]>> cellIDBoundaryLatLons, int sizeOfIcon,
			Color colorOfIcon)
	{
		List<MapPoint> mapPoints = new ArrayList<>();
		// Map<String, ArrayList<String[]>> cellIDBoundaryLatLons

		PoiLayerPolygon poilayer = new PoiLayerPolygon();

		for (Entry<String, ArrayList<String[]>> e : cellIDBoundaryLatLons.entrySet())
		{
			String cellID = e.getKey();

			for (String[] boundaryPoints : e.getValue())
			{
				double lat = Double.valueOf(boundaryPoints[0]);
				double lon = Double.valueOf(boundaryPoints[1]);
				poilayer.addPoint(new MapPoint(lat, lon), new Circle(5, Color.BLUE));
			}
		}

		// // for (Triple<Double, Double, String> locEntry : listOfLocs)
		// // {
		// // mapPoints.add(new MapPoint(locEntry.getFirst(), locEntry.getSecond()));
		// // }
		// // System.out.println(listOfLocs.toString());
		// // System.out.println("listOfLocs.size()=" + listOfLocs.size());
		// // System.out.println("mapPoints.size()=" + mapPoints.size());
		// // LOGGER.log(Level.WARNING, "Position Service not available");
		// PoiLayer2Faster answer = new PoiLayer2Faster(mapPoints.size());
		// // mapPoint = new MapPoint(50., 4.);
		// // answer.addPoint(mapPoint, new Circle(7, Color.RED));
		// // Circle c = new Circle(5, Color.rgb(193, 49, 34, 0.65));
		// for (MapPoint mp : mapPoints)
		// {
		// // Circle c = new Circle(3, Color.rgb(193, 49, 34, 0.65));
		// Circle c = new Circle(sizeOfIcon, colorOfIcon);
		// // c.setStroke(Color.BLACK);
		// answer.addPoint(mp, c);
		// }

		// $$mapPoints.stream().forEach(mp -> answer.addPoint(mp, new Circle(5, Color.rgb(193, 49, 34,
		// 0.65))));
		return poilayer;

	}
	// end of added on June 23 2019

	/**
	 * 
	 * @param listOfLocs
	 * @param sizeOfIcon
	 * @return
	 */
	private MapLayer positionLayerV3_colorByLabelScaled(List<Triple<Double, Double, String>> listOfLocs, int sizeOfIcon)
	{
		PoiLayer2Faster answer = new PoiLayer2Faster(listOfLocs.size());

		List<Double> listOfValsForScaledColourFill = new ArrayList<>();

		for (Triple<Double, Double, String> locEntry : listOfLocs)
		{
			listOfValsForScaledColourFill.add(Double.valueOf(locEntry.getThird()));
		}

		//////////////////
		int numOfBins = 100;
		Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Double> binningRes = StatsUtils
				.binValuesByNumOfBins(listOfValsForScaledColourFill, numOfBins, true);
		// Pair<List<Pair<Double, Integer>>, Double>
		List<Pair<Double, Integer>> valBinIndexList = binningRes.getFirst().getFirst();
		Color[] colors = ColorPalette
				.awtColorToJavaFXColor(ColorMap.getInstance(ColorMap.VIRIDIS).getColorPalette(numOfBins));
		System.out.println("color.length=" + colors.length);

		/////////////////
		for (int i = 0; i < listOfLocs.size(); i++)// MapPoint mp : mapPoints)
		{
			Triple<Double, Double, String> locEntry = listOfLocs.get(i);
			// Circle c = new Circle(3, Color.rgb(193, 49, 34, 0.65));

			Pair<Double, Integer> valBinIndex = valBinIndexList.get(i);
			Double fillValFromLocEntry = Double.valueOf(locEntry.getThird());

			if (valBinIndex.getFirst().equals(fillValFromLocEntry) == false)
			{
				PopUps.printTracedErrorMsg("Error: valBinIndex.getFirst().equals(fillValFromLocEntry)==false");
			}

			int binIndex = valBinIndex.getSecond();// valBinIndexMap.get(Double.valueOf(locEntry.getThird()));

			// Color fillColor = colors[binIndex];
			Color fillColor = colors[colors.length - 1 - binIndex];// reversing colors
			// System.out.println("binIndex= " + binIndex + " Color= " + fillColor + " red " + fillColor.getRed());

			Circle c = new Circle(sizeOfIcon, fillColor);
			// c.setStroke(Color.BLACK);
			answer.addPoint(new MapPoint(locEntry.getFirst(), locEntry.getSecond()), c);
		}

		return answer;

	}

	/**
	 * Fork of positionLayerV3_colorByLabelScaled for hops, showing line segments instead of lines. Incomplete as of 18
	 * Feb 2019
	 * 
	 * @param listOfLocs
	 * @param sizeOfIcon
	 * @return
	 * @since 18 Feb 2019
	 */
	private MapLayer positionLayerV3_colorByLabelScaledForHops(
			List<Pair<Triple<Double, Double, String>, Triple<Double, Double, String>>> listOfLocs, int sizeOfIcon)
	{
		PoiLayer2Faster answer = new PoiLayer2Faster(listOfLocs.size());

		List<Double> listOfValsForScaledColourFill = new ArrayList<>();

		for (Pair<Triple<Double, Double, String>, Triple<Double, Double, String>> locEntry : listOfLocs)
		{
			listOfValsForScaledColourFill.add(Double.valueOf(locEntry.getFirst().getThird()));
			listOfValsForScaledColourFill.add(Double.valueOf(locEntry.getSecond().getThird()));
		}

		//////////////////
		int numOfBins = 100;

		Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Double> binningRes = StatsUtils
				.binValuesByNumOfBins(listOfValsForScaledColourFill, numOfBins, true);
		List<Pair<Double, Integer>> valBinIndexList = binningRes.getFirst().getFirst();
		Color[] colors = ColorPalette
				.awtColorToJavaFXColor(ColorMap.getInstance(ColorMap.VIRIDIS).getColorPalette(numOfBins));
		System.out.println("color.length=" + colors.length);

		/////////////////

		for (int i = 0; i < listOfLocs.size(); i++)// MapPoint mp : mapPoints)
		{
			Pair<Triple<Double, Double, String>, Triple<Double, Double, String>> locEntry = listOfLocs.get(i);
			// Circle c = new Circle(3, Color.rgb(193, 49, 34, 0.65));

			Pair<Double, Integer> valBinIndex = valBinIndexList.get(i);

			Double fillValFromLocEntry1 = Double.valueOf(locEntry.getFirst().getThird());

			if (valBinIndex.getFirst().equals(fillValFromLocEntry1) == false)
			{
				PopUps.printTracedErrorMsg("Error: valBinIndex.getFirst().equals(fillValFromLocEntry)==false");
			}

			int binIndex = valBinIndex.getSecond();// valBinIndexMap.get(Double.valueOf(locEntry.getThird()));

			// Color fillColor = colors[binIndex];
			Color fillColor = colors[colors.length - 1 - binIndex];// reversing colors
			// System.out.println("binIndex= " + binIndex + " Color= " + fillColor + " red " + fillColor.getRed());

			Circle c = new Circle(sizeOfIcon, fillColor);
			// c.setStroke(Color.BLACK);
			answer.addPoint(new MapPoint(locEntry.getFirst().getFirst(), locEntry.getFirst().getSecond()), c);
			answer.addPoint(new MapPoint(locEntry.getSecond().getFirst(), locEntry.getSecond().getSecond()), c);
			// adding two circles temporarily, instead it should be a line segment from one geo-location to the other.
		}

		return answer;

	}

	/**
	 * 
	 * @param listOfLocs
	 * @param sizeOfIcon
	 * @param indexOfLat
	 * @param indexOfLon
	 * @param indexOfLabel
	 * @param indexOfFillVal
	 * @return
	 */
	private MapLayer positionLayerV3(List<List<String>> listOfLocs, int sizeOfIcon, int indexOfLat, int indexOfLon,
			int indexOfLabel, int indexOfFillVal)
	{
		// {MapPoint, valueToDecideForFill}
		// List<Pair<MapPoint, Double>> mapPointsWithFillVal = new ArrayList<>();

		List<MapPoint> mapPoints = new ArrayList<>();
		List<Double> listOfValsForScaledColourFill = new ArrayList<>();
		List<String> listOfLabels = new ArrayList<>();

		for (List<String> locEntry : listOfLocs)
		{
			System.out.println("locEntry=" + locEntry);
			mapPoints.add(
					new MapPoint(Double.valueOf(locEntry.get(indexOfLon)), Double.valueOf(locEntry.get(indexOfLon))));
			listOfValsForScaledColourFill.add(Double.valueOf(locEntry.get(indexOfFillVal)));
			listOfLabels.add(locEntry.get(indexOfLabel));
		}

		// double maxVal = listOfValsForScaledColourFill.stream().mapToDouble(e ->
		// Double.valueOf(e)).max().getAsDouble();
		// double minVal = listOfValsForScaledColourFill.stream().mapToDouble(e ->
		// Double.valueOf(e)).min().getAsDouble();
		// int numOfSteps = 10;
		// double stepSize = (maxVal - minVal) / numOfSteps;

		// double maxHue = 1.0;
		// double minHue = 0.0;
		//
		// Color maxColor = Color.hsb(270, 1.0, 1.0); // hue = 270, saturation & value = 1.0. inplicit alpha of 1.0
		// Color colorOfIcon = maxColor;

		// ColorBrewer.

		int numOfBins = 10;
		Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Double> binningRes = StatsUtils
				.binValuesByNumOfBins(listOfValsForScaledColourFill, numOfBins, true);
		List<Pair<Double, Integer>> valBinIndexMap = binningRes.getFirst().getFirst();

		Color[] colors = ColorPalette
				.awtColorToJavaFXColor(ColorMap.getInstance(ColorMap.VIRIDIS).getColorPalette(numOfBins));
		System.out.println("color.length=" + colors.length);
		// System.out.println(listOfLocs.toString());
		// System.out.println("listOfLocs.size()=" + listOfLocs.size());
		// System.out.println("mapPoints.size()=" + mapPoints.size());
		// LOGGER.log(Level.WARNING, "Position Service not available");
		System.out.println("mapPoints.size= " + mapPoints.size());
		PoiLayer2Faster answer = new PoiLayer2Faster(mapPoints.size());
		// mapPoint = new MapPoint(50., 4.);
		// answer.addPoint(mapPoint, new Circle(7, Color.RED));
		// Circle c = new Circle(5, Color.rgb(193, 49, 34, 0.65));
		for (int i = 0; i < mapPoints.size(); i++)
		{
			// Circle c = new Circle(3, Color.rgb(193, 49, 34, 0.65));
			// $$int binIndexForFillVal = valBinIndexMap.get(listOfValsForScaledColourFill.get(i));
			// $$ Color colorOfIcon = colors[binIndexForFillVal];

			Circle c = new Circle(5, Color.DARKCYAN);
			// c.setStroke(Color.BLACK);
			MapPoint p = mapPoints.get(i);
			// System.out.println(
			// "MapPoint= " + p.getLatitude() + " Color = " + colorOfIcon.toString() + colorOfIcon.getRed());
			answer.addPoint(mapPoints.get(i), c);
		}

		// $$mapPoints.stream().forEach(mp -> answer.addPoint(mp, new Circle(5, Color.rgb(193, 49, 34,
		// 0.65))));
		return answer;

	}

	/**
	 * 
	 * @param args
	 */
	public static void setCacheStorage()
	{

		// if (isWindows() || isMac() || isUnix())
		// {
		// System.setProperty("javafx.platform", "Desktop");
		// }

		// define service for desktop
		StorageService storageService = new StorageService()
			{
				@Override
				public Optional<File> getPrivateStorage()
				{
					// user home app config location (linux: /home/[yourname]/.gluonmaps)
					return Optional.of(new File(System.getProperty("user.home")));
				}

				@Override
				public Optional<File> getPublicStorage(String subdirectory)
				{
					// this should work on desktop systems because home path is public
					return getPrivateStorage();
				}

				@Override
				public boolean isExternalStorageWritable()
				{
					// noinspection ConstantConditions
					return getPrivateStorage().get().canWrite();
				}

				@Override
				public boolean isExternalStorageReadable()
				{
					// noinspection ConstantConditions
					return getPrivateStorage().get().canRead();
				}
			};

		// define service factory for desktop
		ServiceFactory<StorageService> storageServiceFactory = new ServiceFactory<StorageService>()
			{

				@Override
				public Class<StorageService> getServiceType()
				{
					return StorageService.class;
				}

				@Override
				public Optional<StorageService> getInstance()
				{
					return Optional.of(storageService);
				}

			};
		// register service
		Services.registerServiceFactory(storageServiceFactory);

	}

}
