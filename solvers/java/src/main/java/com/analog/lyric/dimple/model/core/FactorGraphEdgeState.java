/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.model.core;

import static java.util.Objects.*;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;

/**
 * Describes an edge from a factor to a variable.
 * <p>
 * Edge state objects are maintained by each {@link FactorGraph} for the edges that connect
 * to it's directly owned {@link Factor}s.
 * <p>
 * To save memory, these objects do not contain a pointer to the owning graph and some methods
 * will require a reference to the parent graph. You can wrap this with an {@link Edge} object
 * if you want a self-contained edge reference.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class FactorGraphEdgeState
{
	/*------------------------------
	 * FactorGraphEdgeState methods
	 */
	
	/**
	 * The index of this edge within the {@link FactorGraph} that owns it.
	 * <p>
	 * This index can be used to look up this object within its parent graph
	 * using the {@link FactorGraph#getEdgeState(int)} method.
	 * <p>
	 * @since 0.08
	 */
	public abstract int edgeIndex();
	
	/**
	 * Return instance of {@link Factor} end of edge, given parent graph.
	 * <p>
	 * @param graph is the parent graph of either the variable or factor ends of the edge.
	 * @since 0.08
	 */
	abstract public Factor getFactor(FactorGraph graph);
	
	/**
	 * Return instance of {@link Variable} end of edge, given parent graph.
	 * <p>
	 * @param graph is the parent graph of either the variable or factor ends of the edge.
	 * @since 0.08
	 */
	abstract public Variable getVariable(FactorGraph graph);
	
	/**
	 * Given one endpoint of edge, return the other one.
	 * <p>
	 * That is given the {@link Variable} end of the edge, this returns the {@link Factor}, and
	 * vice-versa.
	 * <p>
	 * @since 0.08
	 */
	public final Node getSibling(Node node)
	{
		final FactorGraph graph = requireNonNull(node.getContainingGraph());
		
		return node.isVariable() ? getFactor(graph) : getVariable(graph);
	}

	/**
	 * The local id of the {@link Factor} referred to by this edge within its owning {@link FactorGraph}.
	 * @since 0.08
	 */
	public int factorLocalId()
	{
		return NodeId.localIdFromParts(NodeId.FACTOR_TYPE, factorIndex());
	}

	/**
	 * The index of the {@link Factor} referred to by this edge within its owning {@link FactorGraph}.
	 * @since 0.08
	 */
	abstract public int factorIndex();
	
	/**
	 * True if both the factor and variable are owned by the same graph.
	 * @since 0.08
	 */
	abstract public boolean isLocal();
	
	/**
	 * The index of the {@link Variable} referred to by this edge within the graph that owns this edge.
	 * <p>
	 * Note that the graph that owns the edge is the one that owns the edge's {@link Factor}, which may
	 * not be the owner of the variable. In that case, this will be the index of the corresponding
	 * boundary variable.
	 * <p>
	 * @since 0.08
	 */
	abstract public int variableIndex();
	
	/**
	 * The local id of the {@link Variable} referred to by this edge within the graph that owns this edge.
	 * <p>
	 * Note that the graph that owns the edge is the one that owns the edge's {@link Factor}, which may
	 * not be the owner of the variable. In that case, this will return a boundary variable identifier.
	 * <p>
	 * @since 0.08
	 */
	abstract public int variableLocalId();
}