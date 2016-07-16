/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.study;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.hibernate.cfg.Environment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.radiology.Modality;
import org.openmrs.module.radiology.dicom.code.PerformedProcedureStepStatus;
import org.openmrs.module.radiology.dicom.code.ScheduledProcedureStepStatus;
import org.openmrs.module.radiology.order.RadiologyOrder;
import org.openmrs.module.radiology.order.RadiologyOrderService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests {@link RadiologyStudyService}.
 */
public class RadiologyStudyServiceComponentTest extends BaseModuleContextSensitiveTest {
    
    
    private static final String TEST_DATASET =
            "org/openmrs/module/radiology/include/RadiologyStudyServiceComponentTestDataset.xml";
    
    private static final int PATIENT_ID_WITH_TWO_STUDIES_AND_NO_NON_RADIOLOGY_ORDER = 70021;
    
    private static final int RADIOLOGY_ORDER_ID_WITHOUT_STUDY = 2004;
    
    private static final int EXISTING_RADIOLOGY_ORDER_ID = 2001;
    
    private static final int NON_EXISTING_RADIOLOGY_ORDER_ID = 99999;
    
    private static final String EXISTING_STUDY_UUID = "dde7399b-6092-4a3d-88a2-405b6b4499fc";
    
    private static final String NON_EXISTING_STUDY_UUID = "99999999-6092-4a3d-88a2-405b6b4499fc";
    
    private static final String EXISTING_STUDY_INSTANCE_UID = "1.2.826.0.1.3680043.8.2186.1.1";
    
    private static final String NON_EXISTING_STUDY_INSTANCE_UID = "1.2.826.0.1.3680043.8.2186.1.9999";
    
    private static final int EXISTING_STUDY_ID = 1;
    
    private static final int NON_EXISTING_STUDY_ID = 99999;
    
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private RadiologyOrderService radiologyOrderService;
    
    @Autowired
    private RadiologyStudyService radiologyStudyService;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    /**
     * Overriding following method is necessary to enable MVCC which is disabled by default in DB h2
     * used for the component tests. This prevents following exception:
     * org.hibernate.exception.GenericJDBCException: could not load an entity:
     * [org.openmrs.GlobalProperty#order.nextOrderNumberSeed] due to "Timeout trying to lock table "
     * GLOBAL_PROPERTY"; SQL statement:" which occurs in all tests touching methods that call
     * orderService.saveOrder()
     */
    @Override
    public Properties getRuntimeProperties() {
        Properties result = super.getRuntimeProperties();
        String url = result.getProperty(Environment.URL);
        if (url.contains("jdbc:h2:") && !url.contains(";MVCC=TRUE")) {
            result.setProperty(Environment.URL, url + ";MVCC=TRUE");
        }
        return result;
    }
    
    @Before
    public void setUp() throws Exception {
        executeDataSet(TEST_DATASET);
    }
    
    /**
     * @see RadiologyStudyService#saveRadiologyStudy(RadiologyStudy)
     * @verifies create new radiology study from given radiology study
     */
    @Test
    public void saveRadiologyStudy_shouldCreateNewRadiologyStudyFromGivenRadiologyStudy() throws Exception {
        
        RadiologyStudy radiologyStudy = getUnsavedStudy();
        RadiologyOrder radiologyOrder = radiologyOrderService.getRadiologyOrder(RADIOLOGY_ORDER_ID_WITHOUT_STUDY);
        radiologyOrder.setStudy(radiologyStudy);
        
        RadiologyStudy createdStudy = radiologyStudyService.saveRadiologyStudy(radiologyStudy);
        
        assertNotNull(createdStudy);
        assertThat(createdStudy, is(radiologyStudy));
        assertThat(createdStudy.getStudyId(), is(radiologyStudy.getStudyId()));
        assertNotNull(createdStudy.getStudyInstanceUid());
        assertThat(createdStudy.getModality(), is(radiologyStudy.getModality()));
        assertThat(createdStudy.getRadiologyOrder(), is(radiologyStudy.getRadiologyOrder()));
    }
    
