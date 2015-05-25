package com.opex.ansibletoki.parser.identifyis;

import java.util.ArrayList;
import java.util.List;

public class AnsibleDocumentation {
	String moduleName;
	String version;
	String shortDescription;
	String description;
	
	List<Option> options = new ArrayList<Option>();
	
	public String getModuleName() {
		return moduleName;
	}
	
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getShortDescription() {
		return shortDescription;
	}
	
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public void addOption(Option option) {
		options.add(option);
	}

	@Override
	public String toString() {
		return "AnsibleDocumentation [moduleName=" + moduleName + ", version="
				+ version + ", shortDescription=" + shortDescription
				+ ", description=" + description + ", options=" + options + "]";
	}
	
	public List<Option> getOptions() {
		return options;
	}
	
	public void setOptions(List<Option> options) {
		this.options = options;
	}
	
	
}
