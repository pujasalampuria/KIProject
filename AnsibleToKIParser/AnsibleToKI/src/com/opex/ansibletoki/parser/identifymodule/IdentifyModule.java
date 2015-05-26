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
import org.w3c.dom.NamedNodeMap;
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
			    	if(module != null) {
			    		createKI(module, inputFile, description, "ReadState");
			    		createKI(module, inputFile, description, "Created");
			    		createKI(module, inputFile, description, "Deleted");
			    	}
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
		int lastIndexOf = path.lastIndexOf(File.separator);
		path = path.substring(0, lastIndexOf);
		path += File.separator+"__core__Ubuntu__"+method.toUpperCase().replace("_", "")+"__"+type+"__.xml";
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
		String title = "__core__Ubuntu__"+method+"__"+type+"__";
		if(description != null){
			NodeList nodeList = knowledgeItem.getElementsByTagName("Title");
			Element e = (Element) nodeList.item(0);
			e.setTextContent(title);
			String desc = "This KI searches for the DataBlock called "+title+" and "+  description.toLowerCase() + " and writes the current status back to the node.";
			nodeList =  knowledgeItem.getElementsByTagName("Description");
			e = (Element) nodeList.item(0);
			e.setTextContent(desc);
		}
		
		//update create trigger description
		NodeList createTriggerNodeList = knowledgeItem.getElementsByTagName("CreateTrigger");
		if(createTriggerNodeList != null) {
			Element createTriggerElement = (Element)createTriggerNodeList.item(0);
			if(createTriggerElement != null) {
				NodeList issueConditionNodeList = createTriggerElement.getElementsByTagName("IssueCondition");
				if(issueConditionNodeList != null) {
					Element issueConditionElement = (Element) issueConditionNodeList.item(0);
					if(issueConditionElement != null) {
						NodeList descriptionNodeList = issueConditionElement.getElementsByTagName("Description");
						String desc1 = "Checks Issue for DataBlock " + title + " and attribute ExtUID being the same as in the Node the KI binds to.";
						Element e = (Element) descriptionNodeList.item(0);
						e.setTextContent(desc1);
						
						NodeList subItemNodeList = issueConditionElement.getElementsByTagName("SubItem");
						Element sube = (Element) subItemNodeList.item(0);
						sube.setAttribute("Name", title);
					}
				}
			}
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
		if(args != null && args.length == 2) {
			System.out.println("ANSIBLE_CORE_MODULE_DIRECTORY : "+args[0]);
			System.out.println("CORE_KI_OUTPUT_DIRECTORY : "+args[1]);
			IdentifyModule identifyModule = new IdentifyModule(args[0], args[1]);
			identifyModule.identifyModules();
		} else {
			System.out.println("Invalid arguments specified.\n");
			printUsage();
		}
	}
	
	private static void printUsage() {
		System.out.println("============================================= Usage ================================================");
		System.out.println("java -jar ansibleCoreModulesToCoreKIs.jar <ANSIBLE_CORE_MODULE_DIRECTORY> <CORE_KI_OUTPUT_DIRECTORY>");
		System.out.println("For example : ");
		System.out.println("	java -jar ansibleCoreModulesToCoreKIs.jar /tmp/ansible_core_module /tmp/core_ki_output");
		System.out.println("====================================================================================================");
	}
}