package com.viet.SmartQA;


import org.apache.tika.parser.html.DefaultHtmlMapper;

public class CustomizedHTMLMapper extends DefaultHtmlMapper{
	@Override
	public boolean isDiscardElement(String name) {
		//System.out.println("tag: "+name); //debug
		return (name.equals("CODE") || name.equals("PRE"));
	}
}
