
package com.gluonhq.maps.demo;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Pair;

/**
 *
 * A layer that allows to visualise points of interest.
 * 
 * @since June 23 2019
 */
public class PoiLayerPolygon extends MapLayer
{

	private final ObservableList<Pair<MapPoint, Node>> points = FXCollections.observableArrayList();
	private final Polygon polygon;

	// public PoiLayerPolygon()
	// {
	// }

	public PoiLayerPolygon()
	{
		polygon = new Polygon();
		polygon.setStroke(Color.RED);
		polygon.setFill(Color.rgb(255, 0, 0, 0.5));
		this.getChildren().add(polygon);
	}

	public void addPoint(MapPoint p, Node icon)
	{
		points.add(new Pair(p, icon));
		this.getChildren().add(icon);
		this.markDirty();
	}

	@Override
	protected void layoutLayer()
	{

		polygon.getPoints().clear();
		for (Pair<MapPoint, Node> candidate : points)
		{
			MapPoint point = candidate.getKey();
			Node icon = candidate.getValue();
			Point2D mapPoint = baseMap.getMapPoint(point.getLatitude(), point.getLongitude());
			icon.setTranslateX(mapPoint.getX());
			icon.setTranslateY(mapPoint.getY());

			polygon.getPoints().addAll(mapPoint.getX(), mapPoint.getY());
		}
	}
	// double numofPoint = points.size();
	// System.out.println("-->> com.gluonhq.maps.demo.PoiLayer.layoutLayer() called() numofPoint= " + numofPoint);
	//
	// long tPart1 = 0, tPart2 = 0, tPart3 = 0;
	//
	// for (Pair<MapPoint, Node> candidate : points)
	// {
	// long temp1 = System.nanoTime();
	// MapPoint point = candidate.getKey();
	// Node icon = candidate.getValue();
	// long temp2 = System.nanoTime();
	//
	// Point2D mapPoint = baseMap.getMapPoint(point.getLatitude(), point.getLongitude());
	// long temp3 = System.nanoTime();
	// icon.setVisible(true);
	// icon.setTranslateX(mapPoint.getX());
	// icon.setTranslateY(mapPoint.getY());
	// long temp4 = System.nanoTime();
	//
	// tPart1 += (temp2 - temp1);
	// tPart2 += (temp3 - temp2);
	// tPart3 += (temp4 - temp3);
	// }
	//
	// StringBuilder sb = new StringBuilder();
	// sb.append(tPart1 + "," + tPart2 + "," + tPart3 + "," + tPart1 / numofPoint + "," + tPart2 / numofPoint + ","
	// + tPart3 / numofPoint + "\n");
	// System.out.println(sb.toString());
	// WToFile.appendLineToFileAbs(sb.toString(), "mapPerformance1April.csv");

}
