/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.clinicalsummary.web.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryIndex;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.SummaryTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SummarySearchIndexFormController {
	
	private static final Log log = LogFactory.getLog(SummarySearchIndexFormController.class);
	
	public SummarySearchIndexFormController() {
		log.info("Creating SummarySearchIndexFormController object ...");
	}
	
	@RequestMapping("/module/clinicalsummary/summarySearchIndex")
	public void createJsonContent(@RequestParam(required = false, value = "iDisplayStart") int displayStart,
	                              @RequestParam(required = false, value = "iDisplayLength") int displayLength,
	                              @RequestParam(required = false, value = "sEcho") int echo,
	                              @RequestParam(required = false, value = "sSearch") String search,
	                              HttpServletResponse response, HttpServletRequest request) throws IOException,
	                                                                                       ServletRequestBindingException {
		if (Context.isAuthenticated()) {

			response.setContentType("application/json");
			
			SummaryService service = Context.getService(SummaryService.class);
			
			List<SummaryIndex> indexes = service.getIndexes(StringUtils.trim(search), displayStart, displayLength);
			int totalIndexes = service.countIndexes(StringUtils.EMPTY);
			int filteredIndexes = service.countIndexes(search);
			
			ObjectMapper mapper = new ObjectMapper();
			JsonFactory factory = mapper.getJsonFactory();
			JsonGenerator generator = factory.createJsonGenerator(response.getOutputStream(), JsonEncoding.UTF8);
			generator.useDefaultPrettyPrinter();
			
			JsonNode rootNode = mapper.createObjectNode();
			((ObjectNode) rootNode).put("sEcho", echo);
			((ObjectNode) rootNode).put("iTotalRecords", totalIndexes);
			((ObjectNode) rootNode).put("iTotalDisplayRecords", filteredIndexes);
			ArrayNode arrayNode = ((ObjectNode) rootNode).putArray("aaData");
			
			for (SummaryIndex index : indexes) {
				ArrayNode innerArray = arrayNode.addArray();
				
				innerArray.add(index.getIndexId());
				
				String identifier = "N/A";
				Patient patient = index.getPatient();
				if (patient != null && patient.getPatientIdentifier() != null)
					identifier = patient.getPatientIdentifier().getIdentifier();
				innerArray.add(identifier);
				
				innerArray.add(patient.getGivenName());
				innerArray.add(patient.getMiddleName());
				innerArray.add(patient.getFamilyName());
				
				Location location = index.getLocation();
				innerArray.add(location == null ? "N/A" : location.getName());
				
				Date returnDate = index.getReturnDate();
				innerArray.add(returnDate == null ? "N/A" : Context.getDateFormat().format(returnDate));
				
				SummaryTemplate template = index.getTemplate();
				innerArray.add(template == null ? "N/A" : template.getName());
				
				Date initialDate = index.getInitialDate();
				innerArray.add(initialDate == null ? "N/A" : Context.getDateFormat().format(initialDate));
				
				Date generatedDate = index.getGeneratedDate();
				innerArray.add(generatedDate == null ? "N/A" : Context.getDateFormat().format(generatedDate));
			}
			
			mapper.writeTree(generator, rootNode);
			generator.close();
		}
	}
}
