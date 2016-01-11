package org.mule.policies.commons;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public abstract class AbstractPolicyTestCase extends AbstractSamplePoliciesTestCase
{
	protected static final String TEST_RESOURCES = "./src/test/resources/";
	protected Map<String, String> expectedResponseHeaders = new HashMap<>();
	protected List<String> unExpectedResponseHeaders = new ArrayList<>();

	protected static final String POLICY_NAME = "http-headers-add-strip-policy";
	protected Map<String, Object> parameters = new HashMap<>();
	protected String endpointURI;
		
    public AbstractPolicyTestCase(String proxyFile, String apiFile, String autoDiscoveryFile)
    {
        super(proxyFile, apiFile, autoDiscoveryFile, true);
    }
    
    protected void compilePolicy() {    	    
    	try {
    		if (new File(getClass().getResource("/").getFile() + POLICY_NAME + ".xml").exists()){
    			new File(getClass().getResource("/").getFile() + POLICY_NAME + ".xml").delete();	
    		}
    		MustacheFactory mf = new DefaultMustacheFactory();
	        Mustache mustache = mf.compile(POLICY_NAME + ".xml");		        
	        mustache.execute(new FileWriter(getClass().getResource("/").getFile() + POLICY_NAME + ".xml"), parameters).flush();
	        
		} catch (IOException e) {
			logger.error("Error preparing test: " + e);
			e.printStackTrace();
		} 
    }       
       
    protected AssertEndpointResponseBuilder applyPolicyAndTest() throws InterruptedException {		
		addPolicy(POLICY_NAME + ".xml");		
        Thread.sleep(2000);
        try {
			Document document = createXMLDocument(IOUtils.toString(getClass().getResource("/" + POLICY_NAME + ".xml")));
			assertEquals("PolicyId should be set", parameters.get("policyId"), document.getElementsByTagName("policy").item(0).getAttributes().getNamedItem("id").getTextContent());
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
        
		AssertEndpointResponseBuilder assertResponseBuilder = new AssertEndpointResponseBuilder(endpointURI, "post");
        return assertResponseBuilder;
	}
    
    protected void unapplyPolicy(
			AssertEndpointResponseBuilder assertResponseBuilder) {
		removePolicy(POLICY_NAME);        
	}

    protected Document createXMLDocument(
			String body)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		builder = builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(new ByteArrayInputStream(body.getBytes()));
		return xmlDocument;
	}
}
