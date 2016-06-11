package org.openmrs.module.radiology.order.web.resource;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.radiology.order.RadiologyOrder;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0.RestConstants2_0;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Tests {@link RadiologyOrderResource}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, RestUtil.class })
public class RadiologyOrderResourceTest {
    
    
    private static final String RADIOLOGY_ORDER_UUID = "1bae735a-fca0-11e5-9e59-08002719a237";
    
    @Mock
    OrderService orderService;
    
    @Before
    public void before() throws Exception {
        
        PowerMockito.mockStatic(RestUtil.class);
        
        PowerMockito.mockStatic(Context.class);
        RadiologyOrder radiologyOrder = new RadiologyOrder();
        radiologyOrder.setUuid(RADIOLOGY_ORDER_UUID);
        when(Context.getOrderService()).thenReturn(orderService);
        when(Context.getOrderService()
                .getOrderByUuid(RADIOLOGY_ORDER_UUID)).thenReturn(radiologyOrder);
    }
    
    /**
     * @see RadiologyOrderResource#getRepresentationDescription(Representation)
     * @verifies return default representation given instance of defaultrepresentation
     */
    @Test
    public void getRepresentationDescription_shouldReturnDefaultRepresentationGivenInstanceOfDefaultrepresentation()
            throws Exception {
        
        DefaultRepresentation defaultRepresentation = new DefaultRepresentation();
        RadiologyOrderResource radiologyOrderResource = new RadiologyOrderResource();
        
        DelegatingResourceDescription resourceDescription =
                radiologyOrderResource.getRepresentationDescription(defaultRepresentation);
        
    }
    
    /**
     * @see RadiologyOrderResource#getRepresentationDescription(Representation)
     * @verifies return full representation given instance of fullrepresentation
     */
    @Test
    public void getRepresentationDescription_shouldReturnFullRepresentationGivenInstanceOfFullrepresentation()
            throws Exception {
        
        FullRepresentation fullRepresentation = new FullRepresentation();
        RadiologyOrderResource radiologyOrderResource = new RadiologyOrderResource();
        
        DelegatingResourceDescription resourceDescription =
                radiologyOrderResource.getRepresentationDescription(fullRepresentation);
    }
    
    /**
     * @see RadiologyOrderResource#getRepresentationDescription(Representation)
     * @verifies return null for representation other then default or full
     */
    @Test
    public void getRepresentationDescription_shouldReturnNullForRepresentationOtherThenDefaultOrFull() throws Exception {
        
        CustomRepresentation customRepresentation = new CustomRepresentation("some");
        RadiologyOrderResource radiologyOrderResource = new RadiologyOrderResource();
        
        assertThat(radiologyOrderResource.getRepresentationDescription(customRepresentation), is(nullValue()));
        
        NamedRepresentation namedRepresentation = new NamedRepresentation("some");
        radiologyOrderResource = new RadiologyOrderResource();
        
        assertThat(radiologyOrderResource.getRepresentationDescription(namedRepresentation), is(nullValue()));
        
        RefRepresentation refRepresentation = new RefRepresentation();
        radiologyOrderResource = new RadiologyOrderResource();
        
        assertThat(radiologyOrderResource.getRepresentationDescription(refRepresentation), is(nullValue()));
    }
    
    /**
     * @see RadiologyOrderResource#getResourceVersion()
     * @verifies return supported resource version
     */
    @Test
    public void getResourceVersion_shouldReturnSupportedResourceVersion() throws Exception {
        
        RadiologyOrderResource radiologyOrderResource = new RadiologyOrderResource();
        assertThat(radiologyOrderResource.getResourceVersion(), is(RestConstants2_0.RESOURCE_VERSION));
    }
    
    /**
     * @see RadiologyOrderResource#getByUniqueId(String)
     * @verifies return radiology order given its uuid
     */
    @Test
    public void getByUniqueId_shouldReturnRadiologyOrderGivenItsUuid() throws Exception {
        
        RadiologyOrderResource radiologyOrderResource = new RadiologyOrderResource();
        radiologyOrderResource.getByUniqueId(RADIOLOGY_ORDER_UUID);
    }
}
