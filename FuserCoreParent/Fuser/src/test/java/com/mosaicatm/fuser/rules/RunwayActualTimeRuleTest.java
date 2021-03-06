package com.mosaicatm.fuser.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mosaicatm.fuser.transform.matm.airline.AirlineDataSource;
import com.mosaicatm.matmdata.common.FuserSource;
import com.mosaicatm.matmdata.common.MetaData;
import com.mosaicatm.matmdata.flight.MatmFlight;
import com.mosaicatm.matmdata.flight.ObjectFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:config/fuser/beans.rules.test.xml")
public class RunwayActualTimeRuleTest
{
    private static final String FIELD1 = "departureRunwayActualTime.value";
    private static final String FIELD2 = "arrivalRunwayActualTime";
    
    private static final Date CORRECT_FIELD1_TIME = new Date( 50 );
    private static final Date CORRECT_FIELD2_TIME = new Date( 1000 );

    private final ObjectFactory objectFactory = new ObjectFactory();
    
    @Autowired
    @Qualifier("fuser-rules.RunwayActualTimeRule")
    private Rule<MatmFlight> rule;
    
    @Test
    public void test()
    {
        MatmFlight update = getUpdate( FuserSource.TFM_TFDM.name(), null );

        // Don't want to filter out new source
        testSourceOverride( FIELD1, update, null, null, false );
        testSourceOverride( FIELD2, update, null, null, false );
        testUpdatedElements( update, false );
        
        // Don't want to filter out same source
        update = getUpdate( FuserSource.TFM_TFDM.name(), null );
        testSourceOverride( FIELD1, update, FuserSource.TFM_TFDM.name(), null, false );
        testSourceOverride( FIELD2, update, FuserSource.TFM_TFDM.name(), null, false );
        testUpdatedElements( update, false );      

        // TFM_TFDM > FlightStats source
        update = getUpdate( FuserSource.AIRLINE.name(),AirlineDataSource.FLIGHTSTATS.name() );
        testSourceOverride( FIELD1, update, FuserSource.TFM_TFDM.name(), null, true );
        testSourceOverride( FIELD2, update, FuserSource.TFM_TFDM.name(), null, true );
        testUpdatedElements( update, true );       
       
        // TFM_TFDM < FlightHub source
        update = getUpdate( FuserSource.AIRLINE.name(),AirlineDataSource.FLIGHTHUB.name() );
        testSourceOverride( FIELD1, update, FuserSource.TFM_TFDM.name(), null, false );
        testSourceOverride( FIELD2, update, FuserSource.TFM_TFDM.name(), null, false );
        testUpdatedElements( update, false );                 
        
        // TFM_TFDM > FMC source
        update = getUpdate( FuserSource.FMC.name(), null );
        testSourceOverride( FIELD1, update, FuserSource.TFM_TFDM.name(), null, true );
        testSourceOverride( FIELD2, update, FuserSource.TFM_TFDM.name(), null, true );
        testUpdatedElements( update, true ); 
        
        // FMC > TFM source
        update = getUpdate( FuserSource.FMC.name(), null );
        testSourceOverride( FIELD1, update, FuserSource.TFM.name(), null, false );
        testSourceOverride( FIELD2, update, FuserSource.TFM.name(), null, false );
        testUpdatedElements( update, false );        
    }
    
    private MatmFlight getUpdate( String source, String system )
    {
        MatmFlight update = new MatmFlight();
        update.setDepartureRunwayActualTime(objectFactory.createMatmFlightDepartureRunwayActualTime(CORRECT_FIELD1_TIME));
        update.setArrivalRunwayActualTime( CORRECT_FIELD2_TIME );
        update.setLastUpdateSource( source );
        update.setSystemId( system );

        return( update );
    }
    
    private void testSourceOverride( String field, MatmFlight update, 
            String historySource, String historySystem, boolean expectFilteredOut )
    {
        MatmFlight target = new MatmFlight();
        
        MetaData history = new MetaData();
        
        if( historySource != null )
        {
            history.setSource(historySource);
            history.setSystemType(historySystem);
        }
        
        if( expectFilteredOut )
        {
            assertFalse(rule.handleDifference(update, target, history, field));
        }
        else
        {
            assertTrue(rule.handleDifference(update, target, history, field));
        }             
    }
    
    private void testUpdatedElements( MatmFlight update, boolean expectFilteredOut )
    {    
        if( expectFilteredOut )
        {
            assertNull(update.getDepartureRunwayActualTime());
            assertNull(update.getArrivalRunwayActualTime());              
        }
        else
        {
            assertEquals(CORRECT_FIELD1_TIME, update.getDepartureRunwayActualTime().getValue());
            assertEquals(CORRECT_FIELD2_TIME, update.getArrivalRunwayActualTime());        
        }
    }
}
