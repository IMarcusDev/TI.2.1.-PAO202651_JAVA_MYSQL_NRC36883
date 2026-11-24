<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Página para 403, 404 y 500 del contenedor: nunca muestra trazas. --%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="codigo" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CountryAPI</title>
    <link rel="stylesheet" href="${ctx}/css/countryapi.css">
</head>
<body>
<header class="barra"><div class="barra__fila"><a class="marca" href="${ctx}/menu">CountryAPI</a></div></header>
<main class="contenido">
    <div class="aviso aviso--error" role="alert">
        <c:choose>
            <c:when test="${codigo == 403}">La solicitud no es válida o la página caducó. Vuelva a intentarlo desde el menú.</c:when>
            <c:when test="${codigo == 404}">La página no existe.</c:when>
            <c:otherwise>Ocurrió un error inesperado, intente nuevamente</c:otherwise>
        </c:choose>
    </div>
    <a class="btn" href="${ctx}/menu">Ir al menú principal</a>
</main>
</body>
</html>
