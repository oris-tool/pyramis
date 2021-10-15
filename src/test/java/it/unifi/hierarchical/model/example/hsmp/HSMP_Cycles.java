/* This program is part of the PYRAMIS library for compositional analysis of hierarchical UML statecharts.
 * Copyright (C) 2019-2021 The PYRAMIS Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.unifi.hierarchical.model.example.hsmp;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.oristool.math.function.GEN;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.Region.RegionType;

public class HSMP_Cycles {

    public static HierarchicalSMP build() {
    	
    	
    	int depth = 2;
		
    	
    	
    	State E1 = new ExitState("E1",2);
        State E2 = new ExitState("E2",2);
 
        State B2 = new SimpleState(
                "B2", 
                GEN.newDeterministic(new BigDecimal(1)), 
                Arrays.asList(E1), 
                Arrays.asList(1.0), 
                depth);
        
        State B3 = new SimpleState(
                "B3", 
                GEN.newDeterministic(new BigDecimal(1)), 
                Arrays.asList(E2), 
                Arrays.asList(1.0), 
                depth);
        
        State B1 = new SimpleState(
                "B1", 
                GEN.newDeterministic(new BigDecimal(1)), 
                Arrays.asList(B2), 
                Arrays.asList(1.0), 
                depth);
        
        
        Region region1 = new Region(B1, RegionType.EXIT);
        Region region2 = new Region(B3, RegionType.EXIT);
      
        //Level depth 1
        depth = 1;
        
        Map<State, List<State>> nextMap = null;
        Map<State, List<Double>> nextBranch=null;
        
        State End = new ExitState("End",1);
        
        State B = new CompositeState(
                "B",  
                Arrays.asList(region1, region2), 
                nextMap,  
                nextBranch, 
                depth);
        
        
        State C = new SimpleState(
                "C", 
                GEN.newDeterministic(new BigDecimal(1)), 
                Arrays.asList(End), 
                Arrays.asList(1.0), 
                depth);
        
        State D = new SimpleState(
                "D", 
                GEN.newDeterministic(new BigDecimal(1)), 
                Arrays.asList(B), 
                Arrays.asList(1.0), 
                depth);
               
        
        State A = new SimpleState(
                "A", 
                GEN.newDeterministic(new BigDecimal(1)), 
                Arrays.asList(B), 
                Arrays.asList(1.0), 
                depth);
        
        
        ((CompositeState) B).setNextStatesConditional(Map.of(
                        E1,
                        Arrays.asList(B),
                        E2,
                        Arrays.asList(C)));
        
       ((CompositeState) B).setBranchingProbsConditional(Map.of(
               E1,
               Arrays.asList(1.0),
               E2,
               Arrays.asList(1.0)));
        
           
        //Level depth 0
        depth = 0;
        
        List<State> nextStates = null;//Required to avoid ambiguity
        Region region3 = new Region(A, RegionType.NEVER, true);
        
        
        State S0 = new CompositeState(
                "S0",  
                Arrays.asList(region3), 
                nextStates, 
                null, 
                depth);
        
               
        S0.setNextStates(Arrays.asList(S0), Arrays.asList(1.0));
              
        
        B.setCyleLooping(true, false);
        D.setCyleLooping(true, true);
     
        
        return new HierarchicalSMP(S0);
    }
}
