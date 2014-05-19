/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.solvers.gibbs.samplers.generic;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.math.DimpleRandomGenerator;


public class SliceSampler implements IMCMCSampler
{
	private double _initialSliceWidth = 1;	// Default value
	private double _maximumDoublings = 10;	// Default value
	
	@Override
	public void initialize(Domain variableDomain)
	{
	}
	
	@Override
	public boolean nextSample(Value sampleValue, ISamplerClient samplerClient)
	{
		final double y = sampleVerticalSlice(samplerClient);
		final double x = sampleHorizontalSlice(sampleValue.getDouble(), y, (IRealSamplerClient)samplerClient);
		((IRealSamplerClient)samplerClient).setNextSampleValue(x);
		return true;
	}

	// Sample vertical slice in log domain
	// In probability domain this would be uniform from 0 to the value at the current horizontal location (in probability representation)
	public double sampleVerticalSlice(ISamplerClient samplerClient)
	{
		final double yValue = samplerClient.getCurrentSampleScore();
		return yValue - Math.log(DimpleRandomGenerator.rand.nextDouble());
	}

	// Sample horizontal slice using doubling method
	public double sampleHorizontalSlice(double x, double y, IRealSamplerClient samplerClient)
	{
		// First finding slice using doubling method
		double L = x - _initialSliceWidth * DimpleRandomGenerator.rand.nextDouble();
		double R = L + _initialSliceWidth;
		double fL = samplerClient.getSampleScore(L);
		double fR = samplerClient.getSampleScore(R);
		for (int k = 0; k < _maximumDoublings; k++)
		{
			if (y <= fL && y <= fR)
				break;
			if (DimpleRandomGenerator.rand.nextBoolean())	// Flip a coin
			{
				L -= (R - L);
				fL = samplerClient.getSampleScore(L);
			}
			else
			{
				R += (R - L);
				fR = samplerClient.getSampleScore(R);
			}
		}
		
		// Next, shrink the interval as necessary
		double xSample;
		double Ls = L;
		double Rs = R;
		while (true)
		{
			xSample = Ls + (Rs - Ls) * DimpleRandomGenerator.rand.nextDouble();
			double fSample = samplerClient.getSampleScore(xSample);
			
			if (y >= fSample && accept(xSample, x, y, L, R, samplerClient))
				break;	// Accept
			
			// Not accepted yet, shrink the interval
			if (xSample < x)
				Ls = xSample;
			else
				Rs = xSample;
		}
		
		return xSample;
	}
	
	private boolean accept(double xSample, double x, double y, double L, double R, IRealSamplerClient samplerClient)
	{
		boolean D = false;
		double fL = samplerClient.getSampleScore(L);
		double fR = samplerClient.getSampleScore(R);
		
		while (R - L > 1.1 * _initialSliceWidth)
		{
			double M = (L + R) * 0.5;	// Mid-point
			
			if ((x < M && xSample >= M) || (x >= M && xSample < M))
				D = true;
			
			if (xSample < M)
			{
				R = M;
				fR = samplerClient.getSampleScore(R);
			}
			else
			{
				L = M;
				fL = samplerClient.getSampleScore(L);
			}
			
			if (D && (y <= fL) && (y <= fR))
				return false;	// The new point is not acceptable
		}
		
		return true;	// The new point is acceptable
	}

	public double getInitialSliceWidth()
	{
		return _initialSliceWidth;
	}

	public void setInitialSliceWidth(double initialSliceWidth)
	{
		_initialSliceWidth = initialSliceWidth;
	}

	public double getMaximumDoublings()
	{
		return _maximumDoublings;
	}

	public void setMaximumDoublings(double maximumDoublings)
	{
		_maximumDoublings = maximumDoublings;
	}

}
