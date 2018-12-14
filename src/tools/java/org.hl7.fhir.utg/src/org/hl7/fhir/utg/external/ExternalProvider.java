package org.hl7.fhir.utg.external;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.utilities.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ExternalProvider {
	private static final String XMLNS = "http://hl7.org/fhir";

	private String _name;
	private String _title;
	private String _note;
	private List<CodeSystem> _codeSystems; 
	
	public String getName()  { return _name; }
	public String getTitle() { return _title; }
	public String getNote()  { return _note; }
	
	public UUID getUUID() throws UnsupportedEncodingException {
		String source = XMLNS + "external codesystem provider " + this.getName();
		byte[] bytes = source.getBytes("UTF-8");
		return UUID.nameUUIDFromBytes(bytes);
	}
	
	public List<CodeSystem> getCodeSystems() { return _codeSystems; }
	public void addCodeSystem(CodeSystem cs) { _codeSystems.add(cs); }
	
	public ExternalProvider(Element e) {
		_name = e.getAttribute("name");
		_title = e.getAttribute("title");
		_note = e.getAttribute("note");
		_codeSystems = new LinkedList<CodeSystem>();
	}
	
	public void writeXMLManifest(String outputPath) throws ParserConfigurationException, TransformerException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.newDocument();
		
		Element root = doc.createElementNS(XMLNS, "List");
		doc.appendChild(root);
		
		Element eId = doc.createElementNS(XMLNS, "id");
		eId.setAttribute("value", this.getUUID().toString());
		root.appendChild(eId);
		
		Element eStatus = doc.createElementNS(XMLNS, "status");
		eStatus.setAttribute("value", "current");
		root.appendChild(eStatus);
		
		Element eMode = doc.createElementNS(XMLNS, "mode");
		eMode.setAttribute("value", "working");
		root.appendChild(eMode);
		
		Element eTitle = doc.createElementNS(XMLNS, "title");
		eTitle.setAttribute("value", this.getTitle());
		root.appendChild(eTitle);
		
		Element eNote = doc.createElementNS(XMLNS, "note");
		Element eNoteText = doc.createElementNS(XMLNS, "text");
		eNoteText.setAttribute("value", this.getNote());
		eNote.appendChild(eNoteText);
		root.appendChild(eNote);
		
		for (CodeSystem cs : this.getCodeSystems()) {
			Element eEntry = doc.createElementNS(XMLNS, "entry");
			Element eItem = doc.createElementNS(XMLNS, "item");
			Element eReference = doc.createElementNS(XMLNS, "reference");
			eReference.setAttribute("value", cs.getUrl().toString());
			eItem.appendChild(eReference);
			eEntry.appendChild(eItem);
			root.appendChild(eEntry);
		}
		
        // create the xml file
		//transform the DOM Object to an XML File
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		DOMSource domSource = new DOMSource(doc);
		String fileName = "ext_" + this.getName() + "_voc.xml";
		String filePath = outputPath + File.separator + fileName;
		StreamResult streamResult = new StreamResult(new File(filePath));
		//StreamResult streamResult = new StreamResult(System.out);
		transformer.transform(domSource, streamResult);

	}
	
	public static Map<String, ExternalProvider> getExternalProviders(String manifestFilename) throws SAXException, IOException, ParserConfigurationException {
		Map<String, ExternalProvider> providers = new HashMap<>();
		
		File fXmlFile = new File(manifestFilename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		
		NodeList providerNodeList = doc.getElementsByTagName("externalCodeSystemProvider");
		for (int i = 0; i < providerNodeList.getLength(); i++) {
			if (providerNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) providerNodeList.item(i);
				ExternalProvider provider = new ExternalProvider(e);
				providers.put(provider.getName(), provider);
			}
		}
		
		return providers;
	}
}