    /**
     * Convenience method to get a RadiologyStudy object with all required values filled (except
     * radiologyOrder) in but which is not yet saved in the database
     * 
     * @return RadiologyStudy object that can be saved to the database
     */
    public RadiologyStudy getUnsavedStudy() {
        
        RadiologyStudy radiologyStudy = new RadiologyStudy();
        radiologyStudy.setModality(Modality.CT);
        radiologyStudy.setScheduledStatus(ScheduledProcedureStepStatus.SCHEDULED);
        return radiologyStudy;
    }
    
    /**
     * @see RadiologyStudyService#saveRadiologyStudy(RadiologyStudy)
     * @verifies update existing radiology study
     */
    @Test
    public void saveRadiologyStudy_shouldUpdateExistingRadiologyStudy() throws Exception {
        
        RadiologyStudy existingStudy = radiologyStudyService.getRadiologyStudy(EXISTING_STUDY_ID);
        Modality modalityPreUpdate = existingStudy.getModality();
        Modality modalityPostUpdate = Modality.XA;
        existingStudy.setModality(modalityPostUpdate);
        
        RadiologyStudy updatedStudy = radiologyStudyService.saveRadiologyStudy(existingStudy);
        
        assertNotNull(updatedStudy);
        assertThat(updatedStudy, is(existingStudy));
        assertThat(modalityPreUpdate, is(not(modalityPostUpdate)));
        assertThat(updatedStudy.getModality(), is(modalityPostUpdate));
    }
    
    /**
     * @see RadiologyStudyService#saveRadiologyStudy(RadiologyStudy)
     * @verifies throw illegal argument exception if given null
     */
    @Test
    public void saveRadiologyStudy_shouldThrowIllegalArgumentExceptionIfGivenNull() throws Exception {
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("radiologyStudy cannot be null");
        radiologyStudyService.saveRadiologyStudy(null);
    }
    
    /**
     * @see RadiologyStudyService#updateStudyPerformedStatus(String,PerformedProcedureStepStatus)
     * @verifies update performed status of radiology study associated with given study instance uid
     */
    @Test
    public void updateStudyPerformedStatus_shouldUpdatePerformedStatusOfRadiologyStudyAssociatedWithGivenStudyInstanceUid()
            throws Exception {
        
        RadiologyStudy existingStudy = radiologyStudyService.getRadiologyStudy(EXISTING_STUDY_ID);
        PerformedProcedureStepStatus performedStatusPreUpdate = existingStudy.getPerformedStatus();
        PerformedProcedureStepStatus performedStatusPostUpdate = PerformedProcedureStepStatus.COMPLETED;
        
        RadiologyStudy updatedStudy = radiologyStudyService.updateStudyPerformedStatus(existingStudy.getStudyInstanceUid(),
            performedStatusPostUpdate);
        
        assertNotNull(updatedStudy);
        assertThat(updatedStudy, is(existingStudy));
        assertThat(performedStatusPreUpdate, is(not(performedStatusPostUpdate)));
        assertThat(updatedStudy.getPerformedStatus(), is(performedStatusPostUpdate));
    }
    
    /**
     * @see RadiologyStudyService#updateStudyPerformedStatus(String,PerformedProcedureStepStatus)
     * @verifies throw illegal argument exception if study instance uid is null
     */
    @Test
    public void updateStudyPerformedStatus_shouldThrowIllegalArgumentExceptionIfStudyInstanceUidIsNull() throws Exception {
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("studyInstanceUid cannot be null");
        radiologyStudyService.updateStudyPerformedStatus(null, PerformedProcedureStepStatus.COMPLETED);
    }
    
