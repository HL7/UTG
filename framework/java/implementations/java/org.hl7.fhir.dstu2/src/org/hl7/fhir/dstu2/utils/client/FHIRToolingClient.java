package org.hl7.fhir.dstu2.utils.client;


/*
  Copyright (c) 2011+, HL7, Inc.
  All rights reserved.
  
  Redistribution and use in source and binary forms, with or without modification, 
  are permitted provided that the following conditions are met:
  
   * Redistributions of source code must retain the above copyright notice, this 
     list of conditions and the following disclaimer.
   * Redistributions in binary form must reproduce the above copyright notice, 
     this list of conditions and the following disclaimer in the documentation 
     and/or other materials provided with the distribution.
   * Neither the name of HL7 nor the names of its contributors may be used to 
     endorse or promote products derived from this software without specific 
     prior written permission.
  
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  POSSIBILITY OF SUCH DAMAGE.
  
*/

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.hl7.fhir.dstu2.model.Bundle;
import org.hl7.fhir.dstu2.model.Coding;
import org.hl7.fhir.dstu2.model.ConceptMap;
import org.hl7.fhir.dstu2.model.Conformance;
import org.hl7.fhir.dstu2.model.OperationOutcome;
import org.hl7.fhir.dstu2.model.Parameters;
import org.hl7.fhir.dstu2.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.dstu2.model.PrimitiveType;
import org.hl7.fhir.dstu2.model.Resource;
import org.hl7.fhir.dstu2.model.StringType;
import org.hl7.fhir.dstu2.model.ValueSet;
import org.hl7.fhir.utilities.Utilities;

/**
 * Very Simple RESTful client. This is purely for use in the standalone 
 * tools jar packages. It doesn't support many features, only what the tools
 * need.
 * 
 * To use, initialize class and set base service URI as follows:
 * 
 * <pre><code>
 * FHIRSimpleClient fhirClient = new FHIRSimpleClient();
 * fhirClient.initialize("http://my.fhir.domain/myServiceRoot");
 * </code></pre>
 * 
 * Default Accept and Content-Type headers are application/xml+fhir and application/j+fhir.
 * 
 * These can be changed by invoking the following setter functions:
 * 
 * <pre><code>
 * setPreferredResourceFormat()
 * setPreferredFeedFormat()
 * </code></pre>
 * 
 * TODO Review all sad paths. 
 * 
 * @author Claude Nanjo
 *
 */
public class FHIRToolingClient {
	
	public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssK";
	public static final String DATE_FORMAT = "yyyy-MM-dd";

	private String base;
	private ResourceAddress resourceAddress;
	private ResourceFormat preferredResourceFormat;
	private HttpHost proxy;
	private int maxResultSetSize = -1;//_count
	private Conformance conf;
	
	//Pass enpoint for client - URI
	public FHIRToolingClient(String baseServiceUrl) throws URISyntaxException {
		preferredResourceFormat = ResourceFormat.RESOURCE_XML;
    initialize(baseServiceUrl);
	}
	
	public void configureProxy(String proxyHost, int proxyPort) {
		proxy = new HttpHost(proxyHost, proxyPort);
	}
	
	public void initialize(String baseServiceUrl)  throws URISyntaxException {
	  base = baseServiceUrl;
		resourceAddress = new ResourceAddress(baseServiceUrl);
		this.maxResultSetSize = -1;
		checkConformance();
	}
	
	private void checkConformance() {
	  try {
      conf = getConformanceStatementQuick();
	  } catch (Throwable e) {
	  }
   }

  public String getPreferredResourceFormat() {
    return preferredResourceFormat.getHeader();
  }
  
	public void setPreferredResourceFormat(ResourceFormat resourceFormat) {
		preferredResourceFormat = resourceFormat;
	}
	
	public int getMaximumRecordCount() {
		return maxResultSetSize;
	}
	
