package com.opex.ansibletoki.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;


public class Utils {
	public static void convertTOKIModule(){
		InputStream fis = null;
		try{
			
			/*CoreModuleIdenfifier ansibleToKI = new CoreModuleIdenfifier(fis);
			ansibleToKI.parse();*/
			try (BufferedReader br = new BufferedReader(new FileReader(new File("/home/ganesh/eclipse/TestNow/AnsibleToKIParser/temp.py")))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			    	line = line.trim();
			    	if(line.startsWith("def")){
			    		String method = extractMethod(line);
			    		System.out.println(method);
			    	}
			    }
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(fis !=null){
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static String extractMethod(String line) {
		String temp = line.replace("def", "");
		String method = temp.split("(")[0];
		return method.trim();
	}

	public static void main(String[] args) {
		convertTOKIModule();
		
	}
		
}
