package com.viet.SmartQA;


import org.apache.tika.parser.html.DefaultHtmlMapper;

public class CustomizedHTMLMapper extends DefaultHtmlMapper{
	@Override
	public boolean isDiscardElement(String name) {
		return (name.equals("CODE") || name.equals("PRE"));
	}
}
