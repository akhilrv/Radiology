/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.web.controller;

import static org.openmrs.module.radiology.RadiologyRolePrivilegeMetadata._Role.RADIOLOGY_READING_PHYSICIAN;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.radiology.RadiologyService;
import org.openmrs.module.radiology.Study;
import org.openmrs.module.radiology.Utils;
import org.openmrs.module.radiology.web.util.StudyStatusColumnGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PortletsController {
	
	Log log = LogFactory.getLog(getClass());
	
	String str = "";
	
	static RadiologyService radiologyService() {
		return Context.getService(RadiologyService.class);
	}
	
	@RequestMapping("/module/radiology/portlets/patientOverview.portlet")
	String getPatientInfoRoute() {
		String route = "module/radiology/portlets/patientOverview";
		return route;
	}
	
	@RequestMapping(value = "/module/radiology/portlets/orderSearch.portlet")
	ModelAndView ordersTable(@RequestParam(value = "patientQuery", required = false) String patientQuery,
	        @RequestParam(value = "startDate", required = false) String startDateS,
	        @RequestParam(value = "finalDate", required = false) String finalDateS,
	        @RequestParam(value = "pending", required = false) boolean pending,
	        @RequestParam(value = "completed", required = false) boolean completed) {
		str = "";
		String route = "module/radiology/portlets/orderSearch";
		ModelAndView mav = new ModelAndView(route);
		
		List<Order> matchedOrders = dateFilter(patientQuery, startDateS, finalDateS, mav);
		// TODO Status filter
		List<Study> studies = radiologyService().getStudiesByOrders(matchedOrders);
		List<String> statuses = new Vector<String>();
		List<String> priorities = new Vector<String>();
		List<String> schedulers = new Vector<String>();
		List<String> performings = new Vector<String>();
		List<String> readings = new Vector<String>();
		List<String> modalities = new Vector<String>();
		List<String> mwlStatuses = new Vector<String>();
		for (Study study : studies) {
			if (study != null) {
				statuses.add(StudyStatusColumnGenerator.getStatusColumnForStudy(Context.getAuthenticatedUser(), study));
				priorities.add(study.getPriority().name());
				schedulers.add(study.scheduler());
				performings.add(study.performing());
				readings.add(study.reading());
				modalities.add(study.getModality().getFullName());
				mwlStatuses.add(study.getMwlStatus().name());
			}
		}
		
		// TODO optimize all the function, get orders and studies(priorities, statuses, etc) in a row
		
		// Response variables
		
		mav.addObject(matchedOrders);
		mav.addObject("statuses", statuses);
		mav.addObject("priorities", priorities);
		mav.addObject("schedulers", schedulers);
		mav.addObject("performings", performings);
		mav.addObject("readings", readings);
		mav.addObject("modalities", modalities);
		mav.addObject("matchedOrdersSize", matchedOrders.size());
		if (Context.getAuthenticatedUser().hasRole(RADIOLOGY_READING_PHYSICIAN, true)) {
			mav.addObject("obsId", "&obsId");
		}
		mav.addObject("mwlStatuses", mwlStatuses);
		
		log.debug("\n***\n" + str + "\n///");
		return mav;
	}
	
	private List<Order> dateFilter(String patientQuery, String startDateS, String finalDateS, ModelAndView mav) {
		Date startDate = null;
		Date finalDate = null;
		try {
			finalDate = Context.getDateFormat().parse(finalDateS);
		}
		catch (ParseException e) {
			finalDate = null;
		}
		try {
			startDate = Context.getDateFormat().parse(startDateS);
		}
		catch (ParseException e) {
			startDate = null;
		}
		
		if (startDate != null && finalDate != null) {
			str += "s.af(fd) " + startDate.after(finalDate) + "\n";
			if (startDate.after(finalDate)) {
				mav.addObject("error", "crossDate");
				return null;
			}
		}
		
		OrderService os = Context.getOrderService();
		PatientService ps = Context.getPatientService();
		List<Order> preMatchedOrders = os.getOrders(Order.class, ps.getPatients(patientQuery), null, null, null, null, Utils
		        .getRadiologyOrderType());
		List<Order> matchedOrders = new Vector<Order>();
		
		try {
			if (startDate == null && finalDate == null) {
				matchedOrders = preMatchedOrders;
			} else if (startDate == null && finalDate != null)
				for (Order order : preMatchedOrders) {
					if (order.getStartDate() != null && order.getStartDate().compareTo(finalDate) <= 0) {
						matchedOrders.add(order);
					}
				}
			else if (finalDate == null && startDate != null)
				for (Order order : preMatchedOrders) {
					if (order.getStartDate() != null && order.getStartDate().compareTo(startDate) >= 0) {
						matchedOrders.add(order);
					}
				}
			
			else
				for (Order order : preMatchedOrders) {
					if (order.getStartDate() != null && order.getStartDate().compareTo(startDate) >= 0
					        && order.getStartDate().compareTo(finalDate) <= 0) {
						matchedOrders.add(order);
					}
				}
		}
		catch (Exception e) {
			// TODO handle exception
		}
		return matchedOrders;
	}
	
}
