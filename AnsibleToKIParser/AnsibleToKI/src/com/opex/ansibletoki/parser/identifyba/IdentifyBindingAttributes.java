package com.opex.ansibletoki.parser.identifyba;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.opex.ansibletoki.parser.identifyis.AnsibleDocumentation;
import com.opex.ansibletoki.parser.identifyis.IdentifyIssueCondition;
import com.opex.ansibletoki.parser.identifyis.Option;

public class IdentifyBindingAttributes {
	String inputFile;
	Document dom;
	AnsibleDocumentation ansibleDocumentation;
	StringBuffer completeDescription = new StringBuffer();
	
	
	public IdentifyBindingAttributes(String inputFile, Document dom) {
		super();
		this.inputFile = inputFile;
		this.dom = dom;
		ansibleDocumentation = new AnsibleDocumentation();
	}
	
	
	public void identifyBA(){
		try{
			try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			    	if(line.startsWith("DOCUMENTATION =")){
			    		readHeadings(br);
			    		readOptions(br);
			    	}
			    }
			}
			System.out.println(ansibleDocumentation);
			setBindingAttributes();
		}
		
		catch(Exception e){
			System.out.println(inputFile);
			e.printStackTrace();
		}
		
	}
	
	private void setBindingAttributes() {
		Comment comment = dom.createComment(completeDescription.toString());
		Element documentElement = dom.getDocumentElement();
		dom.insertBefore(comment, documentElement);
		
		//dom.insertBefore(dom.createTextNode("\n"), documentElement);

		Element extenstions = getElmentByTagName("SubItem", dom.getDocumentElement(), 1);
		Element andElement = getElmentByTagName("SubItem", extenstions);
		for (Option option : ansibleDocumentation.getOptions()) {
			if(option.isRequired()){
				Element filter = dom.createElement("AttributeFilter");
				filter.setAttribute("Mode", "Exists");
				filter.setAttribute("Name", option.getName());
				andElement.appendChild(filter);
			}
			
		}
		Element filter = dom.createElement("AttributeFilter");
		filter.setAttribute("Mode", "exists");
		filter.setAttribute("Name","module");
		filter.setAttribute("Value",ansibleDocumentation.getModuleName());
		andElement.appendChild(filter);
		
	}

	private Element getElmentByTagName(String tagName, Element e) {
		return getElmentByTagName(tagName, e, 0);
	}
	private Element getElmentByTagName(String tagName, Element e, int index) {
		return (Element) e.getElementsByTagName(tagName).item(index);
	}


	public static void main(String[] args) {
		IdentifyIssueCondition condition = new IdentifyIssueCondition("/home/ganesh/eclipse/TestNow/AnsibleToKIParser/temp.py", null);
		condition.identifyIC();
	}


	private void readOptions(BufferedReader br) throws IOException {
		
		Option option = new Option();
		while(true){
			String line = readLine(br);
			if(line != null){
				line = line.trim();
			}else{
				break;
			}
			if(line.endsWith(":") && !line.startsWith("description")){
				
				if(option.getName() != null){
					ansibleDocumentation.addOption(option);
				}
				String name = line.replace(":", "").trim();
				option = new Option();
				option.setName(name);
			}
			else if(line.startsWith("required")){
				String required = line.replace(":", "").replace("required", "").trim();
				option.setRequired("true".equals(required));
			}
			else if(line.startsWith("default")){
				String defaultValue = line.replace(":", "").replace("default", "").trim();
				option.setDefaultValue(defaultValue);
			}
			else if(line.startsWith("choices")){
				String choices = line.replace(":", "").replace("choices", "").trim();
				option.setChoices(choices);
			}
			else if(line.startsWith("description")){
				line = readLine(br);
				String description = line.replace(":", "").replace("-", "").trim();
				option.setDescription(description);
			}
			else if(line.startsWith("version_added")){
				
			}else if(line.startsWith("author") || line.startsWith("\"\"\"") || line.startsWith("EXAMPLES") || line.startsWith("'''")){
				break;
			}
			
		}
		
	}


	private String readLine(BufferedReader br) throws IOException {
		String line = br.readLine();
		if(!line.startsWith("author"))
			completeDescription.append(line).append("\n");
		return line;
	}


	private void readHeadings(BufferedReader br) throws IOException {
		readLine(br);
		while(true){
			String line = readLine(br);
			
			if(line.startsWith("module")){
				String moduleLine = line;
				String moduleName = moduleLine.split(":")[1];
				moduleName = moduleName.trim();
				ansibleDocumentation.setModuleName(moduleName);
			}
			else if(line.startsWith("version")){			
				String versionLine = line;
				String version = versionLine.split(":")[1].trim();
				version = version.replace("\"", "");
				ansibleDocumentation.setVersion(version);
			}
			else if(line.startsWith("short_description")){		
				String shortDescription = line;
				shortDescription = shortDescription.replace("short_description", "").replace(":", "").trim();
				ansibleDocumentation.setShortDescription(shortDescription);
			}
			else if(line.startsWith("description")){	
			
				String description = line;
				description = description.replace("description", "").replace(":", "").trim();
				ansibleDocumentation.setDescription(description);
			}
			else if(line.startsWith("description")){	
				
				String description = line;
				description = description.replace("description", "").replace(":", "").trim();
				ansibleDocumentation.setDescription(description);
			}
			else if(line.startsWith("options:")){
				break;
			}
			
		}
		
		
		
		
	}
	
}
