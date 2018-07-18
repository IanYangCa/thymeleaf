package hp.hpfb.web.model;

public class ReportSchema {
	private String rule = "N/A";
	private String severity = "Error";
	private String category = "N/A";
	private String label = "HC Validation Report";
	private String details;
	private String test = "Validation Report Overview";
	private String location = "N/A";
	
	
	public String getRule() {
		return rule;
	}


	public void setRule(String rule) {
		this.rule = rule;
	}


	public String getSeverity() {
		return severity;
	}


	public void setSeverity(String severity) {
		this.severity = severity;
	}


	public String getCategory() {
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public String getDetails() {
		return details;
	}


	public void setDetails(String details) {
		this.details = details;
	}


	public String getTest() {
		return test;
	}


	public void setTest(String test) {
		this.test = test;
	}


	public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}


	public String toXML() {
		StringBuilder temp = new StringBuilder("<report_message>");
		temp.append("<rule>").append(rule).append("</rule>")
			.append("<severity>").append(severity).append("</severity>")
			.append("<category>").append(category).append("</category>")
			.append("<label>").append(label).append("</label>")
			.append("<details>").append(details).append("</details>")
			.append("<test>").append(test).append("</test>")
			.append("<location>").append(location).append("</location>")
			.append("</report_message>");
		return temp.toString();
	}
}
