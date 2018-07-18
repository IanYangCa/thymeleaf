package hp.hpfb.web.handler;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class Resolver implements LSResourceResolver {
	/**
	 * Resolve a reference to a resource
	 * 
	 * @param type
	 *            The type of resource, for example a schema, source XML document,
	 *            or query
	 * @param namespace
	 *            The target namespace (in the case of a schema document)
	 * @param publicId
	 *            The public ID
	 * @param systemId
	 *            The system identifier (as written, possibly a relative URI)
	 * @param baseURI
	 *            The base URI against which the system identifier should be
	 *            resolved
	 * @return an LSInput object typically containing the character stream or byte
	 *         stream identified by the supplied parameters; or null if the
	 *         reference cannot be resolved or if the resolver chooses not to
	 *         resolve it.
	 */
	public LSInput resolveResource(String type, String namespace, String publicId, String systemId,
			String baseURI) {
		return null;
	}
}
