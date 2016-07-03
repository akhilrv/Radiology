/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.report.template;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.api.APIException;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Unit test for the {@code MrrtReportTemplateFileParser}.
 */
public class MrrtReportTemplateFileParserTest extends BaseModuleContextSensitiveTest {
    
    
    private MrrtReportTemplateFileParser parser;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    private static final String CHARSET = "UTF-8";
    
    private static final String TEST_DCTERMS_TITLE = "Cardiac MRI: Function and Viability";
    
    private static final String TEST_DCTERMS_DESCRIPTION =
            "Cardiac MRI: Function and Viability template :: Authored by Jacobs JE, et al. ";
    
    private static final String TEST_DCTERMS_IDENTIFIER = "http://www.radreport.org/template/0000049";
    
    private static final String TEST_DCTERMS_LANGUAGE = "en";
    
    private static final String TEST_DCTERMS_TYPE = "IMAGE_REPORT_TEMPLATE";
    
    private static final String TEST_DCTERMS_PUBLISHER = "Radiological Society of North America (RSNA)";
    
    private static final String TEST_DCTERMS_RIGHTS = "May be used gratis, subject to license agreement";
    
    private static final String TEST_DCTERMS_LICENSE = "http://www.radreport.org/license.pdf";
    
    private static final String TEST_DCTERMS_DATE = "2012-07-19";
    
    private static final String TEST_DCTERMS_CREATOR = "Jacobs JE, et al. ";
    
    @Before
    public void setup() {
        parser = new DefaultMrrtReportTemplateFileParser();
    }
    
    /**
    * @see MrrtReportTemplateFileParser#parse()
    * @verifies return an mrrt template object if file is valid
    */
    @Test
    public void parse_shouldReturnAnMrrtTemplateObjectIfFileIsValid() throws Exception {
        File file = new File(getClass().getClassLoader()
                .getResource("mrrttemplates/radreport/0000049.html")
                .getFile());
        
        MrrtReportTemplate template = parser.parse(file);
        
        assertNotNull(template);
        assertThat(template.getCharset(), is(CHARSET));
        assertThat(template.getPath(), is(file.getAbsolutePath()));
        assertThat(template.getDcTermsTitle(), is(TEST_DCTERMS_TITLE));
        assertThat(template.getDcTermsDescription(), is(TEST_DCTERMS_DESCRIPTION));
        assertThat(template.getDcTermsIdentifier(), is(TEST_DCTERMS_IDENTIFIER));
        assertThat(template.getDcTermsLanguage(), is(TEST_DCTERMS_LANGUAGE));
        assertThat(template.getDcTermsLanguage(), is(TEST_DCTERMS_LANGUAGE));
        assertThat(template.getDcTermsType(), is(TEST_DCTERMS_TYPE));
        assertThat(template.getDcTermsPublisher(), is(TEST_DCTERMS_PUBLISHER));
        assertThat(template.getDcTermsRights(), is(TEST_DCTERMS_RIGHTS));
        assertThat(template.getDcTermsLicense(), is(TEST_DCTERMS_LICENSE));
        assertThat(template.getDcTermsDate(), is(TEST_DCTERMS_DATE));
        assertThat(template.getDcTermsCreator(), is(TEST_DCTERMS_CREATOR));
    }
    
    /**
     * @see MrrtReportTemplateFileParser#parse(File)
     * @verifies throw an APIException when file failed validation.
     */
    @Test
    public void parse_shouldThrowAnAPIExceptionWhenFileFailedValidation() throws Exception {
        
        expectedException.expect(APIException.class);
        expectedException.expectMessage("Invalid file extension. Only .html files are accepted");
        File file = File.createTempFile("test", ".php");
        parser.parse(file);
    }
}
