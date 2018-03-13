package jfxtras.styles.jmetro8;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Created by pedro_000 on 8/5/2015.
 */
public class SpinnerController implements Initializable
{
	@FXML
	private Spinner spinner1;
	@FXML
	private Spinner spinner2;
	@FXML
	private Spinner spinner3;
	@FXML
	private Spinner spinner4;
	@FXML
	private Spinner spinner5;
	@FXML
	private Spinner spinner6;
	@FXML
	private Spinner disabledSpinner;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		spinner1.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10));
		spinner2.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10));
		spinner3.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10));
		spinner4.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10));
		spinner5.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10));
		spinner6.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10));

		spinner6.getEditor().setAlignment(Pos.CENTER);
		disabledSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10));
	}
}
