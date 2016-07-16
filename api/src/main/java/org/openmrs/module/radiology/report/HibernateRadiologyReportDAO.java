/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.report;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.radiology.order.RadiologyOrder;

/**
 * Hibernate specific RadiologyReport related functions. This class should not be used directly. All
 * calls should go through the {@link org.openmrs.module.radiology.report.RadiologyReportService} methods.
 *
 * @see org.openmrs.module.radiology.report.RadiologyReportDAO
 * @see org.openmrs.module.radiology.report.RadiologyReportService
 */
class HibernateRadiologyReportDAO implements RadiologyReportDAO {
    
    
    private SessionFactory sessionFactory;
    
    /**
     * Set session factory that allows us to connect to the database that Hibernate knows about.
     *
     * @param sessionFactory SessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * @see org.openmrs.module.radiology.report.RadiologyReportService#getRadiologyReport(Integer)
     */
    @Override
    public RadiologyReport getRadiologyReport(Integer reportId) {
        return (RadiologyReport) sessionFactory.getCurrentSession()
                .get(RadiologyReport.class, reportId);
    }
    
    /**
     * @see org.openmrs.module.radiology.report.RadiologyReportService#getRadiologyReportByUuid(String)
     */
    @Override
    public RadiologyReport getRadiologyReportByUuid(String uuid) {
        return (RadiologyReport) sessionFactory.getCurrentSession()
                .createCriteria(RadiologyReport.class)
                .add(Restrictions.eq("uuid", uuid))
                .uniqueResult();
    }
    
    /**
     * @see org.openmrs.module.radiology.report.RadiologyReportService#saveRadiologyReport(RadiologyReport)
     */
    @Override
    public RadiologyReport saveRadiologyReport(RadiologyReport radiologyReport) {
        sessionFactory.getCurrentSession()
                .saveOrUpdate(radiologyReport);
        return radiologyReport;
    }
    
    /**
     * @see org.openmrs.module.radiology.report.RadiologyReportService#hasRadiologyOrderCompletedRadiologyReport(RadiologyOrder)
     *      (RadiologyReport)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean hasRadiologyOrderCompletedRadiologyReport(RadiologyOrder radiologyOrder) {
        final List<RadiologyReport> radiologyReports = sessionFactory.getCurrentSession()
                .createCriteria(RadiologyReport.class)
                .add(Restrictions.eq("radiologyOrder", radiologyOrder))
                .add(Restrictions.eq("reportStatus", RadiologyReportStatus.COMPLETED))
                .list();
        return radiologyReports.size() == 1;
    }
    
    /**
     * @see org.openmrs.module.radiology.report.RadiologyReportService#hasRadiologyOrderClaimedRadiologyReport(RadiologyOrder)
     */
    @SuppressWarnings("unchecked")
    public boolean hasRadiologyOrderClaimedRadiologyReport(RadiologyOrder radiologyOrder) {
        final List<RadiologyReport> radiologyReports = sessionFactory.getCurrentSession()
                .createCriteria(RadiologyReport.class)
                .add(Restrictions.eq("radiologyOrder", radiologyOrder))
                .add(Restrictions.eq("reportStatus", RadiologyReportStatus.CLAIMED))
                .list();
        return radiologyReports.size() == 1;
    }
    
    /**
     * @see org.openmrs.module.radiology.report.RadiologyReportService#getRadiologyReportsByRadiologyOrderAndReportStatus(RadiologyOrder,
     *      RadiologyReportStatus)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<RadiologyReport> getRadiologyReportsByRadiologyOrderAndRadiologyReportStatus(RadiologyOrder radiologyOrder,
            RadiologyReportStatus radiologyReportStatus) {
        
        final List<RadiologyReport> result = sessionFactory.getCurrentSession()
                .createCriteria(RadiologyReport.class)
                .add(Restrictions.eq("radiologyOrder", radiologyOrder))
                .add(Restrictions.eq("reportStatus", radiologyReportStatus))
                .list();
        return result == null ? new ArrayList<RadiologyReport>() : result;
    }
    
    /**
     * @see org.openmrs.module.radiology.report.RadiologyReportService#getActiveRadiologyReportByRadiologyOrder(RadiologyOrder)
     */
    @Override
    public RadiologyReport getActiveRadiologyReportByRadiologyOrder(RadiologyOrder radiologyOrder) {
        return (RadiologyReport) sessionFactory.getCurrentSession()
                .createCriteria(RadiologyReport.class)
                .add(Restrictions.eq("radiologyOrder", radiologyOrder))
                .add(Restrictions.not(Restrictions.eq("reportStatus", RadiologyReportStatus.DISCONTINUED)))
                .list()
                .get(0);
    }
    
    /**
     * @see org.openmrs.module.radiology.report.RadiologyReportService#getRadiologyReports(RadiologyReportSearchCriteria)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<RadiologyReport> getRadiologyReports(RadiologyReportSearchCriteria searchCriteria) {
        
        final Criteria crit = sessionFactory.getCurrentSession()
                .createCriteria(RadiologyReport.class);
        
        if (!searchCriteria.getIncludeDiscontinued()) {
            crit.add(Restrictions.not(Restrictions.eq("reportStatus", RadiologyReportStatus.DISCONTINUED)));
        }
        if (searchCriteria.getFromDate() != null) {
            crit.add(Restrictions.ge("reportDate", searchCriteria.getFromDate()));
        }
        if (searchCriteria.getToDate() != null) {
            crit.add(Restrictions.le("reportDate", searchCriteria.getToDate()));
        }
        if (searchCriteria.getPrincipalResultsInterpreter() != null) {
            crit.add(Restrictions.eq("principalResultsInterpreter", searchCriteria.getPrincipalResultsInterpreter()));
        }
        if (searchCriteria.getStatus() != null) {
            crit.add(Restrictions.eq("reportStatus", searchCriteria.getStatus()));
        }
        
        crit.addOrder(Order.asc("reportDate"));
        return crit.list();
    }
}
