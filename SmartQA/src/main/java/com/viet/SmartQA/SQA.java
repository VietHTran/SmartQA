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
	
    public static void main( String[] args ) throws IOException
    {
    	getStringFromFiles(WORDS_LIST_PATH,englishWords);
    	getStringFromFiles(LEVEL1_PATH,lvl1Words);
    	getStringFromFiles(LEVEL2_PATH,lvl2Words);
        //Write text file address
        String path="TextFiles/text.txt";
        
        //Get subject and content of the question
        Path filePath = Paths.get(path);
        try {
        	Stream<String> qLines=Files.lines(filePath, StandardCharsets.UTF_8);
        }catch (NoSuchFileException e) {
        	System.out.println("Error 404: File not found");
        	return;
        }
        
        //System.out.println(qLines.toArray()[0].toString());
    }
    
    private static void getStringFromFiles(String path, String[] content) 
    		throws IOException {
    	Path filePath = Paths.get(path);
    	Object[] holder=Files
    			.lines(filePath, StandardCharsets.UTF_8)
    			.toArray();
    	if (holder==null) return;
    	content=new String[holder.length];
    	for (int i=0;i<holder.length;i++) {
    		content[i]=(String)holder[i];
    		//System.out.println(content[i]); //test
    	}
    }
}
