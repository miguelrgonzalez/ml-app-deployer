package com.marklogic.mgmt.api.security.protectedpath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Permission {

	@XmlElement(name = "role-name")
	private String roleName;
	private String capability;

	public Permission() {
	}

	public Permission(String roleName, String capability) {
		this.roleName = roleName;
		this.capability = capability;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getCapability() {
		return capability;
	}

	public void setCapability(String capability) {
		this.capability = capability;
	}
}