	public void setMaximumRecordCount(int maxResultSetSize) {
		this.maxResultSetSize = maxResultSetSize;
	}
	
	public Conformance getConformanceStatement() throws EFhirClientException {
		if (conf != null)
			return conf;
		return getConformanceStatement(false);
	}
	
	public Conformance getConformanceStatement(boolean useOptionsVerb) {
		Conformance conformance = null;
		try {
			if(useOptionsVerb) {
				conformance = (Conformance)ClientUtils.issueOptionsRequest(resourceAddress.getBaseServiceUri(), getPreferredResourceFormat(), proxy).getReference();//TODO fix this
			} else {
				conformance = (Conformance)ClientUtils.issueGetResourceRequest(resourceAddress.resolveMetadataUri(false), getPreferredResourceFormat(), proxy).getReference();
			}
		} catch(Exception e) {
			handleException("An error has occurred while trying to fetch the server's conformance statement", e);
		}
		return conformance;
	}
	
  public Conformance getConformanceStatementQuick() throws EFhirClientException {
    if (conf != null)
      return conf;
    return getConformanceStatementQuick(false);
  }
  
  public Conformance getConformanceStatementQuick(boolean useOptionsVerb) {
    Conformance conformance = null;
    try {
      if(useOptionsVerb) {
        conformance = (Conformance)ClientUtils.issueOptionsRequest(resourceAddress.getBaseServiceUri(), getPreferredResourceFormat(), proxy).getReference();//TODO fix this
      } else {
        conformance = (Conformance)ClientUtils.issueGetResourceRequest(resourceAddress.resolveMetadataUri(true), getPreferredResourceFormat(), proxy).getReference();
      }
    } catch(Exception e) {
      handleException("An error has occurred while trying to fetch the server's conformance statement", e);
    }
    return conformance;
  }
  
	public <T extends Resource> T read(Class<T> resourceClass, String id) {//TODO Change this to AddressableResource
		ResourceRequest<T> result = null;
		try {
			result = ClientUtils.issueGetResourceRequest(resourceAddress.resolveGetUriFromResourceClassAndId(resourceClass, id), getPreferredResourceFormat(), proxy);
			result.addErrorStatus(410);//gone
			result.addErrorStatus(404);//unknown
			result.addSuccessStatus(200);//Only one for now
			if(result.isUnsuccessfulRequest()) {
				throw new EFhirClientException("Server returned error code " + result.getHttpStatus(), (OperationOutcome)result.getPayload());
			}
		} catch (Exception e) {
			handleException("An error has occurred while trying to read this resource", e);
		}
		return result.getPayload();
	}

