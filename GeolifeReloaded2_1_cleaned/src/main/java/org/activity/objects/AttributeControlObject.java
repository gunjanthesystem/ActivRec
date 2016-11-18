package org.activity.objects;

import org.activity.util.HTMLStrings;
import org.activity.util.MetaBase;

/*
 * ControlObject is a class for objects which can control the sql 'where' query.
 * For example: start_time can be an object of this class with the hasOperatorSelection being true
 * 
 */
public class AttributeControlObject
{
	String name;
	boolean hasOperatorSelection; // decides whether operators can be selected
	String datatype;
	String hierarchy;
	String belongsToDimension;
	boolean hasClicked;
	
	// String selectedOperator;
	
	public AttributeControlObject()
	{
		
	}
	
	public AttributeControlObject(String name, String datatype, boolean hasOperatorSelection, String hierarchy, String belongsToDimension)
	{
		this.name = name.trim();
		this.datatype = datatype.trim();
		
		this.hasOperatorSelection = hasOperatorSelection;
		
		this.belongsToDimension = belongsToDimension;
		this.hasClicked = false;
		this.hierarchy = hierarchy;
		
		// this.selectedOperator="=";
		/*
		 * if(datatype.trim().equalsIgnoreCase("Varchar")) { this.operator = "="; }
		 * 
		 * else this.operator=operator.trim();
		 */
	}
	
	// public setSelectedOperator
	
	/*
	 * public static String getHTMLStringAggregationControlObject(String alias_UI_Name) { AttributeControlObject
	 * targetAttributeControlObject=MetaBase.identifyAttribute(alias_UI_Name.replace(' ','_'));
	 * 
	 * String htmlString=""; if(targetAttributeControlObject.getDatatype().equalsIgnoreCase("Varchar")) { htmlString="<div class=\"row marginise  "+
	 * targetAttributeControlObject.getName()+"AggregationAttributeSelectionRow\""+ // we have a missing > here but UI butting aligns better if its missing, need to correct it
	 * later "<div class=\"btn-group marginise\">"+ "<button type=\"button\" class=\"btn btn-info disabled flat-border\">"+ targetAttributeControlObject.getName().replace('_',' ')+
	 * "</button>"+ //"<select class=\"multiSelect\" multiple=\"multiple\" id=\""+targetAttributeControlObject.getName()+"VarcharSelection\">"+
	 * //HTMLStrings.getHTMLOptionValueVarchar(targetAttributeControlObject.getName(),targetAttributeControlObject.getBelongsToDimension())+ // "</select>"+
	 * " <button type='button' id = '"+targetAttributeControlObject.getName()+"DeleteButton'  class='btn deleter btn-mySmall btn-warning dropdown-toggle' >"+
	 * "<span class='glyphicon glyphicon-remove'></span>"+ "</button>"+ "</div>"+ "</div>" ; }
	 * 
	 * 
	 * ////
	 * 
	 * 
	 * htmlString=htmlString+"<script type=\"text/javascript\">"+ " $('#"+targetAttributeControlObject.getName()+"DeleteButton').click(function()"+ "{"+
	 * "$((this).parentNode).html('');"+ // $('#section1').removeClass().addClass('active breadcrumb')+
	 * "$('#"+targetAttributeControlObject.getName()+"AggregationMenuOption').removeClass()"+ //"alert($((this).parentNode).className);"+ "});"+ "</script>"; //%%
	 * System.out.println("html String for attribute controller="+htmlString);
	 * 
	 * return htmlString; }
	 */
	
