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
package org.openmrs.module.clinicalsummary.engine;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.SummaryService;

/**
 *
 */
public class VelocityContextUtils {
	
	public Integer makeInteger(Double d) {
		if (d == null)
			return Integer.valueOf(0);
		return Integer.valueOf(d.intValue());
	}
	
	public String formatDate(Date date) {
		
		if (date == null)
			return "";
		
		return Context.getDateFormat().format(date);
	}
	
	public String printConceptName(Concept concept) {
		
		if (concept == null)
			return "";

		// use the best name as the default name
		String name = concept.getBestName(Context.getLocale()).getName();
		Locale locale = Context.getLocale();
		for (ConceptName conceptName : concept.getNames()) {
			Locale conceptLocale = conceptName.getLocale();
			if (!StringUtils.equalsIgnoreCase(conceptLocale.getLanguage(), locale.getLanguage()))
				continue;
			
			// search for a shorter name if available
			if (StringUtils.length(conceptName.getName()) < 5 && Pattern.matches("(\\s*\\w+\\s*)+", conceptName.getName()))
				name = conceptName.getName();
		}
		
		return name;
	}
	
	public Result eval(Patient patient, LogicCriteria criteria) {
		SummaryService summaryService = Context.getService(SummaryService.class);
		return summaryService.evalCriteria(patient, criteria);
	}
	
	public Result eval(Patient patient, String token, Map<String, Object> parameters) {
		SummaryService summaryService = Context.getService(SummaryService.class);
		return summaryService.evalCriteria(patient, token, parameters);
	}
	
	public LogicCriteria parse(String token) {
		SummaryService summaryService = Context.getService(SummaryService.class);
		return summaryService.parseToken(token);
	}
	
}
