package com.opex.ansibletoki.parser.identifymodule;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.opex.ansibletoki.parser.identifyba.IdentifyBindingAttributes;
import com.opex.ansibletoki.utils.FileWalkHandler;
import com.opex.ansibletoki.utils.FileWalker;


public class IdentifyModule {
	private static final String PY_EXT = ".py";
	String sourceDirectory;
	String outputDirectory;
	
	FileWalkHandler handler = new  FileWalkHandler(){

		@Override
		public void handle(File file) {
			if(file.isFile() && file.toString().endsWith(PY_EXT)){
				convertTOKIModule(file);
				
			}
			else if(file.isDirectory()){
				String outputDirectory = createOutputDirectory(file);
				File outPutFile = new File(outputDirectory);
				if(!outPutFile.exists()){
					outPutFile.mkdir();
				}
			}
		}
		
	};
	
	public IdentifyModule(String sourceDirectory, String outputDirectory) {
		super();
		this.sourceDirectory = sourceDirectory;
		this.outputDirectory = outputDirectory;
	}
	

	public void identifyModules(){
		FileWalker fileWalker = new FileWalker();
		fileWalker.walk(sourceDirectory, handler);
		
	
	}
	
	public void convertTOKIModule(File inputFile){
		String description = null, module = null;
		try{
			try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			    String line;
			    boolean found = false;
			    while ((line = br.readLine()) != null) {
			    	line = line.trim();
			    	if(line.startsWith("short_description:")){
			    		description = line.split("n:")[1];
			    		description = description.trim();
			    	}
			    	if(line.trim().startsWith("module:")){
			    		module = line.split("ule:")[1];
			    		module = module.trim();
			    	}
			    	else if(line.startsWith("def ")){
			    		String method = extractMethod(line);
			    		if(!"main".equals(method)){
			    			found = true;
			    			createKI(method, inputFile, description, "ReadState");
			    			createKI(method, inputFile, description, "Created");
			    			createKI(method, inputFile, description, "Deleted");
			    		}
			    	}
			    }
			    if(!found){
			    	if(module != null)
			    		createKI(module, inputFile, description, "ReadState");
			    		createKI(module, inputFile, description, "Created");
			    		createKI(module, inputFile, description, "Deleted");
			    }
			}
			
		}
		catch(Exception e){
			System.out.println(inputFile);
			e.printStackTrace();
		}
		
	}
	
	private void createKI(String method, File inputFile, String description, String type) {
		try{
			method = method.toUpperCase().replace("_", "");
			Document dom = createXMLDom(method, inputFile, description, type);	
			IdentifyBindingAttributes condition = new IdentifyBindingAttributes(inputFile.toString(), dom);
			condition.identifyBA();
			writeToFile(method, inputFile, dom, type);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}


	private void writeToFile(String method, File inputFile, Document dom, String type) throws Exception {
		String path = createOutputDirectory(inputFile);
		int lastIndexOf = path.lastIndexOf("/");
		path = path.substring(0, lastIndexOf);
		path += "/__core__Ubuntu__"+method.toUpperCase().replace("_", "")+"__"+type+"__.xml";
		//path += ".xml";
		File file = new File(path);
		if(!file.exists()){
			String name = file.getParent();
			File parent = new File(name);
			if(!parent.exists()){
				parent.mkdir();
			}
			try{
				file.createNewFile();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        //transformer.transform(new DOMSource(doc), new StreamResult(System.out));
		//transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(dom), new StreamResult(writer));
		StringBuffer output = writer.getBuffer();		
		BufferedWriter bwr = new BufferedWriter(new FileWriter(file));		 
		bwr.write(output.toString());
		bwr.flush();
		bwr.close();
	
		
	}


	private String createOutputDirectory(File inputFile) {
		String path = inputFile.toString();
		path = path.replace(sourceDirectory, "");
		path = outputDirectory +  path;
		return path;
	}


	private Document createXMLDom(String method, File inputFile, String description, String type) throws Exception{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(this.getClass().getClassLoader().getResourceAsStream("KITemplate_"+type+".xml"));
		Element knowledgeItem = document.getDocumentElement();
		String name = inputFile.getName();
		name = name.replace(PY_EXT, "");
		name = name.toUpperCase();
		method = method.toUpperCase();
		String id = "OpexSoftware:"+name+":__core__Ubuntu__"+method+"__"+type+"__" ;
		knowledgeItem.setAttribute("ID", id);
		if(description != null){
			NodeList nodeList = knowledgeItem.getElementsByTagName("Title");
			Element e = (Element) nodeList.item(0);
			String title = "__core__Ubuntu__"+method+"__"+type+"__";
			e.setTextContent(title);
			
			String desc = "This KI searches for the DataBlock called "+title+" and "+  description + " and writes the current status back to the node.";
			
			nodeList =  knowledgeItem.getElementsByTagName("Description");
			e = (Element) nodeList.item(0);
			e.setTextContent(desc);
		}
		
		//document.getElementsByTagName("Log").item(0).setTextContent(id + ":\t${NODE:id}");
		return document;
	}


	private String extractMethod(String line) {
		String temp = line.replace("def", "");
		String method = temp.split("\\(")[0];
		return method.trim();
	}
	
	
	
	public static void main(String[] args) {
		IdentifyModule identifyModule = new IdentifyModule("/home/ganesh/work/ansible/ansible/lib/ansible/modules/core", "/home/ganesh/eclipse/TestNowSas1/AnsibleToKI/output");
		identifyModule.identifyModules();
	}
}