	public static String getHTMLStringControlObject(String alias_UI_Name)
	{
		AttributeControlObject targetAttributeControlObject = MetaBase.identifyAttribute(alias_UI_Name.replace(' ', '_'));
		
		String htmlString = "";
		if (targetAttributeControlObject.getDatatype().equalsIgnoreCase("Varchar"))
		{
			htmlString = "<div class=\" marginise  " + targetAttributeControlObject.getName() + "AttributeSelectionRow\"" + // we have a missing > here but UI butting aligns better
																															// if its missing, need to correct it later
					"<div class=\"btn-group marginise\">" + "<button type=\"button\" class=\"btn btn-info disabled flat-border \">"
					+ targetAttributeControlObject.getName().replace('_', ' ') + "</button>"
					+ "<select class=\"multiSelect\" multiple=\"multiple\" id=\"" + targetAttributeControlObject.getName()
					+ "VarcharSelection\">"
					+ HTMLStrings.getHTMLOptionValueVarchar(targetAttributeControlObject.getName(),
							targetAttributeControlObject.getBelongsToDimension())
					+ "</select>" + " <button type='button' id = '" + targetAttributeControlObject.getName()
					+ "DeleteButton'  class='btn  deleter  btn-mySmall btn-warning dropdown-toggle' >"
					+ "<span class='glyphicon glyphicon-remove'></span>" + "</button>" + "</div>" + "</div>" +
					
					"<script>" + "$(document).ready(function() {$('.multiSelect').multiselect({"
					+ "includeSelectAllOption: true,enableFiltering: true,maxHeight: 200});});" +
					
					"</script>";
		}
		
		// //
		else if ((targetAttributeControlObject.getDatatype().equalsIgnoreCase("date")))
		{
			// controlObjects.add(new AttributeControlObject(targetAttributeControlObject.getName(),targetAttributeControlObject.getDatatype(),true));
			htmlString = "<div class=\"marginise  " + targetAttributeControlObject.getName() + "AttributeSelectionRow\"" + // we have a missing > here but UI butting aligns better
																															// if its missing, need to correct it later
					"<div class=\"btn-group marginise\">" + "<span class=\"btn-group\">"
					+ "<button type=\"button\" class=\"btn  btn-info disabled flat-border\">"
					+ targetAttributeControlObject.getName().replace('_', ' ') + "</button>" + "<span class=\"btn-group\">"
					+ "<button type=\"button\" class=\"btn btn-default dropdown-toggle flat-border" + targetAttributeControlObject.getName()
					+ "OperatorDisplay  \" data-toggle=\"dropdown\">" + "Op  <span class=\"caret\"></span>" + "</button>"
					+ "<ul class=\"dropdown-menu " + targetAttributeControlObject.getName() + "OperatorSelection\" id=\""
					+ targetAttributeControlObject.getName() + "OperatorSelection\"" + " role=\"menu\">" + "<li><a href=\"#\">=</a></li>"
					+ "<li><a href=\"#\">>=</a></li>" + "<li><a href=\"#\"><=</a></li>" + "<li><a href=\"#\">!=</a></li>" + "</ul>"
					+ "</span>" +
					
					"<span class=\"input-group date col-xs-2\" style=\"width: 130px;\" id=\"" + targetAttributeControlObject.getName()
					+ "TDateSelection\">" + "<span class=\"input-group-addon\">" + "<span class=\"glyphicon glyphicon-time\"></span>"
					+ "</span>" + "<input type=\"text\" class=\"form-control\" placeholder=\"mm/dd/yyyy\" />" + "</span>" + "</span>"
					+ "<button type='button' id = '" + targetAttributeControlObject.getName()
					+ "DeleteButton'  class='btn  deleter  btn-mySmall btn-warning dropdown-toggle' >"
					+ "<span class='glyphicon glyphicon-remove'></span>" + "</button>" + "</div>" + "</div>" +
					/*
					 * 
					 * htmlString="<div class=\"row marginise   "+ targetAttributeControlObject.getName()+"AttributeSelectionRow\""+ // we have a missing > here but UI butting
					 * aligns better if its missing, need to correct it later "<div class=\"btn-group marginise\">"+ "<span class=\"btn-group\">"+
					 * "<button type=\"button\" class=\"btn flat-border btn-info disabled\">"+ targetAttributeControlObject.getName().replace('_',' ')+ "</button>"+
					 * 
					 * "<span class=\"btn-group\">"+ "<button type=\"button\" class=\"btn flat-border btn-default dropdown-toggle "+
					 * targetAttributeControlObject.getName()+"OperatorDisplay \" data-toggle=\"dropdown\">"+ "Op  <span class=\"caret\"></span>"+ "</button>"+
					 * "<ul class=\"dropdown-menu "+targetAttributeControlObject.getName()+"OperatorSelection\" id=\""+targetAttributeControlObject.getName()+
					 * "OperatorSelection\""+" role=\"menu\">"+ "<li><a href=\"#\">=</a></li>"+ "<li><a href=\"#\">>=</a></li>"+ "<li><a href=\"#\"><=</a></li>"+
					 * "<li><a href=\"#\">!=</a></li>"+ "</ul>"+ "</span>"+ //end of btn-group
					 * 
					 * "<span class=\"input-group date col-xs-2\" style=\"width: 2 0px;\" id=\""+targetAttributeControlObject.getName()+"TDateSelection\">"+ //notes: there is a T
					 * added before DateSelection because apparently Dateselection gives an error of not disable time while TDate Selection works.
					 * "<span class=\"input-group-addon\">"+ "<span class=\"glyphicon glyphicon-time\"></span>"+ "</span>"+
					 * "<input type=\"text\" class=\"form-control\" placeholder=\"mm/dd/yyyy	\" />"+ "</span>"+ "</span>"+
					 * "<button type='button' id = '"+targetAttributeControlObject.getName()
					 * +"DeleteButton'  class='btn flat-border btn-mySmall deleter btn-warning dropdown-toggle' >"+ "<span class='glyphicon glyphicon-remove'></span>"+ "</button>"+
					 * "</div>"+
					 * 
					 * "</div>"+
					 */
					
					"</div>" +
					
					"<script type=\"text/javascript\">" + "$(function () {" + "$('#" + targetAttributeControlObject.getName()
					+ "TDateSelection').datetimepicker({pickTime: false, defaultDate:\"\"});" +
					// "var picker = $('#start_dateTDateSelection'l).data('datetimepicker');picker.setLocalDate(new Date(Date.UTC(2014, 2, 1, 0, 0));"+
					"});" +
					
					"</script>";
		}
		// / TO DO: open the calendar at a set default date
		
		else if ((targetAttributeControlObject.getDatatype().equalsIgnoreCase("time")))
		{
			// controlObjects.add(new AttributeControlObject(targetAttributeControlObject.getName(),targetAttributeControlObject.getDatatype(),true));
			htmlString = "<div class=\"marginise  " + targetAttributeControlObject.getName() + "AttributeSelectionRow\"" + // we have a missing > here but UI butting aligns better
																															// if its missing, need to correct it later
					"<div class=\"btn-group marginise\">" + "<span class=\"btn-group\">"
					+ "<button type=\"button\" class=\"btn btn-info disabled flat-border\">"
					+ targetAttributeControlObject.getName().replace('_', ' ') + "</button>" + "<span class=\"btn-group\">"
					+ "<button type=\"button\" class=\"btn btn-default dropdown-toggle flat-border" + targetAttributeControlObject.getName()
					+ "OperatorDisplay  \" data-toggle=\"dropdown\">" + "Op  <span class=\"caret\"></span>" + "</button>"
					+ "<ul class=\"dropdown-menu " + targetAttributeControlObject.getName() + "OperatorSelection\" id=\""
					+ targetAttributeControlObject.getName() + "OperatorSelection\"" + " role=\"menu\">" + "<li><a href=\"#\">=</a></li>"
					+ "<li><a href=\"#\">>=</a></li>" + "<li><a href=\"#\"><=</a></li>" + "<li><a href=\"#\">!=</a></li>" + "</ul>"
					+ "</span>" +
					
					"<span class=\"input-group date col-xs-2\" style=\"width: 120px;\" id=\"" + targetAttributeControlObject.getName()
					+ "TimeSelection\">" + "<span class=\"input-group-addon\">" + "<span class=\"glyphicon glyphicon-time\"></span>"
					+ "</span>" + "<input type=\"text\" class=\"form-control\" placeholder=\"hh:mm AM	\" />" + "</span>" + "</span>"
					+ "<button type='button' id = '" + targetAttributeControlObject.getName()
					+ "DeleteButton'  class='btn  deleter  btn-mySmall btn-warning dropdown-toggle' >"
					+ "<span class='glyphicon glyphicon-remove'></span>" + "</button>" + "</div>" + "</div>" +
					
					"<script type=\"text/javascript\">" + "$(function () {" + "$('#" + targetAttributeControlObject.getName()
					+ "TimeSelection').datetimepicker({pickDate: false, defaultDate:\"\"});" +
					
					// "var picker=('#"+targetAttributeControlObject.getName()+"TimeSelection').data('datetimepicker');"+
					// "picker.setDate(null);"+
					
					"});" +
					
					"</script>";
		}
		
		else
		// int or big decimal
		{
			htmlString = "<div class=\"marginise   " + targetAttributeControlObject.getName() + "AttributeSelectionRow\"" + // we have a missing > here but UI butting aligns better
																															// if its missing, need to correct it later
					"<div class=\"btn-group marginise\">" + "<span class=\"btn-group\">"
					+ "<button type=\"button\" class=\"btn btn-info disabled flat-border\">"
					+ targetAttributeControlObject.getName().replace('_', ' ') + "</button>" + "<span class=\"btn-group\">"
					+ "<button type=\"button\" class=\"btn btn-default dropdown-toggle flat-border "
					+ targetAttributeControlObject.getName() + "OperatorDisplay  \" data-toggle=\"dropdown\">"
					+ "Op  <span class=\"caret\"></span>" + "</button>" + "<ul class=\"dropdown-menu "
					+ targetAttributeControlObject.getName() + "OperatorSelection\" id=\"" + targetAttributeControlObject.getName()
					+ "OperatorSelection\"" + " role=\"menu\">" + "<li><a href=\"#\">=</a></li>" + "<li><a href=\"#\">>=</a></li>"
					+ "<li><a href=\"#\"><=</a></li>" + "<li><a href=\"#\">!=</a></li>" + "</ul>" + "</span>"
					+ "<span class=\"input-group date col-xs-2\" style=\"width: 55px;\" id=\"" + targetAttributeControlObject.getName()
					+ "IntDecimalSelection\">" +
					/*
					 * "<span class=\"input-group-addon\">"+ "<span class=\"glyphicon glyphicon-time\"></span>"+ "</span>"+
					 */
					"<input type=\"text\" class=\"form-control\" id=\"" + targetAttributeControlObject.getName() + "Selection\">"
					+ "</span>" + "</span>" + "<button type='button' id = '" + targetAttributeControlObject.getName()
					+ "DeleteButton'  class='btn btn-mySmall deleter btn-warning dropdown-toggle' >"
					+ "<span class='glyphicon glyphicon-remove'></span>" + "</button>" + "</div>" + "</div>"
					
			;
		}
		
		if (targetAttributeControlObject.getHasOperatorSelection())// !(targetAttributeControlObject.getDatatype().equalsIgnoreCase("Varchar")))//REPLACE WWITH HAS OP SELECTIOn
		{
			// this means it has operator
			htmlString = htmlString + "\n<script>" + "var " + targetAttributeControlObject.getName() + "SelectedOperator;" + "$(\"."
					+ targetAttributeControlObject.getName() + "OperatorSelection li a\").click(function()" + "{"
					+ "var selText = $(this).text();" + "" + // alert(\"operator selection\"+selText);
					"$(this).parents('.btn-group').find('." + targetAttributeControlObject.getName()
					+ "OperatorDisplay').html(selText+' <span class=\"caret\"></span>');" +
					
					targetAttributeControlObject.getName() + "SelectedOperator=selText;" + "});</script>";
		}
		
		htmlString = htmlString + "<script type=\"text/javascript\">" + " $('#" + targetAttributeControlObject.getName()
				+ "DeleteButton').click(function()" + "{" + "$((this).parentNode).remove();" + // html(null);"+
				// "console.log('parent node is'+(this).parentNode));"+
				// $('#section1').removeClass().addClass('active breadcrumb')+
				"$('#" + targetAttributeControlObject.getName() + "MenuOption').removeClass()" +
				// "alert($((this).parentNode).className);"+
				"});" + "</script>";
		// %% System.out.println("html String for attribute controller="+htmlString);
		
		return htmlString;
	}
	
