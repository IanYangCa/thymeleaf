package hp.hpfb.web.handler;

import javax.xml.validation.TypeInfoProvider;

import org.w3c.dom.TypeInfo;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A ContentHandler to receive and display the results
 */
public class LocalContentHandler extends DefaultHandler {
	int indent = 0;
	private TypeInfoProvider provider;

	public LocalContentHandler(TypeInfoProvider provider) {
		this.provider = provider;
	}

	/**
	 * Receive notification of the start of an element.
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		TypeInfo etype = provider.getElementTypeInfo();
		StringBuffer sb = new StringBuffer(100);
		for (int i = 0; i < indent; i++) {
			sb.append("  ");
		}
		sb.append("Element " + qName);
		if (etype == null) {
			sb.append(" of type is undefined! ");
		} else {
			sb.append(" of type {" + etype.getTypeNamespace() + "} " + etype.getTypeName());
		}
		System.out.println(sb.toString());
		for (int a = 0; a < attributes.getLength(); a++) {
			TypeInfo atype = provider.getAttributeTypeInfo(a);
			boolean spec = provider.isSpecified(a);
			sb.setLength(0);
			for (int i = 0; i < indent + 2; i++) {
				sb.append("  ");
			}
			sb.append("Attribute " + attributes.getQName(a) + (spec ? " (specified)" : " (defaulted)"));
			if (atype == null) {
				sb.append(" of unknown type");
			} else {
				sb.append(" of type {" + atype.getTypeNamespace() + '}' + atype.getTypeName());
			}
			System.out.println(sb.toString());
		}
		indent++;
	}

}
