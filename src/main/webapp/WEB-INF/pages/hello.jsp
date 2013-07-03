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
    <form:input path="numberBus" />
    <br/>
    <label>Número total de paradas: </label>
    <form:input path="numberStops" />
    <br/>
    <label> Número máximo de paradas por veículo: </label>
    <form:input path="maximumClientPerBus" />
    <br/>
    <input type="submit" value="Gerar Rota"/>


</form:form>

${info}
</body>
</html>