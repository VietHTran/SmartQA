package com.viet.SmartQA;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Hello world!
 *
 */
public class SQA 
{
	private static final String WORDS_LIST_PATH="TextFiles/wordlist.txt";
	private static final String LEVEL1_PATH="TextFiles/InappropriateWordsLvl1.txt";
	private static final String LEVEL2_PATH="TextFiles/InappropriateWordsLvl2.txt";
	private static final String TEST_PATH="TextFiles/SnippetTest.txt";
	private static String[] englishWords; //If not found then lose points
	private static String[] lvl1Words; //Lose points
	private static String[] lvl2Words; //Immediately exit
	private static String[] question;
	private static boolean isCode=false;
	private static boolean isSnippet=false;
	private static boolean isQuote=false;
	private static Pattern snippets=Pattern.compile("\\`+(.[^\\`]+?)\\`+");
	private static Pattern backticks=Pattern.compile("\\`+");
    public static void main( String[] args ) throws IOException
    {
    	englishWords=getStringFromFiles(WORDS_LIST_PATH);
    	lvl1Words=getStringFromFiles(LEVEL1_PATH);
    	lvl2Words=getStringFromFiles(LEVEL2_PATH);
    	int points=30;
    	
        try {
        	question=getStringFromFiles(TEST_PATH);
        }catch (NoSuchFileException e) {
        	System.out.println("Error 404: File not found");
        	return;
        }
        
        for (int i=0;i<question.length;i++) {
        	checkCodeSnippet(new StringBuilder (question[i]));
        	String[] words = question[i].split("\\s+");
        	if (words==null || words.length==0) continue;
        	for (int k=0;k<words.length;k++) {
        		checkBlockOfCode(words[k]);
        		if (words[k]==null 
        				|| words[k].equals("")
        				||isNumeric(words[k])) continue;
        		//Create a copy of word and leave out all none-alphabetic characters
        		String holder=words[k].toLowerCase()
        				.replaceAll("[^a-z]", "");
        		//([a-zA-Z]+?)(s\b|\b)
        		if (binarySearchWord(holder.toLowerCase(),lvl2Words)){
        			printStatus("Rude words or characters",words[k],i,k);
        			System.out.println("Rated: RTFM");
        			return;
        		}
        		//no need to check for grammar and inappropriate words in code
        		if (isCode) continue;
        		if (binarySearchWord(holder.toLowerCase(),lvl1Words)){
        			printStatus("Inappropriate words or characters",words[k],i,k);
        			points-=5;
        		}
        		if (!binarySearchWord(holder.toLowerCase(),englishWords)
        				&& holder.length()>=2 ){
        			printStatus("Grammar error",words[k],i,k);
        			points--;
        		}
        	}
        }
        System.out.println("Score: "+points);
        if (points<=10) System.out.println("Rated: RTFM");
        else if (points<=15) System.out.println("Rated: Average question");
        else System.out.println("Rated: Smart question");
    }
    
    private static void checkBlockOfCode(String word) {
    	if (word.equals("<code>") || word.equals("<pre>")) 
    		isCode=true; //inside code block
    	else if (word.equals("</code>") || word.equals("</pre>")) 
    		isCode=false; //no longer inside code block
    	//System.out.println(word+" "+isCode);
    }
    
    //check if the word is marked as code text
    private static void checkCodeSnippet(StringBuilder word) {
    	Matcher matcher = backticks.matcher(word);
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
    		Pattern next=Pattern.compile("\\`{"+count+"}(.+?)\\`{"+count+"}"); //Appear {count} times
    		Matcher closing=next.matcher(word.substring(matcher.start()));
    		if (closing.find()) {
    			int end=closing.end();
    			int start=matcher.start();
    			//System.out.println(closing.group()); //debug
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
    		word.delete(holder[0], holder[1]);
    		word.insert(holder[0]," ");
    	}
    	//System.out.println("fix: "+word); //debug
    }
    
    private static void printStatus(String statusType, String word, int x,int y) {
    	System.out.print(statusType+" at ["+x+":"+y+"]: ");
		System.out.println(word);
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
