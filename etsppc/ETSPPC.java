package ads2.ss14.etsppc;

import java.util.*;

/**
 * Klasse zum Berechnen der Tour mittels Branch-and-Bound.
 * Hier sollen Sie Ihre L&ouml;sung implementieren.
 */
public class ETSPPC extends AbstractETSPPC {

        ETSPPCInstance ei;
    
        
	public ETSPPC(ETSPPCInstance instance) {
            this.ei = instance;
            this.setSolution(Double.POSITIVE_INFINITY, null);
		// TODO: Hier ist der richtige Platz fuer Initialisierungen
	}

	/**
	 * Diese Methode bekommt vom Framework maximal 30 Sekunden Zeit zur
	 * Verf&uuml;gung gestellt um eine g&uuml;ltige Tour
	 * zu finden.
	 * 
	 * <p>
	 * F&uuml;gen Sie hier Ihre Implementierung des Branch-and-Bound Algorithmus
	 * ein.
	 * </p>
	 */
	@Override
	public void run() {
            
            // fixed = momentane Route (fixierte Knoten)
            List<Location> fixed = new ArrayList<Location>();
            
            // Hashmap fuer Suche nach Startknoten, Key = Location ID, Value = Anzahl der Constraints in denen die Location Voraussetzung ist
            HashMap<Integer, Integer> h = new HashMap<Integer, Integer>();
            
            for(Integer i : ei.getAllLocations().keySet())
            {
                h.put(i, 0);
            }
            
            // entferne Locations mit Constraints, erhoehe Values
            for(PrecedenceConstraint p : ei.getConstraints())
            {
                h.remove(p.getSecond());
                if(h.containsKey(p.getFirst()))
                {
                    int val = h.get(p.getFirst()) +1;
                    h.remove(p.getFirst());
                    h.put(p.getFirst(), val);
                }
            }
            
            // Filterung nach wichtigsten Locations (hoechster Value)
            int max = Collections.max(h.values());
            ArrayList<Integer> toRemove = new ArrayList<Integer>();
            for(Integer i : h.keySet())
            {
                if(h.get(i) < max)
                    toRemove.add(i);
            }
            
            for(Integer i : toRemove)
            {
                h.remove(i);
            }
            
            // Hinzufuegen der besten Location zu fixed, als Startknoten
            if(h.size() == 1)
            {
                fixed.add(ei.getAllLocations().get(h.keySet().iterator().next()));
            }
            else
            {
                for(Integer i : h.keySet())
                {
                    // find nearest neighbour
                    Location current = ei.getAllLocations().get(i);
                    Location next = null;
                    double mindist = Double.POSITIVE_INFINITY;
                    for(Location l : ei.getAllLocations().values())
                    {
                        double dist = l.distanceTo(current);
                        if(dist < mindist || next == null)
                        {
                            mindist = dist;
                            next = l;
                        }
                    }
                    // test if accessible
                    ArrayList<Location> temp = new ArrayList<Location>();
                    temp.add(current);
                    if(checkConstraints(temp, next))
                    {
                        fixed.add(current);
                        break;
                    }
                }
                if(fixed.isEmpty())
                {
                    fixed.add(ei.getAllLocations().get(h.keySet().iterator().next()));
                }
            }
            
            branch(fixed);
	}
        
        private void branch(List<Location> fixed)
        { 
            if(optimalPathLength(fixed) >= getBestSolution().getUpperBound())   // pruefen ob L < U (globale obere Schranke) 
                return;
            
            List<Location> temp = createRoute(fixed);
            setSolution(Main.calcObjectiveValue(temp), temp);
            
            TreeMap<Double, Location> distances = new TreeMap<Double, Location>();
            Location current = fixed.get(fixed.size()-1);
            
            // erzeugen von TreeMap mit allen gueltigen Nachbarn als naechster Knoten
            // Treemap = sortiert nach Distanz (nearest neighbour)
            for(Location l : ei.getAllLocations().values())
            {
                if(!fixed.contains(l) && checkConstraints(fixed, l))
                {
                    distances.put(current.distanceTo(l), l);
                }
            }
            
            // Rekursive Aufrufe dieser Nachbarn (Enumeration)
            for(Location l : distances.values())
            {
                ArrayList<Location> f2 = new ArrayList<Location>(fixed);
                f2.add(l);
                branch(f2);
            }
                
        }
        
        
        /**
         * creates a route using nearest neighbour and considering precedence constraints
         * @param fixed List containing fixed locations
         * @return List containing the tour (first element only once)
         */
        private List<Location> createRoute(List<Location> fixed)
        {
            List<Location> result = new ArrayList<Location>(fixed);
            
            List<Location> toAdd = new ArrayList<Location>(ei.getAllLocations().values());
            
            for(Location l : result)
                toAdd.remove(l);
                
            while(!toAdd.isEmpty())
            {
                Location next = null;
                double minDist = Double.POSITIVE_INFINITY;
                for(Location l : toAdd)
                {
                    double temp = l.distanceTo(result.get(result.size()-1));
                    if((temp < minDist || next == null) && checkConstraints(result, l) == true)
                    {
                        minDist = temp;
                        next = l;
                    }
                }
                toAdd.remove(next);
                result.add(next);
            }
                
            return result;    
        }
        
        private boolean checkConstraints(List<Location> list, Location loc)
        {
            for(PrecedenceConstraint pc : ei.getConstraints())
            {
                if(pc.getSecond() == loc.getCityId() && !list.contains(ei.getAllLocations().get(pc.getFirst())))
                    return false;
            }
            
            return true;
        }
        
        // Berechnung der unteren Schranke
        private double optimalPathLength(List<Location> fixed)  // ignores precedence constraints
        {
            double result = 0;
            ArrayList<Location> unused = new ArrayList<Location>(ei.getAllLocations().values());
            
            Location last = null;
            for(Location c : fixed) {
                    if(last != null) {
                            result += last.distanceTo(c);
                            unused.remove(c);
                    }	
                    last = c;
            }
            unused.add(last);
                        
            for(Location c : unused) 
            {
                if(!c.equals(last))
                {
                    double shortest = Double.POSITIVE_INFINITY;
                    for(Location l : unused)
                        if(!l.equals(c))
                        {
                            double dist = l.distanceTo(c);
                            if(dist < shortest)
                                shortest = dist;
                        }
                    
                    result += shortest;
                }
            }
            
            
            return result;
        }
        

}
