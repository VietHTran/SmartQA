package com.viet.SmartQA;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlMapper;
import org.apache.tika.sax.BodyContentHandler;
import org.joda.time.DateTime;
import org.xml.sax.SAXException;

/**
 * Hello world!
 *
 */
public class SQA 
{
	private static final String WORDS_LIST_PATH="wordlist.txt";
	private static final String LEVEL1_PATH="InappropriateWordsLvl1.txt";
	private static final String LEVEL2_PATH="InappropriateWordsLvl2.txt";
	//private static final String TEST_LIST_PATH="TextFiles/DictionaryTest.txt"; //test only
	//private static final String TEST_PATH="TextFiles/CommandLineTest.txt"; //test only
	private static String filePath;
	private static String[] englishWords; //If not found then lose points
	private static String[] lvl1Words; //Lose points
	private static String[] lvl2Words; //Immediately exit
	//private static String[] testWords;
	private static Pattern backticks=Pattern.compile("\\`+");
	private static Pattern hyperlink=Pattern.compile("\\[.+?\\][(].+?[)]");
	public static int points=30;
    public static void main( String[] args ) throws IOException
    {
//    	args=splitWhiteSpace(getStringFromFiles(TEST_PATH)[0]); //test only
//    	testWords=getStringFromFiles(TEST_LIST_PATH); //test only
    	englishWords=getStringFromFiles(WORDS_LIST_PATH);
    	lvl1Words=getStringFromFiles(LEVEL1_PATH);
    	lvl2Words=getStringFromFiles(LEVEL2_PATH);
    	handleCommands(args);
    	String fileContent;
        try {
        	fileContent=htmlFormatToText(filePath);
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
    		if (binarySearchWord(holder,lvl2Words)>-1){
    			printStatus("Rude words or characters",words[k]);
    			System.out.println("Rated: RTFM");
    			return;
    		}
    		//System.out.println("before after1: "+words[k]+" "+holder); //debug
    		if (binarySearchWord(holder,lvl1Words)>-1){
    			printStatus("Inappropriate words or characters",words[k]);
    			points-=5;
    			continue;
    		}
    		//System.out.println("before after2: "+words[k]+" "+holder); //debug
    		if (binarySearchWord(holder,englishWords)<=-1
    				&& holder.length()>=2 ){
    			printStatus("Grammar error",words[k]);
    			points--;
    		}
    	}
    	printResult();
    }
    
    private static void handleCommands(String[] args) throws IOException {
    	int length=args.length;
    	if (args==null || length==0) exitCommand("No argument found for this command");
    	String command=args[0];
    	if (command.equals("check")) {
    		checkArguments(args,2,"Use \"check <file path>\" command to check the appropriate level of the question");
    		filePath=args[1];
    		if (filePath.length()<4 ||!filePath.substring(filePath.length()-4, filePath.length()).equals(".txt")) exitCommand("Please type in path to file in .txt format");
    		return;
    	} else if (command.equals("help")) {
    		userGuide();
    		exitCommand("");
    	} else if (command.equals("add")) {
    		addNewWord(args);
    	} else if (command.equals("remove")) {
    		removeWord(args);
    	}
    }
    
    private static void removeWord(String[] args) {
    	checkArguments(args,3,"Use \"remove <dictionary code> <word>\" command to remove word from selected dictionary");
    	int dictCode=0;
		try {
			dictCode=Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			exitCommand("Invalid dictionary code");
		}
		if (dictCode>2 || dictCode<0) exitCommand("Invalid dictionary code");
		//if (dictCode>3 || dictCode<0) exitCommand("Invalid dictionary code"); //test only
		String[] selectedDictionary;
		String dictionaryName;
		String path;
		String word=getAlphabetOnly(args[2]);
		//System.out.println("Commands: "+args[0]+" "+args[1]+" "+args[2]+"\n"); //debug
		switch (dictCode) {
			case 1:
				selectedDictionary=lvl1Words;
				path=LEVEL1_PATH;
				dictionaryName="Inappropriate words list level 1";
				break;
			case 2:
				selectedDictionary=lvl2Words;
				path=LEVEL2_PATH;
				dictionaryName="Inappropriate words list level 2";
				break;
//			case 3: //test only
//				selectedDictionary=testWords;
//				path=TEST_LIST_PATH;
//				dictionaryName="Test dictionary";
//				break;
			default:
				selectedDictionary=englishWords;
				path=WORDS_LIST_PATH;
				dictionaryName="English dictionary";
		}
		int index=binarySearchWord(word,selectedDictionary);
		if (index>-1) {
			removeStringFromFile(selectedDictionary,index,word,path);
			exitCommand("Word \""+word+"\" is successfully removed from "+dictionaryName);
		} else {
			exitCommand("Word \""+word+"\" doesn't exist in "+dictionaryName);
		}
    }
    
