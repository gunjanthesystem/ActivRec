package org.activity.plotting;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * Inspired from TooltipContent
 */
public class GTooltipContent extends GridPane
{
	// private Label label1 = new Label();
	// private Label label2 = new Label();
	// private Label label3 = new Label();
	// private Label label4 = new Label();

	private List<Label> labels = new ArrayList<>();
	private List<Label> labelHeaders = new ArrayList<>();

	GTooltipContent(List<String> labelHeadersString)
	{
		for (int i = 0; i < labelHeadersString.size(); i++)
		// String labelHeaderString : labelHeadersString)
		{
			Label l = new Label();
			Label lh = new Label(labelHeadersString.get(i));

			setConstraints(l, 1, i);
			setConstraints(lh, 0, i);

			// l.getStyleClass().add("activity-tooltip-label");
			// lh.getStyleClass().add("activity-tooltip-label");

			labels.add(l);
			labelHeaders.add(lh);
		}

		// setConstraints(label1Header, 0, 0);
		// setConstraints(label1, 1, 0);
		// setConstraints(label2Header, 0, 1);
		// setConstraints(label2, 1, 1);
		// setConstraints(label3Header, 0, 2);
		// setConstraints(label3, 1, 2);
		// setConstraints(label4Header, 0, 3);
		// setConstraints(label4, 1, 3);

		getChildren().addAll(labels);// label1Header, label1, label2Header, label2, label3Header, label3, label4Header,
		getChildren().addAll(labelHeaders); // label4);
	}

	// GTooltipContent()
	// {
	// Label label1Header = new Label("EndTS:");
	// Label label2Header = new Label("ActName:");
	// Label label3Header = new Label("HIGH:");
	// Label label4Header = new Label("LOW:");
	//
	// label1Header.getStyleClass().add("candlestick-tooltip-label");
	// label2Header.getStyleClass().add("candlestick-tooltip-label");
	// label3Header.getStyleClass().add("candlestick-tooltip-label");
	// label4Header.getStyleClass().add("candlestick-tooltip-label");
	//
	// setConstraints(label1Header, 0, 0);
	// setConstraints(label1, 1, 0);
	// setConstraints(label2Header, 0, 1);
	// setConstraints(label2, 1, 1);
	// setConstraints(label3Header, 0, 2);
	// setConstraints(label3, 1, 2);
	// setConstraints(label4Header, 0, 3);
	// setConstraints(label4, 1, 3);
	//
	// getChildren().addAll(label1Header, label1, label2Header, label2, label3Header, label3, label4Header, label4);
	// }

	// public void update(double endTS, String actName, double high, double low)
	// {
	// label1.setText(Double.toString(endTS));
	// label2.setText(actName);
	// label3.setText(Double.toString(high));
	// label4.setText(Double.toString(low));
	// }

	public void update(String s1, String... others)
	{
		if (1 + others.length != labels.size())
		{
			System.out.println("Warning: 1+others.length!=labels.size()");
		}
		labels.get(0).setText(s1);

		for (int i = 0; i < others.length - 1; i++)
		{
			labels.get(i + 1).setText(others[i]);
		}
		// label1.setText
		// label1.setText(endTS);
		// label2.setText(actName);
		// label3.setText(high);
		// label4.setText(low);
	}

	// /**
	// *
	// * @param endTS
	// * @param actName
	// * @param high
	// * @param low
	// */
	// public void update(String endTS, String actName, String high, String low)
	// {
	// label1.setText(endTS);
	// label2.setText(actName);
	// label3.setText(high);
	// label4.setText(low);
	// }
}
