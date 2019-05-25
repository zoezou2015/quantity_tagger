package org.statnlp.commons.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Standard Class for reading and printing
 * @author allan_jie
 *
 */
public class RAWF {

	public static BufferedReader reader(String path) throws IOException{
        return new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
	}

	public static PrintWriter writer(String path, boolean append) throws IOException{
	    return new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path,append),"UTF-8")));
	}

	public static PrintWriter writer(String path) throws IOException{
	    return new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path),"UTF-8")));
	}
	
	public static ObjectOutputStream objectWriter(String path) throws IOException {
		return new ObjectOutputStream(new FileOutputStream(new File(path)));
	}
	
	public static ObjectInputStream objectReader(String path) throws IOException {
		return new ObjectInputStream(new FileInputStream(new File(path)));
	}
}
