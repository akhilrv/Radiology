/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.tool.dcmof.DcmOF;
import org.openmrs.OrderType;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.metadatadeploy.api.MetadataDeployService;
import org.openmrs.module.metadatadeploy.bundle.MetadataBundle;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */

public class RadiologyActivator extends BaseModuleActivator {
	
	private static final Log log = LogFactory.getLog(RadiologyActivator.class);
	
	private DcmOF dicomOrderFiller;
	
	@Override
	public void started() {
		MetadataDeployService metadataDeployService = Context.getService(MetadataDeployService.class);
		deployMetadataPackages(metadataDeployService);
		startDicomOrderFiller();
		installRadiologyOrderType();
	}
	
	@Override
	public void stopped() {
		stopDicomOrderFiller();
	}
	
	public static boolean badInit(OrderService os) {
		return !Utils.hasRadiology(os);
	}
	
	public void deployMetadataPackages(MetadataDeployService service) {
		MetadataBundle radiologyRolePrivilegeMetadata = Context.getRegisteredComponent("radiologyRolePrivilegeMetadata",
		    MetadataBundle.class);
		service.installBundles(Arrays.asList(radiologyRolePrivilegeMetadata));
	}
	
	/**
	 * Creates radiology order type if not exists
	 */
	public static boolean installRadiologyOrderType() {
		// Create radiology order type if not exists
		try {
			if (Utils.getRadiologyOrderType().isEmpty()) {
				OrderService os = Context.getOrderService();
				os.saveOrderType(new OrderType("Radiology", "Order for radiology procedures"));
			}
			log.info("Radiology order type created!");
		}
		catch (Exception e) {
			log.warn("Need some privilege to startup the module. Go to openmrs/module/radiology/config.list with authenticated user.");
			return false;
		}
		return true;
	}
	
	public void startDicomOrderFiller() {
		try {
			String[] args2 = { "-mwl", Utils.mwlDir(), "-mpps", Utils.mppsDir(), Utils.aeTitle() + ":" + Utils.mwlMppsPort() };
			dicomOrderFiller = DcmOF.main(args2);
			log.info("Started MPPSScu : OpenMRS MPPS SCU Client (dcmof)");
		}
		catch (Exception e) {
			log.warn("Can not start MWL/MPPS DICOM server");
			log.warn("Unable to start MPPSScu : OpenMRS MPPS SCU Client (dcmof)");
		}
	}
	
	public void stopDicomOrderFiller() {
		dicomOrderFiller.stop();
		log.info("Stopped MPPSScu : OpenMRS MPPS SCU Client (dcmof)");
	}
}