    /**
     * @see RadiologyStudyService#updateStudyPerformedStatus(String,PerformedProcedureStepStatus)
     * @verifies throw illegal argument exception if performed status is null
     */
    @Test
    public void updateStudyPerformedStatus_shouldThrowIllegalArgumentExceptionIfPerformedStatusIsNull() throws Exception {
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("performedStatus cannot be null");
        radiologyStudyService.updateStudyPerformedStatus(EXISTING_STUDY_INSTANCE_UID, null);
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudy(Integer)
     * @verifies should return radiology study matching given study id
     */
    @Test
    public void getRadiologyStudy_shouldReturnRadiologyStudyMatchingGivenStudyId() throws Exception {
        
        RadiologyStudy radiologyStudy = radiologyStudyService.getRadiologyStudy(EXISTING_STUDY_ID);
        
        assertNotNull(radiologyStudy);
        assertThat(radiologyStudy.getRadiologyOrder()
                .getOrderId(),
            is(EXISTING_RADIOLOGY_ORDER_ID));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudy(Integer)
     * @verifies return null if no match was found
     */
    @Test
    public void getRadiologyStudy_shouldReturnNullIfNoMatchWasFound() throws Exception {
        
        assertNull(radiologyStudyService.getRadiologyStudy(NON_EXISTING_STUDY_ID));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudy(Integer)
     * @verifies throw illegal argument exception if given null
     */
    @Test
    public void getRadiologyStudy_shouldThrowIllegalArgumentExceptionIfGivenNull() throws Exception {
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("studyId cannot be null");
        radiologyStudyService.getRadiologyStudy(null);
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudyByUuid(String)
     * @verifies return radiology study matching given uuid
     */
    @Test
    public void getRadiologyStudyByUuid_shouldReturnRadiologyStudyMatchingGivenUuid() throws Exception {
        
        RadiologyStudy radiologyStudy = radiologyStudyService.getRadiologyStudyByUuid(EXISTING_STUDY_UUID);
        
        assertNotNull(radiologyStudy);
        assertThat(radiologyStudy.getUuid(), is(EXISTING_STUDY_UUID));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudyByUuid(String)
     * @verifies return null if no match was found
     */
    @Test
    public void getRadiologyStudyByUuid_shouldReturnNullIfNoMatchWasFound() throws Exception {
        
        assertNull(radiologyStudyService.getRadiologyStudyByUuid(NON_EXISTING_STUDY_UUID));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudyByUuid(String)
     * @verifies throw illegal argument exception if given null
     */
    @Test
    public void getRadiologyStudyByUuid_shouldThrowIllegalArgumentExceptionIfGivenNull() throws Exception {
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("uuid cannot be null");
        radiologyStudyService.getRadiologyStudyByUuid(null);
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudyByOrderId(Integer)
     * @verifies return radiology study associated with radiology order for which order id is given
     */
    @Test
    public void getRadiologyStudyByOrderId_shouldReturnRadiologyStudyAssociatedWithRadiologyOrderForWhichOrderIdIsGiven()
            throws Exception {
        
        RadiologyStudy radiologyStudy = radiologyStudyService.getRadiologyStudyByOrderId(EXISTING_RADIOLOGY_ORDER_ID);
        
        assertNotNull(radiologyStudy);
        assertThat(radiologyStudy.getRadiologyOrder()
                .getOrderId(),
            is(EXISTING_RADIOLOGY_ORDER_ID));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudyByOrderId(Integer)
     * @verifies return null if no match was found
     */
    @Test
    public void getRadiologyStudyByOrderId_shouldReturnNullIfNoMatchIsFound() {
        
        assertNull(radiologyStudyService.getRadiologyStudyByOrderId(NON_EXISTING_RADIOLOGY_ORDER_ID));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudyByOrderId(Integer)
     * @verifies throw illegal argument exception if given null
     */
    @Test
    public void getRadiologyStudyByOrderId_shouldThrowIllegalArgumentExceptionIfGivenNull() {
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("orderId cannot be null");
        assertNull(radiologyStudyService.getRadiologyStudyByOrderId(null));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudyByStudyInstanceUid(String)
     * @verifies return radiology study exactly matching given study instance uid
     */
    @Test
    public void getRadiologyStudyByStudyInstanceUid_shouldReturnRadiologyStudyExactlyMatchingGivenStudyInstanceUid()
            throws Exception {
        
        RadiologyStudy radiologyStudy =
                radiologyStudyService.getRadiologyStudyByStudyInstanceUid(EXISTING_STUDY_INSTANCE_UID);
        
        assertNotNull(radiologyStudy);
        assertThat(radiologyStudy.getStudyInstanceUid(), is(EXISTING_STUDY_INSTANCE_UID));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudyByStudyInstanceUid(String)
     * @verifies return null if no match was found
     */
    @Test
    public void getRadiologyStudyByStudyInstanceUid_shouldReturnNullIfNoMatchIsFound() throws Exception {
        
        assertNull(radiologyStudyService.getRadiologyStudyByStudyInstanceUid(NON_EXISTING_STUDY_INSTANCE_UID));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudyByStudyInstanceUid(String)
     * @verifies throw illegal argument exception if given null
     */
    @Test
    public void getRadiologyStudyByStudyInstanceUid_shouldThrowIllegalArgumentExceptionIfGivenNull() throws Exception {
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("studyInstanceUid cannot be null");
        assertNull(radiologyStudyService.getRadiologyStudyByStudyInstanceUid(null));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudiesByRadiologyOrders(List<RadiologyOrder>)
     * @verifies return all radiology studies associated with given radiology orders
     */
    @Test
    public void getRadiologyStudiesByRadiologyOrders_shouldReturnAllRadiologyStudiesAssociatedWithGivenRadiologyOrders()
            throws Exception {
        
        Patient patient = patientService.getPatient(PATIENT_ID_WITH_TWO_STUDIES_AND_NO_NON_RADIOLOGY_ORDER);
        List<RadiologyOrder> radiologyOrders = radiologyOrderService.getRadiologyOrdersByPatient(patient);
        
        List<RadiologyStudy> radiologyStudies = radiologyStudyService.getRadiologyStudiesByRadiologyOrders(radiologyOrders);
        
        assertThat(radiologyStudies.size(), is(radiologyOrders.size()));
        assertThat(radiologyStudies.get(0)
                .getRadiologyOrder(),
            is(radiologyOrders.get(0)));
        assertThat(radiologyStudies.get(1)
                .getRadiologyOrder(),
            is(radiologyOrders.get(1)));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudiesByRadiologyOrders(List<RadiologyOrder>)
     * @verifies return empty list given radiology orders without associated radiology studies
     */
    @Test
    public void
            getRadiologyStudiesByRadiologyOrders_shouldReturnEmptyListGivenRadiologyOrdersWithoutAssociatedRadiologyStudies()
                    throws Exception {
        
        RadiologyOrder radiologyOrderWithoutStudy =
                radiologyOrderService.getRadiologyOrder(RADIOLOGY_ORDER_ID_WITHOUT_STUDY);
        List<RadiologyOrder> radiologyOrders = Arrays.asList(radiologyOrderWithoutStudy);
        
        List<RadiologyStudy> radiologyStudies = radiologyStudyService.getRadiologyStudiesByRadiologyOrders(radiologyOrders);
        
        assertThat(radiologyOrders.size(), is(1));
        assertThat(radiologyStudies.size(), is(0));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudiesByRadiologyOrders(List<RadiologyOrder>)
     * @verifies return empty list given empty radiology order list
     */
    @Test
    public void getRadiologyStudiesByRadiologyOrders_shouldReturnEmptyListGivenEmptyRadiologyOrderList() throws Exception {
        
        List<RadiologyOrder> orders = new ArrayList<RadiologyOrder>();
        
        List<RadiologyStudy> radiologyStudies = radiologyStudyService.getRadiologyStudiesByRadiologyOrders(orders);
        
        assertThat(orders.size(), is(0));
        assertThat(radiologyStudies.size(), is(0));
    }
    
    /**
     * @see RadiologyStudyService#getRadiologyStudiesByRadiologyOrders(List<RadiologyOrder>)
     * @should throw illegal argument exception if given null
     */
    @Test
    public void getRadiologyStudiesByRadiologyOrders_shouldThrowIllegalArgumentExceptionGivenNull() throws Exception {
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("radiologyOrders cannot be null");
        radiologyStudyService.getRadiologyStudiesByRadiologyOrders(null);
    }
}
