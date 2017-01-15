package com.viet.SmartQA;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlMapper;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 * Hello world!
 *
 */
public class SQA 
{
	private static final String WORDS_LIST_PATH="TextFiles/wordlist.txt";
	private static final String LEVEL1_PATH="TextFiles/InappropriateWordsLvl1.txt";
	private static final String LEVEL2_PATH="TextFiles/InappropriateWordsLvl2.txt";
	private static final String TEST_PATH="TextFiles/IntegrationTests/5.txt";
	private static String[] englishWords; //If not found then lose points
	private static String[] lvl1Words; //Lose points
	private static String[] lvl2Words; //Immediately exit
	private static Pattern backticks=Pattern.compile("\\`+");
	private static Pattern hyperlink=Pattern.compile("\\[.+?\\][(].+?[)]");
	public static int points=30;
    public static void main( String[] args ) throws IOException
    {
    	englishWords=getStringFromFiles(WORDS_LIST_PATH);
    	lvl1Words=getStringFromFiles(LEVEL1_PATH);
    	lvl2Words=getStringFromFiles(LEVEL2_PATH);
    	String fileContent;
        try {
        	fileContent=htmlFormatToText(TEST_PATH);
        }catch (Exception e) {
        	System.out.println("Error 404: File not found");
        	return;
        }
        fileContent=trimText(fileContent);
        if (fileContent==null || fileContent.length()==0) printResult();
        String[] words= splitWhiteSpace(fileContent);
    	if (words==null || words.length==0) printResult();
    	for (int k=0;k<words.length;k++) {
    		if (words[k]==null 
    				|| words[k].equals("")
    				||isNumeric(words[k])) continue;
    		//Create a copy of word and leave out all none-alphabetic characters
    		String holder=getAlphabetOnly(words[k]);
    		//System.out.println("before after0: "+words[k]+" "+holder); //debug
    		if (binarySearchWord(holder,lvl2Words)){
    			printStatus("Rude words or characters",words[k]);
    			System.out.println("Rated: RTFM");
    			return;
    		}
    		//System.out.println("before after1: "+words[k]+" "+holder); //debug
    		if (binarySearchWord(holder,lvl1Words)){
    			printStatus("Inappropriate words or characters",words[k]);
    			points-=5;
    			continue;
    		}
    		//System.out.println("before after2: "+words[k]+" "+holder); //debug
    		if (!binarySearchWord(holder,englishWords)
    				&& holder.length()>=2 ){
    			printStatus("Grammar error",words[k]);
    			points--;
    		}
    	}
    	printResult();
    }
    
    private static void printResult() {
    	System.out.println("Score: "+points);
        if (points<=10) System.out.println("Rated: RTFM");
        else if (points<=15) System.out.println("Rated: Average question");
        else System.out.println("Rated: Smart question");
        System.exit(0);
    }
    
    private static String trimText(String text) {
    	if (text==null || text.length()==0) return null;
    	StringBuilder builder=new StringBuilder(text);
    	checkHyperlink(builder);
    	checkCodeSnippet(builder);
    	return builder.toString();
    }
    
    private static InputStream wrapTextWithHTMLFormat(InputStream content) {
    	String beginning = "<!DOCTYPE html>\n<html>";
    	String end = "\n</html>";
    	List<InputStream> streams = Arrays.asList(
    	    new ByteArrayInputStream(beginning.getBytes()),
    	    content,
    	    new ByteArrayInputStream(end.getBytes()));
    	InputStream result = new SequenceInputStream(Collections.enumeration(streams));
    	return result;
    }
    
    private static String htmlFormatToText(String path) {
    	String text="";
    	try {
            InputStream input=wrapTextWithHTMLFormat(new FileInputStream(new File(path)));
            BodyContentHandler textHandler = new BodyContentHandler();
            
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            ParseContext context = new ParseContext();
            context.set(HtmlMapper.class, new CustomizedHTMLMapper());
            parser.parse(input, textHandler, metadata, context);
            //System.out.println("Before: " + textHandler.toString()); //debug
            text=textHandler.toString().replaceAll("(\n)[(\\p{Blank}{4,})(>)](.[^\n]*)", " ").replaceAll("\\s"," ");
            //System.out.println("Body: " + text); //debug
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TikaException e) {
        	e.printStackTrace();	
        }
    	return text;
    }
    
