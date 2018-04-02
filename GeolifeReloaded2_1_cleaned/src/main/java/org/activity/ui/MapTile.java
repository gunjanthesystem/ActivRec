//
// package org.activity.ui;
//
// import static java.lang.Math.floor;
//
// import java.util.LinkedList;
// import java.util.List;
// import java.util.logging.Logger;
//
// import com.gluonhq.impl.maps.BaseMap;
// import com.gluonhq.impl.maps.ImageRetriever;
//
// import javafx.beans.InvalidationListener;
// import javafx.beans.Observable;
// import javafx.beans.WeakInvalidationListener;
// import javafx.beans.property.ReadOnlyDoubleProperty;
// import javafx.scene.image.ImageView;
// import javafx.scene.layout.Region;
// import javafx.scene.transform.Scale;
//
/// **
// *
// */
// class MapTile extends Region
// {
//
// private static final Logger logger = Logger.getLogger(MapTile.class.getName());
//
// final int myZoom;
// final long i, j;
// private String host = "https://tiles.wmflabs.org/bw-mapnik/";// = "http://tile.openstreetmap.org/";// made
// // temporarily public by Gunjan
//
// final BaseMap baseMap;
// // a list of tiles that this tile is covering. In case the covered tiles are
// // not yet loaded, this tile will be rendered.
// final List<MapTile> coveredTiles = new LinkedList();
// /**
// * In most cases, a tile will be shown scaled. The value for the scale factor depends on the active zoom and the
// * tile-specific myZoom
// */
// final Scale scale = new Scale();
//
// public boolean isCovering()
// {
// return coveredTiles.size() > 0;
// }
//
// private final InvalidationListener zl = o -> calculatePosition();
// private ReadOnlyDoubleProperty progress;
//
// // final Image image;
// MapTile(BaseMap baseMap, int nearestZoom, long i, long j)
// {
// this.baseMap = baseMap;
// this.myZoom = nearestZoom;
// this.i = i;
// this.j = j;
// scale.setPivotX(0);
// scale.setPivotY(0);
// getTransforms().add(scale);
// debug("[JVDBG] load image [" + myZoom + "], i = " + i + ", j = " + j);
//
// ImageView iv = new ImageView();
// iv.setMouseTransparent(true);
// this.progress = ImageRetriever.fillImage(iv, myZoom, i, j);
//
// // Label l = new Label("Tile [" + myZoom + "], i = " + i + ", j = " + j);
// getChildren().addAll(iv);// ,l);
// this.progress.addListener(o ->
// {
// if (this.progress.get() == 1.)
// {
// debug("[JVDBG] got image [" + myZoom + "], i = " + i + ", j = " + j);
// this.setNeedsLayout(true);
// }
// });
// baseMap.zoom().addListener(new WeakInvalidationListener(zl));
// baseMap.translateXProperty().addListener(new WeakInvalidationListener(zl));
// baseMap.translateYProperty().addListener(new WeakInvalidationListener(zl));
// calculatePosition();
// this.setMouseTransparent(true);
// }
//
// boolean loading()
// {
// return !(progress.greaterThanOrEqualTo(1.)).get();
// }
//
// /**
// * The immutable zoomlevel for this tile.
// *
// * @return
// */
// int getZoomLevel()
// {
// return myZoom;
// }
//
// private void calculatePosition()
// {
// double currentZoom = baseMap.zoom().get();
// int visibleWindow = (int) floor(currentZoom + BaseMap.TIPPING);
// boolean visible = visibleWindow == myZoom || isCovering()
// || ((visibleWindow >= BaseMap.MAX_ZOOM) && (myZoom == BaseMap.MAX_ZOOM - 1));
// this.setVisible(visible);
// logger.fine("visible tile " + this + "? " + this.isVisible()
// + (this.isVisible() ? " covering? " + isCovering() : ""));
// double sf = Math.pow(2, currentZoom - myZoom);
// scale.setX(sf);
// scale.setY(sf);
// setTranslateX(256 * i * sf);
// setTranslateY(256 * j * sf);
// }
//
// @Override
// public String toString()
// {
// return "tile with z = " + myZoom + " [" + i + "," + j + "]";
// }
//
// /**
// * This tile is covering for the child tile that is still being loaded.
// *
// * @param child
// */
// void addCovering(MapTile child)
// {
// coveredTiles.add(child);
// InvalidationListener il = createProgressListener(child);
// // System.out.println("We have to cover, add "+il);
// child.progress.addListener(il);
// calculatePosition();
// }
//
// InvalidationListener createProgressListener(MapTile child)
// {
// return new InvalidationListener()
// {
// @Override
// public void invalidated(Observable o)
// {
// if (child.progress.get() >= 1.0d)
// {
// MapTile.this.coveredTiles.remove(child);
// child.progress.removeListener(this);
// }
// }
// };
// }
//
// public void debug(String s)
// {
// logger.fine("LOG " + System.currentTimeMillis() % 10000 + ": " + s);
// }
// }
