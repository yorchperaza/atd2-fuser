package com.mosaicatm.tfmplugin;

import org.pf4j.Extension;

import com.mosaicatm.fuser.common.camel.FuserAmqProps;
import com.mosaicatm.fuser.transform.api.AbstractFuserFlightTransformPluginApi;
import com.mosaicatm.fuser.transform.api.FuserPluginProps;
import com.mosaicatm.fuser.transform.api.SourceTransformApi;
import com.mosaicatm.matmdata.flight.MatmFlight;
import com.mosaicatm.tfm.thick.flight.mtfms.data.TfmsFlightTransfer;
import com.mosaicatm.tfmplugin.matm.TfmsFlightTransferMatmTransformApi;

@Extension
public class TfmTransformImpl 
extends AbstractFuserFlightTransformPluginApi<TfmsFlightTransfer>
{
    private String sourceStart = "tfmflight.tfmFlightTransfer";	
    private boolean includeFixTraversalData = false;
    private boolean includeWaypointTraversalData = false;
    private boolean includeCenterTraversalData = false;
    private boolean includeSectorTraversalData = false;

	@Override
	public String getSourceStart()
	{
		return sourceStart;
	}

	@Override
	public String getSourceType()
	{
		return "TfmSingle";
	}
	
	@Override
    public String getComponentName()
    {
        return "tfm.jms";
    }

	@Override
	public void initialize( FuserPluginProps props )
	{
        super.initialize(props);
        
        String includeFixTraversalDataProp = getProperty( "fuser.tfm.transform.includeFixTraversalData" );
		String includeWaypointTraversalDataProp = getProperty( "fuser.tfm.transform.includeWaypointTraversalData" );        
        String includeCenterTraversalDataProp = getProperty( "fuser.tfm.transform.includeCenterTraversalData" );
        String includeSectorTraversalDataProp = getProperty( "fuser.tfm.transform.includeSectorTraversalData" );
        
		String tfmsFlightTransferSourceProp = getProperty( "fuser.topic.tfm.tfmsFlightTransfer.source" );
        
		if( isValidProperty( includeWaypointTraversalDataProp ))
		{
			includeWaypointTraversalData = Boolean.parseBoolean( includeWaypointTraversalDataProp.trim() );
		}

		if( isValidProperty( includeFixTraversalDataProp ))
		{
			includeFixTraversalData = Boolean.parseBoolean( includeFixTraversalDataProp.trim() );
		}        

		if( isValidProperty( includeCenterTraversalDataProp ))
		{
			includeCenterTraversalData = Boolean.parseBoolean( includeCenterTraversalDataProp.trim() );
		} 

		if( isValidProperty( includeSectorTraversalDataProp ))
		{
			includeSectorTraversalData = Boolean.parseBoolean( includeSectorTraversalDataProp.trim() );
		}         
        
        if( isValidProperty( tfmsFlightTransferSourceProp ))
        {
            sourceStart = tfmsFlightTransferSourceProp.trim();
        }
	}

	@Override
	public SourceTransformApi<TfmsFlightTransfer,MatmFlight> getSourceTransformApi()
	{
		return new TfmsFlightTransferMatmTransformApi( 
                includeFixTraversalData, includeWaypointTraversalData, includeCenterTraversalData, includeSectorTraversalData );
	}

	@Override
    public FuserAmqProps getFuserAmqProps()
    {
        FuserAmqProps props = new FuserAmqProps();
        props.setUrl( getProperty( "fuser.tfm.jms.url", getDefaultUrl() ) );
        props.setUser( getProperty( "fuser.tfm.jms.user", getDefaultUser() ) );
        props.setPassword( getProperty( "fuser.tfm.jms.password", getDefaultPassword() ) );
        
        return props;
    }
}
