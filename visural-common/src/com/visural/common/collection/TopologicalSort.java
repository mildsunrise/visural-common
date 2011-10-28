/*
 *  Copyright 2010 Visural.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.visural.common.collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Allows determination of evaluation order of a DAG
 * @author Visural
 * @param <T> 
 */
public class TopologicalSort<T> {
    
    private Set<T> orphaned = Sets.newHashSet();
    private Set<T> noFollows = Sets.newHashSet();
    // the values that precede the keys
    private Multimap<T, T> precedes = HashMultimap.create();

    public TopologicalSort(T... directedNodes) {
        this(new NodeInternalDirectionProvider(), directedNodes);
    }
    
    public TopologicalSort(Collection<T> directedNodes) {
        this(new NodeInternalDirectionProvider(), directedNodes);
    }

    public TopologicalSort(DirectionProvider<T> directionProvider, T... directedNodes) {
        this(directionProvider, Arrays.asList(directedNodes));
    }

    public TopologicalSort(DirectionProvider<T> directionProvider, Collection<T> directedNodes) {
        for (T node : directedNodes) {
            Directional nodeDir = directionProvider.For(node);
            Collection<T> fol = nodeDir.follows();
            for (T follower : fol) {
                precedes.put(node, follower);                
            }
            Collection<T> pres = nodeDir.precedes();
            for (T pre : pres) {
                precedes.put(pre, node);                
            }            
            orphaned.add(node);
            noFollows.add(node);
        }
        // remove node from noEdges which have edges
        noFollows.removeAll(precedes.values());
        orphaned.removeAll(precedes.keySet());
        orphaned.removeAll(precedes.values());
    }
    
    public boolean isCyclic() {
        try {
            evaluationOrder();
        } catch (IllegalArgumentException e) {
            return true;
        }
        return false;
    }
    
    private List<T> evalCache = null;
    
    /**
     * Determine the evaluation order of a DAG. An IllegalArgumentException is
     * thrown if the graph is cyclic.
     * @return 
     */
    public List<T> evaluationOrder() {        
        if (evalCache != null) return evalCache;        
        List<T> eval = new ArrayList<T>(orphaned);
        
        if (noFollows.isEmpty() && !precedes.isEmpty()) {
            throw new IllegalArgumentException("Cycle found in dependency graph");
        } else {
            Set<T> visited = new HashSet<T>();
            for (T node : noFollows) {                
                visit(eval, node, visited);
            }
        }        
        evalCache = eval;
        return eval;        
    }
    
    private void visit(List<T> eval, T node, Set<T> stackVisited) {
        if (stackVisited.contains(node)) {
            throw new IllegalArgumentException("Cycle found in dependency graph");
        } else {
            stackVisited.add(node);
        }
        if (!eval.contains(node)) {
            for (T parent : precedes.get(node)) {
                visit(eval, parent, stackVisited);
            }
            eval.add(node);
        }
        stackVisited.remove(node);        
    }
    
    private static class NodeInternalDirectionProvider<T extends Directional<T>> implements DirectionProvider<T> {
        public Directional<T> For(T node) {
            return node;
        }
    }
}