	public <T extends Resource> T vread(Class<T> resourceClass, String id, String version) {
		ResourceRequest<T> result = null;
		try {
			result = ClientUtils.issueGetResourceRequest(resourceAddress.resolveGetUriFromResourceClassAndIdAndVersion(resourceClass, id, version), getPreferredResourceFormat(), proxy);
			result.addErrorStatus(410);//gone
			result.addErrorStatus(404);//unknown
			result.addErrorStatus(405);//unknown
			result.addSuccessStatus(200);//Only one for now
			if(result.isUnsuccessfulRequest()) {
				throw new EFhirClientException("Server returned error code " + result.getHttpStatus(), (OperationOutcome)result.getPayload());
			}
		} catch (Exception e) {
			handleException("An error has occurred while trying to read this version of the resource", e);
		}
		return result.getPayload();
	}
	
//	
//	public <T extends Resource> T update(Class<T> resourceClass, T resource, String id) {
//		ResourceRequest<T> result = null;
//		try {
//			List<Header> headers = null;
//			result = ClientUtils.issuePutRequest(resourceAddress.resolveGetUriFromResourceClassAndId(resourceClass, id),ClientUtils.getResourceAsByteArray(resource, false, isJson(getPreferredResourceFormat())), getPreferredResourceFormat(), headers, proxy);
//			result.addErrorStatus(410);//gone
//			result.addErrorStatus(404);//unknown
//			result.addErrorStatus(405);
//			result.addErrorStatus(422);//Unprocessable Entity
//			result.addSuccessStatus(200);
//			result.addSuccessStatus(201);
//			if(result.isUnsuccessfulRequest()) {
//				throw new EFhirClientException("Server returned error code " + result.getHttpStatus(), (OperationOutcome)result.getPayload());
//			}
//		} catch(Exception e) {
//			throw new EFhirClientException("An error has occurred while trying to update this resource", e);
//		}
//		// TODO oe 26.1.2015 could be made nicer if only OperationOutcome	locationheader is returned with an operationOutcome would be returned (and not	the resource also) we make another read
//		try {
//		  OperationOutcome operationOutcome = (OperationOutcome)result.getPayload();
//		  ResourceAddress.ResourceVersionedIdentifier resVersionedIdentifier = ResourceAddress.parseCreateLocation(result.getLocation());
//		  return this.vread(resourceClass, resVersionedIdentifier.getId(),resVersionedIdentifier.getVersionId());
//		} catch(ClassCastException e) {
//		  // if we fall throught we have the correct type already in the create
//		}
//
//		return result.getPayload();
//	}

//	
//	public <T extends Resource> boolean delete(Class<T> resourceClass, String id) {
//		try {
//			return ClientUtils.issueDeleteRequest(resourceAddress.resolveGetUriFromResourceClassAndId(resourceClass, id), proxy);
//		} catch(Exception e) {
//			throw new EFhirClientException("An error has occurred while trying to delete this resource", e);
//		}
//
//	}

//	
//	public <T extends Resource> OperationOutcome create(Class<T> resourceClass, T resource) {
//	  ResourceRequest<T> resourceRequest = null;
//	  try {
//	    List<Header> headers = null;
//	    resourceRequest = ClientUtils.issuePostRequest(resourceAddress.resolveGetUriFromResourceClass(resourceClass),ClientUtils.getResourceAsByteArray(resource, false, isJson(getPreferredResourceFormat())), getPreferredResourceFormat(), headers, proxy);
//	    resourceRequest.addSuccessStatus(201);
//	    if(resourceRequest.isUnsuccessfulRequest()) {
//	      throw new EFhirClientException("Server responded with HTTP error code " + resourceRequest.getHttpStatus(), (OperationOutcome)resourceRequest.getPayload());
//	    }
//	  } catch(Exception e) {
//	    handleException("An error has occurred while trying to create this resource", e);
//	  }
//	  OperationOutcome operationOutcome = null;;
//	  try {
//	    operationOutcome = (OperationOutcome)resourceRequest.getPayload();
//	    ResourceAddress.ResourceVersionedIdentifier resVersionedIdentifier = 
//	        ResourceAddress.parseCreateLocation(resourceRequest.getLocation());
//	    OperationOutcomeIssueComponent issue = operationOutcome.addIssue();
//	    issue.setSeverity(IssueSeverity.INFORMATION);
//	    issue.setUserData(ResourceAddress.ResourceVersionedIdentifier.class.toString(),
//	        resVersionedIdentifier);
//	    return operationOutcome;
//	  } catch(ClassCastException e) {
//	    // some server (e.g. grahams) returns the resource directly
//	    operationOutcome = new OperationOutcome();
//	    OperationOutcomeIssueComponent issue = operationOutcome.addIssue();
//	    issue.setSeverity(IssueSeverity.INFORMATION);
//	    issue.setUserData(ResourceRequest.class.toString(),
//	        resourceRequest.getPayload());
//	    return operationOutcome;
//	  }	
//	}

//	
//	public <T extends Resource> Bundle history(Calendar lastUpdate, Class<T> resourceClass, String id) {
//		Bundle history = null;
//		try {
//			history = ClientUtils.issueGetFeedRequest(resourceAddress.resolveGetHistoryForResourceId(resourceClass, id, lastUpdate, maxResultSetSize), getPreferredResourceFormat(), proxy);
//		} catch (Exception e) {
//			handleException("An error has occurred while trying to retrieve history information for this resource", e);
//		}
//		return history;
//	}

//	
//	public <T extends Resource> Bundle history(Date lastUpdate, Class<T> resourceClass, String id) {
//		Bundle history = null;
//		try {
//			history = ClientUtils.issueGetFeedRequest(resourceAddress.resolveGetHistoryForResourceId(resourceClass, id, lastUpdate, maxResultSetSize), getPreferredResourceFormat(), proxy);
//		} catch (Exception e) {
//			handleException("An error has occurred while trying to retrieve history information for this resource", e);
//		}
//		return history;
//	}
//
//	
//	public <T extends Resource> Bundle history(Calendar lastUpdate, Class<T> resourceClass) {
//		Bundle history = null;
//		try {
//			history = ClientUtils.issueGetFeedRequest(resourceAddress.resolveGetHistoryForResourceType(resourceClass, lastUpdate, maxResultSetSize), getPreferredResourceFormat(), proxy);
//		} catch (Exception e) {
//			handleException("An error has occurred while trying to retrieve history information for this resource type", e);
//		}
//		return history;
//	}
//	
//	
//	public <T extends Resource> Bundle history(Date lastUpdate, Class<T> resourceClass) {
//		Bundle history = null;
//		try {
//			history = ClientUtils.issueGetFeedRequest(resourceAddress.resolveGetHistoryForResourceType(resourceClass, lastUpdate, maxResultSetSize), getPreferredResourceFormat(), proxy);
//		} catch (Exception e) {
//			handleException("An error has occurred while trying to retrieve history information for this resource type", e);
//		}
//		return history;
//	}
//	
//	
//	public <T extends Resource> Bundle history(Class<T> resourceClass) {
//		Bundle history = null;
//		try {
//			history = ClientUtils.issueGetFeedRequest(resourceAddress.resolveGetHistoryForResourceType(resourceClass, maxResultSetSize), getPreferredResourceFormat(), proxy);
//		} catch (Exception e) {
//			handleException("An error has occurred while trying to retrieve history information for this resource type", e);
//		}
//		return history;
//	}
//	
//	
//	public <T extends Resource> Bundle history(Class<T> resourceClass, String id) {
//		Bundle history = null;
//		try {
//			history = ClientUtils.issueGetFeedRequest(resourceAddress.resolveGetHistoryForResourceId(resourceClass, id, maxResultSetSize), getPreferredResourceFormat(), proxy);
//		} catch (Exception e) {
//			handleException("An error has occurred while trying to retrieve history information for this resource", e);
//		}
//		return history;
//	}
//
//	
//	public <T extends Resource> Bundle history(Date lastUpdate) {
//		Bundle history = null;
//		try {
//			history = ClientUtils.issueGetFeedRequest(resourceAddress.resolveGetHistoryForAllResources(lastUpdate, maxResultSetSize), getPreferredResourceFormat(), proxy);
//		} catch (Exception e) {
//			handleException("An error has occurred while trying to retrieve history since last update",e);
//		}
//		return history;
//	}
//
//	
//	public <T extends Resource> Bundle history(Calendar lastUpdate) {
//		Bundle history = null;
//		try {
//			history = ClientUtils.issueGetFeedRequest(resourceAddress.resolveGetHistoryForAllResources(lastUpdate, maxResultSetSize), getPreferredResourceFormat(), proxy);
//		} catch (Exception e) {
//			handleException("An error has occurred while trying to retrieve history since last update",e);
//		}
//		return history;
//	}
//
//	
//	public <T extends Resource> Bundle history() {
//		Bundle history = null;
//		try {
//			history = ClientUtils.issueGetFeedRequest(resourceAddress.resolveGetHistoryForAllResources(maxResultSetSize), getPreferredResourceFormat(), proxy);
//		} catch (Exception e) {
//			handleException("An error has occurred while trying to retrieve history since last update",e);
//		}
//		return history;
//	}
//
//	
//	public <T extends Resource> Bundle search(Class<T> resourceClass, Map<String, String> parameters) {
//		Bundle searchResults = null;
//		try {
//			searchResults = ClientUtils.issueGetFeedRequest(resourceAddress.resolveSearchUri(resourceClass, parameters), getPreferredResourceFormat(), proxy);
//		} catch (Exception e) {
//			handleException("Error performing search with parameters " + parameters, e);
//		}
//		return searchResults;
//	}
//	
//  
//  public <T extends Resource> Bundle searchPost(Class<T> resourceClass, T resource, Map<String, String> parameters) {
//    Bundle searchResults = null;
//    try {
//      searchResults = ClientUtils.issuePostFeedRequest(resourceAddress.resolveSearchUri(resourceClass, new HashMap<String, String>()), parameters, "src", resource, getPreferredResourceFormat());
//    } catch (Exception e) {
//      handleException("Error performing search with parameters " + parameters, e);
//    }
//    return searchResults;
//  }
	
	
  public <T extends Resource> Parameters operateType(Class<T> resourceClass, String name, Parameters params) {
  	boolean complex = false;
  	for (ParametersParameterComponent p : params.getParameter())
  		complex = complex || !(p.getValue() instanceof PrimitiveType);
  	Parameters searchResults = null;
			String ps = "";
  		try {
      if (!complex)
  			for (ParametersParameterComponent p : params.getParameter())
  	  		if (p.getValue() instanceof PrimitiveType)
  	  		  ps += p.getName() + "=" + Utilities.encodeUri(((PrimitiveType) p.getValue()).asStringValue())+"&";
   		ResourceRequest<T> result;
  		if (complex)
  			result = ClientUtils.issuePostRequest(resourceAddress.resolveOperationURLFromClass(resourceClass, name, ps), ClientUtils.getResourceAsByteArray(params, false, isJson(getPreferredResourceFormat())), getPreferredResourceFormat(), proxy);
  		else 
  			result = ClientUtils.issueGetResourceRequest(resourceAddress.resolveOperationURLFromClass(resourceClass, name, ps), getPreferredResourceFormat(), proxy);
  			result.addErrorStatus(410);//gone
  			result.addErrorStatus(404);//unknown
  			result.addSuccessStatus(200);//Only one for now
  			if(result.isUnsuccessfulRequest()) 
  				throw new EFhirClientException("Server returned error code " + result.getHttpStatus(), (OperationOutcome)result.getPayload());
  		if (result.getPayload() instanceof Parameters)
  			return (Parameters) result.getPayload();
  		else {
  			Parameters p_out = new Parameters();
  			p_out.addParameter().setName("return").setResource(result.getPayload());
  			return p_out;
  		}
  		} catch (Exception e) {
  			handleException("Error performing operation '"+name+"' with parameters " + ps, e);  		
  		}
  		return null;
  }

  
	public Bundle transaction(Bundle batch) {
		Bundle transactionResult = null;
		try {
			transactionResult = ClientUtils.postBatchRequest(resourceAddress.getBaseServiceUri(), ClientUtils.getFeedAsByteArray(batch, false, isJson(getPreferredResourceFormat())), getPreferredResourceFormat(), proxy);
		} catch (Exception e) {
			handleException("An error occurred trying to process this transaction request", e);
		}
		return transactionResult;
	}
	
