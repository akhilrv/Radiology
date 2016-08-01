/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.report.template;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.test.BaseContextMockTest;
import org.openmrs.web.WebConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests {@code MrrtReportTemplateFormController}.
 */
public class MrrtReportTemplateFormControllerTest extends BaseContextMockTest {
    
    
    @Mock
    private MrrtReportTemplateService mrrtReportTemplateService;
    
    @InjectMocks
    private MrrtReportTemplateFormController controller = new MrrtReportTemplateFormController();
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    private MrrtReportTemplate mrrtReportTemplate;
    
    private MockHttpServletRequest request;
    
    private static final String TEST_UUID_PARAM = "b10750ca-2099-43df-a503-d8548c6624d6";
    
    private static final String RADIOLOGY_DASHBOARD_FORM_VIEW = "/module/radiology/radiologyDashboardForm";
    
    @Before
    public void setUp() {
        mrrtReportTemplate = mock(MrrtReportTemplate.class);
        request = new MockHttpServletRequest();
        
        when(mrrtReportTemplateService.getMrrtReportTemplateByUuid(TEST_UUID_PARAM)).thenReturn(mrrtReportTemplate);
    }
    
    /**
     * @see MrrtReportTemplateFormController#displayMrrtReportTemplate(HttpServletRequest,String)
     * @verifies return the model and view of the report template form page containing template body in model object
     */
    @Test
    public void
            displayMrrtReportTemplate_shouldReturnTheModelAndViewOfTheReportTemplateFormPageContainingTemplateBodyInModelObject()
                    throws Exception {
        String templateBody = "<div><p>Test template body</p></div>";
        when(mrrtReportTemplateService.getMrrtReportTemplateHtmlBody(mrrtReportTemplate)).thenReturn(templateBody);
        ModelAndView modelAndView = controller.displayMrrtReportTemplate(request, TEST_UUID_PARAM);
        assertNotNull(modelAndView);
        assertThat(modelAndView.getViewName(), is(MrrtReportTemplateFormController.MRRT_REPORT_TEMPLATE_FORM_VIEW));
        assertThat(modelAndView.getModel()
                .containsKey("templateBody"),
            is(true));
        String templateBodyFromModel = (String) modelAndView.getModel()
                .get("templateBody");
        assertNotNull(templateBodyFromModel);
        assertThat(templateBodyFromModel, is(templateBody));
    }
    
    /**
     * @see MrrtReportTemplateFormController#displayMrrtReportTemplate(HttpServletRequest,String)
     * @verifies return the model and view of the radiology dashboard page with error message if io exception is thrown
     */
    @Test
    public void
            displayMrrtReportTemplate_shouldReturnTheModelAndViewOfTheRadiologyDashboardPageWithErrorMessageIfIoExceptionIsThrown()
                    throws Exception {
        when(mrrtReportTemplateService.getMrrtReportTemplateHtmlBody(mrrtReportTemplate))
                .thenThrow(new IOException("Error reading file."));
        ModelAndView modelAndView = controller.displayMrrtReportTemplate(request, TEST_UUID_PARAM);
        assertNotNull(modelAndView);
        assertThat(modelAndView.getViewName(), is(RADIOLOGY_DASHBOARD_FORM_VIEW));
        String errorMessage = (String) request.getSession()
                .getAttribute(WebConstants.OPENMRS_ERROR_ATTR);
        assertNotNull(errorMessage);
        assertThat(errorMessage, is("Error occured while dispaying template => Error reading file."));
    }
    
    /**
     * @see MrrtReportTemplateFormController#displayMrrtReportTemplate(HttpServletRequest,String)
     * @verifies throw illegal argument exception if no template was found for given templateId
     */
    @Test
    public void displayMrrtReportTemplate_shouldThrowIllegalArgumentExceptionIfNoTemplateWasFoundForGivenTemplateId()
            throws Exception {
        when(mrrtReportTemplateService.getMrrtReportTemplateByUuid(TEST_UUID_PARAM)).thenReturn(null);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("MrrtReportTemplate not found. templateId '" + TEST_UUID_PARAM + "' unknown.");
        controller.displayMrrtReportTemplate(request, TEST_UUID_PARAM);
    }
    
}