	public void setBelongsToDimension(String dimensionName)
	{
		this.belongsToDimension = dimensionName;
		
	}
	
	public String getBelongsToDimension()
	{
		return this.belongsToDimension;
		
	}
	
	public void setHierarchy(String hierarchy)
	{
		this.hierarchy = hierarchy;
	}
	
	public void setHasClicked(boolean val)
	{
		this.hasClicked = val;
	}
	
	public boolean getHasClicked()
	{
		return this.hasClicked;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setHasOperatorSelection(boolean operatorSelection)
	{
		this.hasOperatorSelection = operatorSelection;
	}
	
	public void setHasOperatorSelection(String attributeType)
	{
		
		// %% System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZinside setting has operator selection for attribute"+ this.name+" , type="+attributeType);
		if (((attributeType.equalsIgnoreCase("int")) || (attributeType.equalsIgnoreCase("Time")) || (attributeType.equalsIgnoreCase("Date"))
				|| (attributeType.equalsIgnoreCase("Decimal"))))
		{
			this.hasOperatorSelection = true;
			// %% System.out.println("true");
		}
		if ((attributeType.equalsIgnoreCase("Varchar")) || (attributeType.equalsIgnoreCase("String")))
		{
			this.hasOperatorSelection = false;
			// %% System.out.println("false");
		}
	}
	
	public void setDatatype(String datatype)
	{
		this.datatype = datatype;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean getHasOperatorSelection()
	{
		// %% System.out.println("YYYYYYYYY has operator for"+this.getName()+"is "+this.hasOperatorSelection);
		return this.hasOperatorSelection;
	}
	
	public String getDatatype()
	{
		return datatype;
	}
	
}