    //check if text contains hyperlink
    private static void checkHyperlink(StringBuilder text) {
    	Matcher hyperlinks=hyperlink.matcher(text);
    	ArrayList<int[]> range=new ArrayList<int[]>();
    	while (hyperlinks.find()) {
    		int[] holder=new int[2];
    		holder[0]=hyperlinks.start();
    		holder[1]=hyperlinks.end();
    		range.add(holder);
    		//System.out.println("hyperlink found: "+hyperlinks.group()); //debug
    	}
    	for (int i=range.size()-1;i>=0;i--) {
    		int[] holder=range.get(i);
    		text.delete(holder[0], holder[1]+1);
    		text.insert(holder[0], ' ');
    	}
    	//System.out.println("modified text: "+text); //debug
    }
    
    //check if the word is marked as code text
    private static void checkCodeSnippet(StringBuilder text) {
    	Matcher matcher = backticks.matcher(text);
    	int currentMax=0; //Current code snippet
    	ArrayList<int[]> range=new ArrayList<int[]>();
    	while (matcher.find()) {
    		//System.out.println(matcher.start()+" "+currentMax); //debug
    		if (matcher.start()<currentMax) {
    			matcher.group();
    			continue;
    		}
    		String result=matcher.group();
    		int count=result.length();
    		//System.out.println("count: "+count); //debug
    		Pattern next=Pattern.compile("\\`{"+count+"}(.+?)\\`{"+count+"}",Pattern.DOTALL); //Appear {count} times
    		Matcher closing=next.matcher(text.substring(matcher.start()));
    		if (closing.find()) {
    			int end=closing.end();
    			int start=matcher.start();
    			//System.out.println("code: "+closing.group()); //debug
    			currentMax=end+start;
    			int[] holder=new int[2];
    			holder[0]=start;
    			holder[1]=currentMax;
    			//System.out.println("range: "+holder[0]+" "+holder[1]); //debug
    			range.add(holder);
    		}
    	}
    	for (int i=range.size()-1;i>=0;i--) {
    		int[] holder=range.get(i);
    		text.delete(holder[0], holder[1]);
    		text.insert(holder[0],' ');
    	}
    	//System.out.println("fix: "+word); //debug
    }
    
    private static String getAlphabetOnly(String word) {
    	return word.toLowerCase().replaceAll("[^a-z]", "");
    }
    
    private static String[] splitWhiteSpace(String word) {
    	String[] holder;
    	try {
    		holder=word.split("\\s+");
    	} catch (NullPointerException e) {
    		return null;
    	}
    	return word.split("\\s+");
    }
    
    private static void printStatus(String statusType, String word) {
    	System.out.println(statusType+": "+word);
    }
    
    private static String[] getStringFromFiles(String path) 
    		throws IOException {
    	Path filePath = Paths.get(path);
    	Object[] holder=Files
    			.lines(filePath, StandardCharsets.UTF_8)
    			.toArray();
    	if (holder==null) return null;
    	String[] content=new String[holder.length];
    	for (int i=0;i<holder.length;i++) {
    		content[i]=(String)holder[i];
    		//System.out.println(content[i]); //test
    	}
    	return content;
    }
    
    private static boolean binarySearchWord(String word, String[] collections) {
    	if (word==null || word.length()<2 ||
    			collections==null || collections.length==0) return false;
    	int left=0,right=collections.length-1,mid;
    	//System.out.println(word);
    	while (left<=right) {
    		mid=(left+right)/2;
    		//System.out.println("mid word: "+collections[mid]);
    		int comp=word.compareTo(collections[mid]);
    		if (comp==0)
    			return true;
    		else if (comp>0) //word is lexicographically larger than collections[i]
    			left=mid+1;
    		else //word is lexicographically smaller than collections[i]
    			right=mid-1;
    	}
    	return false;
    }
    
    private static boolean isNumeric(String s) {  
        return s.matches("[-+]?\\d*\\.?\\d+");  
    }  
}
