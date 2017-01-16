package com.viet.SmartQA;


import org.apache.tika.parser.html.DefaultHtmlMapper;

public class CustomizedHTMLMapper extends DefaultHtmlMapper{
	@Override
	public boolean isDiscardElement(String name) {
		//System.out.println("tag: "+name); //debug
		if ((name.equals("CODE") || name.equals("PRE") || name.equals("A"))) {
			SQA.points+=10; //give credits for posting code
			return true;
		}
		return false;
	}
}
