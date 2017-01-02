package com.viet.SmartQA;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Hello world!
 *
 */
public class SQA 
{
    public static void main( String[] args ) throws IOException
    {
        Scanner sc= new Scanner(System.in);
        String path=sc.nextLine();
        Path filePath = Paths.get(path);
        Stream<String> lines=Files.lines(filePath, StandardCharsets.UTF_8);
        System.out.println(lines.toArray()[0]);
    }
}
