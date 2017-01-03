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
	private static String[] englishWords;
	
    public static void main( String[] args ) throws IOException
    {
    	getEnglishWords();
        Scanner sc= new Scanner(System.in);
        //Write text file address
        String path=sc.nextLine();
        
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
    //Get all grammatically corrected words
    private static void getEnglishWords() throws IOException {
    	Path filePath = Paths.get(WORDS_LIST_PATH);
    	Object[] holder=Files
    			.lines(filePath, StandardCharsets.UTF_8)
    			.toArray();
    	if (holder==null) return;
    	englishWords=new String[holder.length];
    	for (int i=0;i<holder.length;i++) {
    		englishWords[i]=(String)holder[i];
    		//System.out.println(englishWords[i]); //test
    	}
    }
}