    private static void addNewWord(String[] args) {
    	checkArguments(args,3,"Use \"add <dictionary code> <word>\" command to add new word to selected dictionary");
		int dictCode=0;
		try {
			dictCode=Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			exitCommand("Invalid dictionary code");
		}
		if (dictCode>2 || dictCode<0) exitCommand("Invalid dictionary code");
		//if (dictCode>3 || dictCode<0) exitCommand("Invalid dictionary code"); //test only
		String[] selectedDictionary;
		String dictionaryName;
		String path;
		String newWord=getAlphabetOnly(args[2]);
		//System.out.println("Commands: "+args[0]+" "+args[1]+" "+args[2]+"\n"); //debug
		switch (dictCode) {
			case 1:
				selectedDictionary=lvl1Words;
				path=LEVEL1_PATH;
				dictionaryName="Inappropriate words list level 1";
				break;
			case 2:
				selectedDictionary=lvl2Words;
				path=LEVEL2_PATH;
				dictionaryName="Inappropriate words list level 2";
				break;
//			case 3: //test only
//				selectedDictionary=testWords;
//				path=TEST_LIST_PATH;
//				dictionaryName="Test dictionary";
//				break;
			default:
				selectedDictionary=englishWords;
				path=WORDS_LIST_PATH;
				dictionaryName="English dictionary";
		}
		int index=binarySearchWord(newWord,selectedDictionary);
		if (index>-1) {
			exitCommand(dictionaryName+" already contains word \""+args[2]+"\"");
		} else if (index==-1) {
			exitCommand("Invalid word");
		} else { 
			index*=-1;
			index-=newWord.compareTo(selectedDictionary[index-2])<0 ? 1:0; //file is base 1
			//System.out.println(index+": "+selectedDictionary[index-1]); //debug
			try {
				insertStringInFile(selectedDictionary,index, newWord, path);
			} catch (Exception e) {
				e.printStackTrace();
				exitCommand("");
			}
			exitCommand("New word \""+newWord+"\" is successfully inserted into "+dictionaryName);
		}
    }
    
    private static void removeStringFromFile(String[] dict, int line, String word, String filePath) {
    	try {
			PrintWriter writer = new PrintWriter(filePath,"UTF-8");
			for (int i=0;i<dict.length;i++) {
				if (line==i) {
					continue;
				}
				writer.println(dict[i]);
				writer.flush();
			}
			//System.out.println("test: "+filePath); //debug
			writer.close();
			System.out.println("Removing word from SQA.jar...");
			Runtime rt1 = Runtime.getRuntime();
			rt1.exec("zip -d SQA.jar "+filePath).waitFor();
			Runtime rt2 = Runtime.getRuntime();
			rt2.exec("jar uf SQA.jar "+filePath).waitFor();
			File writerFile=new File(filePath);
			writerFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
			exitCommand("Remove unsucessful");
		} catch (InterruptedException e) {
			e.printStackTrace();
			exitCommand("Remove unsucessful");
		}
    }
    
	private static void insertStringInFile(String[] dict, int line, String newWord, String filePath) {
		try {
			line--;
			PrintWriter writer = new PrintWriter(filePath,"UTF-8");
			for (int i=0;i<dict.length;i++) {
				if (line==i) {
					writer.println(newWord);
					writer.flush();
				}
				writer.println(dict[i]);
				writer.flush();
			}
			//System.out.println("test: "+filePath); //debug
			writer.close();
			System.out.println("Inserting new word to SQA.jar...");
			Runtime rt1 = Runtime.getRuntime();
			rt1.exec("zip -d SQA.jar "+filePath).waitFor();
			Runtime rt2 = Runtime.getRuntime();
			rt2.exec("jar uf SQA.jar "+filePath).waitFor();
			File writerFile=new File(filePath);
			writerFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
			exitCommand("Insert unsucessful");
		} catch (InterruptedException e) {
			e.printStackTrace();
			exitCommand("Insert unsucessful");
		}
	}
    
    private static void checkArguments(String[] args, int requiredArgs, String description) {
    	int length=args.length;
    	if (length>requiredArgs) exitCommand("Redundant argument");
    	if (length<requiredArgs) exitCommand(description);
    }
    
    private static void userGuide() {
    	System.out.println("check <txt path>: check the appropriate level of the question in the text file");
    	System.out.println("add <dictionary code> <word>: add new word to selected dictionary");
    	System.out.println("remove <dictionary code> <word>: remove a word out of the selected dictionary");
    	System.out.println("\tdictionary code: english dictionary[0], inappropriate words level 1[1], inappropriate words level 2[2]");
    }
    
    private static void exitCommand(String respond) {
    	System.out.println(respond);
    	System.exit(0);
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
            text=textHandler.toString()
            		.replaceAll("(\n)(\\p{Blank}{4,})(.[^\n]*)", " ")
            		.replaceAll("\\s"," ");
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
    	InputStream inp = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    	BufferedReader reader=new BufferedReader(new InputStreamReader(inp));
    	String line=null;
    	ArrayList<String> holder= new ArrayList<String>();
    	while ((line=reader.readLine())!=null) {
    		holder.add(line);
    	}
    	return holder.toArray(new String[0]);
    }
    
    private static int binarySearchWord(String word, String[] collections) {
    	if (word==null || word.length()<2 ||
    			collections==null || collections.length==0) return -1;
    	int left=0,right=collections.length-1,mid=1;
    	//System.out.println(word);
    	while (left<=right) {
    		mid=(left+right)>>1;
    		//System.out.println("mid word: "+collections[mid]);
    		int comp=word.compareTo(collections[mid]);
    		if (comp==0)
    			return mid;
    		else if (comp>0) //word is lexicographically larger than collections[i]
    			left=mid+1;
    		else //word is lexicographically smaller than collections[i]
    			right=mid-1;
    	}
    	return -1*(mid+2);
    }
    
    private static boolean isNumeric(String s) {  
        return s.matches("[-+]?\\d*\\.?\\d+");  
    }  
}
