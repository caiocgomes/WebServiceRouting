<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
<title> Maplink Roterizador </title>
<link rel="stylesheet" type="text/css" href="http://www.maplink.com.br/Content/Layoutv2.css" media="screen" />
<body>
<h2> Maplink Roterizador </h2>


<br>
<br>
<br>
<form:form method="post" action="addContact.html">
    <label>Numero de Veículos: </label>
    <form:input path="quantityBus" />
    <br/>
    <label>Número total de pessoas: </label>
    <form:input path="quantityClients" />
    <br/>
    <label> Capacidade de cada veículo: </label>
    <form:input path="busCapacity" />
    <br/>
    <input type="submit" value="Gerar Rota"/>


</form:form>

${info}
</body>
</html>