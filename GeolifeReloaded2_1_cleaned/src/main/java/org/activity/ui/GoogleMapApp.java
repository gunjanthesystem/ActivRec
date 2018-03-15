package org.activity.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.activity.io.ReadingFromFile;
import org.activity.objects.Triple;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.DirectionsPane;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.LatLongBounds;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.service.directions.DirectionStatus;
import com.lynden.gmapsfx.service.directions.DirectionsRenderer;
import com.lynden.gmapsfx.service.directions.DirectionsResult;
import com.lynden.gmapsfx.service.directions.DirectionsServiceCallback;
import com.lynden.gmapsfx.service.elevation.ElevationResult;
import com.lynden.gmapsfx.service.elevation.ElevationServiceCallback;
import com.lynden.gmapsfx.service.elevation.ElevationStatus;
import com.lynden.gmapsfx.service.geocoding.GeocoderStatus;
import com.lynden.gmapsfx.service.geocoding.GeocodingResult;
import com.lynden.gmapsfx.service.geocoding.GeocodingService;
import com.lynden.gmapsfx.service.geocoding.GeocodingServiceCallback;
import com.lynden.gmapsfx.util.MarkerImageFactory;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

/**
 * Example Application for creating and loading a GoogleMap into a JavaFX application
 *
 * @author Rob Terpilowski
 */
