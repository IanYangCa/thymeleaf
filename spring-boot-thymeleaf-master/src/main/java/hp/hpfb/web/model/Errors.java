//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.09.20 at 11:57:26 AM EDT 
//


package hp.hpfb.web.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://purl.oclc.org/dsdl/svrl}failed-assert"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "failedAssert"
})
@XmlRootElement(name = "errors", namespace="http://purl.oclc.org/dsdl/svrl")
public class Errors {

    @XmlElement(name = "failed-assert", required = true, namespace="http://purl.oclc.org/dsdl/svrl")
    protected FailedAssert failedAssert;

    /**
     * Gets the value of the failedAssert property.
     * 
     * @return
     *     possible object is
     *     {@link FailedAssert }
     *     
     */
    public FailedAssert getFailedAssert() {
        return failedAssert;
    }

    /**
     * Sets the value of the failedAssert property.
     * 
     * @param value
     *     allowed object is
     *     {@link FailedAssert }
     *     
     */
    public void setFailedAssert(FailedAssert value) {
        this.failedAssert = value;
    }

}
