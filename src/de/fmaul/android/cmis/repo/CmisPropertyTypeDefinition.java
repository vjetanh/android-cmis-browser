package de.fmaul.android.cmis.repo;

import org.dom4j.Element;

public class CmisPropertyTypeDefinition {

	private String id;
	private String localName;
	private String localNamespace;
	private String displayName;
	private String queryName;
	private String description;
	private String propertyType;
	private String cardinality;
	private String updatability;
	private boolean inherited;
	private boolean required;
	private boolean queryable;
	private boolean orderable;
	private boolean openChoice;

	public static CmisPropertyTypeDefinition createFromElement(Element propElement) {
		CmisPropertyTypeDefinition cpd = new CmisPropertyTypeDefinition();

		cpd.id = propElement.elementText("id");
		cpd.localName = propElement.elementText("localName");
		cpd.localNamespace = propElement.elementText("localNamespace");
		cpd.displayName = propElement.elementText("displayName");
		cpd.queryName = propElement.elementText("queryName");
		cpd.description = propElement.elementText("description");
		cpd.propertyType = propElement.elementText("propertyType");
		cpd.cardinality = propElement.elementText("cardinality");
		cpd.updatability = propElement.elementText("updatability");
		cpd.inherited = Boolean.parseBoolean(propElement.elementText("inherited"));
		cpd.required = Boolean.parseBoolean(propElement.elementText("required"));
		cpd.queryable = Boolean.parseBoolean(propElement.elementText("queryable"));
		cpd.orderable = Boolean.parseBoolean(propElement.elementText("orderable"));
		cpd.openChoice = Boolean.parseBoolean(propElement.elementText("openChoice"));

		return cpd;
	}

	public String getId() {
		return id;
	}

	public String getLocalName() {
		return localName;
	}

	public String getLocalNamespace() {
		return localNamespace;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getQueryName() {
		return queryName;
	}

	public String getDescription() {
		return description;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public String getCardinality() {
		return cardinality;
	}

	public String getUpdatability() {
		return updatability;
	}

	public boolean isInherited() {
		return inherited;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isQueryable() {
		return queryable;
	}

	public boolean isOrderable() {
		return orderable;
	}

	public boolean isOpenChoice() {
		return openChoice;
	}

}
