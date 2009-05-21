/*
 * Copyright 2008 Matthew Neeley
 *
 * This file is part of JLabrad.
 *
 * JLabrad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * JLabrad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JLabrad.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.labrad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.labrad.annotations.Setting;
import org.labrad.data.Data;
import org.labrad.data.Hydrant;
import org.labrad.data.Request;
import org.labrad.types.Str;

/**
 *
 * @author maffoo
 */
public class TestServerContext extends AbstractServerContext {
	private Map<String, Data> registry = new HashMap<String, Data>();
	
	/**
	 * Called when this context is first created.
	 */
	public void init() {
    	registry.put("Test", Data.valueOf("blah"));
    	System.out.println("Context " + getContext() + " created.");
    }
	
	/**
	 * Called when this context has expired.  Do any cleanup here.
	 */
	public void expire() {
		System.out.println("Context " + getContext() + " expired.");
	}
	
	/**
	 * Print out a log message when a setting is called.
	 * @param setting
	 * @param data
	 */
    private void log(String setting, Data data) {
        System.out.println(setting + " called [" + data.getTag() + "]: " + data.pretty());
    }

    /**
	 * Print out a log message when a setting is called.
	 * @param setting
	 * @param data
	 */
    private void log(String setting) {
        System.out.println(setting + " called with no data");
    }
    
    /**
	 * Make a request to the given server and setting.
	 * @param server
	 * @param setting
	 * @param data
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private Data makeRequest(String server, String setting, Data data)
			throws InterruptedException, ExecutionException {
		Request request = Request.to(server, getContext()).add(setting, data);
	    List<Data> response = getConnection().sendAndWait(request);
	    return response.get(0);
	}

	/**
     * Echo back the data sent to this setting.
     * @param data
     * @return
     */
    @Setting(ID=1, name="Echo", accepts="?", returns="?",
             description="Echoes back any data sent to this setting.")
    public Data echo(Data data) {
        log("Echo", data);
        return data;
    }
    
    /**
     * Echo back data after a specified delay.
     * @param data
     * @return
     * @throws InterruptedException
     */
    @Setting(ID=2, name="Delayed Echo", accepts="v[s]?", returns="?",
    		 description="Echoes back data after a specified delay.")
    public Data delayedEcho(Data data) throws InterruptedException {
    	log("Delayed Echo", data);
    	double delay = data.get(0).getValue();
    	Data payload = data.get(1);
    	Thread.sleep((long)(delay*1000));
    	return payload;
    }
    
    /**
     * Set a key in this context.
     * @param data
     * @return
     */
    @Setting(ID=3, name="Set", accepts="s?", returns="?",
             description="Sets a key value pair in the current context.")
    public Data set(Data data) {
        log("Set", data);
        String key = data.get(0).getString();
        Data value = data.get(1).clone();
        registry.put(key, value);
        return value;
    }

    /**
     * Get a key from this context.
     * @param data
     * @return
     */
    @Setting(ID=4, name="Get", accepts="s", returns="?",
             description="Gets a key from the current context.")
    public Data get(Data data) {
        log("Get", data);
        String key = data.getString();
        if (!registry.containsKey(key)) {
            throw new RuntimeException("Invalid key: " + key);
        }
        return registry.get(key);
    }

    /**
     * Get all key-value pairs defined in this context.
     * @param data
     * @return
     */
    @Setting(ID=5, name="Get All", accepts="", returns="?",
             description="Gets all of the key-value pairs defined in this context.")
    public Data getAll() {
        log("Get All");
        List<Data> items = new ArrayList<Data>();
        for (String key : registry.keySet()) {
            items.add(Data.clusterOf(Data.valueOf(key), registry.get(key)));
        }
        return Data.clusterOf(items);
    }

    /**
     * Get all keys defined in this context.
     * @param data
     * @return
     */
    @Setting(ID=6, name="Keys", accepts="", returns="*s",
             description="Returns a list of all keys defined in this context.")
    public Data getKeys() {
        log("Keys");
        return Data.ofType("*s").setStringList(new ArrayList<String>(registry.keySet()));
    }

    @Setting(ID=7, name="Remove", accepts="s", returns="",
    		 description="Removes the specified key from this context.")
    public void remove(Data data) {
    	log("Remove", data);
    	registry.remove(data.getString());
    }
    
    /**
     * Get a random LabRAD data object.
     * @param data
     * @return
     */
    @Setting(ID=8, name="Get Random Data", accepts={"s", ""}, returns="?",
             description="Returns random LabRAD data.  " +
             		     "If a type is specified, the data will be of that type; " +
             		     "otherwise it will be of a random type.")
    public Data getRandomData(Data data) {
        log("Get Random Data", data);
    	if (data.getType() instanceof Str) {
    		return Hydrant.getRandomData(data.getString());
    	}
        return Hydrant.getRandomData();
    }
    
    /**
     * Get Random data by calling the python test server
     * @param data
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Setting(ID=9, name="Get Random Data Remote", accepts={"s", ""}, returns="?",
            description="Fetches random data by making a request to the python test server.")
   public Data getRandomDataRemote(Data data) throws InterruptedException, ExecutionException {
       log("Get Random Data Remote", data);
       return makeRequest("Python Test Server", "Get Random Data", data);
   }
    
    /**
     * Forward a request on to another server.
     * @param data
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Setting(ID=10, name="Forward Request", accepts="ss?", returns="?",
             description="Forwards a request on to another server, specified by name and setting.")
    public Data forwardRequest(Data data) throws InterruptedException, ExecutionException {
        log("Forward Request", data);
        String server = data.get(0).getString();
        String setting = data.get(1).getString();
        Data payload = data.get(2);
        return makeRequest(server, setting, payload);
    }
    
    // commented annotations will give errors
    
    @Setting(ID=11, name="Test No Args", accepts="", returns="b", description="")
    //@Setting(ID=11, name="Test No Args", accepts="s", returns="b", description="")
    public Data noArgs() {
    	log("Test No Args");
    	return Data.valueOf(true);
    }
    
    @Setting(ID=12, name="Test No Return", accepts="?", returns="", description="")
    //@Setting(ID=12, name="Test No Return", accepts="?", returns="s", description="")
    public void noReturn(Data data) {
    	log("Test No Return", data);
    }
    
    @Setting(ID=13, name="Test No Args No Return", accepts="", returns="", description="")
    //@Setting(ID=13, name="Test No Args No Return", accepts="s", returns="", description="")
    //@Setting(ID=13, name="Test No Args No Return", accepts="", returns="s", description="")
    public void noArgsNoReturn() {
    	log("Test No Args No Return");
    }
}