	@SuppressWarnings("unchecked")
	
	public <T extends Resource> OperationOutcome validate(Class<T> resourceClass, T resource, String id) {
		ResourceRequest<T> result = null;
		try {
			result = ClientUtils.issuePostRequest(resourceAddress.resolveValidateUri(resourceClass, id), ClientUtils.getResourceAsByteArray(resource, false, isJson(getPreferredResourceFormat())), getPreferredResourceFormat(), proxy);
			result.addErrorStatus(400);//gone
			result.addErrorStatus(422);//Unprocessable Entity
			result.addSuccessStatus(200);//OK
			if(result.isUnsuccessfulRequest()) {
				throw new EFhirClientException("Server returned error code " + result.getHttpStatus(), (OperationOutcome)result.getPayload());
			}
		} catch(Exception e) {
			handleException("An error has occurred while trying to validate this resource", e);
		}
		return (OperationOutcome)result.getPayload();
	}
	
	/* change to meta operations
	
	public List<Coding> getAllTags() {
		TagListRequest result = null;
		try {
			result = ClientUtils.issueGetRequestForTagList(resourceAddress.resolveGetAllTags(), getPreferredResourceFormat(), null, proxy);
		} catch (Exception e) {
			handleException("An error has occurred while trying to retrieve all tags", e);
		}
		return result.getPayload();
	}
	
	
	public <T extends Resource> List<Coding> getAllTagsForResourceType(Class<T> resourceClass) {
		TagListRequest result = null;
		try {
			result = ClientUtils.issueGetRequestForTagList(resourceAddress.resolveGetAllTagsForResourceType(resourceClass), getPreferredResourceFormat(), null, proxy);
		} catch (Exception e) {
			handleException("An error has occurred while trying to retrieve tags for this resource type", e);
		}
		return result.getPayload();
	}
	
	
	public <T extends Resource> List<Coding> getTagsForReference(Class<T> resource, String id) {
		TagListRequest result = null;
		try {
			result = ClientUtils.issueGetRequestForTagList(resourceAddress.resolveGetTagsForReference(resource, id), getPreferredResourceFormat(), null, proxy);
		} catch (Exception e) {
			handleException("An error has occurred while trying to retrieve tags for this resource", e);
		}
		return result.getPayload();
	}
	
	
	public <T extends Resource> List<Coding> getTagsForResourceVersion(Class<T> resource, String id, String versionId) {
		TagListRequest result = null;
		try {
			result = ClientUtils.issueGetRequestForTagList(resourceAddress.resolveGetTagsForResourceVersion(resource, id, versionId), getPreferredResourceFormat(), null, proxy);
		} catch (Exception e) {
			handleException("An error has occurred while trying to retrieve tags for this resource version", e);
		}
		return result.getPayload();
	}
	
//	
//	public <T extends Resource> boolean deleteTagsForReference(Class<T> resourceClass, String id) {
//		try {
//			return ClientUtils.issueDeleteRequest(resourceAddress.resolveGetTagsForReference(resourceClass, id), proxy);
//		} catch(Exception e) {
//			handleException("An error has occurred while trying to retrieve tags for this resource version", e);
//			throw new EFhirClientException("An error has occurred while trying to delete this resource", e);
//		}
//
//	}
//	
//	
//	public <T extends Resource> boolean deleteTagsForResourceVersion(Class<T> resourceClass, String id, List<Coding> tags, String version) {
//		try {
//			return ClientUtils.issueDeleteRequest(resourceAddress.resolveGetTagsForResourceVersion(resourceClass, id, version), proxy);
//		} catch(Exception e) {
//			handleException("An error has occurred while trying to retrieve tags for this resource version", e);
//			throw new EFhirClientException("An error has occurred while trying to delete this resource", e);
//		}
//	}
	
	
	public <T extends Resource> List<Coding> createTags(List<Coding> tags, Class<T> resourceClass, String id) {
		TagListRequest request = null;
		try {
			request = ClientUtils.issuePostRequestForTagList(resourceAddress.resolveGetTagsForReference(resourceClass, id),ClientUtils.getTagListAsByteArray(tags, false, isJson(getPreferredResourceFormat())), getPreferredResourceFormat(), null, proxy);
			request.addSuccessStatus(201);
			request.addSuccessStatus(200);
			if(request.isUnsuccessfulRequest()) {
				throw new EFhirClientException("Server responded with HTTP error code " + request.getHttpStatus());
			}
		} catch(Exception e) {
			handleException("An error has occurred while trying to set tags for this resource", e);
		}
		return request.getPayload();
	}
	
	
	public <T extends Resource> List<Coding> createTags(List<Coding> tags, Class<T> resourceClass, String id, String version) {
		TagListRequest request = null;
		try {
			request = ClientUtils.issuePostRequestForTagList(resourceAddress.resolveGetTagsForResourceVersion(resourceClass, id, version),ClientUtils.getTagListAsByteArray(tags, false, isJson(getPreferredResourceFormat())), getPreferredResourceFormat(), null, proxy);
			request.addSuccessStatus(201);
			request.addSuccessStatus(200);
			if(request.isUnsuccessfulRequest()) {
				throw new EFhirClientException("Server responded with HTTP error code " + request.getHttpStatus());
			}
		} catch(Exception e) {
			handleException("An error has occurred while trying to set the tags for this resource version", e);
		}
		return request.getPayload();
	}

	
	public <T extends Resource> List<Coding> deleteTags(List<Coding> tags, Class<T> resourceClass, String id, String version) {
		TagListRequest request = null;
		try {
			request = ClientUtils.issuePostRequestForTagList(resourceAddress.resolveDeleteTagsForResourceVersion(resourceClass, id, version),ClientUtils.getTagListAsByteArray(tags, false, isJson(getPreferredResourceFormat())), getPreferredResourceFormat(), null, proxy);
			request.addSuccessStatus(201);
			request.addSuccessStatus(200);
			if(request.isUnsuccessfulRequest()) {
				throw new EFhirClientException("Server responded with HTTP error code " + request.getHttpStatus());
			}
		} catch(Exception e) {
			handleException("An error has occurred while trying to delete the tags for this resource version", e);
		}
		return request.getPayload();
	}
	*/