public class GoogleMapApp extends Application implements MapComponentInitializedListener, ElevationServiceCallback,
		GeocodingServiceCallback, DirectionsServiceCallback
{

	protected GoogleMapView mapComponent;
	protected GoogleMap map;
	protected DirectionsPane directions;

	private Button btnZoomIn;
	private Button btnZoomOut;
	private Label lblZoom;
	private Label lblCenter;
	private Label lblClick;
	private ComboBox<MapTypeIdEnum> mapTypeCombo;

	private MarkerOptions markerOptions2;
	private Marker myMarker2;
	// private Button btnHideMarker;
	// private Button btnDeleteMarker;

	DirectionsRenderer renderer;

	private List<Triple<Double, Double, String>> listOfMarkers;
	boolean useCustomMarker = false;

	public GoogleMapApp()
	{
		super();
	}

	@Override
	public void start(final Stage stage) throws Exception
	{
		String absFileNameForLatLonToReadAsMarker = "./dataToRead/Mar12/gowalla_spots_subset1_fromRaw28Feb2018smallerFileWithSampleWithTZ1.csv";
		String delimiter = ",";
		int latColIndex = 3, lonColIndex = 2, labelColIndex = 1;

		BorderPane bp = getMapPane(absFileNameForLatLonToReadAsMarker, delimiter, latColIndex, lonColIndex,
				labelColIndex, true);
		Scene scene = new Scene(bp);
		scene.getStylesheets().add("./jfxtras/styles/jmetro8/GJMetroLightTheme.css");// gsheetNative.css");
		stage.setScene(scene);
		stage.show();
	}

	// public MapApp(boolean useCustomMarker)
	// {
	// this.useCustomMarker = useCustomMarker;
	// }

	/**
	 * 
	 * @param absFileNameForLatLonToReadAsMarker
	 * @param delimiter
	 * @param latColIndex
	 * @param lonColIndex
	 * @param labelColIndex
	 * @return
	 * @throws Exception
	 */
	public BorderPane getMapPane(String absFileNameForLatLonToReadAsMarker, String delimiter, int latColIndex,
			int lonColIndex, int labelColIndex, boolean useCustomMarker) throws Exception
	{
		this.useCustomMarker = useCustomMarker;
		mapComponent = new GoogleMapView(Locale.getDefault().getLanguage(), null);
		mapComponent.addMapInitializedListener(this);

		// "./dataToRead/Mar12/gowalla_spots_subset1_fromRaw28Feb2018smallerFileWithSampleWithTZ1.csv"
		setListOfMarkers(absFileNameForLatLonToReadAsMarker, delimiter, latColIndex, lonColIndex, labelColIndex);

		BorderPane bp = new BorderPane();
		ToolBar tb = new ToolBar();

		btnZoomIn = new Button("Zoom In");
		btnZoomIn.setOnAction(e ->
			{
				map.zoomProperty().set(map.getZoom() + 1);
			});
		btnZoomIn.setDisable(true);

		btnZoomOut = new Button("Zoom Out");
		btnZoomOut.setOnAction(e ->
			{
				map.zoomProperty().set(map.getZoom() - 1);
			});
		btnZoomOut.setDisable(true);

		lblZoom = new Label();
		lblCenter = new Label();
		lblClick = new Label();

		mapTypeCombo = new ComboBox<>();
		mapTypeCombo.setOnAction(e ->
			{
				map.setMapType(mapTypeCombo.getSelectionModel().getSelectedItem());
			});
		mapTypeCombo.setDisable(true);

		Button btnType = new Button("Map type");
		btnType.setOnAction(e ->
			{
				map.setMapType(MapTypeIdEnum.HYBRID);
			});

		// btnHideMarker = new Button("Hide Marker");
		// btnHideMarker.setOnAction(e ->
		// {
		// hideMarker();
		// });
		//
		// btnDeleteMarker = new Button("Delete Marker");
		// btnDeleteMarker.setOnAction(e ->
		// {
		// deleteMarker();
		// });

		tb.getItems().addAll(btnZoomIn, btnZoomOut, mapTypeCombo, new Label("Zoom: "), lblZoom, new Label("Center: "),
				lblCenter, new Label("Click: "), lblClick/* , btnHideMarker, btnDeleteMarker */);

		bp.setTop(tb);

		bp.setCenter(mapComponent);
		return bp;
	}

	public List<Triple<Double, Double, String>> getListOfMarkers()
	{
		return listOfMarkers;
	}

	/**
	 * 
	 * @param absFileNameForLatLong
	 * @param delimiter
	 * @param latColIndex
	 * @param lonColIndex
	 * @param labelColIndex
	 */
	public void setListOfMarkers(String absFileNameForLatLong, String delimiter, int latColIndex, int lonColIndex,
			int labelColIndex)
	{
		// String absFileNameForLatLong = ;

		List<List<String>> lines = ReadingFromFile.readLinesIntoListOfLists(absFileNameForLatLong, ",");
		listOfMarkers = new ArrayList<>();
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

			Triple<Double, Double, String> val = new Triple<>(Double.valueOf(line.get(latColIndex)),
					Double.valueOf(line.get(lonColIndex)), "id=" + line.get(labelColIndex));

			// LatLong markerLatLong2 = new LatLong(-1.6073826, 67.9382483);// 47.606189, -122.335842);
			// // Double.valueOf(line.get(2).substring(0, 4)));
			// // LatLong markerLatLong2 = new LatLong(Double.valueOf(line.get(3).substring(0, 4)),
			// // Double.valueOf(line.get(2).substring(0, 4)));
			// System.out.println("LatLong= " + markerLatLong2.toString());
			// markerOptions2.position(markerLatLong2).title(line.get(1)).visible(true);
			// System.out.println("here2");
			// myMarker2 = new Marker(markerOptions2);
			// System.out.println("here3");
			listOfMarkers.add(val);
		}

		// StringBuilder sb = new StringBuilder();
		// listOfMarkers.stream().forEachOrdered(e -> sb.append(e.toString() + "\n"));
		// System.out.println("List of markers= " + sb.toString());
	}

	@Override
	public void mapInitialized()
	{

		// System.out.println("MainApp.mapInitialised....");

		// Once the map has been loaded by the Webview, initialize the map details.
		LatLong center = new LatLong(-1.6073826, 67.9382483);// 47.606189, -122.335842);
		mapComponent.addMapReadyListener(() ->
			{
				// This call will fail unless the map is completely ready.
				checkCenter(center);
			});

		MapOptions options = new MapOptions();
		options.center(center).mapMarker(true).zoom(3).overviewMapControl(false).panControl(false).rotateControl(false)
				.scaleControl(false).streetViewControl(false).zoomControl(false).mapType(MapTypeIdEnum.TERRAIN)
				.clickableIcons(false).disableDefaultUI(true).disableDoubleClickZoom(true).keyboardShortcuts(false)
				.styleString(
						"[{'featureType':'landscape','stylers':[{'saturation':-100},{'lightness':65},{'visibility':'on'}]},{'featureType':'poi','stylers':[{'saturation':-100},{'lightness':51},{'visibility':'simplified'}]},{'featureType':'road.highway','stylers':[{'saturation':-100},{'visibility':'simplified'}]},{\"featureType\":\"road.arterial\",\"stylers\":[{\"saturation\":-100},{\"lightness\":30},{\"visibility\":\"on\"}]},{\"featureType\":\"road.local\",\"stylers\":[{\"saturation\":-100},{\"lightness\":40},{\"visibility\":\"on\"}]},{\"featureType\":\"transit\",\"stylers\":[{\"saturation\":-100},{\"visibility\":\"simplified\"}]},{\"featureType\":\"administrative.province\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"water\",\"elementType\":\"labels\",\"stylers\":[{\"visibility\":\"on\"},{\"lightness\":-25},{\"saturation\":-100}]},{\"featureType\":\"water\",\"elementType\":\"geometry\",\"stylers\":[{\"hue\":\"#ffff00\"},{\"lightness\":-25},{\"saturation\":-97}]}]");

		// [{\"featureType\":\"landscape\",\"stylers\":[{\"saturation\":-100},{\"lightness\":65},{\"visibility\":\"on\"}]},{\"featureType\":\"poi\",\"stylers\":[{\"saturation\":-100},{\"lightness\":51},{\"visibility\":\"simplified\"}]},{\"featureType\":\"road.highway\",\"stylers\":[{\"saturation\":-100},{\"visibility\":\"simplified\"}]},{\"featureType\":\"road.arterial\",\"stylers\":[{\"saturation\":-100},{\"lightness\":30},{\"visibility\":\"on\"}]},{\"featureType\":\"road.local\",\"stylers\":[{\"saturation\":-100},{\"lightness\":40},{\"visibility\":\"on\"}]},{\"featureType\":\"transit\",\"stylers\":[{\"saturation\":-100},{\"visibility\":\"simplified\"}]},{\"featureType\":\"administrative.province\",\"stylers\":[{\"visibility\":\"off\"}]},{\"featureType\":\"water\",\"elementType\":\"labels\",\"stylers\":[{\"visibility\":\"on\"},{\"lightness\":-25},{\"saturation\":-100}]},{\"featureType\":\"water\",\"elementType\":\"geometry\",\"stylers\":[{\"hue\":\"#ffff00\"},{\"lightness\":-25},{\"saturation\":-97}]}]
		map = mapComponent.createMap(options, false);
		directions = mapComponent.getDirec();

		map.setHeading(123.2);
		// System.out.println("Heading is: " + map.getHeading() );

		// MarkerOptions markerOptions = new MarkerOptions();
		// LatLong markerLatLong = new LatLong(-1.6073826, 67.9382483);// 47.606189, -122.335842);
		// markerOptions.position(markerLatLong).title("My new Marker").icon("mymarker.png").animation(Animation.DROP)
		// .visible(true);
		//
		// final Marker myMarker = new Marker(markerOptions);
		//
		// markerOptions2 = new MarkerOptions();
		// LatLong markerLatLong2 = new LatLong(47.906189, -122.335842);
		// markerOptions2.position(markerLatLong2).title("My new Marker").visible(true);
		//
		// myMarker2 = new Marker(markerOptions2);
		//
		// map.addMarker(myMarker);
		// map.addMarker(myMarker2);

		List<Triple<Double, Double, String>> markersToShow = getListOfMarkers();

		for (Triple<Double, Double, String> markerVal : markersToShow)
		{
			LatLong ll = new LatLong(markerVal.getFirst(), markerVal.getSecond());
			MarkerOptions mo = new MarkerOptions();

			if (useCustomMarker)
			{
				mo.position(ll).title(markerVal.getThird()).visible(true).icon(MarkerImageFactory.createMarkerImage(
						"file:///home/gunjan/git/GeolifeReloaded2_1_cleaned/dataToRead/Icons/markerIconSquare2.png",
						"png"));
			}
			else
			{
				mo.position(ll).title(markerVal.getThird()).visible(true);
			}
			// .icon(
			// "'https://developers.google.com/maps/documentation/javascript/examples/full/images/beachflag.png'");

			map.addMarker(new Marker(mo));
		}
		// markersToShow.stream().forEach(m -> map.addMarker(m));
		// InfoWindowOptions infoOptions = new InfoWindowOptions();
		// infoOptions.content("<h2>Here's an info window</h2><h3>with some info</h3>").position(center);
		//
		// InfoWindow window = new InfoWindow(infoOptions);
		// window.open(map, myMarker);
		map.fitBounds(new LatLongBounds(new LatLong(30, 120), center));
		// System.out.println("Bounds : " + map.getBounds());

		lblCenter.setText(map.getCenter().toString());
		map.centerProperty().addListener((ObservableValue<? extends LatLong> obs, LatLong o, LatLong n) ->
			{
				lblCenter.setText(n.toString());
			});

		lblZoom.setText(Integer.toString(map.getZoom()));
		map.zoomProperty().addListener((ObservableValue<? extends Number> obs, Number o, Number n) ->
			{
				lblZoom.setText(n.toString());
			});

		// map.addStateEventHandler(MapStateEventType.center_changed, () -> {
		// System.out.println("center_changed: " + map.getCenter());
		// });
		// map.addStateEventHandler(MapStateEventType.tilesloaded, () -> {
		// System.out.println("We got a tilesloaded event on the map");
		// });

		map.addUIEventHandler(UIEventType.click, (JSObject obj) ->
			{
				LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
				// System.out.println("LatLong: lat: " + ll.getLatitude() + " lng: " + ll.getLongitude());
				lblClick.setText(ll.toString());
			});

		btnZoomIn.setDisable(false);
		btnZoomOut.setDisable(false);
		mapTypeCombo.setDisable(false);

		mapTypeCombo.getItems().addAll(MapTypeIdEnum.ALL);

		// LatLong[] ary = new LatLong[] { markerLatLong, markerLatLong2 };
		// MVCArray mvc = new MVCArray(ary);
		//
		// PolylineOptions polyOpts = new PolylineOptions().path(mvc).strokeColor("red").strokeWeight(2);
		//
		// Polyline poly = new Polyline(polyOpts);
		// map.addMapShape(poly);
		// map.addUIEventHandler(poly, UIEventType.click, (JSObject obj) ->
		// {
		// LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
		// // System.out.println("You clicked the line at LatLong: lat: " + ll.getLatitude() + " lng: " +
		// // ll.getLongitude());
		// });

		// LatLong poly1 = new LatLong(47.429945, -122.84363);
		// LatLong poly2 = new LatLong(47.361153, -123.03040);
		// LatLong poly3 = new LatLong(47.387193, -123.11554);
		// LatLong poly4 = new LatLong(47.585789, -122.96722);
		// LatLong[] pAry = new LatLong[] { poly1, poly2, poly3, poly4 };
		// MVCArray pmvc = new MVCArray(pAry);

		// PolygonOptions polygOpts = new
		// PolygonOptions().paths(pmvc).strokeColor("blue").strokeWeight(2).editable(false)
		// .fillColor("lightBlue").fillOpacity(0.5);
		//
		// Polygon pg = new Polygon(polygOpts);
		// map.addMapShape(pg);
		// map.addUIEventHandler(pg, UIEventType.click, (JSObject obj) ->
		// {
		// // polygOpts.editable(true);
		// pg.setEditable(!pg.getEditable());
		// });
		//
		// LatLong centreC = new LatLong(47.545481, -121.87384);
		// CircleOptions cOpts = new CircleOptions().center(centreC).radius(5000).strokeColor("green").strokeWeight(2)
		// .fillColor("orange").fillOpacity(0.3);
		//
		// Circle c = new Circle(cOpts);
		// map.addMapShape(c);
		// map.addUIEventHandler(c, UIEventType.click, (JSObject obj) ->
		// {
		// c.setEditable(!c.getEditable());
		// });
		//
		// LatLongBounds llb = new LatLongBounds(new LatLong(47.533893, -122.89856), new LatLong(47.580694,
		// -122.80312));
		// RectangleOptions rOpts = new RectangleOptions().bounds(llb).strokeColor("black").strokeWeight(2)
		// .fillColor("null");
		//
		// Rectangle rt = new Rectangle(rOpts);
		// map.addMapShape(rt);
		//
		// LatLong arcC = new LatLong(47.227029, -121.81641);
		// double startBearing = 0;
		// double endBearing = 30;
		// double radius = 30000;
		//
		// MVCArray path = ArcBuilder.buildArcPoints(arcC, startBearing, endBearing, radius);
		// path.push(arcC);
		//
		// Polygon arc = new Polygon(new PolygonOptions().paths(path).strokeColor("blue").fillColor("lightBlue")
		// .fillOpacity(0.3).strokeWeight(2).editable(false));
		//
		// map.addMapShape(arc);
		// map.addUIEventHandler(arc, UIEventType.click, (JSObject obj) ->
		// {
		// arc.setEditable(!arc.getEditable());
		// });

		// GeocodingService gs = new GeocodingService();

		// DirectionsService ds = new DirectionsService();
		// renderer = new DirectionsRenderer(true, map, directions);
		//
		// DirectionsWaypoint[] dw = new DirectionsWaypoint[2];
		// dw[0] = new DirectionsWaypoint("São Paulo - SP");
		// dw[1] = new DirectionsWaypoint("Juiz de Fora - MG");
		//
		// DirectionsRequest dr = new DirectionsRequest("Belo Horizonte - MG", "Rio de Janeiro - RJ",
		// TravelModes.DRIVING,
		// dw);
		// ds.getRoute(dr, this, renderer);

		// LatLong[] location = new LatLong[1];
		// location[0] = new LatLong(-19.744056, -43.958699);
		// LocationElevationRequest loc = new LocationElevationRequest(location);
		// ElevationService es = new ElevationService();
		// es.getElevationForLocations(loc, this);

	}

	private void checkCenter(LatLong center)
	{
		// System.out.println("Testing fromLatLngToPoint using: " + center);
		// Point2D p = map.fromLatLngToPoint(center);
		// System.out.println("Testing fromLatLngToPoint result: " + p);
		// System.out.println("Testing fromLatLngToPoint expected: " + mapComponent.getWidth()/2 + ", " +
		// mapComponent.getHeight()/2);
	}

	/**
	 * The main() method is ignored in correctly deployed JavaFX application. main() serves only as fallback in case the
	 * application can not be launched through deployment artifacts, e.g., in IDEs with limited FX support. NetBeans
	 * ignores main().
	 *
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args)
	{
		System.setProperty("java.net.useSystemProxies", "true");
		launch(args);
	}

	@Override
	public void elevationsReceived(ElevationResult[] results, ElevationStatus status)
	{
		if (status.equals(ElevationStatus.OK))
		{
			for (ElevationResult e : results)
			{
				System.out.println(" Elevation on " + e.getLocation().toString() + " is " + e.getElevation());
			}
		}
	}

	@Override
	public void geocodedResultsReceived(GeocodingResult[] results, GeocoderStatus status)
	{
		if (status.equals(GeocoderStatus.OK))
		{
			for (GeocodingResult e : results)
			{
				System.out.println(e.getVariableName());
				System.out.println("GEOCODE: " + e.getFormattedAddress() + "\n" + e.toString());
			}

		}

	}

	@Override
	public void directionsReceived(DirectionsResult results, DirectionStatus status)
	{
		if (status.equals(DirectionStatus.OK))
		{
			mapComponent.getMap().showDirectionsPane();
			System.out.println("OK");

			DirectionsResult e = results;
			GeocodingService gs = new GeocodingService();

			System.out.println("SIZE ROUTES: " + e.getRoutes().size() + "\n" + "ORIGIN: "
					+ e.getRoutes().get(0).getLegs().get(0).getStartLocation());
			// gs.reverseGeocode(e.getRoutes().get(0).getLegs().get(0).getStartLocation().getLatitude(),
			// e.getRoutes().get(0).getLegs().get(0).getStartLocation().getLongitude(), this);
			System.out.println("LEGS SIZE: " + e.getRoutes().get(0).getLegs().size());
			System.out.println("WAYPOINTS " + e.getGeocodedWaypoints().size());
			/*
			 * double d = 0; for(DirectionsLeg g : e.getRoutes().get(0).getLegs()){ d += g.getDistance().getValue();
			 * System.out.println("DISTANCE " + g.getDistance().getValue()); }
			 */
			try
			{
				System.out
						.println("Distancia total = " + e.getRoutes().get(0).getLegs().get(0).getDistance().getText());
			}
			catch (Exception ex)
			{
				System.out.println("ERRO: " + ex.getMessage());
			}
			System.out.println("LEG(0)");
			System.out.println(e.getRoutes().get(0).getLegs().get(0).getSteps().size());
			/*
			 * for(DirectionsSteps ds : e.getRoutes().get(0).getLegs().get(0).getSteps()){
			 * System.out.println(ds.getStartLocation().toString() + " x " + ds.getEndLocation().toString());
			 * MarkerOptions markerOptions = new MarkerOptions(); markerOptions.position(ds.getStartLocation())
			 * .title(ds.getInstructions()) .animation(Animation.DROP) .visible(true); Marker myMarker = new
			 * Marker(markerOptions); map.addMarker(myMarker); }
			 */
			System.out.println(renderer.toString());
		}
	}
}
