package ads1.ss14.can;

import ads1.ss14.can.exceptions.CANException;
import ads1.ss14.can.exceptions.NoAdditionalStorageAvailable;
import ads1.ss14.can.exceptions.NoSuchDocument;

import java.util.*;

/**
 * @author Tom Tucek
 * Matrikel Nummer: 1325775
 * 
 */


public class Client implements ClientInterface, ClientCommandInterface{
	
    private final String uniqueID;
    private final int networkXSize, networkYSize;
    
    private int maxNumberOfDocuments;
    private Area area;
    
    private ArrayList<ClientInterface> neighbours;
    private HashMap<String, Pair<Document, Position>> docs;
    
        /**
	 * Constructs a new Client
	 * @param uniqueID the ID of the Client in the CAN
	 * @param networkXSize the size along the horizontal axis in the CAN
	 * @param networkYSize the size along the vertical axis in the CAN
	 */
	public Client(String uniqueID, int networkXSize, int networkYSize) {
            
            this.networkXSize = networkXSize;
            this.networkYSize = networkYSize;
            this.uniqueID = uniqueID;
            
            this.maxNumberOfDocuments = -1;
            
            this.area = null;
            
            this.docs = new HashMap<String, Pair<Document, Position>>();
            
            this.neighbours = new ArrayList<ClientInterface>();
		//TODO Implement me!
	}

	@Override
	public String getUniqueID() {
            return this.uniqueID;
	}

	@Override
	public void setMaxNumberOfDocuments(int m) {
            this.maxNumberOfDocuments = m;
	}

	@Override
	public int getMaxNumberOfDocuments() {
            return maxNumberOfDocuments;
	}

	@Override
	public Document getDocument(String documentName) throws NoSuchDocument {
            if(!docs.containsKey(documentName))
                throw new NoSuchDocument();
            return docs.get(documentName).first;
	}

	@Override
	public void storeDocument(Document d, Position p) throws NoAdditionalStorageAvailable, CANException {
            if(docs.size() >= this.maxNumberOfDocuments)
                throw new NoAdditionalStorageAvailable();
            docs.put(d.getName(), new Pair<Document, Position>(d, p));
	}

	@Override
	public void deleteDocument(String documentName) throws NoSuchDocument {
            if(!docs.containsKey(documentName))
                throw new NoSuchDocument();
            docs.remove(documentName);
	}

	@Override
	public Position getPosition() {
            return new Position(this.area.getLowerX() + (this.area.getUpperX() - this.area.getLowerX())/2,
                                this.area.getLowerY() + (this.area.getUpperY() - this.area.getLowerY())/2 );
	}

	@Override
	public Area getArea() {
            return this.area;
	}
	
	@Override
	public void setArea(Area newArea) {
            this.area = newArea;
	}

	@Override
	public Iterable<ClientInterface> getNeighbours() {
            return new ArrayList<ClientInterface>(neighbours);
	}
	
	@Override
	public void addNeighbour(ClientInterface newNeighbour){
            this.neighbours.add(newNeighbour);
	}
	
	@Override
	public void removeNeighbour(String clientID) {
            
            ClientInterface toBeDeleted = null;
            
            for(ClientInterface n : neighbours)
            {
                if(n.getUniqueID().equals(clientID))
                    toBeDeleted = n;
            }
            
            if(toBeDeleted != null)
                neighbours.remove(toBeDeleted);
	}
	
	@Override
	public ClientInterface searchForResponsibleClient(Position p) {
            
            if(this.area.contains(p))
                return this;
            
            for(ClientInterface ci : this.neighbours)
            {
                if(ci.getArea().contains(p))
                    return ci;
            }
            
            ClientInterface nearest = null;
            double lowest = Double.POSITIVE_INFINITY;
            
            for(ClientInterface ci : this.neighbours)
            {
                double x = Math.max(Math.min(ci.getArea().getUpperX(), p.getX()), ci.getArea().getLowerX());
                double y = Math.max(Math.min(ci.getArea().getUpperY(), p.getY()), ci.getArea().getLowerY());
                double q = Math.sqrt((x - p.getX()) * (x - p.getX()) + (y - p.getY()) * (y - p.getY()));
                
                if(q < lowest || (q == lowest && ci.getUniqueID().compareTo(nearest.getUniqueID()) > 0))
                {
                    lowest = q;
                    nearest = ci;
                }
            }
            
            return nearest.searchForResponsibleClient(p);
	}

        public Position myHashFunction(String docname, int i)
        {
            int s = 0;
            int m = this.networkXSize * this.networkYSize;
            for(char c : docname.toLowerCase().toCharArray())
            {
                if(c >= 'a' && c <= 'z')
                    s += c - 'a';
            }
            
            int h = ((s%m) + i*((2*(s%(m-2)))+1))%m;
            
            return new Position(h%this.networkXSize, (int)(h/this.networkXSize));
        }
        