	/**
	 * Helper method to prevent nesting of previously thrown EFhirClientExceptions
	 * 
	 * @param e
	 * @throws EFhirClientException
	 */
	protected void handleException(String message, Exception e) throws EFhirClientException {
		if(e instanceof EFhirClientException) {
			throw (EFhirClientException)e;
		} else {
			throw new EFhirClientException(message, e);
		}
	}
	
	/**
	 * Helper method to determine whether desired resource representation
	 * is Json or XML.
	 * 
	 * @param format
	 * @return
	 */
	protected boolean isJson(String format) {
		boolean isJson = false;
		if(format.toLowerCase().contains("json")) {
			isJson = true;
		}
		return isJson;
	}
		
  public Bundle fetchFeed(String url) {
		Bundle feed = null;
		try {
			feed = ClientUtils.issueGetFeedRequest(new URI(url), getPreferredResourceFormat(), proxy);
		} catch (Exception e) {
			handleException("An error has occurred while trying to retrieve history since last update",e);
		}
		return feed;
  }
  
  public ValueSet expandValueset(ValueSet source) {
    List<Header> headers = null;
    ResourceRequest<Resource> result = ClientUtils.issuePostRequest(resourceAddress.resolveOperationUri(ValueSet.class, "expand"), 
        ClientUtils.getResourceAsByteArray(source, false, isJson(getPreferredResourceFormat())), getPreferredResourceFormat(), headers, proxy);
    result.addErrorStatus(410);//gone
    result.addErrorStatus(404);//unknown
    result.addErrorStatus(405);
    result.addErrorStatus(422);//Unprocessable Entity
    result.addSuccessStatus(200);
    result.addSuccessStatus(201);
    if(result.isUnsuccessfulRequest()) {
      throw new EFhirClientException("Server returned error code " + result.getHttpStatus(), (OperationOutcome)result.getPayload());
    }
    return (ValueSet) result.getPayload();
  }

  
  public Parameters lookupCode(Map<String, String> params) {
    ResourceRequest<Resource> result = ClientUtils.issueGetResourceRequest(resourceAddress.resolveOperationUri(ValueSet.class, "lookup", params), getPreferredResourceFormat(), proxy);
    result.addErrorStatus(410);//gone
    result.addErrorStatus(404);//unknown
    result.addErrorStatus(405);
    result.addErrorStatus(422);//Unprocessable Entity
    result.addSuccessStatus(200);
    result.addSuccessStatus(201);
    if(result.isUnsuccessfulRequest()) {
      throw new EFhirClientException("Server returned error code " + result.getHttpStatus(), (OperationOutcome)result.getPayload());
    }
    return (Parameters) result.getPayload();
  }
  public ValueSet expandValueset(ValueSet source, Map<String, String> params) {
    List<Header> headers = null;
    ResourceRequest<Resource> result = ClientUtils.issuePostRequest(resourceAddress.resolveOperationUri(ValueSet.class, "expand", params), 
        ClientUtils.getResourceAsByteArray(source, false, isJson(getPreferredResourceFormat())), getPreferredResourceFormat(), headers, proxy);
    result.addErrorStatus(410);//gone
    result.addErrorStatus(404);//unknown
    result.addErrorStatus(405);
    result.addErrorStatus(422);//Unprocessable Entity
    result.addSuccessStatus(200);
    result.addSuccessStatus(201);
    if(result.isUnsuccessfulRequest()) {
      throw new EFhirClientException("Server returned error code " + result.getHttpStatus(), (OperationOutcome)result.getPayload());
    }
    return (ValueSet) result.getPayload();
  }
  
  
  public String getAddress() {
    return base;
  }

