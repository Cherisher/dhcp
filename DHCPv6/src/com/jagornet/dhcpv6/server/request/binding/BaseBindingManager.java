/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseBindingManager.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.server.request.binding;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.DhcpOptionDAO;
import com.jagornet.dhcpv6.db.IaAddress;
import com.jagornet.dhcpv6.db.IaAddressDAO;
import com.jagornet.dhcpv6.db.IaManager;
import com.jagornet.dhcpv6.db.IaPrefixDAO;
import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.db.IdentityAssocDAO;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;

/**
 * The Class BaseBindingManager.  Abstract base class for all three
 * types of binding managers - NA address, TA address, and prefix.
 */
public abstract class BaseBindingManager
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(BaseBindingManager.class);
	
	/** The server config. */
	protected static DhcpServerConfiguration serverConfig = DhcpServerConfiguration.getInstance();

    /** The ia mgr. */
    protected IaManager iaMgr;
	
	/** The ia dao. */
	protected IdentityAssocDAO iaDao;
	
	/** The ia addr dao. */
	protected IaAddressDAO iaAddrDao;
	
	/** The ia prefix dao. */
	protected IaPrefixDAO iaPrefixDao;
	
	/** The opt dao. */
	protected DhcpOptionDAO optDao;

    /** The binding pool map. */
    protected Map<String, List<? extends BindingPool>> bindingPoolMap;

	/** The reaper. */
	protected Timer reaper;
	
	/**
	 * Initialize the manager.  Read the configuration and build
	 * the pool map and static bindings.
	 * @throws Exception
	 */
	public void init() throws Exception
	{
		initPoolMap();
		initStaticBindings();
	}
    
    /**
     * Initialize the pool map.  Read through the link map from the server's
     * configuration and build the pool map keyed by link address with a
     * value of the list of (na/ta address or prefix) bindings for the link.
     * 
     * @throws Exception the exception
     */
    protected void initPoolMap() throws Exception
    {
		try {
    		SortedMap<Subnet, DhcpLink> linkMap = serverConfig.getLinkMap();
    		if ((linkMap != null) && !linkMap.isEmpty()) {
        		bindingPoolMap = new HashMap<String, List<? extends BindingPool>>();
    			for (DhcpLink dhcpLink : linkMap.values()) {
    				Link link = dhcpLink.getLink();
    				List<? extends BindingPool> bindingPools = buildBindingPools(link);
					if ((bindingPools != null) && !bindingPools.isEmpty()) {
						bindingPoolMap.put(link.getAddress(), bindingPools);
					}
				}
    		}
    		else {
    			log.error("LinkMap is null for DhcpServerConfiguration");
    		}
		}
		catch (Exception ex) {
			log.error("Failed to build poolMap", ex);
			throw ex;
		}
    }


    /**
     * Build the list of BindingPools for the given Link.  The BindingPools
     * are either AddressBindingPools (NA and TA) or PrefixBindingPools.
     * 
     * @param link the configured Link
     * @return the list of BindingPools for the link
     * @throws Exception
     */
    protected abstract List<? extends BindingPool> buildBindingPools(Link link) throws Exception;
    
	
    /**
     * Inits the static bindings.
     * 
     * @throws Exception the exception
     */
    protected void initStaticBindings() throws Exception
    {
		try {
    		SortedMap<Subnet, DhcpLink> linkMap = serverConfig.getLinkMap();
    		if (linkMap != null) {
    			for (DhcpLink dhcpLink : linkMap.values()) {
    				Link link = dhcpLink.getLink();
    				initBindings(link);
    			}
    		}
		}
		catch (Exception ex) {
			log.error("Failed to build staticBindings: " + ex);
			throw ex;
		}
    }
    
    protected abstract void initBindings(Link link) throws Exception;


	/**
	 * Find binding pool.
	 * 
	 * @param link the link
	 * @param inetAddr the inet addr
	 * @param requestMsg the request msg
	 * 
	 * @return the binding pool
	 */
	protected BindingPool findBindingPool(Link link, InetAddress inetAddr, DhcpMessage requestMsg)
	{
		List<? extends BindingPool> bps = bindingPoolMap.get(link.getAddress());
		if ((bps != null) && !bps.isEmpty()) {
			for (BindingPool bindingPool : bps) {
				if (bindingPool.contains(inetAddr)) {
					if ((requestMsg != null) && (bindingPool.getLinkFilter() != null)) {
						if (DhcpServerConfiguration.msgMatchesFilter(requestMsg, 
								bindingPool.getLinkFilter())) {
							return bindingPool;
						}
					}
					else {
						return bindingPool;
					}
				}
			}
		}
		return null;
	}
    
	/**
	 * Find binding pool.
	 * 
	 * @param inetAddr the inet addr
	 * 
	 * @return the binding pool
	 */
	protected BindingPool findBindingPool(InetAddress inetAddr)
	{
		//TODO this kind of sucks - looping thru all pools, on all links
		Collection<List<? extends BindingPool>> allPools = bindingPoolMap.values();
		if ((allPools != null) && !allPools.isEmpty()) {
			for (List<? extends BindingPool> bps : allPools) {
				for (BindingPool bindingPool : bps) {
					if (bindingPool.contains(inetAddr))
						return bindingPool;
				}
			}
		}
		return null;
	}
    
    /**
     * Sets the ip as used.
     * 
     * @param link the link
     * @param iaAddrOption the ia addr option
     * 
     * @throws Exception the exception
     */
    protected void setIpAsUsed(Link link, InetAddress inetAddr)
    {
		BindingPool bindingPool = findBindingPool(link, inetAddr, null);
		if (bindingPool != null) {
			bindingPool.setUsed(inetAddr);
		}
		else {
			log.error("Failed to set address used: No BindingPool found for IP=" + 
					inetAddr.getHostAddress());
		}    	
    }
	
	/**
	 * Free address.
	 * 
	 * @param inetAddr the inet addr
	 */
	protected void freeAddress(InetAddress inetAddr)
	{
		BindingPool bp = findBindingPool(inetAddr);
		if (bp != null) {
			bp.setFree(inetAddr);
		}
		else {
			log.error("Failed to free address: no BindingPool found for address: " +
					inetAddr.getHostAddress());
		}
	}
        
	protected Binding findCurrentBinding(Link clientLink, byte[] duid, byte iatype, long iaid,
			DhcpMessage requestMsg) 
	{
		Binding binding = null;
		try {
			IdentityAssoc ia = iaMgr.findIA(duid, iatype, iaid);
			if (ia != null) {
				binding = buildBindingFromIa(ia, clientLink, requestMsg);
			}
			if (binding != null) {
				log.info("Found current binding: " + binding.toString());
			}
			else {
				if (log.isDebugEnabled())
					log.debug("No current binding found for IA: " +
							" duid=" + Util.toHexString(duid) +
							" iatype=" + iatype +
							" iaid=" + iaid);
			}
		}
		catch (Exception ex) {
			log.error("Failed to find current binding", ex);
		}
		return binding;
	}

	protected Binding createSolicitBinding(Link clientLink, byte[] duid, byte iatype, long iaid,
			List<InetAddress> requestAddrs, DhcpMessage requestMsg, boolean rapidCommit)
	{
		Binding binding = null;
		
		List<InetAddress> inetAddrs = 
			getInetAddrs(clientLink, duid, iatype, iaid, requestAddrs, requestMsg);
		if ((inetAddrs != null) && !inetAddrs.isEmpty()) {
			binding = buildBinding(clientLink, duid, iatype, iaid);
			Set<BindingObject> bindingObjs = buildBindingObjects(clientLink, inetAddrs, requestMsg);
			if (rapidCommit) {
				binding.setState(IdentityAssoc.COMMITTED);
				for (BindingObject bindingObj : bindingObjs) {
					bindingObj.setState(IaAddress.COMMITTED);
				}
			}
			else {
				binding.setState(IdentityAssoc.ADVERTISED);
				for (BindingObject bindingObj : bindingObjs) {
					bindingObj.setState(IaAddress.ADVERTISED);
				}
			}
			binding.setBindingObjects(bindingObjs);
			try {
				iaMgr.createIA(binding);
				return binding;
			}
			catch (Exception ex) {
				log.error("Failed to create binding", ex);
				return null;
			}
		}
		
		if (binding != null) {
			log.info("Created solicit binding: " + binding.toString());
		}
		else {
			log.warn("Failed to created solicit binding");
		}
		return binding;
	}
	
	protected Binding updateBinding(Binding binding, Link clientLink, byte[] duid, byte iatype, long iaid,
			List<InetAddress> requestAddrs, DhcpMessage requestMsg, byte state)
	{
		Collection<? extends IaAddress> addIaAddresses = null;
		Collection<? extends IaAddress> updateIaAddresses = null;
		Collection<? extends IaAddress> delIaAddresses = null;	// not used currently
		
		Collection<BindingObject> bindingObjs = binding.getBindingObjects();
		if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
			log.info("Updating times for existing binding addresses");
			// current binding has addresses, so update times
			setBindingObjsTimes(bindingObjs);
			// the existing IaAddress binding objects will be updated
			updateIaAddresses = binding.getIaAddresses();
		}
		else {
			log.info("Existing binding has no on-link addresses, allocating new address(es)");
			// current binding has no addresses, add new one(s)
			List<InetAddress> inetAddrs = 
				getInetAddrs(clientLink, duid, iatype, iaid, requestAddrs, requestMsg);
			if ((inetAddrs == null) || inetAddrs.isEmpty()) {
				log.error("Failed to update binding, no addresses available");
				return null;
			}
			bindingObjs = buildBindingObjects(clientLink, inetAddrs, requestMsg);
			binding.setBindingObjects(bindingObjs);
			// these new IaAddress binding objects will be added
			addIaAddresses = binding.getIaAddresses();
		}
		binding.setState(state);
		for (BindingObject bindingObj : bindingObjs) {
			bindingObj.setState(state);
		}
		try {
			iaMgr.updateIA(binding, addIaAddresses, updateIaAddresses, delIaAddresses);
			return binding;	// if we get here, it worked
		}
		catch (Exception ex) {
			log.error("Failed to update binding", ex);
			return null;
		}
	}

	protected List<InetAddress> getInetAddrs(Link clientLink, byte[] duid, byte iatype, long iaid,
			List<InetAddress> requestAddrs, DhcpMessage requestMsg)
	{
		List<InetAddress> inetAddrs = new ArrayList<InetAddress>();
		
		if ((requestAddrs != null) && !requestAddrs.isEmpty()) {
			for (InetAddress reqAddr : requestAddrs) {
				BindingPool bp = findBindingPool(clientLink, reqAddr, requestMsg);
				if (bp == null) {
					log.warn("No BindingPool found for requested client address: " +
							reqAddr.getHostAddress());
					// if there is no pool for the requested address, then skip it
					// because that address is either off-link or no longer valid
					continue;
				}
				log.info("Searching existing bindings for requested IP=" +
						reqAddr.getHostAddress());
				IdentityAssoc ia = null;
				try {
					ia = iaMgr.findIA(reqAddr); 
					if (ia != null) {
						// the address is assigned to an IA, which we
						// don't expect to be this IA, because we would
						// have found it using findCurrentBinding...
						// but, perhaps another thread just created it?
						if (isMyIa(duid, iatype, iaid, ia)) {
							log.warn("Requested IP=" + reqAddr.getHostAddress() +
									" is already held by THIS client IA: " +
									" duid=" + Util.toHexString(duid) +
									" iatype=" + iatype +
									" iaid=" + iaid +
									".  Allowing this requested IP.");
						}
						else {
							log.info("Requested IP=" + reqAddr.getHostAddress() +
									" is held by ANOTHER client IA: " +
									" duid=" + Util.toHexString(duid) +
									" iatype=" + iatype +
									" iaid=" + iaid);
							// the address is held by another IA, so get a new one
							reqAddr = getNextFreeAddress(clientLink, requestMsg);
						}
					}
					if (reqAddr != null) {
						inetAddrs.add(reqAddr);
					}
				}
				catch (Exception ex) {
					log.error("Failure finding IA for address", ex);
				}
			}
		}
		
		if (inetAddrs.isEmpty()) {
			// the client did not request any valid addresses, so get the next one
			InetAddress inetAddr = getNextFreeAddress(clientLink, requestMsg);
			if (inetAddr != null) {
				inetAddrs.add(inetAddr);
			}
		}
		return inetAddrs;
	}
	
	/**
	 * Checks if is my ia.
	 * 
	 * @param duid the duid
	 * @param iatype the ia type
	 * @param iaid the ia id
	 * @param ia the ia
	 * 
	 * @return true, if is my ia
	 */
	protected boolean isMyIa(byte[] duid, byte iatype, long iaid, IdentityAssoc ia)
	{
		boolean rc = false;
		if (duid != null) {
			if (Arrays.equals(ia.getDuid(), duid) &&
				(ia.getIatype() == iatype) &&
				(ia.getIaid() == iaid)) {
				rc = true;
			}
		}		
		return rc;
	}
	
	/**
	 * Gets the next free address.
	 * 
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the next free address
	 */
	protected InetAddress getNextFreeAddress(Link clientLink, DhcpMessage requestMsg)
	{
		if (clientLink != null) {
    		List<? extends BindingPool> pools = bindingPoolMap.get(clientLink.getAddress());
    		if (pools != null) {
    			for (BindingPool bp : pools) {
    				LinkFilter filter = bp.getLinkFilter();
    				if ((requestMsg != null) && (filter != null)) {
    					if (!DhcpServerConfiguration.msgMatchesFilter(requestMsg, filter)) {
    						log.info("Client request does not match filter, skipping pool: " +
    								bp.toString());
    						continue;
    					}
    				}
					if (log.isDebugEnabled())
						log.debug("Getting next available address from pool: " +
								bp.toString());
					InetAddress free = bp.getNextAvailableAddress();
					if (free != null) {
						if (log.isDebugEnabled())
							log.debug("Found next available address: " +
									free.getHostAddress());
						return free;
					}
					else {
						// warning here, b/c there may be more pools
						log.warn("No free addresses available in pool: " +
								bp.toString());
					}
    			}
    		}
    		log.error("No Pools defined in server configuration for Link: " +
    				clientLink.getAddress());
		}
		else {
			throw new IllegalStateException("ClientLink is null");
		}
		return null;
	}
	
	/**
	 * Create a Binding given an IdentityAssoc loaded from the database.
	 * 
	 * @param ia the ia
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the binding
	 */
	protected abstract Binding buildBindingFromIa(IdentityAssoc ia, 
			Link clientLink, DhcpMessage requestMsg);

	/**
	 * Builds the binding.
	 * 
	 * @param clientLink the client link
	 * @param duid the duid
	 * @param iaia the ia id
	 * 
	 * @return the binding
	 */
	private Binding buildBinding(Link clientLink, byte[] duid, byte iatype, long iaid)
	{
		IdentityAssoc ia = new IdentityAssoc();
		ia.setDuid(duid);
		ia.setIatype(iatype);
		ia.setIaid(iaid);
		return new Binding(ia, clientLink);		
	}
	
	/**
	 * Builds the binding object.
	 * 
	 * @param clientLink the client link
	 * @param inetAddrs the inet addrs
	 * @param requestMsg the request msg
	 * 
	 * @return the set< binding object>
	 */
	private Set<BindingObject> buildBindingObjects(Link clientLink, 
			List<InetAddress> inetAddrs, DhcpMessage requestMsg)
	{
		Set<BindingObject> bindingObjs = new HashSet<BindingObject>();
		for (InetAddress inetAddr : inetAddrs) {
			BindingObject bindingObj = buildBindingObject(inetAddr, clientLink, requestMsg);
			if (bindingObj != null)
				bindingObjs.add(bindingObj);
		}
		return bindingObjs;
	}
	
	protected abstract BindingObject buildBindingObject(InetAddress inetAddr, 
			Link clientLink, DhcpMessage requestMsg);

	/**
	 * Sets the binding objects times.
	 * 
	 * @param bindingObjs the new binding objects times
	 */
	protected void setBindingObjsTimes(Collection<BindingObject> bindingObjs) 
	{
		if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
			for (BindingObject bindingObj : bindingObjs) {
				BindingPool bp = bindingObj.getBindingPool();
				setBindingObjectTimes(bindingObj, bp.getPreferredLifetimeMs(), bp.getValidLifetimeMs());
				//TODO: if we store the options, and they have changed,
				// 		then we must update those options here somehow
			}
		}
	}

	/**
	 * Sets the binding object times.
	 * 
	 * @param bindingObj the binding object
	 * @param preferred the preferred lifetime in ms
	 * @param valid the valid lifetime in ms
	 */
	protected void setBindingObjectTimes(BindingObject bindingObj, long preferred, long valid)
	{
		log.debug("Updating binding times for address: " + 
				bindingObj.getIpAddress().getHostAddress());
		Date now = new Date();
		bindingObj.setStartTime(now);
		long pEndTime = now.getTime() + preferred;
		bindingObj.setPreferredEndTime(new Date(pEndTime));
		long vEndTime = now.getTime() + valid;
		bindingObj.setValidEndTime(new Date(vEndTime));
	}

	public IaManager getIaMgr() {
		return iaMgr;
	}

	public void setIaMgr(IaManager iaMgr) {
		this.iaMgr = iaMgr;
	}

	public IdentityAssocDAO getIaDao() {
		return iaDao;
	}

	public void setIaDao(IdentityAssocDAO iaDao) {
		this.iaDao = iaDao;
	}

	public IaAddressDAO getIaAddrDao() {
		return iaAddrDao;
	}

	public void setIaAddrDao(IaAddressDAO iaAddrDao) {
		this.iaAddrDao = iaAddrDao;
	}

	public IaPrefixDAO getIaPrefixDao() {
		return iaPrefixDao;
	}

	public void setIaPrefixDao(IaPrefixDAO iaPrefixDao) {
		this.iaPrefixDao = iaPrefixDao;
	}

	public DhcpOptionDAO getOptDao() {
		return optDao;
	}

	public void setOptDao(DhcpOptionDAO optDao) {
		this.optDao = optDao;
	}
    
}