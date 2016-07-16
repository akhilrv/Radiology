/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.order;

import java.util.Date;
import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderContext;
import org.openmrs.api.OrderService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.radiology.RadiologyProperties;
import org.openmrs.module.radiology.study.RadiologyStudyService;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
class RadiologyOrderServiceImpl extends BaseOpenmrsService implements RadiologyOrderService {
    
    
    private RadiologyOrderDAO radiologyOrderDAO;
    
    private RadiologyStudyService radiologyStudyService;
    
    private OrderService orderService;
    
    private EncounterService encounterService;
    
    private RadiologyProperties radiologyProperties;
    
    public void setRadiologyOrderDAO(RadiologyOrderDAO radiologyOrderDAO) {
        this.radiologyOrderDAO = radiologyOrderDAO;
    }
    
    public void setRadiologyStudyService(RadiologyStudyService radiologyStudyService) {
        this.radiologyStudyService = radiologyStudyService;
    }
    
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
    
    public void setEncounterService(EncounterService encounterService) {
        this.encounterService = encounterService;
    }
    
    public void setRadiologyProperties(RadiologyProperties radiologyProperties) {
        this.radiologyProperties = radiologyProperties;
    }
    
    /**
     * @see RadiologyOrderService#placeRadiologyOrder(RadiologyOrder)
     */
    @Override
    @Transactional
    public RadiologyOrder placeRadiologyOrder(RadiologyOrder radiologyOrder) {
        
        if (radiologyOrder == null) {
            throw new IllegalArgumentException("radiologyOrder cannot be null");
        }
        
        if (radiologyOrder.getOrderId() != null) {
            throw new IllegalArgumentException("Cannot edit an existing RadiologyOrder");
        }
        
        if (radiologyOrder.getStudy() == null) {
            throw new IllegalArgumentException("radiologyOrder.study cannot be null");
        }
        
        if (radiologyOrder.getStudy()
                .getModality() == null) {
            throw new IllegalArgumentException("radiologyOrder.study.modality cannot be null");
        }
        
        final Encounter encounter =
                saveRadiologyOrderEncounter(radiologyOrder.getPatient(), radiologyOrder.getOrderer(), new Date());
        encounter.addOrder(radiologyOrder);
        
        final OrderContext orderContext = new OrderContext();
        orderContext.setCareSetting(radiologyProperties.getRadiologyCareSetting());
        orderContext.setOrderType(radiologyProperties.getRadiologyTestOrderType());
        
        final RadiologyOrder result = (RadiologyOrder) orderService.saveOrder(radiologyOrder, orderContext);
        this.radiologyStudyService.saveRadiologyStudy(result.getStudy());
        return result;
    }
    
    /**
     * Save radiology order encounter for given parameters
     * 
     * @param patient the encounter patient
     * @param provider the encounter provider
     * @param encounterDateTime the encounter date
     * @return radiology order encounter for given parameters
     * @should create radiology order encounter
     */
    private Encounter saveRadiologyOrderEncounter(Patient patient, Provider provider, Date encounterDateTime) {
        Encounter radiologyOrderEncounter = new Encounter();
        radiologyOrderEncounter.setPatient(patient);
        radiologyOrderEncounter.setProvider(radiologyProperties.getRadiologyOrderingProviderEncounterRole(), provider);
        radiologyOrderEncounter.setEncounterDatetime(encounterDateTime);
        radiologyOrderEncounter.setEncounterType(radiologyProperties.getRadiologyOrderEncounterType());
        return encounterService.saveEncounter(radiologyOrderEncounter);
    }
    
    /**
     * @throws Exception
     * @see RadiologyOrderService#discontinueRadiologyOrder(RadiologyOrder, Provider, String)
     */
    @Override
    @Transactional
    public Order discontinueRadiologyOrder(RadiologyOrder radiologyOrderToDiscontinue, Provider orderer,
            String nonCodedDiscontinueReason) throws Exception {
        
        if (radiologyOrderToDiscontinue == null) {
            throw new IllegalArgumentException("radiologyOrder cannot be null");
        }
        
        if (radiologyOrderToDiscontinue.getOrderId() == null) {
            throw new IllegalArgumentException("can only discontinue existing RadiologyOrder. orderId is null");
        }
        
        if (radiologyOrderToDiscontinue.isDiscontinuedRightNow()) {
            throw new IllegalArgumentException("radiologyOrder is already discontinued");
        }
        
        if (radiologyOrderToDiscontinue.isInProgress()) {
            throw new IllegalArgumentException("radiologyOrder is in progress");
        }
        
        if (radiologyOrderToDiscontinue.isCompleted()) {
            throw new IllegalArgumentException("radiologyOrder is completed");
        }
        
        if (orderer == null) {
            throw new IllegalArgumentException("provider cannot be null");
        }
        
        final Encounter encounter =
                this.saveRadiologyOrderEncounter(radiologyOrderToDiscontinue.getPatient(), orderer, new Date());
        
        return this.orderService.discontinueOrder(radiologyOrderToDiscontinue, nonCodedDiscontinueReason, null, orderer,
            encounter);
    }
    
    /**
     * @see RadiologyOrderService#getRadiologyOrder(Integer)
     */
    @Override
    public RadiologyOrder getRadiologyOrder(Integer orderId) {
        
        if (orderId == null) {
            throw new IllegalArgumentException("orderId cannot be null");
        }
        
        return radiologyOrderDAO.getRadiologyOrder(orderId);
    }
    
    /**
     * @see RadiologyOrderService#getRadiologyOrderByUuid(String)
     */
    @Override
    public RadiologyOrder getRadiologyOrderByUuid(String uuid) {
        
        if (uuid == null) {
            throw new IllegalArgumentException("uuid cannot be null");
        }
        
        return radiologyOrderDAO.getRadiologyOrderByUuid(uuid);
    }
    
    /**
     * @see RadiologyOrderService#getRadiologyOrdersByPatient(Patient)
     */
    @Override
    public List<RadiologyOrder> getRadiologyOrdersByPatient(Patient patient) {
        
        if (patient == null) {
            throw new IllegalArgumentException("patient cannot be null");
        }
        
        return radiologyOrderDAO.getRadiologyOrdersByPatient(patient);
    }
    
    /**
     * @see RadiologyOrderService#getRadiologyOrdersByPatients
     */
    @Override
    public List<RadiologyOrder> getRadiologyOrdersByPatients(List<Patient> patients) {
        
        if (patients == null) {
            throw new IllegalArgumentException("patients cannot be null");
        }
        
        return radiologyOrderDAO.getRadiologyOrdersByPatients(patients);
    }
}
