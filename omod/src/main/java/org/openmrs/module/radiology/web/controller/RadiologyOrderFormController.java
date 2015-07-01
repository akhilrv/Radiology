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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.radiology.DicomUtils.OrderRequest;
import org.openmrs.module.radiology.Modality;
import org.openmrs.module.radiology.MwlStatus;
import org.openmrs.module.radiology.PerformedProcedureStepStatus;
import org.openmrs.module.radiology.RadiologyService;
import org.openmrs.module.radiology.RequestedProcedurePriority;
import org.openmrs.module.radiology.Roles;
import org.openmrs.module.radiology.ScheduledProcedureStepStatus;
import org.openmrs.module.radiology.Study;
import org.openmrs.module.radiology.Utils;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.EncounterEditor;
import org.openmrs.propertyeditor.OrderTypeEditor;
import org.openmrs.propertyeditor.PatientEditor;
import org.openmrs.propertyeditor.UserEditor;
import org.openmrs.validator.OrderValidator;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RadiologyOrderFormController {
	
	// private Log log = LogFactory.getLog(this.getClass());
	/**
	 * @return Radiology module service instance
	 */
	static RadiologyService radiologyService() {
		return Context.getService(RadiologyService.class);
	}
	
	@InitBinder
	void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(OrderType.class, new OrderTypeEditor());
		binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor("t", "f", true));
		binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
		binder.registerCustomEditor(Concept.class, new ConceptEditor());
		binder.registerCustomEditor(Date.class, new CustomDateEditor(Context.getDateFormat(), true));
		binder.registerCustomEditor(User.class, new UserEditor());
		binder.registerCustomEditor(Patient.class, new PatientEditor());
		binder.registerCustomEditor(Encounter.class, new EncounterEditor());
	}
	
	@RequestMapping(value = "/module/radiology/radiologyOrder.form", method = RequestMethod.POST)
	protected ModelAndView post(HttpServletRequest request,
	        @RequestParam(value = "study_id", required = false) Integer studyId,
	        @RequestParam(value = "patient_id", required = false) Integer patientId, @ModelAttribute("study") Study study,
	        BindingResult sErrors, @ModelAttribute("order") Order order, BindingResult oErrors) throws Exception {
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("module/radiology/radiologyOrderForm");
		if (study.setup(order, studyId)) {
			
			new OrderValidator().validate(order, oErrors);
			boolean ok = executeCommand(order, study, request);
			if (ok) {
				if (patientId == null)
					mav.setViewName("redirect:/module/radiology/radiologyOrder.list");
				else
					mav.setViewName("redirect:/patientDashboard.form?patientId=" + patientId);
			} else {
				populate(mav, order, study);
			}
		} else {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "radiology.studyPerformed");
			populate(mav, order, study);
		}
		return mav;
	}
	
	@RequestMapping(value = "/module/radiology/radiologyOrder.form", method = RequestMethod.GET)
	protected ModelAndView get(@RequestParam(value = "orderId", required = false) Integer orderId,
	        @RequestParam(value = "patientId", required = false) Integer patientId) {
		ModelAndView mav = new ModelAndView("module/radiology/radiologyOrderForm");
		OrderService os = Context.getOrderService();
		Order order = null;
		Study study = null;
		
		if (Context.isAuthenticated()) {
			if (orderId != null) {
				order = os.getOrder(orderId);
				study = radiologyService().getStudyByOrderId(orderId);
			} else {
				study = new Study();
				order = new Order();
				if (patientId != null) {
					order.setPatient(Context.getPatientService().getPatient(patientId));
					mav.addObject("patientId", patientId);
				}
				User u = Context.getAuthenticatedUser();
				if (u.hasRole(Roles.ReferringPhysician, true) && order.getOrderer() == null)
					order.setOrderer(u);
			}
		}
		populate(mav, order, study);
		return mav;
	}
	
	private void populate(ModelAndView mav, Order order, Study study) {
		if (Context.isAuthenticated()) {
			mav.addObject("order", order);
			mav.addObject("study", study);
			boolean referring = Context.getAuthenticatedUser().hasRole(Roles.ReferringPhysician, true);
			mav.addObject("referring", referring);
			boolean scheduler = Context.getAuthenticatedUser().hasRole(Roles.Scheduler, true);
			mav.addObject("scheduler", scheduler);
			boolean performing = Context.getAuthenticatedUser().hasRole(Roles.PerformingPhysician, true);
			mav.addObject("performing", performing);
			boolean reading = Context.getAuthenticatedUser().hasRole(Roles.ReadingPhysician, true);
			mav.addObject("reading", reading);
			mav.addObject("super", !referring && !scheduler && !performing && !reading);
		}
	}
	
	@ModelAttribute("modalities")
	private Map<String, String> getModalityList() {
		
		Map<String, String> modalities = new HashMap<String, String>();
		
		for (Modality modality : Modality.values()) {
			modalities.put(modality.name(), modality.getFullName());
		}
		
		return modalities;
	}
	
	@ModelAttribute("requestedProcedurePriorities")
	private List<String> getRequestedProcedurePriorityList() {
		
		List<String> requestedProcedurePriorities = new LinkedList<String>();
		
		for (RequestedProcedurePriority requestedProcedurePriority : RequestedProcedurePriority.values()) {
			requestedProcedurePriorities.add(requestedProcedurePriority.name());
		}
		
		return requestedProcedurePriorities;
	}
	
	@ModelAttribute("scheduledProcedureStepStatuses")
	private Map<String, String> getScheduledProcedureStepStatusList() {
		
		Map<String, String> scheduledProcedureStepStatuses = new LinkedHashMap<String, String>();
		scheduledProcedureStepStatuses.put("", "Select");
		
		for (ScheduledProcedureStepStatus scheduledProcedureStepStatus : ScheduledProcedureStepStatus.values()) {
			scheduledProcedureStepStatuses.put(scheduledProcedureStepStatus.name(), scheduledProcedureStepStatus.name());
		}
		
		return scheduledProcedureStepStatuses;
	}
	
	@ModelAttribute("performedStatuses")
	private Map<String, String> getPerformedStatusList() {
		
		Map<String, String> performedStatuses = new HashMap<String, String>();
		performedStatuses.put("", "Select");
		
		for (PerformedProcedureStepStatus performedStatus : PerformedProcedureStepStatus.values()) {
			performedStatuses.put(performedStatus.name(), performedStatus.name());
		}
		
		return performedStatuses;
	}
	
	protected boolean executeCommand(Order order, Study study, HttpServletRequest request) {
		if (!Context.isAuthenticated()) {
			return false;
		}
		
		OrderService orderService = Context.getOrderService();
		try {
			if (request.getParameter("saveOrder") != null) {
				orderService.saveOrder(order);
				study.setOrderId(order.getOrderId());
				radiologyService().saveStudy(study);
				//Assigning Study UID                                
				String studyUID = Utils.studyPrefix() + study.getId();
				System.out.println("Radiology order received with StudyUID : " + studyUID + " Order ID : "
				        + order.getOrderId());
				study.setStudyInstanceUid(studyUID);
				radiologyService().saveStudy(study);
				Order o = orderService.getOrder(order.getOrderId());
				radiologyService().sendModalityWorklist(radiologyService().getStudyByOrderId(o.getOrderId()),
				    OrderRequest.Save_Order);
				
				//Saving Study into Database.
				if (radiologyService().getStudyByOrderId(o.getOrderId()).getMwlStatus() == MwlStatus.SAVE_ERR
				        || radiologyService().getStudyByOrderId(o.getOrderId()).getMwlStatus() == MwlStatus.UPDATE_ERR) {
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "radiology.savedFailWorklist");
				} else {
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Order.saved");
				}
			} else if (request.getParameter("voidOrder") != null) {
				Order o = orderService.getOrder(order.getOrderId());
				radiologyService().sendModalityWorklist(radiologyService().getStudyByOrderId(o.getOrderId()),
				    OrderRequest.Void_Order);
				if (radiologyService().getStudyByOrderId(o.getOrderId()).getMwlStatus() == MwlStatus.VOID_OK) {
					orderService.voidOrder(o, order.getVoidReason());
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Order.voidedSuccessfully");
				} else {
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "radiology.failWorklist");
				}
			} else if (request.getParameter("unvoidOrder") != null) {
				Order o = orderService.getOrder(order.getOrderId());
				radiologyService().sendModalityWorklist(radiologyService().getStudyByOrderId(o.getOrderId()),
				    OrderRequest.Unvoid_Order);
				if (radiologyService().getStudyByOrderId(o.getOrderId()).getMwlStatus() == MwlStatus.UNVOID_OK) {
					orderService.unvoidOrder(o);
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Order.unvoidedSuccessfully");
				} else {
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "radiology.failWorklist");
				}
			} else if (request.getParameter("discontinueOrder") != null) {
				Order o = orderService.getOrder(order.getOrderId());
				radiologyService().sendModalityWorklist(radiologyService().getStudyByOrderId(o.getOrderId()),
				    OrderRequest.Discontinue_Order);
				if (radiologyService().getStudyByOrderId(o.getOrderId()).getMwlStatus() == MwlStatus.DISCONTINUE_OK) {
					orderService.discontinueOrder(o, order.getDiscontinuedReason(), order.getDiscontinuedDate());
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Order.discontinuedSuccessfully");
				} else {
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "radiology.failWorklist");
				}
			} else if (request.getParameter("undiscontinueOrder") != null) {
				Order o = orderService.getOrder(order.getOrderId());
				radiologyService().sendModalityWorklist(radiologyService().getStudyByOrderId(o.getOrderId()),
				    OrderRequest.Undiscontinue_Order);
				if (radiologyService().getStudyByOrderId(o.getOrderId()).getMwlStatus() == MwlStatus.UNDISCONTINUE_OK) {
					orderService.undiscontinueOrder(o);
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Order.undiscontinuedSuccessfully");
				} else {
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "radiology.failWorklist");
				}
			}
		}
		catch (Exception ex) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, ex.getMessage());
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
}
