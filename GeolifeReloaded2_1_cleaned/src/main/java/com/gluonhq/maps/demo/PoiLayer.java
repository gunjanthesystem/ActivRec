/*
 * Copyright (c) 2016, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.maps.demo;

import org.activity.io.WritingToFile;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Pair;

/**
 *
 * A layer that allows to visualise points of interest.
 */
public class PoiLayer extends MapLayer
{

	private final ObservableList<Pair<MapPoint, Node>> points = FXCollections.observableArrayList();

	public PoiLayer()
	{
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

		double numofPoint = points.size();
		System.out.println("-->> com.gluonhq.maps.demo.PoiLayer.layoutLayer() called() numofPoint= " + numofPoint);

		long tPart1 = 0, tPart2 = 0, tPart3 = 0;

		for (Pair<MapPoint, Node> candidate : points)
		{
			long temp1 = System.nanoTime();
			MapPoint point = candidate.getKey();
			Node icon = candidate.getValue();
			long temp2 = System.nanoTime();

			Point2D mapPoint = baseMap.getMapPoint(point.getLatitude(), point.getLongitude());
			long temp3 = System.nanoTime();
			icon.setVisible(true);
			icon.setTranslateX(mapPoint.getX());
			icon.setTranslateY(mapPoint.getY());
			long temp4 = System.nanoTime();

			tPart1 += (temp2 - temp1);
			tPart2 += (temp3 - temp2);
			tPart3 += (temp4 - temp3);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(tPart1 + "," + tPart2 + "," + tPart3 + "," + tPart1 / numofPoint + "," + tPart2 / numofPoint + ","
				+ tPart3 / numofPoint + "\n");
		System.out.println(sb.toString());
		WritingToFile.appendLineToFileAbsolute(sb.toString(), "mapPerformance1April.csv");

	}

}
