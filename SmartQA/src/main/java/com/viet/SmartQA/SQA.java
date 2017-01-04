package com.viet.SmartQA;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
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
	private static String[] englishWords; //If not found then lose points
	private static String[] lvl1Words; //Lose points
	private static String[] lvl2Words; //Immediately exit
	private static String[] question;
	
    public static void main( String[] args ) throws IOException
    {
    	englishWords=getStringFromFiles(WORDS_LIST_PATH);
    	lvl1Words=getStringFromFiles(LEVEL1_PATH);
    	lvl2Words=getStringFromFiles(LEVEL2_PATH);
    	int points=30;
        try {
        	question=getStringFromFiles("TextFiles/text.txt");
        }catch (NoSuchFileException e) {
        	System.out.println("Error 404: File not found");
        	return;
        }
        
        for (int i=0;i<question.length;i++) {
        	String[] words = question[i].split(" ");
        	for (int k=0;k<words.length;k++) {
        		if (isNumeric(words[k])) continue;
        		//Create a copy of word and leave out all none-alphabetic characters
        		String holder=words[k].toLowerCase()
        				.replaceAll("[^a-z]", "");
        		if (binarySearchWord(holder.toLowerCase(),lvl2Words)){
        			printStatus("Rude words or characters",words[k],i,k);
        			System.out.println("Rated: RTFM");
        			return;
        		}
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
    	int l=0,r=collections.length-1,mid;
    	//System.out.println(word);
    	while (l<=r) {
    		mid=(l+r)/2;
    		//System.out.println("mid word: "+collections[mid]);
    		int comp=word.compareTo(collections[mid]);
    		if (comp==0)
    			return true;
    		else if (comp>0) //word is lexicographically larger than collections[i]
    			l=mid+1;
    		else //word is lexicographically smaller than collections[i]
    			r=mid-1;
    	}
    	return false;
    }
    
    private static boolean isNumeric(String s) {  
        return s.matches("[-+]?\\d*\\.?\\d+");  
    }  
}
