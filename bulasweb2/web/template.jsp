<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>


<html>
    <head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		       
		<link href="/bulasweb2/resources/css/style.css" rel="stylesheet" type="text/css" media="screen" />
		<link href="/bulasweb2/js/js-mindmap.css" rel="stylesheet" type="text/css" media="screen" /> 
		<link type="text/css" rel="stylesheet" href="/bulasweb2/resources/css/skin.css"/>

		<script type="text/javascript" src="/bulasweb2/js/excanvas.js" />
		<script type="text/javascript" src="/bulasweb2/js/jquery-1.js" />
		<script type="text/javascript" src="/bulasweb2/js/js-mindmap.js" />
	      
		<title> <%= request.getParameter("title") %> </title>
    </head>
    
    <body>

        <div id="logo">
            <h1><a href="#">Solutions  </a></h1>
            <p><em> template design by <a href="http://www.freecsstemplates.org/">Free CSS Templates</a></em></p>
        </div>
        <hr />
        <!-- end #logo -->
        <div id="header">
            <div id="menu">
                <ul>
                    <li><a href="index.jsp">Inicio</a></li>
                    <li><a href="busca.jsp">Busca</a></li>
                    <li><a href="cadastro.jsp">Cadastro</a></li>
                </ul>
            </div>
            <!-- end #menu -->
           <!-- <div id="search">
                <ui:insert name="search"></ui:insert>
                <form method="get" action="">
                    <fieldset>
                        <input type="text" name="s" id="search-text" size="15" />
                        <input type="submit" id="search-submit" value="GO" />
                    </fieldset>
                </form>
            </div>-->
            <!-- end #search -->
        </div>
        <!-- end #header -->
        <!-- end #header-wrapper -->


		<% String pageContents = (String)request.getAttribute("pageContents"); %>
		<jsp:include page="<%= pageContents %>"></jsp:include>


        <div id="footer">
            <p>Copyright (c) 2008 Sitename.com. All rights reserved. Design by <a href="http://www.freecsstemplates.org/">Free CSS Templates</a>.</p>
        </div>

        
	</body>
</html>