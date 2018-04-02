
package com.gluonhq.maps.demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;

import it.unimi.dsi.fastutil.doubles.Double2DoubleOpenHashMap;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Pair;

/**
 *
 * A layer that allows to visualise points of interest.
 */
public class PoiLayer2Faster extends MapLayer
{

	// private final ObservableList<Pair<MapPoint, Node>> points = FXCollections.observableArrayList();

	private List<Pair<MapPoint, Node>> points;
	private Double2DoubleOpenHashMap latPrecomputedValForRendering;

	public PoiLayer2Faster(int numOfPoints)
	{
		points = new ArrayList<>(numOfPoints);
		latPrecomputedValForRendering = new Double2DoubleOpenHashMap(numOfPoints);

	}

	public void addPoint(MapPoint p, Node icon)
	{
		icon.setVisible(true);
		points.add(new Pair<MapPoint, Node>(p, icon));
		this.getChildren().add(icon);
		this.markDirty();

		//// precompute this val for lat for performance gain as it is needed in BaseMap.getMapPoint() for updating
		//// layout
		double latitude = p.getLatitude();
		double lat_rad = Math.PI * latitude / 180;
		double preCompLatValForUpdating = (1
				- (FastMath.log(FastMath.tan(lat_rad) + 1 / FastMath.cos(lat_rad)) / Math.PI)) / 2;
		latPrecomputedValForRendering.put(latitude, preCompLatValForUpdating);
		////

	}

	@Override
	protected void layoutLayer()
	{

		double numofPoint = points.size();
		// System.out.println("-->> com.gluonhq.maps.demo.PoiLayer.layoutLayer() called() numofPoint= " + numofPoint);

		// long tPart1 = 0, tPart2 = 0, tPart3 = 0;

		for (Pair<MapPoint, Node> candidate : points)
		{
			// long temp1 = System.nanoTime();

			MapPoint point = candidate.getKey();
			Node icon = candidate.getValue();
			icon.setVisible(true);
			// long temp2 = System.nanoTime();

			// Point2D mapPoint = baseMap.getMapPoint(point.getLatitude(), point.getLongitude());
			// Point2D mapPoint = baseMap.getMapPointFasterV1(point.getLatitude(), point.getLongitude());
			Point2D mapPoint = baseMap.getMapPointFasterV1(point.getLatitude(), point.getLongitude(),
					latPrecomputedValForRendering.get(point.getLatitude()));

			// long temp3 = System.nanoTime();

			// icon.setVisible(true);
			icon.setTranslateX(mapPoint.getX());
			icon.setTranslateY(mapPoint.getY());

			// long temp4 = System.nanoTime();
			//
			// tPart1 += (temp2 - temp1);
			// tPart2 += (temp3 - temp2);
			// tPart3 += (temp4 - temp3);
		}

		// StringBuilder sb = new StringBuilder();
		// sb.append(tPart1 + "," + tPart2 + "," + tPart3 + "," + tPart1 / numofPoint + "," + tPart2 / numofPoint + ","
		// + tPart3 / numofPoint + "\n");
		// System.out.println(sb.toString());
		// WritingToFile.appendLineToFileAbsolute(sb.toString(), "mapPerformance1April_Faster4.csv");

	}

}
