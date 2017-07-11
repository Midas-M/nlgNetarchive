<%--
File:       $Id$
Revision:   $Revision$
Author:     $Author$
Date:       $Date$

The Netarchive Suite - Software to harvest and preserve websites
Copyright 2004-2017 The Royal Danish Library,
the National Library of France and the Austrian
National Library.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

--%><%@ page import="dk.netarkivet.common.Constants,
                     dk.netarkivet.common.utils.I18n,
                     dk.netarkivet.common.webinterface.HTMLUtils"
    pageEncoding="UTF-8"
%><%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"
%><fmt:setLocale value="<%=HTMLUtils.getLocale(request)%>" scope="page"
/><fmt:setBundle scope="page" basename="<%=Constants.TRANSLATIONS_BUNDLE%>"/><%!
    private static final I18n I18N = new dk.netarkivet.common.utils.I18n(
            Constants.TRANSLATIONS_BUNDLE);
%><%
    HTMLUtils.generateHeader(I18N.getString(response.getLocale(),
                                            "pagetitle;mainmenu"), pageContext);
%>
<div>
    <a href="http://nlg.gr/">
        <img src="transparent_logo_nlg.png" width="500" height="126" border="0"
             alt="National Library of Greece"/>
    </a>
</div>

<div align="right">
    Powered by:
    <div>
    <a href="http://netarchive.dk/suite">
        <img src="transparent_logo.png" width="250" height="30" border="0"
             alt="<fmt:message key="netarchive.suite"/>"/>
    </a>
    </div>
</div>
<%
    HTMLUtils.generateFooter(out);
%>