  public ConceptMap initializeClosure(String name) {
    Parameters params = new Parameters();
    params.addParameter().setName("name").setValue(new StringType(name));
    List<Header> headers = null;
    ResourceRequest<Resource> result = ClientUtils.issuePostRequest(resourceAddress.resolveOperationUri(null, "closure", new HashMap<String, String>()),
        ClientUtils.getResourceAsByteArray(params, false, isJson(getPreferredResourceFormat())), getPreferredResourceFormat(), headers, proxy);
    result.addErrorStatus(410);//gone
    result.addErrorStatus(404);//unknown
    result.addErrorStatus(405);
    result.addErrorStatus(422);//Unprocessable Entity
    result.addSuccessStatus(200);
    result.addSuccessStatus(201);
    if(result.isUnsuccessfulRequest()) {
      throw new EFhirClientException("Server returned error code " + result.getHttpStatus(), (OperationOutcome)result.getPayload());
    }
    return (ConceptMap) result.getPayload();
  }

  public ConceptMap updateClosure(String name, Coding coding) {
    Parameters params = new Parameters();
    params.addParameter().setName("name").setValue(new StringType(name));
    params.addParameter().setName("concept").setValue(coding);
    List<Header> headers = null;
    ResourceRequest<Resource> result = ClientUtils.issuePostRequest(resourceAddress.resolveOperationUri(null, "closure", new HashMap<String, String>()),
        ClientUtils.getResourceAsByteArray(params, false, isJson(getPreferredResourceFormat())), getPreferredResourceFormat(), headers, proxy);
    result.addErrorStatus(410);//gone
    result.addErrorStatus(404);//unknown
    result.addErrorStatus(405);
    result.addErrorStatus(422);//Unprocessable Entity
    result.addSuccessStatus(200);
    result.addSuccessStatus(201);
    if(result.isUnsuccessfulRequest()) {
      throw new EFhirClientException("Server returned error code " + result.getHttpStatus(), (OperationOutcome)result.getPayload());
    }
    return (ConceptMap) result.getPayload();
  }

}
