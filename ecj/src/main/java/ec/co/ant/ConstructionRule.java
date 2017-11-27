/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.EvolutionState;
import ec.Setup;
import ec.co.ConstructiveIndividual;

/**
 *
 * @author Eric O. Scott
 */
public interface ConstructionRule extends Setup
{
    public abstract ConstructiveIndividual constructSolution(EvolutionState state, ConstructiveIndividual ind, int startNode, PheromoneMatrix pheromones);
}
