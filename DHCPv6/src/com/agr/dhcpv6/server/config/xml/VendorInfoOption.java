//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-520 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.02.22 at 08:27:45 PM EST 
//


package com.agr.dhcpv6.server.config.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for vendorInfoOption complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="vendorInfoOption">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="enterpriseNumber" type="{}enterpriseNumber"/>
 *         &lt;element name="suboptions" type="{}option" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="code" type="{}code" fixed="17" />
 *       &lt;attribute name="name" type="{}name" fixed="Vendor-specific Information" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "vendorInfoOption", propOrder = {
    "enterpriseNumber",
    "suboptions"
})
public class VendorInfoOption
    implements Serializable
{

    protected int enterpriseNumber;
    @XmlElement(required = true)
    protected List<Option> suboptions;
    @XmlAttribute
    protected Short code;
    @XmlAttribute
    protected String name;

    /**
     * Gets the value of the enterpriseNumber property.
     * 
     */
    public int getEnterpriseNumber() {
        return enterpriseNumber;
    }

    /**
     * Sets the value of the enterpriseNumber property.
     * 
     */
    public void setEnterpriseNumber(int value) {
        this.enterpriseNumber = value;
    }

    /**
     * Gets the value of the suboptions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the suboptions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSuboptions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Option }
     * 
     * 
     */
    public List<Option> getSuboptions() {
        if (suboptions == null) {
            suboptions = new ArrayList<Option>();
        }
        return this.suboptions;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getCode() {
        if (code == null) {
            return ((short) 17);
        } else {
            return code;
        }
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setCode(Short value) {
        this.code = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        if (name == null) {
            return "Vendor-specific Information";
        } else {
            return name;
        }
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}