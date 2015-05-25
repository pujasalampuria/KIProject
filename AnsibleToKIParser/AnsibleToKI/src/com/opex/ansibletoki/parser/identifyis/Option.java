package com.opex.ansibletoki.parser.identifyis;

public class Option {
	String name;
	String defaultValue;
	String choices;
	String description;
	boolean required;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getChoices() {
		return choices;
	}
	public void setChoices(String choices) {
		this.choices = choices;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	@Override
	public String toString() {
		return "Option [name=" + name + ", defaultValue=" + defaultValue
				+ ", choices=" + choices + ", description=" + description
				+ ", required=" + required + "]";
	}
	
	
}
