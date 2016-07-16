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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.radiology.order.RadiologyOrder;

/**
 * Hibernate specific RadiologyStudy related functions. This class should not be used directly. All calls
 * should go through the {@link org.openmrs.module.radiology.study.RadiologyStudyService} methods.
 *
 * @see org.openmrs.module.radiology.study.RadiologyStudyDAO
 * @see org.openmrs.module.radiology.study.RadiologyStudyService
 */
class HibernateRadiologyStudyDAO implements RadiologyStudyDAO {
    
    
    private SessionFactory sessionFactory;
    
    /**
     * Set session factory that allows us to connect to the database that Hibernate knows about.
     *
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * @see org.openmrs.module.radiology.study.RadiologyStudyService#saveRadiologyStudy(RadiologyStudy)
     */
    @Override
    public RadiologyStudy saveRadiologyStudy(RadiologyStudy radiologyStudy) {
        sessionFactory.getCurrentSession()
                .saveOrUpdate(radiologyStudy);
        return radiologyStudy;
    }
    
    /**
     * @see org.openmrs.module.radiology.study.RadiologyStudyService#getRadiologyStudy(Integer)
     */
    @Override
    public RadiologyStudy getRadiologyStudy(Integer studyId) {
        return (RadiologyStudy) sessionFactory.getCurrentSession()
                .get(RadiologyStudy.class, studyId);
    }
    
    /**
     * @see org.openmrs.module.radiology.study.RadiologyStudyService#getRadiologyStudyByUuid(String)
     */
    @Override
    public RadiologyStudy getRadiologyStudyByUuid(String uuid) {
        
        return (RadiologyStudy) sessionFactory.getCurrentSession()
                .createCriteria(RadiologyStudy.class)
                .add(Restrictions.eq("uuid", uuid))
                .uniqueResult();
    }
    
    /**
     * @see org.openmrs.module.radiology.study.RadiologyStudyService#getRadiologyStudyByOrderId(Integer)
     */
    @Override
    public RadiologyStudy getRadiologyStudyByOrderId(Integer orderId) {
        final String query = "from RadiologyStudy s where s.radiologyOrder.orderId = '" + orderId + "'";
        return (RadiologyStudy) sessionFactory.getCurrentSession()
                .createQuery(query)
                .uniqueResult();
    }
    
    /**
     * @see org.openmrs.module.radiology.study.RadiologyStudyService#getRadiologyStudyByStudyInstanceUid(String)
     */
    @Override
    public RadiologyStudy getRadiologyStudyByStudyInstanceUid(String studyInstanceUid) {
        return (RadiologyStudy) sessionFactory.getCurrentSession()
                .createCriteria(RadiologyStudy.class)
                .add(Restrictions.eq("studyInstanceUid", studyInstanceUid))
                .uniqueResult();
    }
    
    /**
     * @see org.openmrs.module.radiology.study.RadiologyStudyService#getRadiologyStudiesByRadiologyOrders
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<RadiologyStudy> getRadiologyStudiesByRadiologyOrders(List<RadiologyOrder> radiologyOrders) {
        List<RadiologyStudy> result = null;
        if (!radiologyOrders.isEmpty()) {
            final Criteria studyCriteria = sessionFactory.getCurrentSession()
                    .createCriteria(RadiologyStudy.class);
            addRestrictionOnRadiologyOrders(studyCriteria, radiologyOrders);
            result = (List<RadiologyStudy>) studyCriteria.list();
        }
        
        if (result == null) {
            return new ArrayList<RadiologyStudy>();
        } else {
            return result;
        }
    }
    
    /**
     * Adds an in restriction for given radiologyOrders on given criteria if radiologyOrders is not
     * empty
     *
     * @param criteria criteria on which in restriction is set if radiologyOrders is not empty
     * @param radiologyOrders radiology order list for which in restriction will be set
     */
    private void addRestrictionOnRadiologyOrders(Criteria criteria, List<RadiologyOrder> radiologyOrders) {
        if (!radiologyOrders.isEmpty()) {
            criteria.add(Restrictions.in("radiologyOrder", radiologyOrders));
        }
    }
}
