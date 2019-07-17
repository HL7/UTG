package org.hl7.fhir.utilities;

import com.github.rjeschke.txtmark.Processor;
import com.overzealous.remark.Remark;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.hl7.fhir.utilities.xml.XMLUtil;
import org.w3c.dom.Element;

public class MarkDownProcessor {

  public enum Dialect {DARING_FIREBALL, COMMON_MARK};
  
  private Dialect dialect;
  
  
  public MarkDownProcessor(Dialect dialect) {
    super();
    this.dialect = dialect;
  }


  public String process(String source, String context) {
    switch (dialect) {
    case DARING_FIREBALL : return Processor.process(source); 
    case COMMON_MARK : return processCommonMark(source); 
    default: throw new Error("Unknown Markdown Dialect: "+dialect.toString()+" at "+context); 
    }
  }

  public static String htmlToMarkdown(Element sourceElement) {
	  return htmlToMarkdown(XMLUtil.elementToString(sourceElement));	  
  }
  
  public static String htmlToMarkdown(String source) {
		Remark remark = new Remark();
		return remark.convert(source);
  }
  
  private String processCommonMark(String source) {
    Parser parser = Parser.builder().build();
    Node document = parser.parse(source);
    HtmlRenderer renderer = HtmlRenderer.builder().build();
    return renderer.render(document);
  }
  
}