	@Override
	public ClientInterface joinNetwork(ClientInterface entryPoint, Position p) throws CANException {
            
            if(entryPoint == null)
            {
                this.setArea(new Area(0, this.networkXSize, 0, this.networkYSize));
                return null;
            }
            
            ClientInterface cp = entryPoint.searchForResponsibleClient(p);
            
            // Fall 3: Bereich ist zu klein zum Teilen
            if((cp.getArea().getUpperX() - cp.getArea().getLowerX()) < 2 || 
                    cp.getArea().getUpperY() - cp.getArea().getLowerY() < 2)
                return null;
            
            Pair<Area,Area> new_areas;
            
            // Fall 1 & 2: Area wird halbiert
            if((cp.getArea().getUpperX() - cp.getArea().getLowerX()) >= (cp.getArea().getUpperY() - cp.getArea().getLowerY()))
            {
                new_areas = cp.getArea().splitVertically();
                
                cp.setArea(new_areas.first);
                this.setArea(new_areas.second);
                
                ArrayList<ClientInterface> right = new ArrayList<ClientInterface>();
                ArrayList<ClientInterface> upOrDown = new ArrayList<ClientInterface>();
                
                for(ClientInterface n : cp.getNeighbours())
                {
                    if(n.getArea().getLowerX() > cp.getArea().getUpperX())
                        right.add(n);
                    
                    if(n.getArea().getLowerY() == cp.getArea().getUpperY() || 
                            n.getArea().getUpperY() == cp.getArea().getLowerY())
                        upOrDown.add(n);
                }
                
                for(ClientInterface n : right)
                {
                    cp.removeNeighbour(n.getUniqueID());
                    n.removeNeighbour(cp.getUniqueID());
                    
                    this.addNeighbour(n);
                    n.addNeighbour(this);
                }
                
                cp.addNeighbour(this);
                this.addNeighbour(cp);
                
                for(ClientInterface n : upOrDown)
                {
                    if((n.getArea().getLowerX() <= this.getArea().getLowerX() && this.getArea().getLowerX() < n.getArea().getUpperX())
                            || (n.getArea().getLowerX() < this.getArea().getUpperX() && this.getArea().getUpperX() <= n.getArea().getUpperX()) )
                    {
                        this.addNeighbour(n);
                        n.addNeighbour(this);
                    }
                    
                    if(!((n.getArea().getLowerX() <= cp.getArea().getLowerX() && cp.getArea().getLowerX() < n.getArea().getUpperX())
                            || (n.getArea().getLowerX() < cp.getArea().getUpperX() && cp.getArea().getUpperX() <= n.getArea().getUpperX())))
                    {
                        cp.removeNeighbour(n.getUniqueID());
                        n.removeNeighbour(cp.getUniqueID());
                    }
                }
            }
            else
            {
                new_areas = cp.getArea().splitHorizontally();
                
                cp.setArea(new_areas.second);
                this.setArea(new_areas.first);
                
                ArrayList<ClientInterface> down = new ArrayList<ClientInterface>();
                ArrayList<ClientInterface> rightOrLeft = new ArrayList<ClientInterface>();
                
                for(ClientInterface n : cp.getNeighbours())
                {
                    if(n.getArea().getLowerY() > cp.getArea().getUpperY())
                        down.add(n);
                    
                    if(n.getArea().getLowerX() == cp.getArea().getUpperX() || 
                            n.getArea().getUpperX() == cp.getArea().getLowerX())
                        rightOrLeft.add(n);
                }
                
                for(ClientInterface n : down)
                {
                    cp.removeNeighbour(n.getUniqueID());
                    n.removeNeighbour(cp.getUniqueID());
                    
                    this.addNeighbour(n);
                    n.addNeighbour(this);
                }
                
                cp.addNeighbour(this);
                this.addNeighbour(cp);
                
                for(ClientInterface n : rightOrLeft)
                {
                    if((n.getArea().getLowerY() <= this.getArea().getLowerY() && this.getArea().getLowerY() < n.getArea().getUpperY())
                            || (n.getArea().getLowerY() < this.getArea().getUpperY() && this.getArea().getUpperY() <= n.getArea().getUpperY()) )
                    {
                        this.addNeighbour(n);
                        n.addNeighbour(this);
                    }
                    
                    if(!((n.getArea().getLowerY() <= cp.getArea().getLowerY() && cp.getArea().getLowerY() < n.getArea().getUpperY())
                            || (n.getArea().getLowerY() < cp.getArea().getUpperY() && cp.getArea().getUpperY() <= n.getArea().getUpperY())))
                    {
                        cp.removeNeighbour(n.getUniqueID());
                        n.removeNeighbour(cp.getUniqueID());
                    }
                }
                
            }
            
//            cp.setArea(new_areas.first);
//            this.setArea(new_areas.second);
//            
//            for(ClientInterface c : cp.getNeighbours())
//            {
//                this.addNeighbour(c);
//                c.adaptNeighbours(this);
//            }
//            
//            cp.adaptNeighbours(this);
//            this.adaptNeighbours(cp);
            
            Iterable<Pair<Document, Position>> newdocs = cp.removeUnmanagedDocuments();
            int mnod = this.maxNumberOfDocuments;
            this.setMaxNumberOfDocuments(Integer.MAX_VALUE);
            
            for(Pair<Document, Position> doc : newdocs)
            {
                this.storeDocument(doc.first, doc.second);
            }
            
            this.setMaxNumberOfDocuments(mnod);
            
            return cp;
	}

