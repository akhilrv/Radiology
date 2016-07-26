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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openmrs.Patient;

/**
 * Tests {@link RadiologyOrderSearchCriteria}.
 */
public class RadiologyOrderSearchCriteriaTest {
    
    
    private RadiologyOrderSearchCriteria radiologyOrderSearchCriteria;
    
    /**
     * @see RadiologyOrderSearchCriteria.Builder#build()
     * @verifies create a new radiology order search criteria instance with patient if patient is set
     */
    @Test
    public void build_createANewRadiologyOrderSearchCriteriaInstanceWithPatientIfPatientIsSet() throws Exception {
        
        Patient patient = new Patient(1);
        radiologyOrderSearchCriteria = new RadiologyOrderSearchCriteria.Builder().withPatient(patient)
                .build();
        
        assertTrue(radiologyOrderSearchCriteria.getPatient()
                .equals(patient));
        assertFalse(radiologyOrderSearchCriteria.getIncludeVoided());
        assertFalse(radiologyOrderSearchCriteria.getIncludeDiscontinued());
    }
    
    /**
     * @see RadiologyOrderSearchCriteria.Builder#build()
     * @verifies create a new radiology order search criteria instance with include voided set to true if voided orders should be included
     */
    @Test
    public void
            build_createANewRadiologyOrderSearchCriteriaInstanceWithIncludeVoidedSetToTrueIfVoidedOrdersShouldBeIncluded()
                    throws Exception {
        
        radiologyOrderSearchCriteria = new RadiologyOrderSearchCriteria.Builder().includeVoided()
                .build();
        
        assertTrue(radiologyOrderSearchCriteria.getIncludeVoided());
        assertNull(radiologyOrderSearchCriteria.getPatient());
        assertFalse(radiologyOrderSearchCriteria.getIncludeDiscontinued());
    }
    
    /**
     * @see RadiologyOrderSearchCriteria.Builder#build()
     * @verifies create a new radiology order search criteria instance with include discontinued set to true if discontinued orders should be included
     */
    @Test
    public void
            build_createANewRadiologyOrderSearchCriteriaInstanceWithIncludeDiscontinuedSetToTrueIfDiscontinuedOrdersShouldBeIncluded()
                    throws Exception {
        
        radiologyOrderSearchCriteria = new RadiologyOrderSearchCriteria.Builder().includeDiscontinued()
                .build();
        
        assertTrue(radiologyOrderSearchCriteria.getIncludeDiscontinued());
        assertNull(radiologyOrderSearchCriteria.getPatient());
        assertFalse(radiologyOrderSearchCriteria.getIncludeVoided());
        
    }
}
