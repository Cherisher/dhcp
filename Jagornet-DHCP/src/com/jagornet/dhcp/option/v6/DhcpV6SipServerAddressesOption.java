/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6SipServerAddressesOption.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.option.v6;

import com.jagornet.dhcp.option.base.BaseIpAddressListOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.xml.V6SipServerAddressesOption;

/**
 * <p>Title: DhcpV6SipServerAddressesOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6SipServerAddressesOption extends BaseIpAddressListOption
{
	/**
	 * Instantiates a new dhcp sip server addresses option.
	 */
	public DhcpV6SipServerAddressesOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp sip server addresses option.
	 * 
	 * @param sipServerAddressesOption the sip server addresses option
	 */
	public DhcpV6SipServerAddressesOption(V6SipServerAddressesOption sipServerAddressesOption)
	{
		super(sipServerAddressesOption);
		setCode(DhcpConstants.V6OPTION_SIP_SERVERS_ADDRESS_LIST);
	}
}