	@Override
	public Iterable<Pair<Document, Position>> removeUnmanagedDocuments() {
            
            ArrayList<Pair<Document, Position>> toBeRemoved = new ArrayList<Pair<Document, Position>>();
            
            for(Map.Entry<String,Pair<Document, Position>> doc : this.docs.entrySet())
            {
                if(!this.area.contains(doc.getValue().second))
                    toBeRemoved.add(doc.getValue());
            }
            
            for(Pair<Document, Position> doc : toBeRemoved)
            {
                try
                {
                    this.deleteDocument(doc.first.getName());
                }
                catch(NoSuchDocument nsd)
                {
                    
                }
            }
            
            return toBeRemoved;
	}
		
	@Override
	public void adaptNeighbours(ClientInterface joiningClient) {
            
            this.addNeighbour(joiningClient);
            ArrayList<ClientInterface> temp = new ArrayList<ClientInterface>();
            
            for(ClientInterface neighbour : this.neighbours)
            {
                // oben:    n.y2 == this.y1 && n.x2 > this.x1 && n.x1 < this.x2
                // unten:   n.y1 == this.y2 && n.x2 > this.x1 && n.x1 < this.x2
                // links:   n.x2 == this.x1 && n.y2 > this.y1 && n.y1 < this.y2
                // rechts:  n.x1 == this.x2 && n.y2 > this.y1 && n.y1 < this.y2
                Area n = neighbour.getArea();
                // Abfrage ob neighbour an this angrenzt
                if(!((n.getUpperY() == this.area.getLowerY() && n.getUpperX() > this.area.getLowerX() && n.getLowerX() < this.area.getUpperX())
                    || (n.getLowerY() == this.area.getUpperY() && n.getUpperX() > this.area.getLowerX() && n.getLowerX() < this.area.getUpperX())
                    || (n.getUpperX() == this.area.getLowerX() && n.getUpperY() > this.area.getLowerY() && n.getLowerY() < this.area.getUpperY())
                    || (n.getUpperX() == this.area.getLowerX() && n.getUpperY() > this.area.getLowerY() && n.getLowerY() < this.area.getUpperY())))
                {
                    temp.add(neighbour);
                }
            }
            
            for(ClientInterface neighbour : temp)
            {
                this.removeNeighbour(neighbour.getUniqueID());
            }
	}

	@Override
	public void addDocumentToNetwork(Document d) throws CANException {
            
            for(int i = 0; i < this.networkXSize*this.networkYSize; i++)
            {
                Position p = this.myHashFunction(d.getName(), i);
                if(this.area.contains(p))
                {
                    try
                    {
                        this.storeDocument(d, p);
                        return;
                    }
                    catch(NoAdditionalStorageAvailable nasa)
                    {

                    }
                }
                else
                {
                    try
                    {
                        this.searchForResponsibleClient(p).storeDocument(d, p);
                        return;
                    }
                    catch(NoAdditionalStorageAvailable nasa)
                    {

                    }
                }
            }
	}

	@Override
	public void removeDocumentFromNetwork(String documentName) {
            
            for(int i = 0; i < this.networkXSize*this.networkYSize; i++)
            {
                Position p = this.myHashFunction(documentName, i);
                if(this.area.contains(p))
                {
                    try
                    {
                        this.deleteDocument(documentName);
                        return;
                    }
                    catch(NoSuchDocument nsd)
                    {

                    }
                }
                else
                {
                    try
                    {
                        this.searchForResponsibleClient(p).deleteDocument(documentName);
                        return;
                    }
                    catch(NoSuchDocument nsd)
                    {

                    }
                }
            }
	}

	@Override
	public Document searchForDocument(String documentName) throws CANException {
		
            for(int i = 0; i < this.networkXSize*this.networkYSize; i++)
            {
                Position p = this.myHashFunction(documentName, i);
                if(this.area.contains(p))
                {
                    try
                    {
                        Document temp = this.getDocument(documentName);
                        return temp;
                    }
                    catch(NoSuchDocument nsd)
                    {

                    }
                }
                else
                {
                    try
                    {
                        Document temp = this.searchForResponsibleClient(p).getDocument(documentName);
                        return temp;
                    }
                    catch(NoSuchDocument nsd)
                    {

                    }
                }
            }
            return null;
	}
}
