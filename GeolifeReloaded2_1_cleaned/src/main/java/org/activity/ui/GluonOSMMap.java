package org.activity.ui;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.activity.io.ReadingFromFile;
import org.activity.objects.Triple;

import com.gluonhq.charm.down.ServiceFactory;
import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.Position;
import com.gluonhq.charm.down.plugins.PositionService;
import com.gluonhq.charm.down.plugins.StorageService;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import com.gluonhq.maps.demo.PoiLayer;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
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

		BorderPane bp = getMapPane(absFileNameForLatLonToReadAsMarker, delimiter, latColIndex, lonColIndex,
				labelColIndex);

		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		Scene scene = new Scene(bp, bounds.getWidth(), bounds.getHeight());
		stage.setScene(scene);
		stage.show();

		// view.flyTo(1., mapPoint, 2.);
	}

	public BorderPane getMapPane(String absFileNameForLatLonToReadAsMarker, String delimiter, int latColIndex,
			int lonColIndex, int labelColIndex)
	// String absFileNameForLatLonToReadAsMarker, String delimiter, int latColIndex,
	// int lonColIndex, int labelColIndex, boolean useCustomMarker) throws Exception
	{
		MapView view = new MapView();

		view.setCenter(42.472309, 6.897996);
		view.setZoom(12);
		List<Triple<Double, Double, String>> listOfLocs = readListOfLocationsV2(absFileNameForLatLonToReadAsMarker,
				delimiter, latColIndex, lonColIndex, labelColIndex);

		view.addLayer(positionLayerV2(listOfLocs));
		view.setZoom(4);
		// Scene scene;
		BorderPane bp = new BorderPane();
		bp.setCenter(view);
		listOfLocs.clear();
		// final Label label = new Label("Gluon Maps Demo");
		// label.setAlignment(Pos.CENTER);
		// label.setMaxWidth(Double.MAX_VALUE);
		// label.setStyle("-fx-background-color: dimgrey; -fx-text-fill: white;");
		// bp.setTop(label);

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

	private MapLayer positionLayerV2(List<Triple<Double, Double, String>> listOfLocs)
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
					Circle c = new Circle(3, Color.rgb(193, 49, 34, 0.65));
					// c.setStroke(Color.BLACK);
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
					// LOGGER.log(Level.WARNING, "Position Service not available");
					PoiLayer answer = new PoiLayer();
					// mapPoint = new MapPoint(50., 4.);
					// answer.addPoint(mapPoint, new Circle(7, Color.RED));
					// Circle c = new Circle(5, Color.rgb(193, 49, 34, 0.65));
					for (MapPoint mp : mapPoints)
					{
						Circle c = new Circle(3, Color.rgb(193, 49, 34, 0.65));
						// c.setStroke(Color.BLACK);
						answer.addPoint(mp, c);
					}

					// $$mapPoints.stream().forEach(mp -> answer.addPoint(mp, new Circle(5, Color.rgb(193, 49, 34,
					// 0.65))));
					return answer;
				});
	}

	// /**
	// *
	// * @param absFileNameForLatLong
	// * @param delimiter
	// * @param latColIndex
	// * @param lonColIndex
	// * @param labelColIndex
	// */
	// public static List<Triple<Double, Double, String>> readListOfLocations(String absFileNameForLatLong,
	// String delimiter, int latColIndex, int lonColIndex, int labelColIndex)
	// {
	// // String absFileNameForLatLong = ;
	//
	// List<List<String>> lines = ReadingFromFile.readLinesIntoListOfLists(absFileNameForLatLong, ",");
	// // System.out.println("lines.size()=" + lines.size());
	// List<Triple<Double, Double, String>> listOfLocations = new ArrayList<>();
	// int count = 0;
	//
	// for (List<String> line : lines)
	// {
	// count += 1;
	//
	// if (count == 1)
	// {
	// continue;
	// }
	// if (++count > 1000000)
	// {
	// break;
	// }
	// // System.out.println("line= " + line);
	// // System.out.println("here 1");
	//
	// Triple<Double, Double, String> val = new Triple<>(Double.valueOf(line.get(latColIndex)),
	// Double.valueOf(line.get(lonColIndex)), "id=" + line.get(labelColIndex));
	//
	// // LatLong markerLatLong2 = new LatLong(-1.6073826, 67.9382483);// 47.606189, -122.335842);
	// // // Double.valueOf(line.get(2).substring(0, 4)));
	// // // LatLong markerLatLong2 = new LatLong(Double.valueOf(line.get(3).substring(0, 4)),
	// // // Double.valueOf(line.get(2).substring(0, 4)));
	// // System.out.println("LatLong= " + markerLatLong2.toString());
	// // markerOptions2.position(markerLatLong2).title(line.get(1)).visible(true);
	// // System.out.println("here2");
	// // myMarker2 = new Marker(markerOptions2);
	// // System.out.println("here3");
	// listOfLocations.add(val);
	// }
	//
	// // StringBuilder sb = new StringBuilder();
	// // listOfMarkers.stream().forEachOrdered(e -> sb.append(e.toString() + "\n"));
	// // System.out.println("List of markers= " + sb.toString());
	// // System.out.println("listOfLocations.size()=" + listOfLocations.size());
	// return listOfLocations;
	// }

	/**
	 * 
	 * @param absFileNameForLatLong
	 * @param delimiter
	 * @param latColIndex
	 * @param lonColIndex
	 * @param labelColIndex
	 */
	public static List<Triple<Double, Double, String>> readListOfLocationsV2(String absFileNameForLatLong,
			String delimiter, int latColIndex, int lonColIndex, int labelColIndex)
	{
		List<Triple<Double, Double, String>> listOfLocations = new ArrayList<>();
		try
		{
			List<List<String>> lines = ReadingFromFile.nColumnReaderStringLargeFileSelectedColumns(
					new FileInputStream(new File(absFileNameForLatLong)), ",", true, false,
					new int[] { latColIndex, lonColIndex, labelColIndex });

			System.out.println("lines.size()=" + lines.size());

			int count = 0;

			for (List<String> line : lines)
			{
				count += 1;

				if (count == 1)
				{
					continue;
				}
				if (++count > 20000)
				{
					break;
				}
				// System.out.println("line= " + line);
				// System.out.println("here 1");

				Triple<Double, Double, String> val = new Triple<>(Double.valueOf(line.get(0)),
						Double.valueOf(line.get(1)), "id=" + line.get(2));

				// LatLong markerLatLong2 = new LatLong(-1.6073826, 67.9382483);// 47.606189, -122.335842);
				// // Double.valueOf(line.get(2).substring(0, 4)));
				// // LatLong markerLatLong2 = new LatLong(Double.valueOf(line.get(3).substring(0, 4)),
				// // Double.valueOf(line.get(2).substring(0, 4)));
				// System.out.println("LatLong= " + markerLatLong2.toString());
				// markerOptions2.position(markerLatLong2).title(line.get(1)).visible(true);
				// System.out.println("here2");
				// myMarker2 = new Marker(markerOptions2);
				// System.out.println("here3");
				listOfLocations.add(val);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// StringBuilder sb = new StringBuilder();
		// listOfMarkers.stream().forEachOrdered(e -> sb.append(e.toString() + "\n"));
		// System.out.println("List of markers= " + sb.toString());
		// System.out.println("listOfLocations.size()=" + listOfLocations.size());
		return listOfLocations;
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
